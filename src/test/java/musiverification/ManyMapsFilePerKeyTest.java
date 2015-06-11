package musiverification;

import ddp.api.TestUtils;
import net.openhft.chronicle.engine.Chassis;
import net.openhft.chronicle.engine.api.InvalidSubscriberException;
import net.openhft.chronicle.engine.api.TopicSubscriber;
import net.openhft.chronicle.engine.map.AuthenticatedKeyValueStore;
import net.openhft.chronicle.engine.map.FilePerKeyValueStore;
import net.openhft.lang.Jvm;
import org.junit.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static net.openhft.chronicle.engine.Chassis.addLeafRule;
import static net.openhft.chronicle.engine.Chassis.enableTranslatingValuesToBytesStore;

/* On linux.
fs.inotify.max_user_watches = 1500000
fs.inotify.max_user_instances = 2000
 */
@Ignore
public class ManyMapsFilePerKeyTest {
    private static Map<String, Map<String, String>> _maps;
    private static String _mapBaseName = "Test-Map-";
    //todo takes a long time to create 1100 maps slow for testing
    //todo https://higherfrequencytrading.atlassian.net/browse/HCOLL-365
    private static int _noOfMaps = 1_100;
    //    private static int _noOfMaps = 300;
    private static int _noOfKvps = 1_000;

    @BeforeClass
    public static void setUp() throws IOException {

        enableTranslatingValuesToBytesStore();

        addLeafRule(AuthenticatedKeyValueStore.class, "FilePer Key",
                (context, asset) -> new FilePerKeyValueStore(context.basePath(Jvm.TMP + "/fpk"), asset));
        _maps = new HashMap<>();

        System.out.println("Creating maps.");
        AtomicInteger count = new AtomicInteger();
        IntStream.rangeClosed(1, _noOfMaps).forEach(i -> {
            String mapName = _mapBaseName + i;

            Map<String, String> map = Chassis.acquireMap(mapName, String.class, String.class);

            for (int j = 1; j <= _noOfKvps; j++) {
                map.put(TestUtils.getKey(mapName, j), TestUtils.getValue(mapName, j));
            }

            _maps.put(mapName, map);
            if (count.incrementAndGet() % 25 == 0)
                System.out.println("... " + count);
        });
        System.out.println("... " + _noOfMaps + " Done.");
    }

    @Before
    public void initTest() throws Exception {
//        createAndFillMaps();
    }

    /**
     * Test that the number of maps created exist.
     * Test that the number of key-value-pairs in the map matches the expected.
     * Test that all the keys in this map contains the map name (ie. no other map's keys overlap).
     * Test that all the values in this map contains the map name (ie. no other map's values overlap).
     *
     * @throws Exception
     */
    @Test
    public void testKeysAndValuesInEachMap() throws Exception {
        //Test that the number of maps created exist
        Assert.assertEquals(_noOfMaps, _maps.size());

        _maps.entrySet().parallelStream().forEach((Map.Entry<String, Map<String, String>> mapEntry) -> {
            Map<String, String> map = mapEntry.getValue();

            //Test that the number of key-value-pairs in the map matches the expected.
            Assert.assertEquals(_noOfKvps, map.size());

            //Test that all the keys in this map contains the map name (ie. no other map's keys overlap).
            Assert.assertFalse(map.keySet().stream().anyMatch(k -> !k.contains(mapEntry.getKey())));

            //Test that all the values in this map contains the map name (ie. no other map's values overlap).
            Assert.assertFalse(map.values().stream().anyMatch(v -> !v.contains(mapEntry.getKey())));
        });
    }

    /**
     * Test that having a large number of maps and TopicSubscriptions for each of them.
     * Test that subscribers only have events triggered for the given map that they subscribe to.
     *
     * @throws Exception
     */
    @Test
    public void testManyMapsManyTopicListeners() throws Exception {
        Map<String, EventsForMapSubscriber> eventsForMapSubscriberMap = new HashMap<>();

        _maps.keySet().parallelStream().forEach((String key) -> {
            EventsForMapSubscriber eventsForMapSubscriber = new EventsForMapSubscriber(key);
            eventsForMapSubscriberMap.put(key, eventsForMapSubscriber);

            Chassis.registerTopicSubscriber(key + "?bootstrap=false", String.class, String.class, eventsForMapSubscriber);
        });

        //This gets all of the maps an re-puts all values
        _maps = new HashMap<>();
        createAndFillMaps();

        _maps.keySet().parallelStream().forEach((String key) -> {
            EventsForMapSubscriber eventsForMapSubscriber = eventsForMapSubscriberMap.get(key);

            Assert.assertEquals(_noOfKvps, eventsForMapSubscriber.getNoOfEvents());

            Chassis.unregisterTopicSubscriber(key, String.class, String.class, eventsForMapSubscriber);
        });
    }

