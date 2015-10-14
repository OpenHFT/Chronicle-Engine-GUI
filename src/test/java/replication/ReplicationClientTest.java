package replication;

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
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.session.VanillaSessionProvider;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.api.session.SessionProvider;
import net.openhft.chronicle.network.connection.SocketAddressSupplier;
import net.openhft.chronicle.network.connection.TcpChannelHub;
import net.openhft.chronicle.threads.EventGroup;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import net.openhft.chronicle.wire.YamlLogging;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import topicsubscriptionrepro.ConstructorExceptionClient;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;

import static net.openhft.chronicle.engine.api.tree.RequestContext.requestContext;

/**
 * Created by Rob Austin
 */

public class ReplicationClientTest {


    private static MapView<String, String> map1;
    private static MapView<String, String> map2;
    private static VanillaAssetTree tree;
    private ServerEndpoint server1;
    private ServerEndpoint server2;

    private static MapView<String, String> create(String nameName, Integer hostId, String connectUri,
                                                  BlockingQueue<MapEvent> q, Function<Bytes, Wire> wireType) {
        tree = new VanillaAssetTree(hostId);

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
                eventLoop, wireType, "", new SocketAddressSupplier(new String[]{connectUri}, ""), true, ConstructorExceptionClient.clientConnectionMonitor()));
        asset.addView(AuthenticatedKeyValueStore.class, new RemoteKeyValueStore(requestContext(nameName), asset));

        MapView<String, String> result = tree.acquireMap(nameName, String.class, String.class);

        result.clear();
        tree.registerSubscriber(nameName, MapEvent.class, q::add);
        return result;
    }

    @After
    public void after() throws Exception {
        tree.close();
        server1.close();
        server2.close();
    }

    @Test
    public void test() throws InterruptedException, IOException {

        ReplicationServerMain server = new ReplicationServerMain();

        // creates the first instance of the server
        server1 = server.create(1, "localhost");

        // creates the seconds instance of the server
        server2 = server.create(2, "localhost");

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

        map1.put("hello", "world");

        Assert.assertEquals("InsertedEvent{assetName='/map', key=hello, value=world}", q1.take().toString());
        Assert.assertEquals("InsertedEvent{assetName='/map', key=hello, value=world}", q2.take().toString());

        //Test map 1 content
        Assert.assertEquals(1, map1.size());
        Assert.assertEquals("world", map1.get("hello"));

        //Test map 1 content
        Assert.assertEquals(1, map2.size());
        Assert.assertEquals("world", map2.get("hello"));

        map2.remove("hello");

        Assert.assertEquals("RemovedEvent{assetName='/map', key=hello, oldValue=world, isReplicationEvent=false}", q1.take().toString());
        Assert.assertEquals("RemovedEvent{assetName='/map', key=hello, oldValue=world, isReplicationEvent=false}", q2.take().toString());
    }
}

