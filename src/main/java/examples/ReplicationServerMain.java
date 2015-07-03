package examples;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.engine.api.EngineReplication;
import net.openhft.chronicle.engine.api.collection.ValuesCollection;
import net.openhft.chronicle.engine.api.map.KeyValueStore;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.map.SubscriptionKeyValueStore;
import net.openhft.chronicle.engine.api.pubsub.Publisher;
import net.openhft.chronicle.engine.api.pubsub.Reference;
import net.openhft.chronicle.engine.api.pubsub.Replication;
import net.openhft.chronicle.engine.api.pubsub.TopicPublisher;
import net.openhft.chronicle.engine.api.session.SessionProvider;
import net.openhft.chronicle.engine.api.set.EntrySetView;
import net.openhft.chronicle.engine.api.set.KeySetView;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.collection.VanillaValuesCollection;
import net.openhft.chronicle.engine.fs.Clusters;
import net.openhft.chronicle.engine.fs.HostDetails;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.engine.pubsub.VanillaReference;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.server.WireType;
import net.openhft.chronicle.engine.session.VanillaSessionProvider;
import net.openhft.chronicle.engine.set.VanillaKeySetView;
import net.openhft.chronicle.engine.tree.*;
import net.openhft.chronicle.threads.EventGroup;
import net.openhft.chronicle.threads.Threads;
import net.openhft.chronicle.threads.api.EventLoop;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.YamlLogging;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Function;

import static net.openhft.chronicle.engine.api.tree.RequestContext.requestContext;

/**
 * Created by Rob Austin
 */

public class ReplicationServerMain {


    public static final String HOST = System.getProperty("remote.host");
    public static final Integer HOST_ID = Integer.getInteger("hostId", 1);

    public static void main(String[] args) throws IOException {
        simple();
        //
        // complex();
    }

    private static void simple() throws IOException {
        YamlLogging.clientReads = true;
        YamlLogging.clientWrites = true;
        WireType wireType = WireType.TEXT;
        WireType.wire = wireType;
        final Integer host = HOST_ID;

        System.out.println("using hostid=" + HOST_ID);
        System.out.println("using host=" + HOST);


        final VanillaAssetTree tree = new VanillaAssetTree(host);
        newCluster(host, tree);
        tree.root().addLeafRule(EngineReplication.class, "Engine replication holder",
                CMap2EngineReplicator::new);

        tree.root().addView(SessionProvider.class, new VanillaSessionProvider());
        tree.root().addWrappingRule(Replication.class, "replication", VanillaReplication::new, MapView.class);
        tree.root().addWrappingRule(MapView.class, "mapv view", VanillaMapView::new, AuthenticatedKeyValueStore.class);
        tree.root().addWrappingRule(TopicPublisher.class, " topic publisher", VanillaTopicPublisher::new, MapView.class);
        tree.root().addWrappingRule(Publisher.class, "publisher", VanillaReference::new, MapView.class);
        tree.root().addLeafRule(ObjectKVSSubscription.class, " vanilla", VanillaKVSSubscription::new);

        ThreadGroup threadGroup = new ThreadGroup("my-named-thread-group");
        tree.root().addView(ThreadGroup.class, threadGroup);

        tree.root().addView(EventLoop.class, new EventGroup(false));
        Asset asset = tree.root().acquireAsset(requestContext("map"), "map");
        asset.addView(AuthenticatedKeyValueStore.class, new ChronicleMapKeyValueStore<>(requestContext("map"), asset));

        tree.root().addLeafRule(ObjectKVSSubscription.class, " ObjectKVSSubscription",
                VanillaKVSSubscription::new);





        new ServerEndpoint(5700 + host, tree);
    }


