package examples;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.engine.api.map.MapEvent;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.pubsub.Publisher;
import net.openhft.chronicle.engine.api.pubsub.TopicPublisher;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.map.AuthenticatedKeyValueStore;
import net.openhft.chronicle.engine.map.ObjectKVSSubscription;
import net.openhft.chronicle.engine.map.VanillaMapView;
import net.openhft.chronicle.engine.map.remote.RemoteKVSSubscription;
import net.openhft.chronicle.engine.map.remote.RemoteKeyValueStore;
import net.openhft.chronicle.engine.pubsub.RemoteReference;
import net.openhft.chronicle.engine.pubsub.RemoteTopicPublisher;
import net.openhft.chronicle.engine.session.VanillaSessionProvider;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.api.session.SessionProvider;
import net.openhft.chronicle.network.connection.SocketAddressSupplier;
import net.openhft.chronicle.network.connection.TcpChannelHub;
import net.openhft.chronicle.threads.EventGroup;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import net.openhft.chronicle.wire.YamlLogging;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;

import static net.openhft.chronicle.engine.api.tree.RequestContext.requestContext;

public class ReplicationClientMain {
    private static MapView<String, String> map1;
    private static MapView<String, String> map2;

    public static void main(String[] args) throws Exception {
        YamlLogging.clientReads = true;
        YamlLogging.clientWrites = true;
        WireType wireType = WireType.TEXT;

        final Integer hostId = Integer.getInteger("hostId", 1);

        BlockingQueue q1 = new ArrayBlockingQueue(1);
        BlockingQueue q2 = new ArrayBlockingQueue(1);

        {
            String hostname = System.getProperty("host1", "localhost");
            int port = Integer.getInteger("port1", 5701);
            map1 = create("map", hostId, hostname + ":" + port, q1, wireType);
        }

        {
            String hostname = System.getProperty("host2", "localhost");
            int port = Integer.getInteger("port2", 5702);
            map2 = create("map", hostId, hostname + ":" + port, q2, wireType);
        }

        System.out.println("Putting map1...");

        map1.put("hello", "world");

        System.out.println("Checking queue1...");

        if (!"InsertedEvent{assetName='/map', key=hello, value=world}".equals(q1.take().toString())) {
            throw new Exception("Doesn't match 1...");
        }

        System.out.println("Checking queue2...");

        if (!"InsertedEvent{assetName='/map', key=hello, value=world}".equals(q2.take().toString())) {
            throw new Exception("Doesn't match 2...");
        }

        System.out.println("Checking map1...");

        //Test map 1 content
        if (map1.size() != 1) {
            throw new Exception("Doesn't match 3...");
        }

        if (!"world".equals(map1.get("hello"))) {
            throw new Exception("Doesn't match 4...");
        }

        System.out.println("Checking map2...");

        //Test map 2 content
        if (map2.size() != 1) {
            throw new Exception("Doesn't match 5...");
        }

        if (!"world".equals(map2.get("hello"))) {
            throw new Exception("Doesn't match 6...");
        }

        System.out.println("Removing map2...");

        map2.remove("hello");

        System.out.println("Checking queues again for remove event...");

        if (!"RemovedEvent{assetName='/map', key=hello, oldValue=world}".equals(q1.take().toString())) {
            throw new Exception("Doesn't match 7...");
        }

        if (!"RemovedEvent{assetName='/map', key=hello, oldValue=world}".equals(q2.take().toString())) {
            throw new Exception("Doesn't match 8...");
        }

        System.out.println("Checking map1 for null...");

        if (map1.get("hello") != null) {
            throw new Exception("Doesn't match 8...");
        }

        System.out.println("Checking map2 for null...");

        if (map2.get("hello") != null) {
            throw new Exception("Doesn't match 9...");
        }

        System.out.println("DONE!");
    }

    private static MapView<String, String> create(String nameName, Integer hostId, String connectUri,
                                                  BlockingQueue<MapEvent> q, Function<Bytes, Wire> wireType) {
        final VanillaAssetTree tree = new VanillaAssetTree(hostId);

        final Asset asset = tree.root().acquireAsset(nameName);
        ThreadGroup threadGroup = new ThreadGroup("host=" + connectUri);
        tree.root().addView(ThreadGroup.class, threadGroup);

        tree.root().addLeafRule(ObjectKVSSubscription.class, " ObjectKVSSubscription",
                RemoteKVSSubscription::new);

        tree.root().addWrappingRule(MapView.class, "mapv view", VanillaMapView::new, AuthenticatedKeyValueStore.class);
        tree.root().addWrappingRule(TopicPublisher.class, " topic publisher", RemoteTopicPublisher::new, MapView.class);
        tree.root().addLeafRule(Publisher.class, "publisher", RemoteReference::new);

        EventGroup eventLoop = new EventGroup(true);
        SessionProvider sessionProvider = new VanillaSessionProvider();

        tree.root().addView(TcpChannelHub.class, new TcpChannelHub(sessionProvider,
                eventLoop, wireType, "", new SocketAddressSupplier(new String[]{connectUri}, "")));
        asset.addView(AuthenticatedKeyValueStore.class, new RemoteKeyValueStore(requestContext(nameName), asset));

        MapView<String, String> result = tree.acquireMap(nameName, String.class, String.class);

        result.clear();
        tree.registerSubscriber(nameName, MapEvent.class, q::add);
        return result;
    }
}