package musiverification;

import ddp.api.*;
import net.openhft.chronicle.engine2.*;
import net.openhft.chronicle.engine2.api.*;
import net.openhft.chronicle.engine2.api.map.*;
import org.easymock.*;
import org.junit.*;

import java.util.*;
import java.util.function.*;

public class SubscriptionModelTests
{
    private static Map<String, String> _stringStringMap;
    private static String _mapName = "chronicleMapString";
    private static String _mapArgs = "putReturnsNull=true";
    private static AssetTree _clientAssetTree;

    //TODO DS all events should be asynchronous

    @Before
    public void setUp() throws Exception
    {
        Chassis.resetChassis();

        _stringStringMap = Chassis.acquireMap(String.format("%s?%s", _mapName, _mapArgs), String.class, String.class);
        _stringStringMap.clear();

        _clientAssetTree = Chassis.defaultSession();
    }

    @After
    public void tearDown() throws Exception
    {
        Chassis.defaultSession().close();
    }

    //TODO DS call back method must include map name (I don't think it does?), key name, value inserted

    /**
     * Test subscribing to all MapEvents for a given map.
     * Expect to receive events for insert, update and remove actions for all keys.
     * All events should be received in the order they are executed.
     *
     * @throws Exception
     */
    @Test
    public void testSubscriptionMapEventOnAllKeys() throws Exception
    {
        MapEventListener<String, String> mapEventListener = EasyMock.createStrictMock(MapEventListener.class);
        _clientAssetTree.registerSubscriber(_mapName, MapEvent.class, e -> e.apply(mapEventListener));

        String testKeyBase = "Key-sub-";
        String testValueBase = "Value-";

        int noOfKeys = 5;
        int noOfValues = 5;

        //Setup all the expected events in the correct order

        //Setup insert events for all keys
        iterateAndExecuteConsumer((k, v) -> mapEventListener.insert(k, v), noOfKeys, _mapName, _mapName);

//        for (int i = 0; i < noOfKeys; i++)
//        {
////            mapEventListener.insert(String.format("%s%s", testKeyBase, i), String.format("%s%s:%s", testValueBase, i, i - noOfKeys));
//            mapEventListener.insert(TestUtils.getKey(_mapName, i),  TestUtils.getValue(_mapName, i));
//
//            System.out.println("Mock insert: " + TestUtils.getKey(_mapName, i) + " | " + TestUtils.getValue(_mapName, i));
//        }

        //Setup update events for all keys
        for (int i = 0; i < noOfKeys * noOfValues; i++)
        {
            int keyId = i % noOfKeys;
            int value = i - noOfKeys;
            String key = String.format("%s%s", testKeyBase, keyId);
            String oldValue = String.format("%s%s:%s", testValueBase, keyId, value);
            String newValue = String.format("%s%s:%s", testValueBase, keyId, i);

//            mapEventListener.update(key, oldValue, newValue);
            mapEventListener.update(TestUtils.getKey(_mapName, i % noOfKeys), TestUtils.getValue(_mapName, i), TestUtils.getValue(_mapName, i + noOfKeys));

            System.out.println("Mock UPDATE: " + TestUtils.getKey(_mapName, i % noOfKeys) + " | " + TestUtils.getValue(_mapName, i) + " | " + TestUtils.getValue(_mapName, i + noOfKeys));
        }

        //Setup remove events for all keys
        iterateAndExecuteConsumer((k, v) -> mapEventListener.remove(k, v), c -> c, c -> noOfKeys * noOfValues + c, noOfKeys, _mapName, _mapName);
//        for (int i = 0; i < noOfKeys; i++)
//        {
//            String key = String.format("%s%s", testKeyBase, i);
//            String newValue = String.format("%s%s:%s", testValueBase, i, noOfKeys * noOfValues - noOfKeys + i);
//
////            mapEventListener.remove(key, newValue);
//
//            System.out.println("Mock REMOVE: " + TestUtils.getKey(_mapName, i) + " | " + TestUtils.getValue(_mapName, noOfKeys * noOfValues + i));
//            mapEventListener.remove(TestUtils.getKey(_mapName, i), TestUtils.getValue(_mapName, noOfKeys * noOfValues + i));
//        }

        EasyMock.replay(mapEventListener);

        //Perform all initial puts (insert events)
        iterateAndExecuteConsumer((k, v) -> _stringStringMap.put(k, v), noOfKeys, _mapName, _mapName);

//        for (int i = 0; i < noOfKeys; i++)
//        {
////            _stringStringMap.put(String.format("%s%s", testKeyBase, i), String.format("%s%s:%s", testValueBase, i, i - noOfKeys));
//
//            System.out.println("Real insert: " + TestUtils.getKey(_mapName, i) + " | " + TestUtils.getValue(_mapName, i));
//
//            _stringStringMap.put(TestUtils.getKey(_mapName, i), TestUtils.getValue(_mapName, i));
//        }

        //Perform all puts (update events)
        for (int i = 0; i < noOfKeys * noOfValues; i++)
        {
            int keyId = i % noOfKeys;
            String key = String.format("%s%s", testKeyBase, keyId);
            String newValue = String.format("%s%s:%s", testValueBase, keyId, i);

            System.out.println("Mock UPDATE: " + TestUtils.getKey(_mapName, i % noOfKeys) + " | " + "?" + " | " + TestUtils.getValue(_mapName, i + noOfKeys));

//            _stringStringMap.put(key, newValue);
            _stringStringMap.put(TestUtils.getKey(_mapName, i % noOfKeys), TestUtils.getValue(_mapName, i + noOfKeys));
        }

        //Perform all remove (remove events)
        iterateAndExecuteConsumer((k, v) -> _stringStringMap.remove(k),noOfKeys, _mapName, _mapName);
//        for (int i = 0; i < noOfKeys; i++)
//        {
//            String key = String.format("%s%s", testKeyBase, i);
//
////            _stringStringMap.remove(key);
//
//            System.out.println("Real REMOVE: " + TestUtils.getKey(_mapName, i));
//            _stringStringMap.remove(TestUtils.getKey(_mapName, i));
//        }

        EasyMock.verify(mapEventListener);
    }