    @Test
    @Ignore("todo")
    public void testConnectToMultipleMapsUsingTheSamePort() throws Exception {
        throw new UnsupportedOperationException("DS test that we can connect and interact with a large number of maps on the same port");
    }

    @Test
    @Ignore("todo")
    public void testMapReplication() throws Exception {
        throw new UnsupportedOperationException("DS test that maps are automatically replicated on one or more failover servers, with each map on a server being uniquely associated with given name");
    }

    /**
     * Test creating an engine with an underlying Chronicle Map store where the base
     * path is specified as a folder that exist.
     *
     * @throws Exception
     */
    @Test
    public void testChronicleMapCreationFolderBasePath() throws Exception {
        String basePath = Jvm.TMP;

        testMultipleMapsWithUnderlyingChronicleMap(basePath);
    }

    /**
     * Test creating an engine with an underlying Chronicle Map store where the base path is specified
     * as a full path to a file that does not exist.
     *
     * @throws Exception
     */
    @Test
    public void testChronicleMapCreationFileBasePath() throws Exception {
        String basePath = Jvm.TMP + "nonExistingFileOrFolder";

        testMultipleMapsWithUnderlyingChronicleMap(basePath);
    }

    /**
     * Create an engine using a ChronicleMapKeyValueStore as underlying. Get two maps and put values into them.
     *
     * @param basePath for key value store
     */
    private void testMultipleMapsWithUnderlyingChronicleMap(String basePath) {
        String map1Name = "MyMap1";
        String map2Name = "MyMap2";

        Chassis.resetChassis();

        //Get map1 - expect 1 file to be created
        Map<String, String> map1 = Chassis.acquireMap(map1Name, String.class, String.class);

        String key1 = "KeyMap1";
        String value1 = "ValueMap1";

        map1.put(key1, value1);

        Assert.assertEquals(value1, map1.get(key1));

        //Get map2 - expect a second file to be created
        Map<String, String> map2 = Chassis.acquireMap(map2Name, String.class, String.class);

        String key2 = "KeyMap2";
        String value2 = "ValueMap2";

        map2.put(key2, value2);

        Assert.assertEquals(value2, map2.get(key2));
    }

    /**
     * Test that we can put a map as value and get it back and get values from it.
     *
     * @throws Exception
     */
    @Test
    public void testSupportForNestedMaps() throws Exception {
        String mapName = "MapOfMaps";
        String testKey = "TestKey";
        String testValue = "TestValue";

        Map<String, Map> map = Chassis.acquireMap(mapName, String.class, Map.class);

        Map<String, String> mapInMap = new HashMap<>();
        mapInMap.put(testKey, testValue);

        map.put(testKey, mapInMap);

        Map<String, Map> newMapRef = Chassis.acquireMap(mapName, String.class, Map.class);

        Map<String, String> newMapInMapRef = newMapRef.get(testKey);
        String valueFromMap = newMapInMapRef.get(testKey);

        Assert.assertEquals(testValue, valueFromMap);
    }

    /**
     * Creates configured number of maps and fills them with configured number of key/value pairs
     */
    private void createAndFillMaps() {

        System.out.println("Filling maps");
        AtomicInteger count = new AtomicInteger();
        _maps.entrySet().parallelStream().forEach(e -> {
            e.getValue().clear();
            for (int j = 1; j <= _noOfKvps; j++) {
                e.getValue().put(TestUtils.getKey(e.getKey(), j), TestUtils.getValue(e.getKey(), j));
            }
            if (count.incrementAndGet() % 25 == 0)
                System.out.println("... " + count);
        });
        System.out.println("... " + _maps.size() + " done.");

    }

    /**
     * Checks that all updates triggered are for the map specified in the constructor and increments the number of
     * updates.
     */
    class EventsForMapSubscriber implements TopicSubscriber<String, String> {
        private String _mapName;
        private AtomicInteger _noOfEvents = new AtomicInteger(0);

        public EventsForMapSubscriber(String mapName) {
            _mapName = mapName;
        }

        public int getNoOfEvents() {
            return _noOfEvents.get();
        }

        @Override
        public void onMessage(String key, String value) throws InvalidSubscriberException {
            int eventNo = _noOfEvents.incrementAndGet();

            //Test that the key matches the expected
            Assert.assertEquals(TestUtils.getKey(_mapName, eventNo), key);

            //Test that the value matches the expected
            Assert.assertEquals(TestUtils.getValue(_mapName, eventNo), value);
        }
    }
}