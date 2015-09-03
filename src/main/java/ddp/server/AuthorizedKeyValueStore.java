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
import net.openhft.chronicle.engine.map.KVSSubscription;
import net.openhft.chronicle.network.api.session.SessionDetails;
import net.openhft.chronicle.network.api.session.SessionProvider;
import org.jetbrains.annotations.Nullable;

//TODO DS implement closeable?
public class AuthorizedKeyValueStore<K, V> implements SubscriptionKeyValueStore<K, V>
{
    private final SessionProvider sessionProvider;
    private final SubscriptionKeyValueStore<K, V> kvStore;
    private final Asset asset;
    private RequestContext context;

    public AuthorizedKeyValueStore(RequestContext context, Asset asset, SubscriptionKeyValueStore<K, V> kvStore) throws AssetNotFoundException
    {
        System.out.println(this.getClass().getName() + ": constructor");
        this.context = context;
        this.asset = asset;
        this.sessionProvider = asset.findView(SessionProvider.class);
        this.kvStore = kvStore;
    }

    @Override
    public KVSSubscription<K, V> subscription(boolean createIfAbsent)
    {
        System.out.println(this.getClass().getName() + ": subscription");

        checkPermission("READ");
        return kvStore.subscription(createIfAbsent);
    }

    @Override
    public boolean put(K key, V value) {
        System.out.println(this.getClass().getName() + ": put");

        checkPermission("WRITE");
        return kvStore.put(key, value);
    }

    @Override
    public boolean remove(K key) {
        System.out.println(this.getClass().getName() + ": remove");

        checkPermission("WRITE");
        return kvStore.remove(key);
    }

    @Nullable
    @Override
    public V getAndPut(K key, V value)
    {
        System.out.println(this.getClass().getName() + ": getAndPut");

        checkPermission("WRITE");
        return kvStore.getAndPut(key, value);
    }

    @Nullable
    @Override
    public V getAndRemove(K key)
    {
        System.out.println(this.getClass().getName() + ": getAndRemove");

        checkPermission("WRITE");
        return kvStore.getAndRemove(key);
    }

    @Nullable
    @Override
    public V getUsing(K key, Object value)
    {
        System.out.println(this.getClass().getName() + ": getUsing");

        checkPermission("READ");
        return kvStore.getUsing(key, value);
    }

    @Override
    public long longSize()
    {
        System.out.println(this.getClass().getName() + ": longSize");

        checkPermission("READ");
        return kvStore.longSize();
    }

    @Override
    public void keysFor(int segment, SubscriptionConsumer<K> kConsumer) throws InvalidSubscriberException
    {
        System.out.println(this.getClass().getName() + ": keysFor");

        checkPermission("READ");
        kvStore.keysFor(segment, kConsumer);
    }

    @Override
    public void entriesFor(int segment, SubscriptionConsumer<MapEvent<K, V>> kvConsumer) throws InvalidSubscriberException
    {
        System.out.println(this.getClass().getName() + ": entriesFor");

        checkPermission("READ");
        kvStore.entriesFor(segment, kvConsumer);
    }

    @Override
    public void clear()
    {
        System.out.println(this.getClass().getName() + ": clear");

        checkPermission("WRITE");
        kvStore.clear();
    }

    @Override
    public boolean containsValue(V value)
    {
        System.out.println(this.getClass().getName() + ": containsValue");

        checkPermission("READ");
        return kvStore.containsValue(value);
    }

    @Override
    public Asset asset()
    {
        System.out.println(this.getClass().getName() + ": asset");

        checkPermission("READ");
        //TODO DS consider whether this is correct
        return asset;
    }

    @Nullable
    @Override
    public KeyValueStore<K, V> underlying()
    {
        System.out.println(this.getClass().getName() + ": underlying");

        checkPermission("READ");
        //TODO DS reconsider
        return kvStore;
    }

    @Override
    public void close()
    {
        System.out.println(this.getClass().getName() + ": close");

        checkPermission("READ");
        //TODO DS anything we need to close in this class?
        kvStore.close();
    }

    @Override
    public void accept(EngineReplication.ReplicationEntry replicationEntry)
    {
        System.out.println(this.getClass().getName() + ": accept");

        checkPermission("READ");
        kvStore.accept(replicationEntry);
    }

    //TODO DS should use enum
    //TODO DS throw exception if user doesn't have access
    //TODO DS implement
    private void checkPermission(String accessLevelRequired)
    {
        SessionDetails sessionDetails = sessionProvider.get();
        if (sessionDetails == null)
            throw new IllegalStateException("No SessionDetails");

        String userId = sessionDetails.userId();

        System.out.println("Checking permissions {" + accessLevelRequired + "} for user " + userId);

        boolean isAccessGranted = false;

        switch (accessLevelRequired)
        {
            case "WRITE":
            case "READ":
                isAccessGranted = true;
                break;
            default:
                throw new RuntimeException(""); //TODO DS throw appropriate exception
        }

        if (!isAccessGranted)
        {
            throw new RuntimeException(""); //TODO DS throw appropriate exception
        }
    }
}