    @NotNull
    private static void complex() throws IOException {


        YamlLogging.clientReads = true;
        YamlLogging.clientWrites = true;
        WireType wireType = WireType.TEXT;
        WireType.wire = wireType;
        final Integer hostId = HOST_ID;

        System.out.println("using hostid=" + HOST_ID);
        System.out.println("using host=" + HOST);

        Function<Bytes, Wire> writeType = net.openhft.chronicle.wire.WireType.TEXT;
        VanillaAssetTree tree = new VanillaAssetTree((byte) (int) hostId);

        newCluster(hostId, tree);

        Asset asset = tree.root();
        asset.root().addWrappingRule(Reference.class, "reference", VanillaReference::new, MapView.class);
        asset.root().addWrappingRule(Replication.class, "replication", VanillaReplication::new, MapView.class);
        asset.root().addWrappingRule(Publisher.class, "publisher", VanillaReference::new, MapView.class);
        asset.root().addWrappingRule(EntrySetView.class, " entrySet", VanillaEntrySetView::new, MapView.class);
        asset.root().addWrappingRule(KeySetView.class, " keySet", VanillaKeySetView::new, MapView.class);
        asset.root().addWrappingRule(ValuesCollection.class, " values", VanillaValuesCollection::new, MapView.class);
        asset.root().addWrappingRule(MapView.class, " string key maps", VanillaMapView::new, ObjectKeyValueStore.class);


        HostIdentifier hostIdentifier = new HostIdentifier((byte) (int) HOST_ID);
        asset.addView(HostIdentifier.class, hostIdentifier);


        ThreadGroup threadGroup = new ThreadGroup("tree-" + hostIdentifier.hostId());
        asset.root().addView(ThreadGroup.class, threadGroup);
        asset.root().addLeafRule(EventLoop.class, " event group", (rc, asset1) ->
                Threads.withThreadGroup(threadGroup, () -> {
                    EventLoop eg = new EventGroup(false);
                    eg.start();
                    return eg;
                }));
        asset.root().addView(SessionProvider.class, new VanillaSessionProvider());
        asset.root().addWrappingRule(TopicPublisher.class, " topic publisher", VanillaTopicPublisher::new, MapView.class);
        asset.root().addWrappingRule(Publisher.class, "publisher", VanillaReference::new, MapView.class);


        asset.root().addLeafRule(AuthenticatedKeyValueStore.class, " vanilla", ChronicleMapKeyValueStore::new);
        asset.root().addLeafRule(SubscriptionKeyValueStore.class, " vanilla", ChronicleMapKeyValueStore::new);
        asset.root().addLeafRule(KeyValueStore.class, " vanilla", ChronicleMapKeyValueStore::new);

        asset.root().addLeafRule(ObjectKVSSubscription.class, " vanilla",
                VanillaKVSSubscription::new);

        asset.root().addLeafRule(TopologySubscription.class, " vanilla",
                VanillaTopologySubscription::new);


        asset.root().addWrappingRule(MapView.class, "map directly to KeyValueStore",
                VanillaMapView::new,
                KeyValueStore.class);
        asset.root().addLeafRule(EngineReplication.class, "Engine replication holder",
                CMap2EngineReplicator::new);
        asset.root().addLeafRule(KeyValueStore.class, "KVS is Chronicle Map", (context, asset0) ->
                new ChronicleMapKeyValueStore(context.wireType(writeType),
                        asset0));


        new ServerEndpoint(5700 + hostId, tree);

    }


    private static void newCluster(Integer host, VanillaAssetTree tree) {
        Clusters clusters = new Clusters();
        HashMap<String, HostDetails> hostDetailsMap = new HashMap<String, HostDetails>();

        {
            final HostDetails value = new HostDetails();
            value.hostId = 1;
            value.hostname = host == 1 ? "localhost" : HOST;
            value.port = 5701;
            value.timeoutMs = 1000;
            hostDetailsMap.put("host1", value);
        }
        {
            final HostDetails value = new HostDetails();
            value.hostId = 2;
            value.hostname = host == 2 ? "localhost" : HOST;
            value.port = 5702;
            value.timeoutMs = 1000;
            hostDetailsMap.put("host2", value);
        }


        clusters.put("cluster", hostDetailsMap);
        tree.root().addView(Clusters.class, clusters);
    }
}

