package musiverification;

import ddp.api.TestUtils;
import junit.framework.TestCase;
import net.openhft.chronicle.engine.api.map.MapEvent;
import net.openhft.chronicle.engine.api.map.MapEventListener;
import net.openhft.chronicle.engine.api.pubsub.InvalidSubscriberException;
import net.openhft.chronicle.engine.api.pubsub.Subscriber;
import net.openhft.chronicle.engine.api.pubsub.TopicSubscriber;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.*;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.wire.WireType;
import net.openhft.chronicle.wire.YamlLogging;
import org.easymock.EasyMock;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static net.openhft.chronicle.engine.Chassis.*;


/**
 * @author Rob Austin.
 */
@RunWith(Parameterized.class)
public class SubscriptionModelTest {

    public static final Double EXPECTED = 1.23;
    private static Map<String, String> _stringStringMap;
    private static String _mapName = "/chronicleMapString";
    private static String _mapArgs = "putReturnsNull=true";
    private static AssetTree _clientAssetTree;
    private static ServerEndpoint _serverEndpoint;
    private static String _serverAddress = "host.port1";
    private final WireType wireType;

    public SubscriptionModelTest(WireType wireType) {
        this.wireType = wireType;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {WireType.TEXT},
                {WireType.BINARY}
        });
    }

    @Before
    public void setUp() throws IOException {
        resetChassis();

        _stringStringMap = acquireMap(String.format("%s?%s", _mapName, _mapArgs), String.class, String.class);
        _stringStringMap.clear();

        _clientAssetTree = assetTree();

        TCPRegistry.createServerSocketChannelFor(_serverAddress);
        _serverEndpoint = new ServerEndpoint(_serverAddress, _clientAssetTree);
    }

    @After
    public void tearDown() {
        assetTree().close();

        if (_serverEndpoint != null) {
            _serverEndpoint.close();
        }

        TCPRegistry.reset();
    }


    @Test
    public void testSubscriptionStringStringMap() throws InterruptedException {
        YamlLogging.setAll(YamlLogging.YamlLoggingLevel.DEBUG_ONLY);
        BlockingQueue<Throwable> onThrowable = new ArrayBlockingQueue<>(1);
        VanillaAssetTree remoteClient = new VanillaAssetTree().forRemoteAccess(_serverAddress,
                wireType, onThrowable::add);

        String mapName = "/test/maps/string/double/test";
        String mapNameSubscriber = mapName + "?bootstrap=false";

        Map<String, String> stringDoubleMapView = remoteClient.acquireMap(mapName, String.class, String.class);
        int size = stringDoubleMapView.size();

        Assert.assertEquals(0, size);

        BlockingQueue<MapEvent> mapEvents = new ArrayBlockingQueue<>(1);

        remoteClient.registerSubscriber(mapNameSubscriber, MapEvent.class, mapEvents::add);

        String key = "k1";
        String value = "1.23";

        stringDoubleMapView.put(key, value);
        MapEvent mapEvent = mapEvents.poll(2000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(key, mapEvent.getKey());
        //Assert.assertEquals(value,mapEvent.getValue(),0.0);

        //No throwables expected
        Assert.assertNull(onThrowable.poll());
    }


    @Test
    public void testSubscriptionFloatFloatMap() throws InterruptedException {
        YamlLogging.setAll(YamlLogging.YamlLoggingLevel.DEBUG_ONLY);
        BlockingQueue<Throwable> onThrowable = new ArrayBlockingQueue<>(1);
        VanillaAssetTree remoteClient = new VanillaAssetTree().forRemoteAccess(_serverAddress,
                wireType,
                onThrowable::add);

        String mapName = "/test/maps/string/double/test";
        String mapNameSubscriber = mapName + "?bootstrap=false";

        Map<Float, Float> stringDoubleMapView = remoteClient.acquireMap(mapName, Float.class,
                Float.class);
        int size = stringDoubleMapView.size();

        Assert.assertEquals(0, size);

        BlockingQueue<MapEvent> mapEvents = new ArrayBlockingQueue<>(1);

        remoteClient.registerSubscriber(mapNameSubscriber, MapEvent.class, mapEvents::add);

        Float key = 1.23f;
        Float value = 1.23f;

        stringDoubleMapView.put(key, value);
        MapEvent mapEvent = mapEvents.poll(2000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(key, (float) mapEvent.getKey(), 0.001);
        //Assert.assertEquals(value,mapEvent.getValue(),0.0);

        //No throwables expected
        Assert.assertNull(onThrowable.poll());
    }


    @Test
    public void testSubscriptionIntegerIntegerMap() throws InterruptedException {
        YamlLogging.setAll(YamlLogging.YamlLoggingLevel.DEBUG_ONLY);
        BlockingQueue<Throwable> onThrowable = new ArrayBlockingQueue<>(1);
        VanillaAssetTree remoteClient = new VanillaAssetTree().forRemoteAccess(_serverAddress,
                wireType,
                onThrowable::add);

        String mapName = "/test/maps/string/double/test";
        String mapNameSubscriber = mapName + "?bootstrap=false";

        Map<Integer, Integer> stringDoubleMapView = remoteClient.acquireMap(mapName, Integer.class,
                Integer.class);
        int size = stringDoubleMapView.size();

        Assert.assertEquals(0, size);

        BlockingQueue<MapEvent> mapEvents = new ArrayBlockingQueue<>(1);

        remoteClient.registerSubscriber(mapNameSubscriber, MapEvent.class, (e) ->
                mapEvents.add(e));

        Integer key = 1;
        Integer value = 2;

        stringDoubleMapView.put(key, value);
        MapEvent mapEvent = mapEvents.poll(2000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(key, mapEvent.getKey());
        //Assert.assertEquals(value,mapEvent.getValue(),0.0);

        //No throwables expected
        Assert.assertNull(onThrowable.poll());
    }


    @Test
    public void testSubscriptionLongLongMap() throws InterruptedException {
        YamlLogging.setAll(YamlLogging.YamlLoggingLevel.DEBUG_ONLY);
        BlockingQueue<Throwable> onThrowable = new ArrayBlockingQueue<>(1);
        VanillaAssetTree remoteClient = new VanillaAssetTree().forRemoteAccess(_serverAddress,
                wireType,
                onThrowable::add);

        String mapName = "/test/maps/string/double/test";
        String mapNameSubscriber = mapName + "?bootstrap=false";

        Map<Long, Long> stringDoubleMapView = remoteClient.acquireMap(mapName, Long.class,
                Long.class);
        int size = stringDoubleMapView.size();

        Assert.assertEquals(0, size);

        BlockingQueue<MapEvent> mapEvents = new ArrayBlockingQueue<>(1);

        remoteClient.registerSubscriber(mapNameSubscriber, MapEvent.class, mapEvents::add);

        Long key = 1L;
        Long value = 2L;

        stringDoubleMapView.put(key, value);
        MapEvent mapEvent = mapEvents.poll(2000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(key, mapEvent.getKey());
        //Assert.assertEquals(value,mapEvent.getValue(),0.0);

        //No throwables expected
        Assert.assertNull(onThrowable.poll());
    }

    @Test
    public void testSubscriptionStringDoubleMap() throws InterruptedException {
        YamlLogging.setAll(YamlLogging.YamlLoggingLevel.DEBUG_ONLY);
        BlockingQueue<Throwable> onThrowable = new ArrayBlockingQueue<>(1);
        VanillaAssetTree remoteClient = new VanillaAssetTree().forRemoteAccess(_serverAddress,
                wireType, onThrowable::add);


        String mapName = "/test/maps/string/double/test";
        String mapNameSubscriber = mapName + "?bootstrap=false";

        Map<String, Double> stringDoubleMapView = remoteClient.acquireMap(mapName, String.class, Double.class);
        int size = stringDoubleMapView.size();

        Assert.assertEquals(0, size);

        BlockingQueue<MapEvent> mapEvents = new ArrayBlockingQueue<>(1);

        remoteClient.registerSubscriber(mapNameSubscriber, MapEvent.class, mapEvents::add);

        String key = "k1";
        double value = 1.23;

        stringDoubleMapView.put(key, value);
        MapEvent mapEvent = mapEvents.poll(2000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(key, mapEvent.getKey());
        //Assert.assertEquals(value,mapEvent.getValue(),0.0);

        //No throwables expected
        Assert.assertNull(onThrowable.poll());
    }


    @Test
    public void testSubscriptionDoubleDoubleMap() throws InterruptedException {
        YamlLogging.setAll(YamlLogging.YamlLoggingLevel.DEBUG_ONLY);
        BlockingQueue<Throwable> onThrowable = new ArrayBlockingQueue<>(1);
        VanillaAssetTree remoteClient = new VanillaAssetTree().forRemoteAccess(_serverAddress,
                wireType, onThrowable::add);


        String mapName = "/test/maps/string/double/test";
        String mapNameSubscriber = mapName + "?bootstrap=false";

        Map<Double, Double> stringDoubleMapView = remoteClient.acquireMap(mapName, Double.class, Double.class);
        int size = stringDoubleMapView.size();

        Assert.assertEquals(0, size);

        BlockingQueue<MapEvent> mapEvents = new ArrayBlockingQueue<>(1);

        remoteClient.registerSubscriber(mapNameSubscriber, MapEvent.class, mapEvents::add);

        double key = 1.0;
        double value = 1.23;

        stringDoubleMapView.put(key, value);
        MapEvent mapEvent = mapEvents.poll(2000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(key, (double) mapEvent.getKey(), 0.001);
        //Assert.assertEquals(value,mapEvent.getValue(),0.0);

        //No throwables expected
        Assert.assertNull(onThrowable.poll());
    }

    @Test
    public void testTopicSubscriptionStringDoubleMap() throws InterruptedException {

        BlockingQueue<Throwable> onThrowable = new ArrayBlockingQueue<>(1);
        VanillaAssetTree remoteClient = new VanillaAssetTree().forRemoteAccess(_serverAddress,
                wireType, onThrowable::add);

        String mapName = "/test/maps/string/double/test";
        String mapNameSubscriber = mapName + "?bootstrap=false";

        Map<String, Double> stringDoubleMapView = remoteClient.acquireMap(mapName, String.class, Double.class);
        int size = stringDoubleMapView.size();

        Assert.assertEquals(0, size);

        BlockingQueue<Double> mapEvents = new ArrayBlockingQueue<>(1);

        remoteClient.registerTopicSubscriber(mapNameSubscriber, String.class, Double.class, (k, v) -> mapEvents.add(v));

        String key = "k1";
        double value = 1.23;

        stringDoubleMapView.put(key, value);
        Assert.assertEquals(value, mapEvents.poll(200, TimeUnit.MILLISECONDS), 0.0);
//        Assert.assertEquals(value, (double)mapEvent.getValue(), 0.0);

        //No throwables expected
        Assert.assertNull(onThrowable.poll());
    }

    /**
     * Test subscribing to all MapEvents for a given map. Expect to receive events for insert,
     * update and remove actions for all keys. All events should be received in the order they are
     * executed.
     */
    @Test
    public void testSubscriptionMapEventOnAllKeys() {
        MapEventListener<String, String> mapEventListener = EasyMock.createStrictMock(MapEventListener.class);
        _clientAssetTree.registerSubscriber(_mapName, MapEvent.class, e -> e.apply(mapEventListener));

        int noOfKeys = 5;
        int noOfValues = 5;

        //Setup all the expected events in the correct order

        //Setup insert events for all keys
        iterateAndExecuteConsumer((k, v) -> mapEventListener.insert(_mapName, k, v), noOfKeys, _mapName, _mapName);

        //Setup update events for all keys
        for (int i = 0; i < noOfKeys * noOfValues; i++) {
            mapEventListener.update(_mapName, TestUtils.getKey(_mapName, i % noOfKeys), TestUtils.getValue(_mapName, i), TestUtils.getValue(_mapName, i + noOfKeys));
        }

        //Setup remove events for all keys
        iterateAndExecuteConsumer((k, v) -> mapEventListener.remove(_mapName, k, v), c -> c, c -> noOfKeys * noOfValues + c, noOfKeys, _mapName, _mapName);

        EasyMock.replay(mapEventListener);

        //Perform all initial puts (insert events)
        iterateAndExecuteConsumer((k, v) -> _stringStringMap.put(k, v), noOfKeys, _mapName, _mapName);

        //Perform all puts (update events)
        for (int i = 0; i < noOfKeys * noOfValues; i++) {
            _stringStringMap.put(TestUtils.getKey(_mapName, i % noOfKeys), TestUtils.getValue(_mapName, i + noOfKeys));
        }

        //Perform all remove (remove events)
        iterateAndExecuteConsumer((k, v) -> _stringStringMap.remove(k), noOfKeys, _mapName, _mapName);

        EasyMock.verify(mapEventListener);
    }

    /**
     * Test subscribing to updates on a specific key. Perform initial puts (insert). Perform more
     * puts (updates). Remove the key.
     */
    @Test
    public void testSubscriptionSpecificKey() throws InvalidSubscriberException {
        String testKey = "Key-sub-1";

        Subscriber<String> testChronicleKeyEventSubscriber = EasyMock.createStrictMock(Subscriber.class);

        //Set up teh mock
        String update1 = "Update1";
        String update2 = "Update2";
        String update3 = "Update3";
        String update4 = "Update4";
        String update5 = "Update5";

        testChronicleKeyEventSubscriber.onMessage(update1);
        testChronicleKeyEventSubscriber.onMessage(update2);
        testChronicleKeyEventSubscriber.onMessage(update3);
        testChronicleKeyEventSubscriber.onMessage(update4);
        testChronicleKeyEventSubscriber.onMessage(update5);
        testChronicleKeyEventSubscriber.onMessage(null); //Key removed

        EasyMock.replay(testChronicleKeyEventSubscriber);

        //Setting bootstrap = false otherwise we would get an initial event with null
        _clientAssetTree.registerSubscriber(_mapName + "/" + testKey + "?bootstrap=false", String.class, testChronicleKeyEventSubscriber);

        //Perform some puts and replace
        _stringStringMap.put(testKey, update1);
        _stringStringMap.put(testKey, update2);
        _stringStringMap.replace(testKey, update2, update3);
        _stringStringMap.put(testKey, update4);

        //Perform one put on test key and a number of operations on another key and check number of updater
        String irrelevantTestKey = "Key-nonsub";
        _stringStringMap.put(irrelevantTestKey, "RandomVal1");
        _stringStringMap.put(testKey, update5);
        _stringStringMap.put(irrelevantTestKey, "RandomVal2");
        _stringStringMap.replace(irrelevantTestKey, "RandomVal2", "RandomVal3");
        _stringStringMap.remove(irrelevantTestKey);

        //Remove the test key and test the number of updates
        _stringStringMap.remove(testKey);

        EasyMock.verify(testChronicleKeyEventSubscriber);

        // expect to be told when the tree is torn down.
        EasyMock.reset(testChronicleKeyEventSubscriber);
        testChronicleKeyEventSubscriber.onEndOfSubscription();
        EasyMock.replay(testChronicleKeyEventSubscriber);
    }

    /**
     * Test that we get a key event for every insert, update, remove action performed on a key. Test
     * order of events.
     */
    @Test
    public void testSubscriptionKeyEvents() throws InvalidSubscriberException {
        String testKey1 = "Key-sub-1";
        String testKey2 = "Key-sub-2";
        String testKey3 = "Key-sub-3";
        String testKey4 = "Key-sub-4";
        String testKey5 = "Key-sub-5";

        Subscriber<String> testChronicleKeyEventSubscriber = EasyMock.createStrictMock(Subscriber.class);

        String update1 = "Update1";
        String update2 = "Update2";
        String update3 = "Update3";
        String update4 = "Update4";
        String update5 = "Update5";

        //Set up the mock
        testChronicleKeyEventSubscriber.onMessage(testKey1);
        testChronicleKeyEventSubscriber.onMessage(testKey2);
//        testChronicleKeyEventSubscriber.onMessage(testKey3); // no event as the key doesn't exist.
        testChronicleKeyEventSubscriber.onMessage(testKey4);
        testChronicleKeyEventSubscriber.onMessage(testKey5);

        //More updates on the same keys
        testChronicleKeyEventSubscriber.onMessage(testKey1);
        testChronicleKeyEventSubscriber.onMessage(testKey2);

        //Removes
        testChronicleKeyEventSubscriber.onMessage(testKey1);
        testChronicleKeyEventSubscriber.onMessage(testKey5);

        EasyMock.replay(testChronicleKeyEventSubscriber);

        //Register as subscriber on map to get keys
        _clientAssetTree.registerSubscriber(_mapName, String.class, testChronicleKeyEventSubscriber);

        //Perform some puts and replace
        _stringStringMap.put(testKey1, update1);
        _stringStringMap.put(testKey2, update2);
        // this key doesn't exist so it doesn't trigger an event.
        _stringStringMap.replace(testKey3, update2, update3);
        _stringStringMap.put(testKey4, update4);
        _stringStringMap.put(testKey5, update5);

        //Perform more events on the same keys
        _stringStringMap.put(testKey1, update1);
        _stringStringMap.put(testKey2, update2);

        //Remove keys
        _stringStringMap.remove(testKey1);
        _stringStringMap.remove(testKey5);

        EasyMock.verify(testChronicleKeyEventSubscriber);

        // expect to be told when the tree is torn down.
        EasyMock.reset(testChronicleKeyEventSubscriber);
        testChronicleKeyEventSubscriber.onEndOfSubscription();
        EasyMock.replay(testChronicleKeyEventSubscriber);
    }

    /**
     * Test that a number of updates for a number of keys (all intermingled) all trigger events on
     * the topic in the order in which the events take place. <p> Test that removing all of the keys
     * trigger ordered events where the value is null
     */
    @Test
    public void testSubscriptionOnMap() throws InvalidSubscriberException {
        //Using a strict mock as we want to verify that events come in in the right order
        TopicSubscriber<String, String> topicSubscriberMock = EasyMock.createStrictMock(TopicSubscriber.class);
        _clientAssetTree.registerTopicSubscriber(_mapName, String.class, String.class, topicSubscriberMock);

        int noOfKeys = 5;
        int noOfValues = 5;

        //Setup the mock with the expected updates
        iterateAndExecuteConsumer((k, v) -> {
            try {
                topicSubscriberMock.onMessage(k, v);
            } catch (InvalidSubscriberException e) {
                TestCase.fail("Exception thrown");
            }
        }, c -> c % noOfKeys, c -> c, noOfKeys * noOfValues, _mapName, _mapName);

        //Setup the mock with the removes
        for (int i = 0; i < noOfKeys; i++) {
            topicSubscriberMock.onMessage(TestUtils.getKey(_mapName, i), null);
        }

        EasyMock.replay(topicSubscriberMock);

        //Perform the updates
        iterateAndExecuteConsumer((k, v) -> _stringStringMap.put(k, v), c -> c % noOfKeys, c -> c, noOfKeys * noOfValues, _mapName, _mapName);

        //Perform the removes
        iterateAndExecuteConsumer((k, v) -> _stringStringMap.remove(k), noOfKeys, _mapName, _mapName);

        EasyMock.verify(topicSubscriberMock);

        // expect to be told when the tree is torn down.
        EasyMock.reset(topicSubscriberMock);
        topicSubscriberMock.onEndOfSubscription();
        EasyMock.replay(topicSubscriberMock);

    }

    /**
     * Test that TopicSubscriber does NOT bootstrap when configured not to do so
     *
     * @throws InvalidSubscriberException
     */
    @Test
    public void testTopicSubscriptionBootstrappingFalse() throws InvalidSubscriberException {
        //Using a strict mock as we want to verify that events come in in the right order
        TopicSubscriber<String, String> topicSubscriberMock = EasyMock.createStrictMock(TopicSubscriber.class);

        String key = "KeyBootstrappingFalse";
        String value = "BootstrappingFalse";
        _stringStringMap.put(key, value);

        _clientAssetTree.registerTopicSubscriber(_mapName + "?bootstrap=false", String.class, String.class, topicSubscriberMock);

        //No events should be triggered on topic subscriber

        EasyMock.replay(topicSubscriberMock);


        EasyMock.verify(topicSubscriberMock);

        // expect to be told when the tree is torn down.
        EasyMock.reset(topicSubscriberMock);
        topicSubscriberMock.onEndOfSubscription();
        EasyMock.replay(topicSubscriberMock);
    }

    /**
     * Test that TopicSubscriber DOES bootstrap when configured TO DO so
     *
     * @throws InvalidSubscriberException
     */
    @Test
    public void testTopicSubscriptionBootstrappingTrue() throws InvalidSubscriberException {
        //Using a strict mock as we want to verify that events come in in the right order
        TopicSubscriber<String, String> topicSubscriberMock = EasyMock.createStrictMock(TopicSubscriber.class);
        _clientAssetTree.registerTopicSubscriber(_mapName + "?bootstrap=true", String.class, String.class, topicSubscriberMock);

        String key = "KeyBootstrappingTrue";
        String value = "BootstrappingTrue";

        topicSubscriberMock.onMessage(key, value);

        EasyMock.replay(topicSubscriberMock);

        _stringStringMap.put(key, value);

        EasyMock.verify(topicSubscriberMock);

        // expect to be told when the tree is torn down.
        EasyMock.reset(topicSubscriberMock);
        topicSubscriberMock.onEndOfSubscription();
        EasyMock.replay(topicSubscriberMock);
    }

    /**
     * Test event listeners on maps inserted, updated, removed are triggered correctly when expected
     * and in the correct order.
     */
    @Test
    @Ignore //Not supported
    public void testMapAddedKeyListener() throws InvalidSubscriberException {
        //DS test that we can be notified when maps are added
        resetChassis();

        String parentUri = "/mapbase";
        String mapBaseUri = parentUri + "/maps";

        String mapName1 = "TestMap1";
        String mapName2 = "TestMap2";

        String mapUri1 = mapBaseUri + '/' + mapName1;
        String mapUri2 = mapBaseUri + '/' + mapName2;

        TopicSubscriber<String, String> assetTreeSubscriber = EasyMock.createStrictMock("assetTreeSubscriber", TopicSubscriber.class);
        registerTopicSubscriber(parentUri, String.class, String.class, assetTreeSubscriber);

        // when added
        assetTreeSubscriber.onMessage("maps", mapName1);
        assetTreeSubscriber.onMessage("maps", mapName2);

        // and when removed
        assetTreeSubscriber.onMessage("maps", mapName1);
        assetTreeSubscriber.onMessage("maps", mapName2);

        EasyMock.replay(assetTreeSubscriber);

        Subscriber<TopologicalEvent> mapEventKeySubscriber = EasyMock.createStrictMock("mapEventKeySubscriber", Subscriber.class);
        // expect a bootstrap event
        mapEventKeySubscriber.onMessage(ExistingAssetEvent.of(parentUri, "maps"));

        EasyMock.replay(mapEventKeySubscriber);

        registerSubscriber(mapBaseUri, TopologicalEvent.class, mapEventKeySubscriber);

        EasyMock.verify(mapEventKeySubscriber);
        EasyMock.reset(mapEventKeySubscriber);

        //First the two maps will be inserted into
        mapEventKeySubscriber.onMessage(AddedAssetEvent.of(mapBaseUri, mapName1));
        mapEventKeySubscriber.onMessage(AddedAssetEvent.of(mapBaseUri, mapName2));

        //Second the two maps are removed
        mapEventKeySubscriber.onMessage(RemovedAssetEvent.of(mapBaseUri, mapName1));
        mapEventKeySubscriber.onMessage(RemovedAssetEvent.of(mapBaseUri, mapName2));

        EasyMock.replay(mapEventKeySubscriber);

        //Create the two maps
        Map<String, String> map1 = acquireMap(mapUri1, String.class, String.class);
        Map<String, String> map2 = acquireMap(mapUri2, String.class, String.class);

        //Perform some actions on the maps - should not trigger events
        String keyMap1 = "KeyMap1";
        String keyMap2 = "KeyMap2";
        String valueMap1 = "ValueMap1";
        String valueMap2 = "ValueMap1";

        map1.put(keyMap1, valueMap1);
        map2.put(keyMap2, valueMap2);

        Assert.assertEquals(valueMap1, map1.get(keyMap1));
        Assert.assertNull(map1.get(keyMap2));

        Assert.assertEquals(valueMap2, map2.get(keyMap2));
        Assert.assertNull(map2.get(keyMap1));

        map1.remove(keyMap1);
        map2.remove(keyMap2);

        getAsset(mapBaseUri).removeChild(mapName1);
        getAsset(mapBaseUri).removeChild(mapName2);

        EasyMock.verify(assetTreeSubscriber);

        EasyMock.verify(mapEventKeySubscriber);
        EasyMock.reset(mapEventKeySubscriber);

        mapEventKeySubscriber.onEndOfSubscription();

        EasyMock.replay(mapEventKeySubscriber);
    }

    /**
     * Perform a for loop for the noOfKeys (from 0) and perform the methodToExecute with the given
     * key (manipulated) and given value (manipulated).
     *
     * @param methodToExecute   Method to be executed for each iteration.
     * @param keyManipulation   Manipulation to be performed on the key counter value before before
     *                          creating the key.
     * @param valueManipulation Manipulation to be performed on the value counter value before
     *                          before creating the value.
     * @param noOfKeys          No of iterations.
     * @param keyBase           Base value for the key - typically the map name.
     * @param valueBase         Base value for the value - typically the map name.
     */
    private void iterateAndExecuteConsumer(BiConsumer<String, String> methodToExecute, Function<Integer, Integer> keyManipulation, Function<Integer, Integer> valueManipulation, int noOfKeys, String keyBase, String valueBase) {
        IntStream.range(0, noOfKeys).forEach((i) -> methodToExecute.accept(TestUtils.getKey(keyBase, keyManipulation.apply(i)), TestUtils.getValue(valueBase, valueManipulation.apply(i))));
    }

    /**
     * Perform a for loop for the noOfKeys (from 0) and perform the methodToExecute with key based
     * on base value and counter and a value based on the base value and the counter.
     *
     * @param methodToExecute Method to be executed for each iteration.
     * @param noOfKeys        No of iterations.
     * @param keyBase         Base value for the key - typically the map name.
     * @param valueBase       Base value for the value - typically the map name.
     */
    private void iterateAndExecuteConsumer(BiConsumer<String, String> methodToExecute, int noOfKeys, String keyBase, String valueBase) {
        iterateAndExecuteConsumer(methodToExecute, c -> c, c -> c, noOfKeys, keyBase, valueBase);
    }
}