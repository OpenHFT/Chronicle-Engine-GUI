package jp.mufg.chronicle.map.eventlistener;

import ddp.api.TestUtils;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.engine.api.pubsub.TopicSubscriber;
import net.openhft.chronicle.engine.map.AuthenticatedKeyValueStore;
import net.openhft.chronicle.engine.map.FilePerKeyValueStore;
import net.openhft.chronicle.engine.tree.VanillaAsset;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import org.junit.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static net.openhft.chronicle.core.Jvm.pause;

/**
 * Created by daniels on 31/03/2015.
 */
@Ignore("todo fix")
public class FastMapEventListenerStatelessClientTest {
    private static final String _mapBasePath = "Chronicle"; //OS.TARGET + "/Chronicle";

    private static final VanillaAssetTree clientAssetTree = new VanillaAssetTree().forRemoteAccess();
    private static final VanillaAssetTree serverAssetTree = new VanillaAssetTree().forTesting();

    private ChronicleTestEventListener _chronicleTestEventListener;

    private Map<String, BytesStore> _StringStringMap;
    private Map<String, BytesStore> _StringStringMapClient;

    private final BytesStore _value1 = BytesStore.wrap(ByteBuffer.allocateDirect(2 << 20));//;"TestValue1";
    private final BytesStore _value2 = BytesStore.wrap(ByteBuffer.allocateDirect(2_000_000));//;"TestValue2";

    private static final AtomicInteger _noOfEventsTriggered = new AtomicInteger();

    public FastMapEventListenerStatelessClientTest() {
    }

    @BeforeClass
    public static void beforeClass() throws IOException {

        VanillaAsset root = (VanillaAsset) serverAssetTree.root();
        root.addLeafRule(AuthenticatedKeyValueStore.class, "use File Per Key",
                (context, asset) -> new FilePerKeyValueStore(context.basePath(_mapBasePath), asset));
    }

    @Before
    public void setUp() throws Exception {
        _noOfEventsTriggered.set(0);

        _StringStringMap = serverAssetTree.acquireMap("chronicleMapString?putReturnsNull=true", String.class, BytesStore.class);
        _StringStringMap.clear();

        _chronicleTestEventListener = new ChronicleTestEventListener();

        // TODO change this to be a remote session.
        _StringStringMapClient = clientAssetTree.acquireMap("chronicleMapString", String.class, BytesStore.class);
        clientAssetTree.registerTopicSubscriber("chronicleMapString", String.class, BytesStore.class, _chronicleTestEventListener);

    }

    @After
    public void tearDown() throws Exception {
        _StringStringMap.clear();
        clientAssetTree.close();
        serverAssetTree.close();
    }

    /**
     * Test that event listener is triggered for every put.
     *
     * @throws Exception
     */
    @Test
    public void testMapEvenListenerClientPut() throws Exception {
        String testKey = "TestKeyPut";
        int noOfIterations = 50;

        testIterateAndAlternate(
                (x) -> _StringStringMap.put(testKey, x),
                (x) -> _StringStringMap.put(testKey, x),
                noOfIterations);
    }

    /**
     * Test that event listener is triggered for every replace.
     *
     * @throws Exception
     */
    @Test
    public void testMapEvenListenerReplace() throws Exception {
        String testKey = "TestKeyGetReplace";
        int noOfIterations = 50;

        _StringStringMap.put(testKey, _value2);

        Consumer<BytesStore> consumer = (x) -> _StringStringMap.replace(testKey, x);

        testIterateAndAlternate(
                consumer,
                consumer,
                noOfIterations);
    }

    /**
     * Test that event listener is triggered for every "acquireUsingLocked" value update.
     *
     * @throws Exception
     */
/*
    @Test
    public void testMapEvenListenerAcquireUsingLocked() throws Exception
    {
        StringValue valueInstance = _chronicleMapStringValue.newValueInstance();

        String testKey = "TestKeyAcquireUsingLocked";
        int noOfIterations = 50;

        Consumer<String> consumer = (x) -> {
            try (WriteContext<String, StringValue> writeContext = _chronicleMapStringValue.acquireUsingLocked(testKey, valueInstance))
            {
                valueInstance.setValue(x);
            }
        };

        testIterateAndAlternate(
                consumer,
                consumer,
                noOfIterations);
    }
*/

    /**
     * Test that event listener is triggered for every "acquireUsing" value update.
     *
     * @throws Exception
     */
/*
    @Test
    public void testMapEvenListenerAcquireUsing() throws Exception
    {
        StringValue valueInstance = _chronicleMapStringValue.newValueInstance();

        String testKey = "TestKeyAcquireUsing";
        int noOfIterations = 50;

        Consumer<String> consumer = (x) -> {
            StringValue stringValue = _chronicleMapStringValue.acquireUsing(testKey, valueInstance);
            stringValue.setValue(x);
        };

        testIterateAndAlternate(
                consumer,
                consumer,
                noOfIterations);
    }
*/

    /**
     * Test that event listener is triggered for every "getUsing" value update.
     *
     * @throws Exception
     */
/*
    @Test
    public void testMapEvenListenerGetUsing() throws Exception
    {
        StringValue valueInstance = _chronicleMapStringValue.newValueInstance();

        String testKey = "TestKeyGetUsing";
        int noOfIterations = 50;

        _chronicleMapStringValue.put(testKey, valueInstance);

        Consumer<String> consumer = (x) -> {
            StringValue using = _chronicleMapStringValue.getUsing(testKey, valueInstance);
            using.setValue(x);
        };

        testIterateAndAlternate(
                consumer,
                consumer,
                noOfIterations);
    }
*/

    /**
     * Performs the given number of iterations and alternates between calling consumer1 and consumer2 passing
     * either _value1 or _value2.
     *
     * @param consumer1      Consumer1 to call.
     * @param consumer2      Consumer2 to call.
     * @param noOfIterations Number of iterations to perform.
     */
    private void testIterateAndAlternate(Consumer<BytesStore> consumer1, Consumer<BytesStore> consumer2, int noOfIterations) {
        long startTime = System.nanoTime();
        int count = 0;
        while (System.nanoTime() - startTime < 5e9) {
            for (int i = 0; i < noOfIterations; i++) {
                if (i % 2 == 0) {
                    consumer1.accept(_value1);
                } else {
                    consumer2.accept(_value2);
                }
            }
            count++;
        }

        double runtime = TestUtils.calculateAndPrintRuntime(startTime, count);

        //Test that 50 updates takes less than 1 second
//        Assert.assertTrue(runtime < 1000000000);

        for (int i = 0; i < 100; i++) {
            pause(20);
                if (_noOfEventsTriggered.get() >= noOfIterations * count) break;
        }
        Assert.assertEquals(noOfIterations * count, _noOfEventsTriggered.get(), count);
    }

    static class ChronicleTestEventListener implements TopicSubscriber<String, BytesStore> {
        @Override
        public void onMessage(String topic, BytesStore message) {
            _noOfEventsTriggered.incrementAndGet();
        }
    }
}