//package jp.mufg.chronicle.map.eventlistener;
//
//import jp.mufg.*;
//import net.openhft.chronicle.hash.replication.*;
//import net.openhft.chronicle.map.*;
//import net.openhft.chronicle.tools.*;
//import net.openhft.lang.values.*;
//import org.junit.*;
//
//import java.io.*;
//import java.net.*;
//import java.util.function.*;
//
///**
//* Created by daniels on 31/03/2015.
//*/
//public class MapEventListenerStatelessClientTest
//{
//    private static final String _chronicleMapStringFilePath = "C:\\LocalFolder\\temp\\Chronicle\\chronicleMapStringListenerTest";
//    private static File _chronicleStringMapFile;
//
//    private static final String _chronicleMapStringValueFilePath = "C:\\LocalFolder\\temp\\Chronicle\\chronicleMapStringValueListenerTest";
//    private static File _chronicleStringValueMapFile;
//
//    private ChronicleTestEventListener _chronicleTestEventListener;
//
//    private ChronicleMap<String, String> _chronicleMapString;
//    private ChronicleMap<String, String> _chronicleMapStringClient;
//
//    private ChronicleMap<String, StringValue> _chronicleMapStringValue;
//    private ChronicleMap<String, StringValue> _chronicleMapStringValueClient;
//
//    private final String _value1 = "TestValue1";
//    private final String _value2 = "TestValue2";
//
//    private static int _noOfEventsTriggered = 0;
//
//    @BeforeClass
//    public static void beforeClass() throws IOException
//    {
//        _chronicleStringMapFile = new File(_chronicleMapStringFilePath);
//        _chronicleStringValueMapFile = new File(_chronicleMapStringValueFilePath);
//    }
//
//    @Before
//    public void setUp() throws Exception
//    {
//        _noOfEventsTriggered = 0;
//
//        ChronicleTools.deleteDirOnExit(_chronicleMapStringFilePath);
//        ChronicleTools.deleteDirOnExit(_chronicleMapStringValueFilePath);
//
//        _chronicleTestEventListener = new ChronicleTestEventListener();
//
//        _chronicleMapString = ChronicleMapBuilder
//                .of(String.class, String.class)
//                .replication((byte) 2, TcpTransportAndNetworkConfig.of(8076))
//                .eventListener(_chronicleTestEventListener)
//                .createPersistedTo(_chronicleStringMapFile);
//
//        _chronicleMapStringClient = ChronicleMapBuilder
//                .of(String.class, String.class, new InetSocketAddress("localhost", 8076))
//                .putReturnsNull()
//                .create();
//
//        _chronicleMapStringClient = ChronicleMapStatelessClientBuilder
//                .createClientOf(new InetSocketAddress("localhost", 8076));
//
////        _chronicleMapStringValue = ChronicleMapBuilder
////                .of(String.class, StringValue.class)
////                .replication((byte) 3, TcpTransportAndNetworkConfig.of(8078))
////                .eventListener(_chronicleTestEventListener)
////                .createPersistedTo(_chronicleStringValueMapFile);
////
////        _chronicleMapStringValueClient = ChronicleMapStatelessClientBuilder
////                .createClientOf(new InetSocketAddress("localhost", 8078));
//    }
//
//    @After
//    public void tearDown() throws Exception
//    {
//        ChronicleTools.deleteDirOnExit(_chronicleMapStringFilePath);
//        ChronicleTools.deleteDirOnExit(_chronicleMapStringValueFilePath);
//    }
//
//    /**
//     * Test that event listener is triggered for every put.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testMapEvenListenerClientPut() throws Exception
//    {
//        String testKey = "TestKeyPut";
//        int noOfIterations = 50;
//
//        testIterateAndAlternate(
//                (x) -> _chronicleMapString.put(testKey, x),
//                (x) -> _chronicleMapString.put(testKey, x),
//                noOfIterations);
//    }
//
//    /**
//     * Test that event listener is triggered for every replace.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testMapEvenListenerReplace() throws Exception
//    {
//        String testKey = "TestKeyGetReplace";
//        int noOfIterations = 50;
//
//        _chronicleMapString.put(testKey, _value2);
//        _noOfEventsTriggered = 0;
//
//        Consumer<String> consumer = (x) -> _chronicleMapString.replace(testKey, x);
//
//        testIterateAndAlternate(
//                consumer,
//                consumer,
//                noOfIterations);
//    }
//
//    /**
//     * Test that event listener is triggered for every "acquireUsingLocked" value update.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testMapEvenListenerAcquireUsingLocked() throws Exception
//    {
//        StringValue valueInstance = _chronicleMapStringValue.newValueInstance();
//
//        String testKey = "TestKeyAcquireUsingLocked";
//        int noOfIterations = 50;
//
//        Consumer<String> consumer = (x) -> {
//            try (WriteContext<String, StringValue> writeContext = _chronicleMapStringValue.acquireUsingLocked(testKey, valueInstance))
//            {
//                valueInstance.setValue(x);
//            }
//        };
//
//        testIterateAndAlternate(
//                consumer,
//                consumer,
//                noOfIterations);
//    }
//
//    /**
//     * Test that event listener is triggered for every "acquireUsing" value update.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testMapEvenListenerAcquireUsing() throws Exception
//    {
//        StringValue valueInstance = _chronicleMapStringValue.newValueInstance();
//
//        String testKey = "TestKeyAcquireUsing";
//        int noOfIterations = 50;
//
//        Consumer<String> consumer = (x) -> {
//            StringValue stringValue = _chronicleMapStringValue.acquireUsing(testKey, valueInstance);
//            stringValue.setValue(x);
//        };
//
//        testIterateAndAlternate(
//                consumer,
//                consumer,
//                noOfIterations);
//    }
//
//    /**
//     * Test that event listener is triggered for every "getUsing" value update.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testMapEvenListenerGetUsing() throws Exception
//    {
//        StringValue valueInstance = _chronicleMapStringValue.newValueInstance();
//
//        String testKey = "TestKeyGetUsing";
//        int noOfIterations = 50;
//
//        _chronicleMapStringValue.put(testKey, valueInstance);
//
//        Consumer<String> consumer = (x) -> {
//            StringValue using = _chronicleMapStringValue.getUsing(testKey, valueInstance);
//            using.setValue(x);
//        };
//
//        testIterateAndAlternate(
//                consumer,
//                consumer,
//                noOfIterations);
//    }
//
//    /**
//     * Performs the given number of iterations and alternates between calling consumer1 and consumer2 passing
//     * either _value1 or _value2.
//     *
//     * @param consumer1      Consumer1 to call.
//     * @param consumer2      Consumer2 to call.
//     * @param noOfIterations Number of iterations to perform.
//     */
//    private void testIterateAndAlternate(Consumer<String> consumer1, Consumer<String> consumer2, int noOfIterations)
//    {
//        long startTime = System.nanoTime();
//
//        for (int i = 0; i < noOfIterations; i++)
//        {
//            if (i % 2 == 0)
//            {
//                consumer1.accept(_value1);
//            }
//            else
//            {
//                consumer2.accept(_value2);
//            }
//        }
//
//        double runtime = TestUtils.calculateAndPrintRuntime(startTime);
//
//        //Test that 50 updates takes less than 1 second
////        Assert.assertTrue(runtime < 1000000000);
//
//        Assert.assertEquals(noOfIterations, _noOfEventsTriggered);
//    }
//
//    private class ChronicleTestEventListener extends MapEventListener
//    {
//        @Override
//        public void onPut(Object key, Object newValue, Object replacedValue, boolean replicationEvent)
//        {
//            _noOfEventsTriggered++;
//        }
//    }
//}