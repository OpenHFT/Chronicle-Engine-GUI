package topicsubscriptionrepro;

import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.VanillaSessionDetails;
import net.openhft.chronicle.wire.WireType;

import java.io.IOException;
import java.util.Map;

public class ClientMain {
    private static final WireType _wireType = WireType.BINARY;
    private static VanillaAssetTree _assetTree;
    private static String _serverAddress = "localhost:5566";
    private static String _mapUri = "/mfil/test/map";

    public static void main(String[] args) throws IOException, InterruptedException {
        _assetTree = new VanillaAssetTree();

        _assetTree.root().forRemoteAccess(
                new String[]{_serverAddress}, _wireType,
                VanillaSessionDetails.of("mfil-daniels", null, ""), null);

        //This works
        _assetTree.registerSubscriber(_mapUri, String.class, message -> System.out.println("Subscriber 1: " + message));

        //This works
        _assetTree.registerSubscriber(_mapUri, String.class, message -> System.out.println("Subscriber 2: " + message));

        Thread.sleep(500);

        //FIXME this doesn't work
        _assetTree.registerTopicSubscriber(_mapUri, String.class, String.class,
                (topic, message) -> System.out.println("TopicSubscriber 1: " + topic + " - " + message));

        //Allow time for the subscribers to be registered
        Thread.sleep(500);

        Map<String, String> map = _assetTree.acquireMap(_mapUri, String.class, String.class);

        map.put("Key1", "Value1");
        map.put("Key2", "Value2");
        map.put("Key3", "Value3");

        System.out.println("Press any key to exit...");
        System.in.read();
    }
}