package examples;

import net.openhft.chronicle.engine.api.pubsub.Publisher;
import net.openhft.chronicle.engine.api.pubsub.Subscriber;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.wire.WireType;
import net.openhft.chronicle.wire.YamlLogging;

import java.io.IOException;
import java.util.Map;

/**
 * Server side class to demonstrate issue whereby there is a timeout on the client
 * if the same instance of ChronicleMapClient is used to subscribe to a topic
 * and to get a map on client side and access for a key specifically.
 *
 * This is standalone class so no re-use from other objects.
 */
public class TestTimeoutOnDirectMapAccessAndSubscriptionExample
{
    public static void main(String[] args)
    {
        final int chroniclePort = 8088;
        try
        {
            ChronicleDataPublisher chronicleDataPublisher = new ChronicleDataPublisher(chroniclePort, WireType.BINARY);
            Map<String, String> map1 = chronicleDataPublisher.getMap("/adept/examples/mapcollection1", String.class, String.class);
            map1.put("1", "test");
            map1.put("2", "test2");
            map1.put("3", "test3");
            Map<String, String> map2 = chronicleDataPublisher.getMap("/adept/examples/mapcollection2", String.class, String.class);
            map2.put("1", "test");
            map2.put("2", "test2");
            map2.put("3", "test3");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Inner class to handle building map and publishing to the end point.
     */
    public static class ChronicleDataPublisher implements AutoCloseable
    {
        //TODO DS get config settings from db or prop file
        private final VanillaAssetTree _assetTree = new VanillaAssetTree().forServer(false);
        private WireType _wireType;
        private int _port;
        private ServerEndpoint _serverEndpoint;

        public ChronicleDataPublisher() throws IOException
        {
            this(5801, WireType.BINARY);
        }

        public ChronicleDataPublisher(int port, WireType wireType) throws IOException
        {
            YamlLogging.showServerReads(true);
            YamlLogging.showServerWrites(true);
            _port = port;
            _wireType = wireType;

            _serverEndpoint = new ServerEndpoint("*:" + _port, _assetTree, "cluster");
        }

        public <K, V> Map<K, V> getMap(String uri, Class<K> keyClass, Class<V> valueClass)
        {
            return _assetTree.acquireMap(uri, keyClass, valueClass);
        }

        public <E> Publisher<E> getPublisher(String uri, Class<E> eventClass)
        {
            return _assetTree.acquirePublisher(uri, eventClass);
        }

        public <E> void registerSubscriber(String uri, Class<E> eventClass, Subscriber<E> subscriber)
        {
            _assetTree.registerSubscriber(uri, eventClass, subscriber);
        }

        @Override
        public void close() throws Exception
        {
            if(_assetTree != null)
            {
                _assetTree.close();
            }
        }
    }
}
