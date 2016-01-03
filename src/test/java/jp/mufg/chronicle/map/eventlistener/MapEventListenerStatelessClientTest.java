package jp.mufg.chronicle.map.eventlistener;

import ddp.api.TestUtils;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.pubsub.TopicSubscriber;
import net.openhft.chronicle.engine.map.AuthenticatedKeyValueStore;
import net.openhft.chronicle.engine.map.FilePerKeyValueStore;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAsset;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.wire.WireType;
import org.junit.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Created by daniels on 31/03/2015.
 */
public class MapEventListenerStatelessClientTest {
    private static final String _mapBasePath = OS.TARGET + "/MapEventListenerStatelessClientTest";
    private static final VanillaAssetTree serverAssetTree = new VanillaAssetTree().forTesting(Throwable::printStackTrace);
    private static final AtomicInteger _noOfEventsTriggered = new AtomicInteger();
    private static VanillaAssetTree clientAssetTree;
    private static ChronicleTestEventListener _chronicleTestEventListener;
    private static MapView<String, String> _StringStringMap;
    private static ServerEndpoint serverEndpoint;
    private final String _value1 = new String(new char[2 << 20]);//;"TestValue1";
    private final String _value2;

    public MapEventListenerStatelessClientTest() {
        char[] value = new char[2 << 20];
        Arrays.fill(value, 'd');
        _value2 = new String(value);
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        TCPRegistry.createServerSocketChannelFor("MapEventListenerStatelessClientTest");
        clientAssetTree = new VanillaAssetTree().forRemoteAccess
                ("MapEventListenerStatelessClientTest", WireType.BINARY, Throwable::printStackTrace);

        VanillaAsset root = serverAssetTree.root();
        root.enableTranslatingValuesToBytesStore();
        root.addLeafRule(AuthenticatedKeyValueStore.class, "use File Per Key",
                (context, asset) -> new FilePerKeyValueStore(context.basePath(_mapBasePath), asset));

        serverEndpoint = new ServerEndpoint("MapEventListenerStatelessClientTest", serverAssetTree, WireType.BINARY);

//        _StringStringMap = serverAssetTree.acquireMap("chronicleMapString?putReturnsNull=true", String.class, String.class);

        _chronicleTestEventListener = new ChronicleTestEventListener();

        _StringStringMap = clientAssetTree.acquireMap("chronicleMapString", String.class, String.class);
        clientAssetTree.registerTopicSubscriber("chronicleMapString", String.class, String.class, _chronicleTestEventListener);
    }

    @AfterClass
    public static void tearDown() {
        _StringStringMap.clear();
        clientAssetTree.close();
        serverAssetTree.close();
//        serverEndpoint.close();
    }

    @Before
    public void setUp() {
        _noOfEventsTriggered.set(0);

        _StringStringMap.clear();


    }

    /**
     * Test that event listener is triggered for every put.
     *
     * @
     */
    @Test
    public void testMapEvenListenerClientPut() {
        String testKey = "TestKeyPut";
        int noOfIterations = 50;

        testIterateAndAlternate(
                (x) -> _StringStringMap.put(testKey, x),
                (x) -> _StringStringMap.put(testKey, x),
                noOfIterations);
    }

    /**
     * Test that event listener is triggered for every replace.
     */
    @Test
    @Ignore("Fixed in 1.5.6-beta")
    public void testMapEvenListenerReplace() {
        String testKey = "TestKeyGetReplace";
        String testKey2 = "TestKeyGetReplace";
        int noOfIterations = 50;

        _StringStringMap.put(testKey, _value2);

        Consumer<String> consumer = (x) -> _StringStringMap.replace(testKey, x);
        Consumer<String> consumer2 = (x) -> _StringStringMap.replace(testKey2, x);

        testIterateAndAlternate(
                consumer,
                consumer2,
                noOfIterations);
    }

    /**
     * Test that event listener is triggered for every "acquireUsingLocked" value update.
     */
    @Test
    @Ignore("TODO FIX")
    public void testMapEvenListenerAcquireUsingLocked()
    {
        String testKey = "TestKeyAcquireUsingLocked";
        String testKey2 = "TestKeyAcquireUsingLocked2";
        int noOfIterations = 50;

        Consumer<String> consumer = x -> _StringStringMap.asyncUpdateKey(testKey, prev -> x);
        Consumer<String> consumer2 = x -> _StringStringMap.asyncUpdateKey(testKey2, prev -> x);

        testIterateAndAlternate(
                consumer,
                consumer2,
                noOfIterations);
    }

    /**
     * Test that event listener is triggered for every "getUsing" value update.
     *
     */
    @Test
    @Ignore("TODO FIX")
    public void testMapEvenListenerGetUsing()
    {
        String testKey = "testMapEvenListenerGetUsing";
        String testKey2 = "testMapEvenListenerGetUsing2";
        int noOfIterations = 50;

        Consumer<String> consumer = x -> _StringStringMap.applyToKey(testKey, value -> value);
        Consumer<String> consumer2 = x -> _StringStringMap.applyToKey(testKey2, value -> value);

        long startTime = System.nanoTime();
        int count = 0;
        while (System.nanoTime() - startTime < 5e9) {
            for (int i = 0; i < noOfIterations; i++) {
                if (i % 2 == 0) {
                    consumer.accept(_value1);
                } else {
                    consumer2.accept(_value2);
                }
            }
            count++;
        }

        double runtime = TestUtils.calculateAndPrintRuntime(startTime, count);

        Assert.assertEquals(0, _noOfEventsTriggered.get());
    }

    /**
     * Performs the given number of iterations and alternates between calling consumer1 and
     * consumer2 passing either _value1 or _value2.
     *
     * @param consumer1      Consumer1 to call.
     * @param consumer2      Consumer2 to call.
     * @param noOfIterations Number of iterations to perform.
     */
    private void testIterateAndAlternate(Consumer<String> consumer1, Consumer<String> consumer2, int noOfIterations) {
        long startTime = System.nanoTime();
        int count = 0;
        int events = 0;
        while (System.nanoTime() - startTime < 5e9) {
            for (int i = 0; i < noOfIterations; i++) {
                if (i % 2 == 0) {
                    consumer1.accept(_value1);
                } else {
                    consumer2.accept(_value2);
                }
                final int finalEvents = events;
                waitFor(() -> _noOfEventsTriggered.get() >= finalEvents);
                events++;
            }
            count++;
        }

        double runtime = TestUtils.calculateAndPrintRuntime(startTime, count);
        final int finalCount = count;
        waitFor(() -> _noOfEventsTriggered.get() >= noOfIterations * (finalCount - 1));

        Assert.assertEquals(noOfIterations * count, _noOfEventsTriggered.get(), count);
    }


    private void waitFor(BooleanSupplier b) {
        for (int i = 1; i <= 40; i++)
            if (!b.getAsBoolean())
                Jvm.pause(i * i);
    }

    static class ChronicleTestEventListener implements TopicSubscriber<String, String> {
        @Override
        public void onMessage(String topic, String message) {
            _noOfEventsTriggered.incrementAndGet();
        }
    }
}