    private void iterateAndExecuteConsumer(BiConsumer<String, String> methodToExecute, Function<Integer, Integer> keyManipulation, Function<Integer, Integer> valueManipulation, int noOfKeys, String keyBase, String valueBase)
    {
        // TODO DS use int stream
        for (int i = 0; i < noOfKeys; i++)
        {
            methodToExecute.accept(TestUtils.getKey(keyBase, keyManipulation.apply(i)), TestUtils.getValue(valueBase, valueManipulation.apply(i)));
        }
    }

    //TODO DS move
    //Setup insert events for all keys
    private void iterateAndExecuteConsumer(BiConsumer<String, String> methodToExecute, int noOfKeys, String keyBase, String valueBase)
    {
        iterateAndExecuteConsumer(methodToExecute, c -> c, c -> c, noOfKeys, keyBase, valueBase);
    }

    /**
     * Test subscribing to updates on one specific key.
     * Perform 2 puts and check the number of updates.
     * Perform a put and and a replace and test the number of updates.
     * Perform one put on test key and a number of operations on another key and check number of updater.
     * Remove the test key and test the number of updates.
     *
     * @throws Exception
     */
    @Test
    public void testSubscriptionSpecificKey() throws Exception
    {
        //TODO DS connecting to a server based Java component using the clietn API can be notified by callback methods for specified key in a given map

        //TODO DS refactor to use mock (strict)
        String testKey = "Key-sub-1";

        KeySubscriber<String> testChronicleKeyEventSubscriber = EasyMock.createStrictMock(KeySubscriber.class);

        //Set up teh mock
        String update1 = "Update1";
        String update2 = "Update2";
        String update3 = "Update3";
        String update4 = "Update4";
        String update5 = "Update5";

        testChronicleKeyEventSubscriber.onMessage(update1);
        testChronicleKeyEventSubscriber.onMessage(update2);
//        testChronicleKeyEventSubscriber.onMessage(update3); // TODO DS replace not yet supported
        testChronicleKeyEventSubscriber.onMessage(update4);
        testChronicleKeyEventSubscriber.onMessage(update5);
        testChronicleKeyEventSubscriber.onMessage(null); //Key removed

        EasyMock.replay(testChronicleKeyEventSubscriber);


        //Setting bootstrap = false otherwise we would get an initial event with null
        _clientAssetTree.registerSubscriber(_mapName + "/" + testKey + "?bootstrap=false", String.class, testChronicleKeyEventSubscriber); //TODO DS do a test with boot strapping

        //Perform some puts and replace
        _stringStringMap.put(testKey, update1);
        _stringStringMap.put(testKey, update2);
//        _stringStringMap.replace(testKey, update2, update3); //TODO DS not yet supported
        _stringStringMap.put(testKey, update4);

        //Perform one put on test key and a number of operations on another key and check number of updater
        String irrelevantTestKey = "Key-nonsub";
        _stringStringMap.put(irrelevantTestKey, "RandomVal1");
        _stringStringMap.put(testKey, update5);
        _stringStringMap.put(irrelevantTestKey, "RandomVal2");
//        _stringStringMap.replace(irrelevantTestKey, "RandomVal2", "RandomVal3"); //TODO DS not yet supported
        _stringStringMap.remove(irrelevantTestKey);

        //Remove the test key and test the number of updates
        _stringStringMap.remove(testKey);

        EasyMock.verify(testChronicleKeyEventSubscriber);
    }

