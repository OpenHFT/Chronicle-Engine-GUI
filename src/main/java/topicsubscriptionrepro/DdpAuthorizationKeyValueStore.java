package topicsubscriptionrepro;

import net.openhft.chronicle.engine.api.*;
import net.openhft.chronicle.engine.api.map.*;
import net.openhft.chronicle.engine.api.pubsub.*;
import net.openhft.chronicle.engine.api.tree.*;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.network.api.session.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

public class DdpAuthorizationKeyValueStore<K, V> implements SubscriptionKeyValueStore<K, V>
{
    private static final Logger _logger = LoggerFactory.getLogger(DdpAuthorizationKeyValueStore.class);

    private final String _assetName;
    private final SessionProvider _sessionProvider;
    private final Asset _asset;
    private final SubscriptionKeyValueStore<K, V> _keyValueStore;
    private final RequestContext _requestContext;

    public DdpAuthorizationKeyValueStore(RequestContext context, Asset asset, SubscriptionKeyValueStore<K, V> keyValueStore) throws AssetNotFoundException
    {
        System.out.println(this.getClass().getName() + ": constructor");

        _requestContext = context;
        _asset = asset;
        _assetName = asset.fullName();
        _sessionProvider = asset.findView(SessionProvider.class);
        _keyValueStore = keyValueStore;

        System.out.println(context.name());
    }

    //TODO DS figure out where this is used. I think we need to adjust the required permissions.
    @Override
    public KVSSubscription<K, V> subscription(boolean createIfAbsent)
    {
        System.out.println(this.getClass().getName() + ": subscription");

        checkPermissions("POLL");

        return _keyValueStore.subscription(createIfAbsent);
    }

    @Override
    public boolean put(K key, V value)
    {
        System.out.println(this.getClass().getName() + ": put");

        checkAddOrUpdatePermissions(key);

        return _keyValueStore.put(key, value);
    }

    @Nullable
    @Override
    public V getAndPut(K key, V value)
    {
        System.out.println(this.getClass().getName() + ": getAndPut");

        //TODO DS check wether this is an add or update and check permissions accordingly - containsKey
        //TODO DS this assumes you are allowed to read if you are allowed to publish
        checkAddOrUpdatePermissions(key);

        return _keyValueStore.getAndPut(key, value);
    }

    @Override
    public boolean remove(K k)
    {
        System.out.println(this.getClass().getName() + ": remove");

        checkPermissions("REMOVE");

        return _keyValueStore.remove(k);
    }

    @Nullable
    @Override
    public V getAndRemove(K key)
    {
        System.out.println(this.getClass().getName() + ": getAndRemove");

        //TODO DS this assumes you are allowed to read if you are allowed to remove
        checkPermissions("REMOVE");

        return _keyValueStore.getAndRemove(key);
    }

    @Nullable
    @Override
    public V getUsing(K key, Object value)
    {
        System.out.println(this.getClass().getName() + ": getUsing");

        checkPermissions("POLL");

        return _keyValueStore.getUsing(key, value);
    }

    @Override
    public long longSize()
    {
        System.out.println(this.getClass().getName() + ": longSize");

        checkPermissions("POLL");

        return _keyValueStore.longSize();
    }

    @Override
    public void keysFor(int segment, SubscriptionConsumer<K> kConsumer) throws InvalidSubscriberException
    {
        System.out.println(this.getClass().getName() + ": keysFor");

        checkPermissions("POLL");

        _keyValueStore.keysFor(segment, kConsumer);
    }

    @Override
    public void entriesFor(int segment, SubscriptionConsumer<MapEvent<K, V>> kvConsumer) throws InvalidSubscriberException
    {
        System.out.println(this.getClass().getName() + ": entriesFor");

        checkPermissions("POLL");

        _keyValueStore.entriesFor(segment, kvConsumer);
    }

    @Override
    public void clear()
    {
        System.out.println(this.getClass().getName() + ": clear");

        checkPermissions("REMOVE");

        _keyValueStore.clear();
    }

    @Override
    public boolean containsValue(V value)
    {
        System.out.println(this.getClass().getName() + ": containsValue");

        checkPermissions("POLL");

        return _keyValueStore.containsValue(value);
    }

    @Override
    public Asset asset()
    {
        System.out.println(this.getClass().getName() + ": asset");

        checkPermissions("POLL");
        return _asset;
    }

    @Nullable
    @Override
    public KeyValueStore<K, V> underlying()
    {
        System.out.println(this.getClass().getName() + ": underlying");

        //TODO DS do we need a special data permission for this? Admin or the like?
        checkPermissions("POLL");

        //TODO DS reconsider
        return _keyValueStore;
    }

    @Override
    public void close()
    {
        System.out.println(this.getClass().getName() + ": close");

        //TODO DS do we need a special data permission for this? Admin or the like?
        checkPermissions("REMOVE");

        //TODO DS anything we need to close in this class?
        _keyValueStore.close();
    }

    @Override
    public void accept(EngineReplication.ReplicationEntry replicationEntry)
    {
        System.out.println(this.getClass().getName() + ": accept");

        //TODO DS shouldn't have auth on replication
//        checkPermissions("READ");

        _keyValueStore.accept(replicationEntry);
    }

    //TODO DS worried about performance implications
    private void checkAddOrUpdatePermissions(K key)
    {
        if (_keyValueStore.containsKey(key))
        {
            checkPermissions("UPDATE");
        }
        else
        {
            checkPermissions("ADD");
        }
    }

    //TODO DS move to common class?
    private void checkPermissions(String permissionRequired)
    {
        SessionDetails sessionDetails = _sessionProvider.get();

        if (sessionDetails == null)
        {
            String errorMessage = String.format("Session details not set on request on asset '%s'!", _assetName);
            _logger.error(errorMessage);

            throw new PermissionDeniedException(errorMessage);
        }

        String userId = sessionDetails.userId();

        System.out.println("Checking permissions {" + permissionRequired + "} for user " + userId);

        //Everyone is authorized
    }
}