package examples;

import net.openhft.chronicle.bytes.*;
import net.openhft.chronicle.engine.api.map.*;
import net.openhft.chronicle.engine.api.pubsub.*;
import net.openhft.chronicle.engine.api.session.*;
import net.openhft.chronicle.engine.api.tree.*;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.engine.map.remote.*;
import net.openhft.chronicle.engine.session.*;
import net.openhft.chronicle.engine.tree.*;
import net.openhft.chronicle.network.connection.*;
import net.openhft.chronicle.threads.*;
import net.openhft.chronicle.wire.*;

import java.io.*;
import java.util.concurrent.*;
import java.util.function.*;

import static net.openhft.chronicle.engine.api.tree.RequestContext.requestContext;

public class ReplicationClientMain
{
    private static MapView<String, String, String> map1;
    private static MapView<String, String, String> map2;

    public static void main(String[] args) throws Exception
    {
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

        if(!"InsertedEvent{assetName='/map', key=hello, value=world}".equals(q1.take().toString()))
        {
            throw new Exception("Doesn't match 1...");
        }

        System.out.println("Checking queue2...");

        if(!"InsertedEvent{assetName='/map', key=hello, value=world}".equals(q2.take().toString()))
        {
            throw new Exception("Doesn't match 2...");
        }

        System.out.println("Checking map1...");

        //Test map 1 content
        if(map1.size() != 1)
        {
            throw new Exception("Doesn't match 3...");
        }

        if(!"world".equals(map1.get("hello")))
        {
            throw new Exception("Doesn't match 4...");
        }

        System.out.println("Checking map2...");

        //Test map 2 content
        if(map2.size() != 1)
        {
            throw new Exception("Doesn't match 5...");
        }

        if(!"world".equals(map2.get("hello")))
        {
            throw new Exception("Doesn't match 6...");
        }

        System.out.println("Removing map2...");

        map2.remove("hello");

        System.out.println("Checking queues again for remove event...");

        if(!"RemovedEvent{assetName='/map', key=hello, oldValue=world}".equals(q1.take().toString()))
        {
            throw new Exception("Doesn't match 7...");
        }

        if(!"RemovedEvent{assetName='/map', key=hello, oldValue=world}".equals(q2.take().toString()))
        {
            throw new Exception("Doesn't match 8...");
        }

        System.out.println("Checking map1 for null...");

        if(map1.get("hello") != null)
        {
            throw new Exception("Doesn't match 8...");
        }

        System.out.println("Checking map2 for null...");

        if(map2.get("hello") != null)
        {
            throw new Exception("Doesn't match 9...");
        }

        System.out.println("DONE!");
    }

    private static MapView<String, String, String> create(String nameName, Integer hostId, String connectUri,
                                                          BlockingQueue<MapEvent> q, Function<Bytes, Wire> wireType)
    {
        final VanillaAssetTree tree = new VanillaAssetTree(hostId);

        final Asset asset = tree.root().acquireAsset(nameName);
        ThreadGroup threadGroup = new ThreadGroup("host=" + connectUri);
        tree.root().addView(ThreadGroup.class, threadGroup);

        tree.root().addLeafRule(ObjectKVSSubscription.class, " ObjectKVSSubscription",
                RemoteKVSSubscription::new);

        tree.root().addWrappingRule(MapView.class, "mapv view", VanillaMapView::new, AuthenticatedKeyValueStore.class);
        tree.root().addWrappingRule(TopicPublisher.class, " topic publisher", RemoteTopicPublisher::new, MapView.class);
        tree.root().addWrappingRule(Publisher.class, "publisher", RemotePublisher::new, MapView.class);

        EventGroup eventLoop = new EventGroup(true);
        SessionProvider sessionProvider = new VanillaSessionProvider();

        tree.root().addView(TcpChannelHub.class, new TcpChannelHub(sessionProvider, connectUri, eventLoop, wireType));
        asset.addView(AuthenticatedKeyValueStore.class, new RemoteKeyValueStore(requestContext(nameName), asset));

        MapView<String, String, String> result = tree.acquireMap(nameName, String.class, String.class);

        result.clear();
        tree.registerSubscriber(nameName, MapEvent.class, q::add);
        return result;
    }
}