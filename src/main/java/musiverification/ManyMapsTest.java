package musiverification;

import ddp.api.TestUtils;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.engine.api.map.KeyValueStore;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.pubsub.InvalidSubscriberException;
import net.openhft.chronicle.engine.api.pubsub.TopicSubscriber;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.map.ChronicleMapKeyValueStore;
import net.openhft.chronicle.engine.map.VanillaMapView;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.wire.WireType;
import org.junit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ManyMapsTest {
    static {
        System.setProperty("heartbeat.timeout", "100000");
    }
    private static Map<String, Map<String, String>> _maps;
    private static String _mapBaseName = "ManyMapsTest-";

    //    private static int _noOfMaps = Boolean.getBoolean("quick") ? 100 : 1_100;
    private static int _noOfMaps = Boolean.getBoolean("quick") ? 10 : 1100;
    private static int _noOfKvps = 1_000;
    private static AssetTree assetTree = new VanillaAssetTree().forTesting();

    @BeforeClass
    public static void setUp() throws IOException {
        String basePath = OS.TARGET + "/ManyMapTests";
        Files.createDirectories(Paths.get(basePath));

        Files.walk(Paths.get(basePath)).filter(p -> !Files.isDirectory(p)).forEach(p -> {
            try {
                Files.deleteIfExists(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        assetTree.root().addWrappingRule(MapView.class, "map directly to KeyValueStore", VanillaMapView::new, KeyValueStore.class);
        assetTree.root().addLeafRule(KeyValueStore.class, "use Chronicle Map", (context, asset) ->
                new ChronicleMapKeyValueStore(context.basePath(basePath + "/" + asset.name()).entries(1200), asset));
        _maps = Collections.synchronizedMap(new HashMap<>());

        System.out.println("Creating maps.");
        AtomicInteger count = new AtomicInteger();
        IntStream.rangeClosed(1, _noOfMaps).parallel().forEach(i -> {
            String mapName = _mapBaseName + i;

            Map<String, String> map = assetTree.acquireMap(mapName, String.class, String.class);

            for (int j = 1; j <= _noOfKvps; j++) {
                map.put(TestUtils.getKey(mapName, j), TestUtils.getValue(mapName, j));
            }

            _maps.put(mapName, map);
            if (count.incrementAndGet() % 100 == 0)
                System.out.print("... " + count);
        });
        System.out.println("... " + _noOfMaps + " Done.");
    }

    @Before
    public void initTest() {
//        createAndFillMaps();
    }

    @AfterClass
    public static void tearDown() {
        assetTree.close();
    }

    /**
     * Test that the number of maps created exist. Test that the number of key-value-pairs in the
     * map matches the expected. Test that all the keys in this map contains the map name (ie. no
     * other map's keys overlap). Test that all the values in this map contains the map name (ie. no
     * other map's values overlap).
     */
    @Test
    public void testKeysAndValuesInEachMap() {
        //Test that the number of maps created exist
        Assert.assertEquals(_noOfMaps, _maps.size());

        for (Map.Entry<String, Map<String, String>> mapEntry : _maps.entrySet()) {
            Map<String, String> map = mapEntry.getValue();

            //Test that the number of key-value-pairs in the map matches the expected.
            Assert.assertEquals(_noOfKvps, map.size());

            //Test that all the keys in this map contains the map name (ie. no other map's keys overlap).
            Assert.assertFalse(map.keySet().stream().anyMatch(k -> !k.contains(mapEntry.getKey())));

            //Test that all the values in this map contains the map name (ie. no other map's values overlap).
            Assert.assertFalse(map.values().stream().anyMatch(v -> !v.contains(mapEntry.getKey())));
        }
    }

    /**
     * Test that having a large number of maps and TopicSubscriptions for each of them. Test that
     * subscribers only have events triggered for the given map that they subscribe to.
     *
     * @
     */
    @Test
    public void testManyMapsManyTopicListeners() {
        Map<String, EventsForMapSubscriber> eventsForMapSubscriberMap = new HashMap<>();

        for (String key : _maps.keySet()) {
            EventsForMapSubscriber eventsForMapSubscriber = new EventsForMapSubscriber(key);
            eventsForMapSubscriberMap.put(key, eventsForMapSubscriber);

            assetTree.registerTopicSubscriber(key + "?bootstrap=false", String.class, String.class, eventsForMapSubscriber);
        }

        //This gets all of the maps an re-puts all values
        _maps = new HashMap<>();
        createAndFillMaps();

        for (String key : _maps.keySet()) {
            EventsForMapSubscriber eventsForMapSubscriber = eventsForMapSubscriberMap.get(key);

            Assert.assertEquals(_noOfKvps, eventsForMapSubscriber.getNoOfEvents());

            assetTree.unregisterTopicSubscriber(key, eventsForMapSubscriber);
        }
    }

    @Test
    public void testConnectToMultipleMapsUsingTheSamePort() throws IOException {
        Map<String, Map<String, String>> _clientMaps = new HashMap<>();
        TCPRegistry.createServerSocketChannelFor("SubscriptionEventTest.host.port");
        ServerEndpoint serverEndpoint = new ServerEndpoint("SubscriptionEventTest.host.port", assetTree, WireType.BINARY);

        AssetTree clientAssetTree = new VanillaAssetTree().forRemoteAccess("SubscriptionEventTest.host.port", WireType.BINARY);
        System.out.println("Creating maps.");
        AtomicInteger count = new AtomicInteger();
        IntStream.rangeClosed(1, _noOfMaps).forEach(i -> {
            String mapName = _mapBaseName + i;

            Map<String, String> map = clientAssetTree.acquireMap(mapName, String.class, String.class);

            for (int j = 1; j <= _noOfKvps; j++) {
                map.put(TestUtils.getKey(mapName, j), TestUtils.getValue(mapName, j));
            }
            Assert.assertEquals(_noOfKvps, map.size());

            _clientMaps.put(mapName, map);
            if (count.incrementAndGet() % 100 == 0)
                System.out.print("... " + count);
        });
        System.out.println("...client maps " + _noOfMaps + " Done.");

        //Test that the number of maps created exist
        Assert.assertEquals(_noOfMaps, _clientMaps.size());

        for (Map.Entry<String, Map<String, String>> mapEntry : _clientMaps.entrySet()) {
            System.out.println(mapEntry.getKey());
            Map<String, String> map = mapEntry.getValue();

            //Test that the number of key-value-pairs in the map matches the expected.
            Assert.assertEquals(_noOfKvps, map.size());

//            //Test that all the keys in this map contains the map name (ie. no other map's keys overlap).
//            String key = mapEntry.getKey();
//            SerializablePredicate<String> stringPredicate = k -> !k.contains(key);
//            Assert.assertFalse(map.keySet().stream().anyMatch(stringPredicate));
//
//            //Test that all the values in this map contains the map name (ie. no other map's values overlap).
//            SerializablePredicate<String> stringPredicate1 = v -> !v.contains(key);
//            Assert.assertFalse(map.values().stream().anyMatch(stringPredicate1));
        }
    }

    @Test
    @Ignore("todo")
    public void testMapReplication() {
        throw new UnsupportedOperationException("DS test that maps are automatically replicated on one or more failover servers, with each map on a server being uniquely associated with given name");
    }

    /**
     * Test creating an engine with an underlying Chronicle Map store where the base path is
     * specified as a folder that exist.
     *
     * @
     */
    @Test
    public void testChronicleMapCreationFolderBasePath() {
        String basePath = OS.TARGET;

        testMultipleMapsWithUnderlyingChronicleMap(basePath);
    }

    /**
     * Test creating an engine with an underlying Chronicle Map store where the base path is
     * specified as a full path to a file that does not exist.
     *
     * @
     */
    @Test
    public void testChronicleMapCreationFileBasePath() {
        String basePath = OS.TARGET + "nonExistingFileOrFolder";

        testMultipleMapsWithUnderlyingChronicleMap(basePath);
    }

    /**
     * Create an engine using a ChronicleMapKeyValueStore as underlying. Get two maps and put values
     * into them.
     *
     * @param basePath for key value store
     */
    private void testMultipleMapsWithUnderlyingChronicleMap(String basePath) {
        String map1Name = "MyMap1";
        String map2Name = "MyMap2";

        //Get map1 - expect 1 file to be created
        Map<String, String> map1 = assetTree.acquireMap(map1Name, String.class, String.class);

        String key1 = "KeyMap1";
        String value1 = "ValueMap1";

        map1.put(key1, value1);

        Assert.assertEquals(value1, map1.get(key1));

        //Get map2 - expect a second file to be created
        Map<String, String> map2 = assetTree.acquireMap(map2Name, String.class, String.class);

        String key2 = "KeyMap2";
        String value2 = "ValueMap2";

        map2.put(key2, value2);

        Assert.assertEquals(value2, map2.get(key2));
    }

    /**
     * Test that we can put a map as value and get it back and get values from it.
     *
     * @
     */
    @Test
    public void testSupportForNestedMaps() {
        String mapName = "MapOfMaps";
        String testKey = "TestKey";
        String testValue = "TestValue";

        Map<String, Map> map = assetTree.acquireMap(mapName, String.class, Map.class);

        Map<String, String> mapInMap = new HashMap<>();
        mapInMap.put(testKey, testValue);

        map.put(testKey, mapInMap);

        Map<String, Map> newMapRef = assetTree.acquireMap(mapName, String.class, Map.class);

        Map<String, String> newMapInMapRef = newMapRef.get(testKey);
        String valueFromMap = newMapInMapRef.get(testKey);

        Assert.assertEquals(testValue, valueFromMap);
    }

    /**
     * Creates configured number of maps and fills them with configured number of key/value pairs
     */
    private void createAndFillMaps() {
        _maps.entrySet().forEach(e -> {
            e.getValue().clear();
            for (int j = 1; j <= _noOfKvps; j++) {
                e.getValue().put(TestUtils.getKey(e.getKey(), j), TestUtils.getValue(e.getKey(), j));
            }
        });

    }

    /**
     * Checks that all updates triggered are for the map specified in the constructor and increments
     * the number of updates.
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