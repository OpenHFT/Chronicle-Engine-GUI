package ddp.api;

import net.openhft.chronicle.hash.RemoteCallTimeoutException;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapStatelessClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link ddp.api.DataCache} implementation specifically for Stateless Chronicle maps.
 * This keeps a reference to a stateless Chronicle map (connected to stateful server) and will automatically attempt
 * to connect to configured replicas in case of failures to connect with primary.
 * <p>
 * All method calls will automatically retry replicas on certain exceptions, but re-throw unexpected exception for the
 * client to handle.
 * <p>
 * Should all configured replicas be unavailable for a while but later come online this class will automatically
 * reconnect when the next operation is performed.
 * <p>
 * This class is final and package protected and thus instances will have to be created using a builder. This ensures
 * that clients will not gain unauthorized access.
 *
 * @param <K> Type of the key used for the {@link ddp.api.DataCache}
 * @param <V> Type of the value used for the {@link ddp.api.DataCache}
 */
final class ChronicleDataCache<K, V> implements DataCache<K, V>
{
    private static final Logger _logger = LoggerFactory.getLogger(ChronicleDataCache.class);

    private ChronicleMap<K, V> _chronicleMap; //Stateless client

    private List<DataCacheConfiguration> _dataCacheConfigurations;
    private Integer _currentDataCacheConfigurationIndex = null;

    /**
     * Creates an instance of {@link ddp.api.DataCache} with the given arguments.
     *
     * @param dataCacheConfiguration Server configuration for the Data Cache server.
     * @throws ConfigurationException Thrown if it fails to connect to the configured data cache.
     */
    ChronicleDataCache(DataCacheConfiguration dataCacheConfiguration) throws ConfigurationException
    {
        this(new ArrayList<DataCacheConfiguration>()
        {{
                if (dataCacheConfiguration != null)
                {
                    add(dataCacheConfiguration);
                }
            }});
    }

    /**
     * Creates an instance of {@link ddp.api.DataCache} with the given arguments.
     *
     * @param dataCacheConfigurations Server configurations for the Data Cache server. Ordered list and as such
     *                                connections will be attempted in the given order.
     * @throws ConfigurationException Thrown if it fails to connect to any of the configured data caches.
     */
    ChronicleDataCache(List<DataCacheConfiguration> dataCacheConfigurations) throws ConfigurationException
    {
        _dataCacheConfigurations = dataCacheConfigurations;

        reconnectUsingNextDataCacheConfiguration();
    }

    /**
     * Attempts to initialise the local {@link net.openhft.chronicle.map.ChronicleMap} using the given configuration.
     *
     * @param dataCacheConfiguration Configuration to be used for connection/initialisation.
     * @return True if connection/initialisation is successful, false otherwise.
     */
    private boolean initialiseChronicleMap(DataCacheConfiguration dataCacheConfiguration)
    {
        try
        {
            _logger.info("Attemting to connect to Data Cache '{}'", dataCacheConfiguration);

            //TODO DS create stateless client
            _chronicleMap = ChronicleMapStatelessClientBuilder.<K, V>of(new InetSocketAddress
                    (dataCacheConfiguration.getHostname(), dataCacheConfiguration.getPort()))
                    .putReturnsNull(true).removeReturnsNull(true).create();

            _logger.info("Successfully connected to Data Cache '{}'", dataCacheConfiguration);

            return true;
        } //If connection cannot be made a RemoteCallTimeoutException is thrown.
        catch (RemoteCallTimeoutException rcte)
        {
            _logger.error("Failed to connect to data cache {}!", dataCacheConfiguration);

            return false;
        }
        catch (IOException ioe)
        {
            _logger.error("Failed to connect to data cache {}!", dataCacheConfiguration);

            return false;
        }
    }

