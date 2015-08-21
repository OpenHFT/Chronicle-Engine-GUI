package ddp.server;

import net.openhft.chronicle.engine.api.*;
import net.openhft.chronicle.engine.api.map.*;
import net.openhft.chronicle.engine.api.pubsub.*;
import net.openhft.chronicle.engine.api.tree.*;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.network.api.session.*;
import org.jetbrains.annotations.*;

//TODO DS implement closeable?
public class AuthorizedKeyValueStore<K, V> implements SubscriptionKeyValueStore<K, V>
{
    private final SessionProvider sessionProvider;
    private final SubscriptionKeyValueStore<K, V> kvStore;
    private RequestContext context;
    private final Asset asset;

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

    @Nullable
    @Override
    public V getAndPut(K key, V value)
    {
        System.out.println(this.getClass().getName() + ": getAndPut");

        checkPermission("READ");
        return kvStore.getAndPut(key, value);
    }

    @Nullable
    @Override
    public V getAndRemove(K key)
    {
        System.out.println(this.getClass().getName() + ": getAndRemove");

        checkPermission("READ");
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

        checkPermission("READ");
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

        String userId = sessionDetails.userId();

        System.out.println("Checking permissions {" + accessLevelRequired + "} for user " + userId);

        boolean isAccessGranted = false;

        switch (accessLevelRequired)
        {
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