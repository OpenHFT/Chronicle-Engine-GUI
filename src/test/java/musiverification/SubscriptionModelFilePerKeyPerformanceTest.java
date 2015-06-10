package musiverification;

import ddp.api.TestUtils;
import net.openhft.chronicle.engine.Chassis;
import net.openhft.chronicle.engine.api.InvalidSubscriberException;
import net.openhft.chronicle.engine.api.TopicSubscriber;
import net.openhft.chronicle.engine.api.map.*;
import net.openhft.chronicle.engine.map.FilePerKeyValueStore;
import net.openhft.lang.Jvm;
import org.junit.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import static net.openhft.chronicle.engine.Chassis.addLeafRule;
import static net.openhft.chronicle.engine.Chassis.enableTranslatingValuesToBytesStore;
import static org.junit.Assert.assertEquals;

public class SubscriptionModelFilePerKeyPerformanceTest {
    static final AtomicInteger counter = new AtomicInteger();

    private static final int _noOfPuts = 50;
    private static final int _noOfRunsToAverage = 2;
    private static final long _secondInNanos = 10_000_000_000L;
    private static String _testStringFilePath = "Vols" + File.separator + "USDVolValEnvOIS-BO.xml";
    private static String _twoMbTestString;
    private static int _twoMbTestStringLength;
    private static Map<String, String> _testMap;
    private static String _mapName = "PerfTestMap";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        _twoMbTestString = TestUtils.loadSystemResourceFileToString(_testStringFilePath);
        _twoMbTestStringLength = _twoMbTestString.length();
    }

    @Before
    public void setUp() throws Exception {
        Chassis.resetChassis();

        enableTranslatingValuesToBytesStore();

        addLeafRule(KeyValueStore.class, "FilePer Key",
                (context, asset) -> new FilePerKeyValueStore(context.basePath(Jvm.TMP + "/fpk/" + counter.getAndIncrement()), asset));
        _testMap = Chassis.acquireMap(_mapName, String.class, String.class);

        _testMap.clear();
    }

    @After
    public void tearDown() throws IOException {
        ((Closeable) ((MapView) _testMap).underlying()).close();
    }

    /**
     * Test that listening to events for a given key can handle 50 updates per second of 2 MB string values.
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testSubscriptionMapEventOnKeyPerformance() {
        String key = TestUtils.getKey(_mapName, 0);

        //Create subscriber and register
        TestChronicleKeyEventSubscriber keyEventSubscriber = new TestChronicleKeyEventSubscriber(_twoMbTestStringLength);

        //todo This ends up getting a SubAsset not an asset
        Chassis.registerSubscriber(_mapName + "/" + key + "?bootstrap=false", String.class, keyEventSubscriber);

        //Perform test a number of times to allow the JVM to warm up, but verify runtime against average
        TestUtils.runMultipleTimesAndVerifyAvgRuntime(
                () -> IntStream.range(0, _noOfPuts).parallel().forEach(
                        i -> _testMap.put(key, i + _twoMbTestString)),
                _noOfRunsToAverage, _secondInNanos);

        //Test that the correct number of events was triggered on event listener
        keyEventSubscriber.waitForEvents(_noOfPuts * _noOfRunsToAverage, 0.45);
    }

    /**
     * Test that listening to events for a given map can handle 50 updates per second of 2 MB string values and are
     * triggering events which contain both the key and value (topic).
     */
    @Test
    public void testSubscriptionMapEventOnTopicPerformance() {
        String key = TestUtils.getKey(_mapName, 0);

        //Create subscriber and register
        TestChronicleTopicSubscriber topicSubscriber = new TestChronicleTopicSubscriber(key, _twoMbTestStringLength);

        Chassis.registerTopicSubscriber(_mapName, String.class, String.class, topicSubscriber);

        //Perform test a number of times to allow the JVM to warm up, but verify runtime against average
        TestUtils.runMultipleTimesAndVerifyAvgRuntime(() -> {
            IntStream.range(0, _noOfPuts).forEach(i ->
            {
                _testMap.put(key, i + _twoMbTestString);
            });
        }, _noOfRunsToAverage, _secondInNanos);

        //Test that the correct number of events was triggered on event listener
        topicSubscriber.waitForEvents(_noOfPuts * _noOfRunsToAverage, 0.2);
    }

    /**
     * Tests the performance of an event listener on the map for Insert events of 2 MB strings.
     * Expect it to handle at least 50 2 MB updates per second.
     */
    @Test
    public void testSubscriptionMapEventListenerInsertPerformance() {
        //Create subscriber and register
        TestChronicleMapEventListener mapEventListener = new TestChronicleMapEventListener(_mapName, _twoMbTestStringLength);
        Chassis.registerSubscriber(_mapName, MapEvent.class, e -> e.apply(mapEventListener));

        //Perform test a number of times to allow the JVM to warm up, but verify runtime against average
        TestUtils.runMultipleTimesAndVerifyAvgRuntime(() -> {
            _testMap.clear();
            pause(500);
            mapEventListener.resetCounters();
        }, () -> {
            IntStream.range(0, _noOfPuts).forEach(i ->
            {
                _testMap.put(TestUtils.getKey(_mapName, i), _twoMbTestString);
            });
            mapEventListener.waitForNMaps(_noOfPuts);

            //Test that the correct number of events were triggered on event listener
            if (_noOfPuts != mapEventListener.getNoOfInsertEvents().get())
                pause(50);
            assertEquals(_noOfPuts, mapEventListener.getNoOfInsertEvents().get());
            assertEquals(0, mapEventListener.getNoOfRemoveEvents().get());
            assertEquals(0, mapEventListener.getNoOfUpdateEvents().get());
        }, _noOfRunsToAverage, _secondInNanos);
    }

    /**
     * Tests the performance of an event listener on the map for Update events of 2 MB strings.
     * Expect it to handle at least 50 2 MB updates per second.
     */
    @Test
    public void testSubscriptionMapEventListenerUpdatePerformance() {
        //Put values before testing as we want to ignore the insert events
        Function<Integer, Object> putFunction = a -> _testMap.put(TestUtils.getKey(_mapName, a), System.nanoTime() + _twoMbTestString);

        IntStream.range(0, _noOfPuts).parallel().forEach(i ->
        {
            putFunction.apply(i);
        });

        //Create subscriber and register
        TestChronicleMapEventListener mapEventListener = new TestChronicleMapEventListener(_mapName, _twoMbTestStringLength);

        Chassis.registerSubscriber(_mapName + "?bootstrap=false", MapEvent.class, e -> e.apply(mapEventListener));

        //Perform test a number of times to allow the JVM to warm up, but verify runtime against average
        TestUtils.runMultipleTimesAndVerifyAvgRuntime(() -> {
            pause(200);
            mapEventListener.resetCounters();
        }, () -> {
            IntStream.range(0, _noOfPuts).forEach(i ->
            {
                putFunction.apply(i);
            });
            mapEventListener.waitForNMaps(_noOfPuts);

            //Test that the correct number of events were triggered on event listener
            assertEquals(_noOfPuts, mapEventListener.getNoOfUpdateEvents().get());
            assertEquals(0, mapEventListener.getNoOfInsertEvents().get());
            assertEquals(0, mapEventListener.getNoOfRemoveEvents().get());

        }, _noOfRunsToAverage, _secondInNanos);
    }

    /**
     * Tests the performance of an event listener on the map for Remove events of 2 MB strings.
     * Expect it to handle at least 50 2 MB updates per second.
     */
    @Test
    public void testSubscriptionMapEventListenerRemovePerformance() throws InterruptedException {
        //Put values before testing as we want to ignore the insert and update events

        //Create subscriber and register
        TestChronicleMapEventListener mapEventListener = new TestChronicleMapEventListener(_mapName, _twoMbTestStringLength);

        Chassis.registerSubscriber(_mapName + "?bootstrap=false", MapEvent.class, e -> e.apply(mapEventListener));

        //Perform test a number of times to allow the JVM to warm up, but verify runtime against average
        long runtimeInNanos = 0;

        for (int i = 0; i < _noOfRunsToAverage; i++) {
            mapEventListener.resetCounters();

            //Put values before testing as we want to ignore the insert and update events
            IntStream.range(0, _noOfPuts).forEach(c ->
            {
                _testMap.put(TestUtils.getKey(_mapName, c), _twoMbTestString);
            });

            mapEventListener.waitForNMaps(_noOfPuts);

            mapEventListener.resetCounters();

            long startTime = System.nanoTime();

            IntStream.range(0, _noOfPuts).parallel().forEach(c ->
            {
                _testMap.remove(TestUtils.getKey(_mapName, c));
            });

            mapEventListener.waitForNMaps(_noOfPuts);
            runtimeInNanos += System.nanoTime() - startTime;

            //Test that the correct number of events were triggered on event listener
            assertEquals(0, mapEventListener.getNoOfInsertEvents().get());
            assertEquals(_noOfPuts, mapEventListener.getNoOfRemoveEvents().get());
            assertEquals(0, mapEventListener.getNoOfUpdateEvents().get());
        }

        Assert.assertTrue((runtimeInNanos / (_noOfPuts * _noOfRunsToAverage)) <= _secondInNanos);
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Checks that all updates triggered are for the key specified in the constructor and increments the number of
     * updates.
     */
    class TestChronicleKeyEventSubscriber implements KeySubscriber<String> {
        private int _stringLength;
        private AtomicInteger _noOfEvents = new AtomicInteger(0);

        public TestChronicleKeyEventSubscriber(int stringLength) {
            _stringLength = stringLength;
        }

        public AtomicInteger getNoOfEvents() {
            return _noOfEvents;
        }

        @Override
        public void onMessage(String newValue) {
            assertEquals(_stringLength + 2, newValue.length(), 1);
            _noOfEvents.incrementAndGet();
        }

        public void waitForEvents(int events, double error) {
            for (int i = 1; i <= 30; i++) {
                if (events * (1 - error / 2) <= getNoOfEvents().get())
                    break;
                pause(i * i);
            }
            pause(100);
            assertEquals(events * (1 - error / 2), getNoOfEvents().get(), error / 2 * events);
        }
    }

    /**
     * Topic subscriber checking for each message that it is for the right key (in constructor) and the expected size
     * value.
     * Increments event counter which can be checked at the end of the test.
     */
    class TestChronicleTopicSubscriber implements TopicSubscriber<String, String> {
        private String _keyName;
        private int _stringLength;
        private AtomicInteger _noOfEvents = new AtomicInteger(0);

        public TestChronicleTopicSubscriber(String keyName, int stringLength) {
            _keyName = keyName;
            _stringLength = stringLength;
        }

        /**
         * Test that the topic/key is the one specified in constructor and the message is the expected size.
         *
         * @throws InvalidSubscriberException
         */
        @Override
        public void onMessage(String topic, String message) throws InvalidSubscriberException {
            assertEquals(_keyName, topic);
            assertEquals(_stringLength + 2, message.length(), 1);

            _noOfEvents.incrementAndGet();
        }

        public AtomicInteger getNoOfEvents() {
            return _noOfEvents;
        }

        public void waitForEvents(int events, double error) {
            for (int i = 1; i <= 30; i++) {
                if (events * (1 - error / 2) <= getNoOfEvents().get())
                    break;
                pause(i * i);
            }
            pause(100);
            assertEquals(events * (1 - error / 2), getNoOfEvents().get(), error / 2 * events);
        }
    }

    /**
     * Map event listener for performance testing. Checks that the key is the one expected and the size of the value is
     * as expected.
     * Increments event specific counters that can be used to check agains the expected number of events.
     */
    class TestChronicleMapEventListener implements MapEventListener<String, String> {
        private AtomicInteger _noOfInsertEvents = new AtomicInteger(0);
        private AtomicInteger _noOfUpdateEvents = new AtomicInteger(0);
        private AtomicInteger _noOfRemoveEvents = new AtomicInteger(0);
        private Set<String> mapsUpdated = Collections.synchronizedSet(new TreeSet<>());

        private String _mapName;
        private int _stringLength;

        public TestChronicleMapEventListener(String mapName, int stringLength) {
            _mapName = mapName;
            _stringLength = stringLength;
        }

        @Override
        public void update(String key, String oldValue, String newValue) {
            testKeyAndValue(key, newValue, _noOfUpdateEvents);
        }

        @Override
        public void insert(String key, String value) {
            testKeyAndValue(key, value, _noOfInsertEvents);
        }

        @Override
        public void remove(String key, String value) {
            testKeyAndValue(key, value, _noOfRemoveEvents);
        }

        public AtomicInteger getNoOfInsertEvents() {
            return _noOfInsertEvents;
        }

        public AtomicInteger getNoOfUpdateEvents() {
            return _noOfUpdateEvents;
        }

        public AtomicInteger getNoOfRemoveEvents() {
            return _noOfRemoveEvents;
        }

        public void resetCounters() {
            _noOfInsertEvents = new AtomicInteger(0);
            _noOfUpdateEvents = new AtomicInteger(0);
            _noOfRemoveEvents = new AtomicInteger(0);
            mapsUpdated.clear();
        }

        private void testKeyAndValue(String key, String value, AtomicInteger counterToIncrement) {
//            System.out.println("key: " + key);
            counterToIncrement.getAndIncrement();
            mapsUpdated.add(key);
            assertEquals(_stringLength + 8, value.length(), 8);
        }

        public void waitForNMaps(int noOfMaps) {
            for (int i = 1; i <= 40; i++) {
                if (mapsUpdated.size() >= noOfMaps)
                    break;
                pause(i * i);
            }
            assertEquals(toString(), noOfMaps, mapsUpdated.size());
        }

        @Override
        public String toString() {
            return "TestChronicleMapEventListener{" +
                    "_noOfInsertEvents=" + _noOfInsertEvents +
                    ", _noOfUpdateEvents=" + _noOfUpdateEvents +
                    ", _noOfRemoveEvents=" + _noOfRemoveEvents +
                    ", mapsUpdated=" + mapsUpdated.size() +
                    ", _mapName='" + _mapName + '\'' +
                    ", _stringLength=" + _stringLength +
                    '}';
        }
    }
}