    /**
     * Attempts to connect to the remote Chronicle map using the next configuration in the list of configurations.
     * Keeps track of the current configuration which is used to figure out what the next to be used is.
     * If the current config is the last one in the list, the first config is used (rolls).
     * Retries all other possible configurations before eventually failing (if none can be connected to) by throwing
     * a {@link ddp.api.ConfigurationException}.
     *
     * @throws ConfigurationException Thrown if the Chronicle map cannot be initialised using any of the alternative
     *                                replica configurations.
     */
    private void reconnectUsingNextDataCacheConfiguration() throws ConfigurationException
    {
        //Keeps track of connection attempts ensuring we don't retry connections that have already failed in this call
        int retryCount = 0;
        boolean hasConnected = false;

        do
        {
            retryCount++;

            if (_dataCacheConfigurations == null || _dataCacheConfigurations.size() == 0)
            {
                String errorMessage = "Could not find any configurations for Data Cache.";

                _logger.error(errorMessage);

                throw new ConfigurationException(errorMessage);
            }

            //Hasn't yet attempted to connect to any of the configured data cache servers
            if (_currentDataCacheConfigurationIndex == null)
            {
                _currentDataCacheConfigurationIndex = 0;

                hasConnected = initialiseChronicleMap(_dataCacheConfigurations.get(_currentDataCacheConfigurationIndex));

                continue; //If connected no need to try others
            }

            //Roll index if we have reached the end of the list of configurations
            _currentDataCacheConfigurationIndex = (_currentDataCacheConfigurationIndex == _dataCacheConfigurations.size() - 1) ? 0 : (_currentDataCacheConfigurationIndex + 1);

            hasConnected = initialiseChronicleMap(_dataCacheConfigurations.get(_currentDataCacheConfigurationIndex));

            continue;
        }
        while (retryCount < _dataCacheConfigurations.size() && !hasConnected);

        //Failed to connect to any of the configured replicas
        if (!hasConnected)
        {
            String errorMessage = "Failed to connect to any of the configured Data Cache replicas!";

            _logger.error(errorMessage);

            throw new ConfigurationException(errorMessage);
        }
    }

    //TODO DS is there a possible infinite loop if supplier throws exception where the cause is an IO exception?

    /**
     * Attempts to execute the given supplier method and catches exceptions expected to be thrown by underlying Chronicle
     * map when it cannot reach the server. When an expected exception is caught an attempt will be made to connect to
     * any configured map replicas and execute the supplier on that map.
     * <p>
     * Unexpected exceptions will be re-thrown.
     *
     * @param supplierToExecute Method to be executed with retry on replica maps on failure.
     * @param <T>               Type of the supplier return value.
     * @return Return value of the supplier method.
     */
    private <T> T executeWithFailover(Supplier<T> supplierToExecute)
    {
        //TODO DS implement another failover executor for void methods?
        try
        {
            return supplierToExecute.get();
        }
        catch (RemoteCallTimeoutException rcte)
        {
            _logger.warn("Remote method invocation timed out. Will retry on failover if any configured.", rcte);

            return initialiseNextAndExecute(supplierToExecute);
        }
        catch (Exception e)
        {
            //Chronicle wraps IOException as an IORuntimeException which is package private to Chronicle, hence we cannot
            // add it to the catch clause and have to check for it this way.
            //The IORuntimeException is thrown when the server Map is closed (possible under other circumstances too).
            if (e.getCause() instanceof IOException)
            {
                return initialiseNextAndExecute(supplierToExecute);
            }
            else
            {
                _logger.error("Exception thrown which are not handled for retry.", e);

                throw e;
            }
        }
    }

    /**
     * Attempts to reconnect the Chronicle map to the next possible configured server and execute
     * the given supplier against the new server connection with failover (thus if it fails on the newly
     * connected server it will be retried on other servers that can be connected to).
     * <p>
     * Throws {@link ddp.api.ConfigurationException} if supplier fails to execute the replica map.
     *
     * @param supplierToExecute Supplier method to be executed on other replica.
     * @param <T>               Type of the Suppliers return value.
     * @return Return value of supplier.
     */
    private <T> T initialiseNextAndExecute(Supplier<T> supplierToExecute)
    {
        try
        {
            reconnectUsingNextDataCacheConfiguration();

            return executeWithFailover(supplierToExecute);
        }
        catch (ConfigurationException e)
        {
            String errorMessage = "Remote method invocation failed on all configured Data Caches.";

            _logger.error(errorMessage);

            throw new ConnectionException(errorMessage);
        }
    }

