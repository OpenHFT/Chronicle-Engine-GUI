package replication;

import net.openhft.chronicle.engine.api.map.MapEvent;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.pubsub.Publisher;
import net.openhft.chronicle.engine.api.pubsub.TopicPublisher;
import net.openhft.chronicle.engine.api.session.SessionProvider;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.engine.session.VanillaSessionProvider;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.connection.TcpChannelHub;
import net.openhft.chronicle.threads.EventGroup;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static net.openhft.chronicle.engine.api.tree.RequestContext.requestContext;

/**
 * Created by Rob Austin
 */

public class ReplicationClientMain {

    private static MapView<String, String, String> map1;
    private static MapView<String, String, String> map2;

    @Test
    public void test() throws InterruptedException {
        final Integer host = Integer.getInteger("hostId", 1);
        final VanillaAssetTree tree = new VanillaAssetTree(host);

        BlockingQueue q1 = new ArrayBlockingQueue(1);
        BlockingQueue q2 = new ArrayBlockingQueue(1);
        {
            String hostname = System.getProperty("host1", "localhost");
            int port = Integer.getInteger("port1", 5701);
            map1 = create(tree, "map1", hostname, port, q1);
        }

        {
            String hostname = System.getProperty("host2", "localhost");
            int port = Integer.getInteger("port2", 5702);
            map2 = create(tree, "map1", hostname, port, q2);
        }

        map1.put("hello", "world");

        System.out.println(q1.take());
        System.out.println(q2.take());

        Assert.assertEquals(1, map2.size());
        Assert.assertEquals("world", map2.get("hello"));
    }

    private static MapView<String, String, String> create(VanillaAssetTree tree, String nameName, String host, int port,
                                                          BlockingQueue<MapEvent> q) {
        final Asset asset = tree.root().acquireAsset(requestContext(), nameName);
        ThreadGroup threadGroup = new ThreadGroup("host=" + host);
        tree.root().addView(ThreadGroup.class, threadGroup);

        tree.root().addLeafRule(ObjectKVSSubscription.class, " ObjectKVSSubscription",
                VanillaKVSSubscription::new);

        tree.root().addWrappingRule(MapView.class, "mapv view", VanillaMapView::new, AuthenticatedKeyValueStore.class);
        tree.root().addWrappingRule(TopicPublisher.class, " topic publisher", RemoteTopicPublisher::new, MapView.class);
        tree.root().addWrappingRule(Publisher.class, "publisher", RemotePublisher::new, MapView.class);

        EventGroup eventLoop = new EventGroup(true);
        SessionProvider sessionProvider = new VanillaSessionProvider();
        tree.root().addView(TcpChannelHub.class, new TcpChannelHub(sessionProvider, host, port, eventLoop));
        asset.addView(AuthenticatedKeyValueStore.class, new RemoteKeyValueStore(requestContext(""), asset));

        MapView<String, String, String> result = tree.acquireMap(nameName, String.class, String.class);
        tree.registerSubscriber("map", MapEvent.class, q::add);
        return result;
    }
}

