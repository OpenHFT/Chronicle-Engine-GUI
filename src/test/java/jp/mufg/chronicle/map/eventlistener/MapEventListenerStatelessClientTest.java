package jp.mufg.chronicle.map.eventlistener;

import ddp.api.TestUtils;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.engine2.Chassis;
import net.openhft.chronicle.engine2.api.Session;
import net.openhft.chronicle.engine2.api.TopicSubscriber;
import net.openhft.chronicle.engine2.api.map.KeyValueStore;
import net.openhft.chronicle.engine2.map.FilePerKeyValueStore;
import net.openhft.chronicle.engine2.map.VanillaStringStringKeyValueStore;
import net.openhft.chronicle.engine2.session.StringStringKeyValueStore;
import org.junit.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

import static net.openhft.chronicle.engine2.Chassis.*;

/**
 * Created by daniels on 31/03/2015.
 */
public class MapEventListenerStatelessClientTest {
    private static final String _mapBasePath = OS.TMP + "/Chronicle";

    private ChronicleTestEventListener _chronicleTestEventListener;

    private Map<String, String> _StringStringMap;
    private Map<String, String> _StringStringMapClient;

    private final String _value1 = new String(new char[2 << 20]);//;"TestValue1";
    private final String _value2;

    private static int _noOfEventsTriggered = 0;

    public MapEventListenerStatelessClientTest() {
        char[] value = new char[2 << 20];
        Arrays.fill(value, 'd');
        _value2 = new String(value);
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        resetChassis();

        registerFactory("", StringStringKeyValueStore.class, VanillaStringStringKeyValueStore::new);
        registerFactory("", KeyValueStore.class, context -> new FilePerKeyValueStore(context.basePath(_mapBasePath)));
    }

    @Before
    public void setUp() throws Exception {
        _noOfEventsTriggered = 0;

        _StringStringMap = Chassis.acquireMap("chronicleMapString?putReturnsNull=true", String.class, String.class);
        _StringStringMap.clear();

        _chronicleTestEventListener = new ChronicleTestEventListener();


        // TODO change this to be a remote session.
        Session clientSession = defaultSession();
        _StringStringMapClient = clientSession.acquireMap("chronicleMapString", String.class, String.class);
        clientSession.registerTopicSubscriber("chronicleMapString", String.class, String.class, _chronicleTestEventListener);

    }

    @After
    public void tearDown() throws Exception {
        _StringStringMap.clear();
        Chassis.defaultSession().close();
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
        _noOfEventsTriggered = 0;

        Consumer<String> consumer = (x) -> _StringStringMap.replace(testKey, x);

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
    private void testIterateAndAlternate(Consumer<String> consumer1, Consumer<String> consumer2, int noOfIterations) {
        long startTime = System.nanoTime();
        int count = 0;
        while (System.nanoTime() - startTime < 500e9) {
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
            try {
                Thread.sleep(20);
                if (_noOfEventsTriggered >= noOfIterations * count) break;
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
        }
        Assert.assertEquals(noOfIterations * count, _noOfEventsTriggered);
    }

    static class ChronicleTestEventListener implements TopicSubscriber<String, String> {
        @Override
        public void onMessage(String topic, String message) {
            _noOfEventsTriggered++;
        }
    }
}