    /**
     * Test that a number of updates for a number of keys (all intermingled) all trigger events on the topic
     * in the order in which the events take place.
     * <p/>
     * Test that removing all of the keys trigger ordered events where the value is null
     *
     * @throws Exception
     */
    @Test
    public void testSubscriptionOnMap() throws Exception
    {
        //TODO DS the loops can be refactored by having a method which performs the loop and accepts a consumer
        //TODO DS connecting to a server based Java component using the client API can be notified by callback methods for all updates in map

        //Using a strict mock as we want to verify that events come in in the right order
        TopicSubscriber<String, String> topicSubscriberMock = EasyMock.createStrictMock(TopicSubscriber.class);
        _clientAssetTree.registerTopicSubscriber(_mapName, String.class, String.class, topicSubscriberMock);

        String testKeyBase = "Key-sub-";
        String testValueBase = "Value-";

        int noOfKeys = 5;
        int noOfValues = 5;

        //Setup the mock with the expected updates
        for (int i = 0; i < noOfKeys * noOfValues; i++)
        {
            int keyId = i % noOfKeys;
            topicSubscriberMock.onMessage(String.format("%s%s", testKeyBase, keyId), String.format("%s%s:%s", testValueBase, keyId, i));
        }

        //Setup the mock with the removes
        for (int i = 0; i < noOfKeys; i++)
        {
            topicSubscriberMock.onMessage(String.format("%s%s", testKeyBase, i), null);
        }

        EasyMock.replay(topicSubscriberMock);

        //Perform the updates
        for (int i = 0; i < noOfKeys * noOfValues; i++)
        {
            int keyId = i % noOfKeys;
            _stringStringMap.put(String.format("%s%s", testKeyBase, keyId), String.format("%s%s:%s", testValueBase, keyId, i));
        }

        //Perform the removes
        for (int i = 0; i < noOfKeys; i++)
        {
            _stringStringMap.remove(String.format("%s%s", testKeyBase, i));
        }

        EasyMock.verify(topicSubscriberMock);
    }

    //TODO fix this - how to subscribe to map events (e.g. map added, map removed)?

    /**
     * Test event listeners on maps inserted, updated, removed are triggered correctly when expected and in the correct order.
     *
     * @throws Exception
     */
    @Test
    public void testMapAddedKeyListener() throws Exception
    {
        //TODO DS test that we can be notified when maps are added
        Chassis.resetChassis();

        String mapBaseUri = "mapbase/maps/";

        String mapName1 = "TestMap1";
        String mapName2 = "TestMap2";

        String mapUri1 = mapBaseUri + mapName1;
        String mapUri2 = mapBaseUri + mapName2;

        AssetTree clientAssetTree = Chassis.defaultSession();

        KeySubscriber<String> mapEventKeySubscriber = EasyMock.createStrictMock(KeySubscriber.class);
        clientAssetTree.registerSubscriber(mapBaseUri, String.class, mapEventKeySubscriber);

        //TODO DS how do we subscribe to get insert, update, remove events for maps (not map entities)?

        //First the two maps will be added
        mapEventKeySubscriber.onMessage(mapName1);
        mapEventKeySubscriber.onMessage(mapName2);

        //Second the two maps are removed
        mapEventKeySubscriber.onMessage(mapName1);
        mapEventKeySubscriber.onMessage(mapName2);

        EasyMock.replay(mapEventKeySubscriber);

        //Create the two maps
        Map<String, String> map1 = Chassis.acquireMap(mapUri1, String.class, String.class);
        Map<String, String> map2 = Chassis.acquireMap(mapUri2, String.class, String.class);

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

        EasyMock.verify(mapEventKeySubscriber);
    }

    //TODO DS how do we remove maps? Add test.
}