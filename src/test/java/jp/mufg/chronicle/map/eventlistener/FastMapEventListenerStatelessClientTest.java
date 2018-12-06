package jp.mufg.chronicle.map.eventlistener;

import ddp.api.TestUtils;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.engine.api.pubsub.TopicSubscriber;
import net.openhft.chronicle.engine.map.AuthenticatedKeyValueStore;
import net.openhft.chronicle.engine.map.FilePerKeyValueStore;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAsset;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.wire.WireType;
import net.openhft.chronicle.wire.YamlLogging;
import org.junit.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static net.openhft.chronicle.core.Jvm.pause;

/**
 * Created by Peter Lawrey
 */
@Ignore("CHENT-49 attempts to use optimisations which are not ready")
public class FastMapEventListenerStatelessClientTest {
    private static final String _mapBasePath = OS.TARGET + "/FastMapEventListenerStatelessClientTest";
    private static final VanillaAssetTree serverAssetTree = new VanillaAssetTree().forServer(true);
    private static final AtomicInteger _noOfEventsTriggered = new AtomicInteger();
    private static VanillaAssetTree clientAssetTree;
    private static ChronicleTestEventListener _chronicleTestEventListener;
    private static Map<String, BytesStore> _StringStringMap;
    private static Map<String, BytesStore> _StringStringMapClient;
    private static ServerEndpoint serverEndpoint;
    private final BytesStore _value1 = BytesStore.wrap(ByteBuffer.allocateDirect(2 << 20));//;"TestValue1";
    private final BytesStore _value2 = BytesStore.wrap(ByteBuffer.allocateDirect(2_000_000));//;"TestValue2";

    public FastMapEventListenerStatelessClientTest() {
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        TCPRegistry.createServerSocketChannelFor("FastMapEventListenerStatelessClientTest");
        clientAssetTree = new VanillaAssetTree().forRemoteAccess
                ("FastMapEventListenerStatelessClientTest", WireType.TEXT, Throwable::printStackTrace);
        VanillaAsset root = serverAssetTree.root();
        root.enableTranslatingValuesToBytesStore();
        root.addLeafRule(AuthenticatedKeyValueStore.class, "use File Per Key",
                (context, asset) -> new FilePerKeyValueStore(context.basePath(_mapBasePath), asset));

        serverEndpoint = new ServerEndpoint("FastMapEventListenerStatelessClientTest", serverAssetTree, "cluster");

        _StringStringMap = serverAssetTree.acquireMap("chronicleMapString?putReturnsNull=true", String.class, BytesStore.class);

        _chronicleTestEventListener = new ChronicleTestEventListener();

        _StringStringMapClient = clientAssetTree.acquireMap("chronicleMapString", String.class, BytesStore.class);
        clientAssetTree.registerTopicSubscriber("chronicleMapString", String.class, BytesStore.class, _chronicleTestEventListener);

    }

    @AfterClass
    public static void tearDown() {
        _StringStringMap.clear();
        clientAssetTree.close();
        serverEndpoint.close();
        serverAssetTree.close();
    }

    @Before
    public void setUp() {
        _noOfEventsTriggered.set(0);
        _StringStringMap.clear();
        YamlLogging.setAll(false);
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
     *
     * @
     */
    @Test
    public void testMapEvenListenerReplace() {
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