    @Override
    public int size()
    {
        return executeWithFailover(() -> _chronicleMap.size());
    }

    @Override
    public boolean isEmpty()
    {
        return executeWithFailover(() -> _chronicleMap.isEmpty());
    }

    @Override
    public boolean containsKey(Object key)
    {
        return executeWithFailover(() -> _chronicleMap.containsKey(key));
    }

    @Override
    public boolean containsValue(Object value)
    {
        return executeWithFailover(() -> _chronicleMap.containsValue(value));
    }

    @Override
    public V get(Object key)
    {
        return executeWithFailover(() -> _chronicleMap.get(key));
    }

    @Override
    public V put(K key, V value)
    {
        return executeWithFailover(() -> _chronicleMap.put(key, value));
    }

    @Override
    public V remove(Object key)
    {
        return executeWithFailover(() -> _chronicleMap.remove(key));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        //TODO DS consider having a specific failover function which accepts void!
        executeWithFailover(() -> {

            _chronicleMap.putAll(m);

            return null; //TODO DS not very pretty
        });
    }

    @Override
    public void clear()
    {
        //TODO DS consider having a specific failover function which accepts void!
        executeWithFailover(() -> {

            _chronicleMap.clear();

            return null; //TODO DS not very pretty
        });
    }

    @Override
    public Set<K> keySet()
    {
        return executeWithFailover(() -> _chronicleMap.keySet());
    }

    @Override
    public Collection<V> values()
    {
        return executeWithFailover(() -> _chronicleMap.values());
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        return executeWithFailover(() -> _chronicleMap.entrySet());
    }

    @Override
    public V getOrDefault(Object key, V defaultValue)
    {
        return executeWithFailover(() -> _chronicleMap.getOrDefault(key, defaultValue));
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action)
    {
        //TODO DS consider having a specific failover function which accepts void!
        executeWithFailover(() -> {

            _chronicleMap.forEach(action);

            return null; //TODO DS not very pretty
        });
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
    {
        //TODO DS consider having a specific failover function which accepts void!
        executeWithFailover(() -> {

            _chronicleMap.replaceAll(function);

            return null; //TODO DS not very pretty
        });
    }

    @Override
    public V putIfAbsent(K key, V value)
    {
        return executeWithFailover(() -> _chronicleMap.putIfAbsent(key, value));
    }

    @Override
    public boolean remove(Object key, Object value)
    {
        return executeWithFailover(() -> _chronicleMap.remove(key, value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue)
    {
        return executeWithFailover(() -> _chronicleMap.replace(key, oldValue, newValue));
    }

    @Override
    public V replace(K key, V value)
    {
        return executeWithFailover(() -> _chronicleMap.replace(key, value));
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
    {
        return executeWithFailover(() -> _chronicleMap.computeIfAbsent(key, mappingFunction));
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        return executeWithFailover(() -> _chronicleMap.computeIfPresent(key, remappingFunction));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        return executeWithFailover(() -> _chronicleMap.compute(key, remappingFunction));
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
    {
        return executeWithFailover(() -> _chronicleMap.merge(key, value, remappingFunction));
    }

    //TODO DS implement - might need to be at build time in Chronicle.
    @Override
    public boolean addEventListener(DataCacheEventListener dataCacheEventListener)
    {
        //TODO DS execute with retry
        throw new NotImplementedException();
    }

    //TODO DS implement - might need to be at build time in Chronicle.
    @Override
    public boolean removeEventListener(DataCacheEventListener dataCacheEventListener)
    {
        //TODO DS execute with retry
        throw new NotImplementedException();
    }

    @Override
    public void close()
    {
        if (_chronicleMap != null)
        {
            _chronicleMap.close();
        }
    }
}