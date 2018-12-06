package ddp.api;

import ddp.api.authorisation.*;
import ddp.api.security.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * Instance of a {@link ddp.api.DataCache}  for which access is restricted. Methods are protected by the user
 * entitlement to use the named {@link ddp.api.DataCache}.
 * Class cannot be inherited.
 * @param <K> Key type for the give {@link ddp.api.DataCache}
 * @param <V> Value type for the given {@link ddp.api.DataCache}
 */
public final class RestrictedDataCache<K, V> implements DataCache<K, V>
{
    private Entitlement _entitlement;
    private DataCache<K, V> _underlyingDataCache;

    RestrictedDataCache(DataCache<K, V> underlyingDataCache,  Entitlement entitlement)
    {
        _underlyingDataCache = underlyingDataCache;
        _entitlement = entitlement;
    }

    @Override
    public int size()
    {
        _entitlement.checkDataAccess(DataAccessLevel.READ);

        return _underlyingDataCache.size();
    }

    @Override
    public boolean isEmpty()
    {
        _entitlement.checkDataAccess(DataAccessLevel.READ);

        return _underlyingDataCache.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        _entitlement.checkDataAccess(DataAccessLevel.READ);

        return _underlyingDataCache.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        _entitlement.checkDataAccess(DataAccessLevel.READ);

        return _underlyingDataCache.containsValue(value);
    }

    @Override
    public V get(Object key)
    {
        _entitlement.checkDataAccess(DataAccessLevel.READ);

        return _underlyingDataCache.get(key);
    }

    @Override
    public V put(K key, V value)
    {
        //TODO DS consider having an "update" access level
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        return _underlyingDataCache.put(key, value);
    }

    @Override
    public V remove(Object key)
    {
        //TODO DS should we have a specific access level which allows the deletion of keys?
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        return _underlyingDataCache.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        _underlyingDataCache.putAll(m);
    }

    @Override
    public void clear()
    {
        //TODO DS should we have a specific access level which allows the deletion of keys?
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        _underlyingDataCache.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet()
    {
        _entitlement.checkDataAccess(DataAccessLevel.READ);

        return _underlyingDataCache.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values()
    {
        _entitlement.checkDataAccess(DataAccessLevel.READ);

        return _underlyingDataCache.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet()
    {
        _entitlement.checkDataAccess(DataAccessLevel.READ);

        return _underlyingDataCache.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue)
    {
        _entitlement.checkDataAccess(DataAccessLevel.READ);

        return _underlyingDataCache.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action)
    {
        //TODO DS need to test that if we for each do update on the value that it goes through security check.
        //TODO DS this might pose another problem that we can't prevent users updating objects, although as long as we use Chronicle they will have to do put to update on server, or Acquire using which we can prevent..
        _entitlement.checkDataAccess(DataAccessLevel.READ);

        _underlyingDataCache.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
    {
        //TODO DS should this be an update level??
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        _underlyingDataCache.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value)
    {
        //TODO DS should this be an update level??
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        return _underlyingDataCache.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value)
    {
        //TODO DS should this be an delete/remove level??
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        return _underlyingDataCache.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue)
    {
        //TODO DS should this be an update level??
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        return _underlyingDataCache.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value)
    {
        //TODO DS should this be an update level??
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        return _underlyingDataCache.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
    {
        //TODO DS should this be an update level??
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        return _underlyingDataCache.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        //TODO DS should this be an update level??
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        return _underlyingDataCache.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
    {
        //TODO DS should this be an update level??
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        return _underlyingDataCache.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
    {
        //TODO DS should this be an update level??
        _entitlement.checkDataAccess(DataAccessLevel.WRITE);

        return _underlyingDataCache.merge(key, value, remappingFunction);
    }

    @Override
    public boolean addEventListener(DataCacheEventListener dataCacheEventListener)
    {
        _entitlement.checkDataAccess(DataAccessLevel.SUBSCRIBE);

        return _underlyingDataCache.addEventListener(dataCacheEventListener);
    }

    //TODO DS execute this when subscribe access is dynamically removed.
    @Override
    public boolean removeEventListener(DataCacheEventListener dataCacheEventListener)
    {
        //TODO DS do we need to authorise this? Should be okay to remove any event listeners even if no longer has authorisation to subscribe?
        return _underlyingDataCache.removeEventListener(dataCacheEventListener);
    }

    @Override
    public void close()
    {
        //TODO DS do we need to authorise this? Wouldn't think so
        if(_underlyingDataCache != null)
        {
            _underlyingDataCache.close();
        }
    }
}