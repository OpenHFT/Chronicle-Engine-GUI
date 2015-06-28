package jp.mufg.chronicle.map.eventlistener;

import ddp.api.TestUtils;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.MapEventListener;
import net.openhft.chronicle.map.WriteContext;
import net.openhft.chronicle.tools.ChronicleTools;
import net.openhft.lang.values.StringValue;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by daniels on 31/03/2015.
 */
public class MapEventListenerTest
{
    private static final String _chronicleMapStringFilePath = OS.TARGET + "/chronicleMapStringListenerTest";
    private static final String _chronicleMapStringValueFilePath = OS.TARGET + "/chronicleMapStringValueListenerTest";
    private static File _chronicleStringMapFile;
    private static File _chronicleStringValueMapFile;

//    private ChronicleTestEventListener _chronicleTestEventListener;
private static int _noOfEventsTriggered = 0;
    private final String _value1 = "TestValue1";
    private final String _value2 = "TestValue2";
    private ChronicleMap<String, String> _chronicleMapString;
    private ChronicleMap<String, StringValue> _chronicleMapStringValue;

    @BeforeClass
    public static void beforeClass() throws IOException
    {
        new File(OS.TARGET).mkdir();

        _chronicleStringMapFile = new File(_chronicleMapStringFilePath);
        _chronicleStringMapFile.delete();
        _chronicleStringValueMapFile = new File(_chronicleMapStringValueFilePath);
        _chronicleStringValueMapFile.delete();
    }

    @Before
    public void setUp() throws IOException {
        _noOfEventsTriggered = 0;

        ChronicleTools.deleteDirOnExit(_chronicleMapStringFilePath);
        ChronicleTools.deleteDirOnExit(_chronicleMapStringValueFilePath);

        ChronicleTestEventListener _chronicleTestEventListener = new ChronicleTestEventListener();

        _chronicleMapString = ChronicleMapBuilder
                .of(String.class, String.class)
                .eventListener(_chronicleTestEventListener)
                .createPersistedTo(_chronicleStringMapFile);

        _chronicleMapStringValue = ChronicleMapBuilder
                .of(String.class, StringValue.class)
                .eventListener(_chronicleTestEventListener)
                .createPersistedTo(_chronicleStringValueMapFile);
    }

    /**
     * Test that event listener is triggered for every put.
     *
     * @
     */
    @Test
    public void testMapEvenListenerPut()
    {
        String testKey = "TestKeyPut";
        int noOfIterations = 50;

        testIterateAndAlternate(
                (x) -> _chronicleMapString.put(testKey, x),
                (x) -> _chronicleMapString.put(testKey, x),
                noOfIterations);
    }

    /**
     * Test that event listener is triggered for every replace.
     *
     * @
     */
    @Test
    public void testMapEvenListenerReplace()
    {
        String testKey = "TestKeyGetReplace";
        int noOfIterations = 50;

        _chronicleMapString.put(testKey, _value2);
        _noOfEventsTriggered = 0;

        Consumer<String> consumer = (x) -> _chronicleMapString.replace(testKey, x);

        testIterateAndAlternate(
                consumer,
                consumer,
                noOfIterations);
    }

    /**
     * Test that event listener is triggered for every "acquireUsingLocked" value update.
     *
     * @
     */
    @Test
    @Ignore("TODO")
    public void testMapEvenListenerAcquireUsingLocked()
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

    /**
     * Test that event listener is triggered for every "acquireUsing" value update.
     *
     * @
     */
    @Test
    @Ignore("TODO")
    public void testMapEvenListenerAcquireUsing()
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

    /**
     * Test that event listener is triggered for every "getUsing" value update.
     *
     * @
     */
    @Test
    @Ignore("TODO")
    public void testMapEvenListenerGetUsing()
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

    /**
     * Performs the given number of iterations and alternates between calling consumer1 and consumer2 passing
     * either _value1 or _value2.
     *
     * @param consumer1      Consumer1 to call.
     * @param consumer2      Consumer2 to call.
     * @param noOfIterations Number of iterations to perform.
     */
    private void testIterateAndAlternate(Consumer<String> consumer1, Consumer<String> consumer2, int noOfIterations)
    {
        long startTime = System.nanoTime();

        for (int i = 0; i < noOfIterations; i++)
        {
            if (i % 2 == 0)
            {
                consumer1.accept(_value1);
            }
            else
            {
                consumer2.accept(_value2);
            }
        }

        double runtime = TestUtils.calculateAndPrintRuntime(startTime);

        //Test that 50 updates takes less than 1 second
        Assert.assertTrue(runtime < 1000000000);

        Assert.assertEquals(noOfIterations, _noOfEventsTriggered);
    }

    private class ChronicleTestEventListener extends MapEventListener
    {
        @Override
        public void onPut(Object key, Object newValue, Object replacedValue, boolean replicationEvent)
        {
            _noOfEventsTriggered++;
        }
    }
}