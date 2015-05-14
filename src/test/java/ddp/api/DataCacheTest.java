package ddp.api;

import ddp.api.util.DdpAssert;
import net.openhft.chronicle.hash.replication.TcpTransportAndNetworkConfig;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.ChronicleMapStatelessClientBuilder;
import org.easymock.EasyMock;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataCacheTest {
    public static final int MAX_RUNTIME = 2000000000;
    private static String _testMapsDirectory = System.getProperty("java.io.tmpdir");



    //<String, Double> data cache
    private static ChronicleMap<String, Double> _dataCacheChronicleMapDouble;
    private static String _dataCacheFilePathDouble = _testMapsDirectory + "/DdpDataCacheTestFileDouble";
    private static DataCache<String, Double> _dataCacheDouble;
    private static int _dataCachePortDouble = 8833;
    private static String _dataCacheNameDouble = "DdpDataCacheTestDouble";

    //<String, String> data cache
    private static ChronicleMap<String, String> _dataCacheChronicleMapString;
    private static String _dataCacheFilePathString = _testMapsDirectory + "/DdpDataCacheTestFileString";
    private static DataCache<String, String> _dataCacheString;
    private static int _dataCachePortString = 8844;
    private static String _dataCacheNameString = "DdpDataCacheTestString";

    private static String _dataCacheHostname = "localhost";
    private static String _dataCacheIp = "127.0.0.1";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestUtils.createDirectoryIfNotExists(_testMapsDirectory);

        //Create local Chronicle map String, Double
        TestUtils.deleteFile(_dataCacheFilePathDouble);
        File fileDouble = new File(_dataCacheFilePathDouble);

        TcpTransportAndNetworkConfig tcpConfigDouble = TcpTransportAndNetworkConfig.of(_dataCachePortDouble);

        _dataCacheChronicleMapDouble = ChronicleMapBuilder
                .of(String.class, Double.class)
                .replication((byte) 1, tcpConfigDouble)
           //     .entries(1 << 13)
               .createPersistedTo(fileDouble);

        //Create Data Cache String, Double
        DataCacheConfiguration dataCacheConfigurationDouble = new DataCacheConfiguration(_dataCacheHostname, _dataCacheIp, _dataCachePortDouble, _dataCacheNameDouble);

        _dataCacheDouble = new ChronicleDataCache<String, Double>(dataCacheConfigurationDouble);


        //Create local Chronicle map String, String
        TestUtils.deleteFile(_dataCacheFilePathString);

        File fileString = new File(_dataCacheFilePathString);

        TcpTransportAndNetworkConfig tcpConfigString = TcpTransportAndNetworkConfig.of(_dataCachePortString);

        _dataCacheChronicleMapString = ChronicleMapBuilder
                .of(String.class, String.class)
                .averageValueSize(31457280) //30MB should account for 22MB plus serialisation
                .entries(50)
                .replication((byte) 1, tcpConfigString)
                .createPersistedTo(fileString);

        //Create Data Cache String, Double
        DataCacheConfiguration dataCacheConfigurationString = new DataCacheConfiguration(_dataCacheHostname, _dataCacheIp, _dataCachePortString, _dataCacheNameString);

        _dataCacheString = new ChronicleDataCache<String, String>(dataCacheConfigurationString);
    }

    @After
    public void tearDown() throws Exception {
        if (_dataCacheDouble != null) {
            _dataCacheDouble.clear();
//            _dataCacheDouble.close();
        }

        if (_dataCacheString != null) {
            _dataCacheString.clear();
//            _dataCacheString.close();
        }
    }

    @AfterClass
    public static void stopAll() {
        if (_dataCacheDouble != null) {
            _dataCacheDouble.close();
        }

        if (_dataCacheString != null) {
            _dataCacheString.close();
        }
    }

    /**
     * Add an event listener before performing any put/remove operations.
     * Test that the two onPut methods are called when a value is put initially (old value expected to be null).
     * Test that the onPut methods with the updated value and old value set are triggered.
     * Test that the onRemove methods are called upon removal.
     *
     * @throws Exception
     */
    @Test
    @Ignore("EventListener not yet implemented in chronicle map")
    public void testEventListenerAddedBeforeAnyActionsAndTriggeredOnPutAndRemove() throws Exception {
        String key = "testKeyBeforeInitialPut";
        double value = 1.0;
        double valueUpdated = 2.0;

        //Event listener mock
        DataCacheEventListener dataCacheEventListener = EasyMock.createStrictMock(DataCacheEventListener.class);

        _dataCacheDouble.addEventListener(dataCacheEventListener);

        //Test that the two onPut methods are called when a value is put initially (old value expected to be null).
        dataCacheEventListener.onPut(key, value, null);
        dataCacheEventListener.onPut(key, value);

        EasyMock.replay(dataCacheEventListener);

        _dataCacheDouble.put(key, value);

        EasyMock.verify(dataCacheEventListener);


        //Test that the onPut methods with the updated value and old value set are triggered.
        EasyMock.reset(dataCacheEventListener);

        dataCacheEventListener.onPut(key, valueUpdated, value);
        dataCacheEventListener.onPut(key, valueUpdated);

        EasyMock.replay(dataCacheEventListener);

        _dataCacheDouble.put(key, valueUpdated);

        EasyMock.verify(dataCacheEventListener);


        //Test that the onRemove methods are called upon removal.
        EasyMock.reset(dataCacheEventListener);

        dataCacheEventListener.onRemove(key, valueUpdated);
        dataCacheEventListener.onRemove(key);

        _dataCacheDouble.remove(key);

        EasyMock.verify(dataCacheEventListener);
    }

    /**
     * Add an event listener after performing the initial put of the key/value pair and then performing subsequent put/remove operations.
     * Test that the onPut methods with the updated value and old value set are triggered, even when the event listener did not exist when the value was initially put.
     * Test that the onRemove methods are called upon removal.
     *
     * @throws Exception
     */
    @Ignore("EventListener not yet implemented in chronicle map")
    @Test
    public void testEventListenerAddedAfterInitialPutAndTriggeredOnPutAndRemove() throws Exception {
        String key = "testKeyAfterInitialPut";
        double value = 1.0;
        double valueUpdated = 2.0;

        //Put value before adding event listener
        _dataCacheDouble.put(key, value);

        //Add event listener
        DataCacheEventListener dataCacheEventListener = EasyMock.createStrictMock(DataCacheEventListener.class);

        _dataCacheDouble.addEventListener(dataCacheEventListener);

        //Test that the onPut methods with the updated value and old value set are triggered.
        dataCacheEventListener.onPut(key, valueUpdated, value);
        dataCacheEventListener.onPut(key, valueUpdated);

        EasyMock.replay(dataCacheEventListener);

        _dataCacheDouble.put(key, valueUpdated);

        EasyMock.verify(dataCacheEventListener);


        //Test that the onRemove methods are called upon removal.
        EasyMock.reset(dataCacheEventListener);

        dataCacheEventListener.onRemove(key, valueUpdated);
        dataCacheEventListener.onRemove(key);

        _dataCacheDouble.remove(key);

        EasyMock.verify(dataCacheEventListener);
    }

    /**
     * Add event listener, receive an update, remove event listener and ensure events are no longer triggered on the listener.
     * Test that the two onPut methods are called when a value is put initially (old value expected to be null).
     * Remove the event listener and confirm no methods are triggered.
     *
     * @throws Exception
     */
    @Test
    @Ignore("EventListener not yet implemented in chronicle map")
    public void testEventListenerNoLongerTriggeredAfterRemoved() throws Exception {
        String key = "testKeyRemoval";
        double value = 1.0;
        double valueUpdated = 2.0;

        //Event listener mock
        DataCacheEventListener dataCacheEventListener = EasyMock.createStrictMock(DataCacheEventListener.class);

        _dataCacheDouble.addEventListener(dataCacheEventListener);

        //Test that the two onPut methods are called when a value is put initially (old value expected to be null).
        dataCacheEventListener.onPut(key, value, null);
        dataCacheEventListener.onPut(key, value);

        EasyMock.replay(dataCacheEventListener);

        _dataCacheDouble.put(key, value);

        EasyMock.verify(dataCacheEventListener);


        //Remove the event listener and confirm no methods are triggered
        EasyMock.reset(dataCacheEventListener);

        _dataCacheDouble.removeEventListener(dataCacheEventListener);

        EasyMock.verify(dataCacheEventListener);
    }

    /**
     * Test that events are triggered in the event listener in the order they are performed on the map.
     *
     * @throws Exception
     */
    //@Test
    public void testEventListenerEventsAreTriggeredInCorrectOrder() throws Exception {
        String key = "testKeyTriggerInOrder";
        double value = 0.0;
        int noOfPuts = 1000;

        //Put value before adding event listener
        _dataCacheDouble.put(key, value);

        //Add event listener
        DataCacheEventListener dataCacheEventListener = EasyMock.createStrictMock(DataCacheEventListener.class);

        _dataCacheDouble.addEventListener(dataCacheEventListener);

        //Expect both onPut methods to be triggered.
        for (double i = 1.0; i <= noOfPuts; i++) {
            dataCacheEventListener.onPut(key, i, i - 1);
            dataCacheEventListener.onPut(key, i);
        }

        EasyMock.replay(dataCacheEventListener);

        //Perform the updates on the data cache in order
        for (double i = 1.0; i <= noOfPuts; i++) {
            _dataCacheDouble.put(key, i);
        }

        //Verify the events
        EasyMock.verify(dataCacheEventListener);


        //Verify the order for remove
        EasyMock.reset(dataCacheEventListener);
    }

    //TODO DS this is pretty fast when running locally, but slows down significantly when it runs on a remote server

    /**
     * Test for "real-time" server libor curves - JPY.
     * Test that we can put 50 key/value pairs where the value is a 1.4MB string and get all the same key/value pairs
     * all within 1 second.
     * This is currently running on the local computer, but should be tested between two computers.
     *
     * @throws Exception
     */
    @Test
    public void testServerLiborCurveJpy() throws Exception {
        String resourcePath = "ServerLiborCurves" + File.separator + "JPYValEnv.xml";
        int noOfPutAndGets = 50;
        int maxRuntime = MAX_RUNTIME;


        verifyRuntimeForNumberOfPutAndGetsDifferentKeysStringMap(resourcePath, noOfPutAndGets, maxRuntime, true);
    }

    /**
     * Test for "real-time" server libor curves - USD.
     * Test that we can put 50 key/value pairs where the value is a 1MB string and get all the same key/value pairs
     * all within 1 second.
     * This is currently running on the local computer, but should be tested between two computers.
     *
     * @throws Exception
     */
    @Test
    public void testServerLiborCurveUsd() throws Exception {
        String resourcePath = "ServerLiborCurves" + File.separator + "USDValEnv.xml";
        int noOfPutAndGets = 50;
        int maxRuntime = MAX_RUNTIME;

        verifyRuntimeForNumberOfPutAndGetsDifferentKeysStringMap(resourcePath, noOfPutAndGets, maxRuntime, true);
    }

    /**
     * Test putting and getting the Discount Factors as a map (whole map updated at once).
     * It is rather small so we would expect it to take less than 0.1 seconds
     *
     * @throws Exception
     */
    @Test
    public void testServerLiborDfCsvAsMap() throws Exception {
        {
            String resourcePath = "ServerLiborDf" + File.separator + "EURBasis.csv";
            int noOfPutAndGets = 50;
            int maxRuntime = 100_000_000;//0.1s

            verifyRuntimeForNumberOfPutAndGetsDifferentKeysDoubleMap(resourcePath, noOfPutAndGets, maxRuntime);

        }
        {
            String resourcePath = "ServerLiborCurves" + File.separator + "JPYValEnv.xml";
            int noOfPutAndGets = 50;
            int maxRuntime = MAX_RUNTIME;


            verifyRuntimeForNumberOfPutAndGetsDifferentKeysStringMap(resourcePath, noOfPutAndGets, maxRuntime, true);
        }
    }

    /**
     * Test putting and getting the Discount Factors as a map (whole map updated at once).
     * It is rather small so we would expect it to take less than 0.1 seconds
     *
     * @throws Exception
     */
    @Test
    public void testServerLiborDfCsvAsString() throws Exception {
        String resourcePath = "ServerLiborDf" + File.separator + "EURBasis.csv";
        int noOfPutAndGets = 50;
        int maxRuntime = 100000000;

        verifyRuntimeForNumberOfPutAndGetsDifferentKeysStringMap(resourcePath, noOfPutAndGets, maxRuntime, false);
    }

    @Test
    public void testServerLiborDfCsvAsStringPerformanceComparedToFile() throws Exception {
        String resourcePath = "ServerLiborDf" + File.separator + "EURBasis.csv";
        String testFileExtension = ".xml";
        int noOfPuts = 1;

        compareFileSaveToPut(resourcePath, testFileExtension, noOfPuts);
    }

    /**
     * Test that a collateral valuation environment can be updated at least once a minutes
     * Updated every 60 seconds.
     * <p>
     * File size c. 2.4 MB.
     *
     * @throws Exception
     */
    @Test
    public void testTraderIrCurveCollateral() throws Exception {
        String resourcePath = "TraderIrCurves" + File.separator + "Collateral_valenvOIS.xml";
        int noOfPutAndGets = 1;
        int maxRuntime = MAX_RUNTIME;

        verifyRuntimeForNumberOfPutAndGetsDifferentKeysStringMap(resourcePath, noOfPutAndGets, maxRuntime, false);
    }

    /**
     * Test that writing to the cache is as fast or faster than writing to file.
     * <p>
     * File size c. 2.4 MB.
     *
     * @throws Exception
     */
    @Test
    public void testTraderIrCurveCollateralPerformanceComparedToFile() throws Exception {
        String resourcePath = "TraderIrCurves" + File.separator + "Collateral_valenvOIS.xml";
        String testFileExtension = ".xml";
        int noOfPuts = 1;

        compareFileSaveToPut(resourcePath, testFileExtension, noOfPuts);
    }

    /**
     * Valuation environment published to Murex. Updated app. every 2 minutes.
     * <p>
     * File size c. 5MB
     *
     * @throws Exception
     */
    @Test
    public void testTraderIrCurveMxValEnv() throws Exception {
        String resourcePath = "TraderIrCurves" + File.separator + "mxvalenv.xml";
        int noOfPutAndGets = 1;
        int maxRuntime = 100000000;

        verifyRuntimeForNumberOfPutAndGetsDifferentKeysStringMap(resourcePath, noOfPutAndGets, maxRuntime, false);
    }

    /**
     * Test that writing to the cache is as fast or faster than writing to file.
     * <p>
     * File size c. 5MB
     *
     * @throws Exception
     */
    @Test
    public void testTraderIrCurveMxValEnvPerformanceComparedToFile() throws Exception {
        String resourcePath = "TraderIrCurves" + File.separator + "mxvalenv.xml";
        String testFileExtension = ".xml";
        int noOfPuts = 1;

        compareFileSaveToPut(resourcePath, testFileExtension, noOfPuts);
    }

    /**
     * Test putting a collection of large strings using the putAll method while another thread is trying to update
     * one of the keys.
     *
     * @throws Exception
     */
    @Test
    @Ignore("TODO Fix test, resource is missing")
    public void testPutAllRaceConditionWithOtherPuts() throws Exception {
        String testKey = "BaseKey";
        String keyToCheck = testKey + 1;

        String testString = TestUtils.loadSystemResourceFileToString("EUR_ValEnv_1.xml");

        int noOfPuts = 50;

        long startTime = System.nanoTime();

        Map<String, String> memoryMap = new HashMap<>();

        //Put the large string value for the number of keys
        for (int i = 0; i < noOfPuts; i++) {
            memoryMap.put(testKey + i, testString);
        }

        //Create a new thread and let it finish before finishing execution of putAll
        Thread t = new Thread(
                () -> {
                    try {
                        Thread.sleep(1000);

                        boolean keepRunning = true;
                        boolean isValuePutAndGet = false;

                        while (keepRunning) {
                            ChronicleMap<String, String> otherStatelessClient = ChronicleMapStatelessClientBuilder
                                    .createClientOf(new InetSocketAddress(_dataCacheHostname, _dataCachePortString));

                            String valueFromMap = otherStatelessClient.get(testKey + 1);

                            if (valueFromMap != null && valueFromMap.length() > 20) {
                                keepRunning = false;
                            } else {
                                otherStatelessClient.put(keyToCheck, "ShortTestValue");

                                isValuePutAndGet = true;
                            }
                        }

                        Assert.assertTrue(isValuePutAndGet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        t.start();

        _dataCacheString.putAll(memoryMap);

        while (t.isAlive()) {
            Thread.sleep(200);
        }

        String finalCacheResult = _dataCacheString.get(keyToCheck);

        //Test that the end result is the large string
        Assert.assertEquals(1653674, finalCacheResult.length());

        TestUtils.calculateAndPrintRuntime(startTime);
    }

    //TODO DS Test put/get
    //TODO DS Test put/get large strings
    //TODO DS Test put get usages from Ian
    //TODO DS Test put/get large strings compressed??
    //TODO DS Test put/get large strings using event listeners
    //TODO DS Test remove large strings???? Compressed as well?
    //TODO DS Test compute if absent and other more Chronicle specific methods?
    //TODO DS Test update all values in map with a lock.

    //TODO DS test get collection of keys - snap. Do we need to lock? How does chronicle work with get collection? Streaming or is it a "snap"?
    //TODO DS how do we actually test this? Does Chronicle do a "snap", ie. lock, of the dataset and send back to the client or is there a possible race condition where a value is updated??

    /**
     * Test that a "snap" of the current state of the cache by updating a value in the cache while iterating the entry set.
     *
     * @throws Exception
     */
    @Test
    public void testGetEntrySetSnap() throws Exception {
        Map<String, Double> populatedTestMap = getPopulatedTestMap();

        //Get first key from test map
        String keyToUpdate = populatedTestMap.keySet().stream().findFirst().get();
        double valueToUpdate = -1.0;

        _dataCacheDouble.putAll(populatedTestMap);

        Set<Map.Entry<String, Double>> entries = _dataCacheDouble.entrySet();

        //Update the data cache
        _dataCacheDouble.put(keyToUpdate, valueToUpdate);

        for (Map.Entry<String, Double> entry : populatedTestMap.entrySet()) {
            if (entry.getKey().equals(keyToUpdate)) {
                Assert.assertNotEquals(entry.getValue(), valueToUpdate, 0.0);
            }

            Assert.assertTrue(entries.contains(entry));
        }
    }

    /**
     * Populates a map with test values.
     *
     * @return
     */
    private Map<String, Double> getPopulatedTestMap() {
        String baseKey = "Key";
        double value = 1.0;
        int noOfKeys = 44;

        Map<String, Double> testMap = new HashMap<>();

        for (int i = 0; i < noOfKeys; i++) {
            testMap.put(baseKey + i, value + i);
        }

        return testMap;
    }

    /**
     * Performs the configured number of put and gets on the String map changing the key for each put, but using the
     * same String value as loaded from the given resources.
     * <p>
     * The runtime is calculated and checked against the given max runtime.
     *
     * @param resourcePath      Path to string resource that will be loaded and used as value for all puts.
     * @param noOfPutAndGets    Number of puts and gets to be performed
     * @param maxRuntimeInNanos Max runtime for all of the put and gets.
     * @throws IOException
     * @throws URISyntaxException
     */
    private void verifyRuntimeForNumberOfPutAndGetsDifferentKeysStringMap(String resourcePath, int noOfPutAndGets, int maxRuntimeInNanos, boolean useDifferentKeys) throws IOException, URISyntaxException {

        String testString = TestUtils.loadSystemResourceFileToString(resourcePath);
        String key = "BaseKey";

        long startTime = System.nanoTime();
        int count = 0;
        while (System.nanoTime() - startTime < 2e9) {
            StringBuilder keyPutStringBuilder = new StringBuilder(key);
            StringBuilder keyGetStringBuilder = new StringBuilder(key);
            count++;

            //Put the large string value for the number of keys
            for (int i = 0; i < noOfPutAndGets; i++) {
                //System.out.println(count + " : " +i + " : " + keyPutStringBuilder.append(i).toString());

                if (useDifferentKeys) {
                    //_dataCacheString.put(keyPutStringBuilder.append(i).toString(), testString);
                    _dataCacheString.put(key +i, testString);
                } else {
                    _dataCacheString.put(key, testString);
                }
            }

            //Get the large string value for the number of keys
            for (int i = 0; i < noOfPutAndGets; i++) {
                String resultString = null;

                if (useDifferentKeys) {
                    resultString = _dataCacheString.get(key +i);
                } else {
                    resultString = _dataCacheString.get(key);
                }

                //Test that the string from the cache matches the put string
                Assert.assertEquals(testString, resultString);
            }
        }

        long runtimeInNanos = TestUtils.calculateAndPrintRuntime(startTime, count);

        //Test that the 50 puts and gets took 1 second or less
        DdpAssert.assertTimeLimit(maxRuntimeInNanos, runtimeInNanos);
    }

    /**
     * Loads the file as string and saves it as a new file to compare the time it takes to create the new file with the
     * time it takes to put the string into the cache.
     *
     * @param resourcePath  Path to file resource.
     * @param fileExtension File extension for file to write.
     * @param noOfPuts      Number of puts to perform for comparison.
     * @throws Exception
     */
    private void compareFileSaveToPut(String resourcePath, String fileExtension, int noOfPuts) throws Exception {
        long timeToSaveFileToDiskInNanos = getTimeToSaveFileToDiskInNanos(resourcePath, fileExtension);

        String key = "BaseKey";

        String testString = TestUtils.loadSystemResourceFileToString(resourcePath);

        long startTime = System.nanoTime();
        int count = 0;
        while (System.nanoTime() - startTime < 2e9) {
            count++;

            //Put the large string value for the number of keys
            for (int i = 0; i < noOfPuts; i++) {
                _dataCacheString.put(key + '-' + i, testString);
            }
        }

        long runtimeInNanos = TestUtils.calculateAndPrintRuntime(startTime, count);

        //Test that the 50 puts and gets took 1 second or less
        DdpAssert.assertTimeLimit(timeToSaveFileToDiskInNanos * noOfPuts, runtimeInNanos);
    }

    /**
     * Loads the resource as as string and saves it to a new test file with the given extension while measuring how long
     * it takes. Deletes the test file afterwards and returns the runtime.
     *
     * @param resourcePath  Path to resource that is loaded as string.
     * @param fileExtension File extension use for file saved.
     * @return Time it takes to save string as a new file.
     * @throws Exception
     */
    public long getTimeToSaveFileToDiskInNanos(String resourcePath, String fileExtension) throws Exception {
        String testString = TestUtils.loadSystemResourceFileToString(resourcePath);

        long startTime = System.nanoTime();

        int count = 0;
        while (System.nanoTime() - startTime < 2e9) {
            TestUtils.saveTestFileToDisk(fileExtension, testString);
            count++;
        }

        long runtimeInNanos = TestUtils.calculateAndPrintRuntime(startTime, count);

        TestUtils.deleteTestFile(fileExtension);

        return runtimeInNanos;
    }

    /**
     * Performs the configured number of put and gets on the String map changing the key for each put, but using the
     * same String value as loaded from the given resources.
     * <p>
     * The runtime is calculated and checked against the given max runtime.
     *
     * @param resourcePath      Path to string resource that will be loaded and used as value for all puts.
     * @param noOfPutAndGets    Number of puts and gets to be performed
     * @param maxRuntimeInNanos Max runtime for all of the put and gets.
     * @throws IOException
     * @throws URISyntaxException
     */
    private void verifyRuntimeForNumberOfPutAndGetsDifferentKeysDoubleMap(String resourcePath, int noOfPutAndGets, int maxRuntimeInNanos) throws IOException, URISyntaxException {
        Map<String, Double> testKvpMap = TestUtils.loadSystemResourceKeyValueCsvFileToMap(resourcePath);

        long startTime = System.nanoTime();
        int count = 0;
        while (System.nanoTime() - startTime < 2e9) {
            //Put the large string value for the number of keys
            for (int i = 0; i < noOfPutAndGets; i++) {
                _dataCacheDouble.putAll(testKvpMap);
            }

            //Get the large string value for the number of keys
            for (int i = 0; i < noOfPutAndGets; i++) {
                _dataCacheDouble.forEach((k, v) -> Assert.assertEquals(testKvpMap.get(k), v));
            }
            count++;
        }

        long runtimeInNanos = TestUtils.calculateAndPrintRuntime(startTime, count);

        //Test that the 50 puts and gets took 1 second or less
        DdpAssert.assertTimeLimit(maxRuntimeInNanos, runtimeInNanos);
    }
}