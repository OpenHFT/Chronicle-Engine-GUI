package ddp.server;

import net.openhft.chronicle.engine.api.EngineReplication;
import net.openhft.chronicle.engine.api.map.KeyValueStore;
import net.openhft.chronicle.engine.api.map.MapEvent;
import net.openhft.chronicle.engine.api.map.SubscriptionKeyValueStore;
import net.openhft.chronicle.engine.api.pubsub.InvalidSubscriberException;
import net.openhft.chronicle.engine.api.pubsub.SubscriptionConsumer;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.AssetNotFoundException;
import net.openhft.chronicle.engine.api.tree.RequestContext;
import net.openhft.chronicle.engine.map.AuthenticatedKeyValueStore;
import net.openhft.chronicle.engine.map.KVSSubscription;
import net.openhft.chronicle.network.api.session.SessionDetails;
import net.openhft.chronicle.network.api.session.SessionProvider;
import org.jetbrains.annotations.Nullable;

public class DdpAuthenticatedKeyValueStore<K, V> implements AuthenticatedKeyValueStore<K, V>
{
    private final SessionProvider sessionProvider;
    private final Asset asset;
    private final SubscriptionKeyValueStore<K, V> kvStore;
    private RequestContext context;

    public DdpAuthenticatedKeyValueStore(RequestContext context, Asset asset, SubscriptionKeyValueStore<K, V> kvStore) throws AssetNotFoundException
    {
        System.out.println(this.getClass().getName() + ": constructor");
        this.context = context;
        this.asset = asset;
        this.kvStore = kvStore;
        this.sessionProvider = asset.findView(SessionProvider.class);
    }

    @Override
    public KVSSubscription<K, V> subscription(boolean createIfAbsent)
    {
        System.out.println(this.getClass().getName() + ": subscription");

        isAuthenticated();
        return kvStore.subscription(createIfAbsent);
    }

    @Override
    public boolean put(K key, V value) {
        System.out.println(this.getClass().getName() + ": put");

        isAuthenticated();
        return kvStore.put(key, value);
    }

    @Override
    public boolean remove(K key) {
        System.out.println(this.getClass().getName() + ": remove");

        isAuthenticated();
        return kvStore.remove(key);
    }

    @Nullable
    @Override
    public V getAndPut(K key, V value)
    {
        System.out.println(this.getClass().getName() + ": getAndPut");

        isAuthenticated();
        return kvStore.getAndPut(key, value);
    }

    @Nullable
    @Override
    public V getAndRemove(K key)
    {
        System.out.println(this.getClass().getName() + ": getAndRemove");

        isAuthenticated();
        return kvStore.getAndRemove(key);
    }

    @Nullable
    @Override
    public V getUsing(K key, Object value)
    {
        System.out.println(this.getClass().getName() + ": getUsing");

        isAuthenticated();
        return kvStore.getUsing(key, value);
    }

    @Override
    public long longSize()
    {
        System.out.println(this.getClass().getName() + ": longSize");

        isAuthenticated();
        return kvStore.longSize();
    }

    @Override
    public void keysFor(int segment, SubscriptionConsumer<K> kConsumer) throws InvalidSubscriberException
    {
        System.out.println(this.getClass().getName() + ": keysFor");

        isAuthenticated();
        kvStore.keysFor(segment, kConsumer);
    }

    @Override
    public void entriesFor(int segment, SubscriptionConsumer<MapEvent<K, V>> kvConsumer) throws InvalidSubscriberException
    {
        System.out.println(this.getClass().getName() + ": entriesFor");

        isAuthenticated();
        kvStore.entriesFor(segment, kvConsumer);
    }

    @Override
    public void clear()
    {
        System.out.println(this.getClass().getName() + ": clear");

        isAuthenticated();
        kvStore.clear();
    }

    @Override
    public boolean containsValue(V value)
    {
        System.out.println(this.getClass().getName() + ": containsValue");

        isAuthenticated();
        return kvStore.containsValue(value);
    }

    @Override
    public Asset asset()
    {
        System.out.println(this.getClass().getName() + ": asset");

        isAuthenticated();
        //TODO DS is this correct?
        return asset;
    }

    @Nullable
    @Override
    public KeyValueStore<K, V> underlying()
    {
        System.out.println(this.getClass().getName() + ": underlying");

        isAuthenticated();
        //TODO DS reconsider
        return kvStore;
    }

    @Override
    public void close()
    {
        System.out.println(this.getClass().getName() + ": close");

        isAuthenticated();
        //TODO DS anything we need to close in this class?
        kvStore.close();
    }

    @Override
    public void accept(EngineReplication.ReplicationEntry replicationEntry)
    {
        System.out.println(this.getClass().getName() + ": accept");

        isAuthenticated();
        kvStore.accept(replicationEntry);
    }

    private void isAuthenticated()
    {
        SessionDetails sessionDetails = sessionProvider.get();

        String userId = sessionDetails.userId();

        System.out.println(this.getClass().getName() + ": Authenticating " + userId);

        //TODO DS throw exception if not authenticated and log...
    }
}