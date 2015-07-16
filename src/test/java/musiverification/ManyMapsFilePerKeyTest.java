package musiverification;

import ddp.api.TestUtils;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.engine.api.pubsub.InvalidSubscriberException;
import net.openhft.chronicle.engine.api.pubsub.TopicSubscriber;
import net.openhft.chronicle.engine.map.AuthenticatedKeyValueStore;
import net.openhft.chronicle.engine.map.FilePerKeyValueStore;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/* On linux.
fs.inotify.max_user_watches = 1500000
fs.inotify.max_user_instances = 2000
 */
public class ManyMapsFilePerKeyTest {
    private static Map<String, Map<String, String>> _maps;
    private static String _mapBaseName = "ManyMapsFilePerKeyTest-";

    //    private static int _noOfMaps = 1_100;
    private static int _noOfMaps = Boolean.getBoolean("quick") ? 10 : 100;
    private static int _noOfKvps = 1_000;

    private static VanillaAssetTree assetTree = new VanillaAssetTree().forTesting();

    @BeforeClass
    public static void setUp() throws IOException {
        assetTree.root().enableTranslatingValuesToBytesStore();

        assetTree.root().addLeafRule(AuthenticatedKeyValueStore.class, "FilePer Key",
                (context, asset) -> new FilePerKeyValueStore(context.basePath(OS.TARGET + "/fpk"), asset));
        _maps = Collections.synchronizedMap(new HashMap<>());

        System.out.println("Creating maps.");
        AtomicInteger count = new AtomicInteger();
        IntStream.rangeClosed(1, _noOfMaps).forEach(i -> {
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

    @AfterClass
    public static void tearDown() {
        assetTree.close();
    }

    /**
     * Test that the number of maps created exist.
     * Test that the number of key-value-pairs in the map matches the expected.
     * Test that all the keys in this map contains the map name (ie. no other map's keys overlap).
     * Test that all the values in this map contains the map name (ie. no other map's values overlap).
     *
     * @
     */
    @Test
    public void testKeysAndValuesInEachMap() {
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
     * @
     */
    @Test
    public void testManyMapsManyTopicListeners() {
        Map<String, EventsForMapSubscriber> eventsForMapSubscriberMap = new HashMap<>();

        _maps.keySet().parallelStream().forEach((String key) -> {
            EventsForMapSubscriber eventsForMapSubscriber = new EventsForMapSubscriber(key);
            eventsForMapSubscriberMap.put(key, eventsForMapSubscriber);

            assetTree.registerTopicSubscriber(key + "?bootstrap=false", String.class, String.class, eventsForMapSubscriber);
        });

        //This gets all of the maps an re-puts all values
        _maps = new HashMap<>();
        createAndFillMaps();

        _maps.keySet().parallelStream().forEach((String key) -> {
            EventsForMapSubscriber eventsForMapSubscriber = eventsForMapSubscriberMap.get(key);

            Assert.assertEquals(_noOfKvps, eventsForMapSubscriber.getNoOfEvents());

            assetTree.unregisterTopicSubscriber(key, eventsForMapSubscriber);
        });
    }

    /**
     * Test creating an engine with an underlying Chronicle Map store where the base
     * path is specified as a folder that exist.
     *
     * @
     */
    @Test
    public void testChronicleMapCreationFolderBasePath() {
        String basePath = OS.TARGET;

        testMultipleMapsWithUnderlyingChronicleMap(basePath);
    }

    /**
     * Test creating an engine with an underlying Chronicle Map store where the base path is specified
     * as a full path to a file that does not exist.
     *
     * @
     */
    @Test
    public void testChronicleMapCreationFileBasePath() {
        String basePath = OS.TARGET + "/nonExistingFileOrFolder";

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