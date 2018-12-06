package topicsubscriptionrepro;

import net.openhft.chronicle.engine.api.*;
import net.openhft.chronicle.engine.api.map.*;
import net.openhft.chronicle.engine.api.pubsub.*;
import net.openhft.chronicle.engine.api.tree.*;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.network.api.session.*;
import org.jetbrains.annotations.*;

public class DdpAuthenticationKeyValueStore<K, V> implements AuthenticatedKeyValueStore<K, V>
{
    private final SessionProvider _sessionProvider;
    private final Asset _asset;
    private final SubscriptionKeyValueStore<K, V> _keyValueStore;
    private final RequestContext _requestContext;

    public DdpAuthenticationKeyValueStore(RequestContext context, Asset asset, SubscriptionKeyValueStore<K, V> keyValueStore) throws AssetNotFoundException
    {
        System.out.println(this.getClass().getName() + ": constructor");

        _requestContext = context;
        _asset = asset;
        _keyValueStore = keyValueStore;
        _sessionProvider = asset.findView(SessionProvider.class);
    }

    @Override
    public KVSSubscription<K, V> subscription(boolean createIfAbsent)
    {
        System.out.println(this.getClass().getName() + ": subscription");

        isAuthenticated();
        return _keyValueStore.subscription(createIfAbsent);
    }

    @Override
    public boolean put(K k, V v)
    {
        System.out.println(this.getClass().getName() + ": put");

        isAuthenticated();
        return _keyValueStore.put(k, v);
    }

    @Nullable
    @Override
    public V getAndPut(K key, V value)
    {
        System.out.println(this.getClass().getName() + ": getAndPut");

        isAuthenticated();
        return _keyValueStore.getAndPut(key, value);
    }

    @Override
    public boolean remove(K k)
    {
        System.out.println(this.getClass().getName() + ": remove");

        isAuthenticated();
        return _keyValueStore.remove(k);
    }

    @Nullable
    @Override
    public V getAndRemove(K key)
    {
        System.out.println(this.getClass().getName() + ": getAndRemove");

        isAuthenticated();
        return _keyValueStore.getAndRemove(key);
    }

    @Nullable
    @Override
    public V getUsing(K key, Object value)
    {
        System.out.println(this.getClass().getName() + ": getUsing");

        isAuthenticated();
        return _keyValueStore.getUsing(key, value);
    }

    @Override
    public long longSize()
    {
        System.out.println(this.getClass().getName() + ": longSize");

        isAuthenticated();
        return _keyValueStore.longSize();
    }

    @Override
    public void keysFor(int segment, SubscriptionConsumer<K> kConsumer) throws InvalidSubscriberException
    {
        System.out.println(this.getClass().getName() + ": keysFor");

        isAuthenticated();
        _keyValueStore.keysFor(segment, kConsumer);
    }

    @Override
    public void entriesFor(int segment, SubscriptionConsumer<MapEvent<K, V>> kvConsumer) throws InvalidSubscriberException
    {
        System.out.println(this.getClass().getName() + ": entriesFor");

        isAuthenticated();
        _keyValueStore.entriesFor(segment, kvConsumer);
    }

    @Override
    public void clear()
    {
        System.out.println(this.getClass().getName() + ": clear");

        isAuthenticated();
        _keyValueStore.clear();
    }

    @Override
    public boolean containsValue(V value)
    {
        System.out.println(this.getClass().getName() + ": containsValue");

        isAuthenticated();
        return _keyValueStore.containsValue(value);
    }

    @Override
    public Asset asset()
    {
        System.out.println(this.getClass().getName() + ": _asset");

        isAuthenticated();
        //TODO DS is this correct?
        return _asset;
    }

    @Nullable
    @Override
    public KeyValueStore<K, V> underlying()
    {
        System.out.println(this.getClass().getName() + ": underlying");

        isAuthenticated();
        //TODO DS reconsider
        return _keyValueStore;
    }

    @Override
    public void close()
    {
        System.out.println(this.getClass().getName() + ": close");

        isAuthenticated();
        //TODO DS anything we need to close in this class?
        _keyValueStore.close();
    }

    @Override
    public void accept(EngineReplication.ReplicationEntry replicationEntry)
    {
        System.out.println(this.getClass().getName() + ": accept");

        isAuthenticated();
        _keyValueStore.accept(replicationEntry);
    }

    private void isAuthenticated()
    {
        SessionDetails sessionDetails = _sessionProvider.get();

        String userId = sessionDetails.userId();

        System.out.println(this.getClass().getName() + ": Authenticating " + userId);

        //Do nothing - all is authenticated
    }
}