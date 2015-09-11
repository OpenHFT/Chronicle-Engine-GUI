package musiverification;

import net.openhft.chronicle.engine.api.tree.*;
import net.openhft.chronicle.engine.server.*;
import net.openhft.chronicle.engine.tree.*;
import net.openhft.chronicle.network.*;
import net.openhft.chronicle.wire.*;
import org.junit.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import static net.openhft.chronicle.engine.Chassis.*;

public class KeySubscriberTest
{
    public static final WireType WIRE_TYPE = WireType.BINARY;
    private static int _port = 6677;

    private static int expectedNoOfEvents = 2;
    private static int maxWait = 5;
    private static int waitCounter = 0;

    private static Map<String, String> _stringStringMap;
    private static String _mapName = "/chronicleMapString";
    private static String _mapArgs = "putReturnsNull=false";
    private static AssetTree _clientAssetTree;

    @Before
    public void setUp() throws IOException
    {
        resetChassis();

        AssetTree serverAssetTree = new VanillaAssetTree(1).forTesting();
        //The following line doesn't add anything and breaks subscriptions
//        serverAssetTree.root().addWrappingRule(MapView.class, "map directly to KeyValueStore", VanillaMapView::new, KeyValueStore.class);
//        serverAssetTree.root().addLeafRule(KeyValueStore.class, "use Chronicle Map", (context, asset) ->
//                new ChronicleMapKeyValueStore(context.basePath(OS.TARGET).entries(20).averageValueSize(10_000), asset));

        TCPRegistry.createServerSocketChannelFor("SubscriptionModelOnKeyTest.port");
        ServerEndpoint serverEndpoint = new ServerEndpoint("SubscriptionModelOnKeyTest.port", serverAssetTree, WireType.BINARY);

        _clientAssetTree = new VanillaAssetTree(89).forRemoteAccess("SubscriptionModelOnKeyTest.port", WireType.BINARY);

        _stringStringMap = _clientAssetTree.acquireMap(_mapName + "?" + _mapArgs, String.class, String.class);
        //FIXME why are we required to make a call first?
//        _stringStringMap.size();
    }

    @After
    public void tearDown()
    {
        waitCounter = 0;
        assetTree().close();
    }

    @Test
    public void testSubscriptionOnKey() throws InterruptedException
    {
        String testKey = "Key-sub-1";
        String keyUri = _mapName + "/" + testKey + "?bootstrap=false";

        AtomicInteger atomicInteger = new AtomicInteger(0);

        _clientAssetTree.registerSubscriber(keyUri, String.class, m -> {
            int eventNo = atomicInteger.incrementAndGet();
            System.out.println("KeySubscriber (#" + eventNo + ") " + m);
        });

        Thread.sleep(200);

        _stringStringMap.put(testKey, "Val1");
        _stringStringMap.put(testKey, "Val2");

        waitForEvents(atomicInteger);

        Assert.assertEquals(2, atomicInteger.get());
    }

    @Test
    public void testSubscriptionOnMap() throws InterruptedException
    {
        String testKey = "Key-sub-1";

        AtomicInteger atomicInteger = new AtomicInteger(0);

        _clientAssetTree.registerSubscriber(_mapName, String.class, m -> {
            int eventNo = atomicInteger.incrementAndGet();
            System.out.println("KeySubscriber (#" + eventNo + ") " + m);
        });

        Thread.sleep(200);

        _stringStringMap.put(testKey, "Val1");
        _stringStringMap.put(testKey, "Val2");

        waitForEvents(atomicInteger);

        Assert.assertEquals(2, atomicInteger.get());
    }

    private void waitForEvents(AtomicInteger atomicInteger) throws InterruptedException
    {
        while (atomicInteger.get() != expectedNoOfEvents && waitCounter != maxWait)
        {
            System.out.println("Waiting for events...");
            waitCounter++;
            Thread.sleep(100);
        }
    }
}