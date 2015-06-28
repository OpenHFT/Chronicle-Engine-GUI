package jp.mufg.chronicle.map;

import ddp.api.TestUtils;
import jp.mufg.chronicle.map.testclasses.MapContainer;
import jp.mufg.chronicle.map.testclasses.QuoteMapKey;
import net.openhft.chronicle.core.OS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

// TODO add expected performance measures.
public class ChronicleMapPutPerformanceTestUpdated {
    private String chronicleMapFile = OS.TARGET + "/chroniclemap2";
    private File file;
    private int noOfPuts = 10000000;
    private MapContainer marketDataCache;
//    private MapContainerEnum marketDataCache;

    @Before
    public void setUp() throws IOException {
        file = new File(chronicleMapFile);
        file.delete();
        file.deleteOnExit();

        marketDataCache = new MapContainer(file);
//        marketDataCache = new MapContainerEnum(file);
    }

    @After
    public void tearDown() {
        marketDataCache.close();
    }

//    @Test
//    public void testCompareMapPuts() throws IOException
//    {
//        //Put to Chronicle
//        setMarketDataMapToChronicle();
//
//        long putChronicleRuntime = putConfiguredNumberOfKeyValues();
//
//        //Replace in Chronicle
//        long replaceChronicleRuntime = replaceConfiguredNumberOfKeyValues();
//
//        //Put to hash map
//        setMarketDataMapToHashMap();
//
//        long putHashMapRuntime = putConfiguredNumberOfKeyValues();
//
//        System.out.println("Diff chronicle put - replace: " + (putChronicleRuntime - replaceChronicleRuntime));
//        System.out.println("Diff chronicle put - hash put: " + (putChronicleRuntime - putHashMapRuntime));
//        System.out.println("Diff chronicle replace - hash put: " + (replaceChronicleRuntime - putHashMapRuntime));
//    }

    @Test
    public void testChronicleMapPuts() throws IOException {
        putConfiguredNumberOfKeyValues();
    }

//    @Test
//    public void testChronicleMapReplace() throws IOException
//    {
//        setMarketDataMapToChronicle();
//
//        replaceConfiguredNumberOfKeyValues();
//    }
//
//    @Test
//    public void testHashMapPuts() throws IOException
//    {
//        setMarketDataMapToHashMap();
//
//        putConfiguredNumberOfKeyValues();
//    }

    private long putConfiguredNumberOfKeyValues() {
        long startTime = System.nanoTime();

        QuoteMapKey[] quoteMapKey = ChronicleMapPutPerformanceTest.quoteMapKey;
        IntStream.range(0, noOfPuts).parallel().forEach(i -> {
            QuoteMapKey quoteMapKey1 = quoteMapKey[i % quoteMapKey.length];
            marketDataCache.put(quoteMapKey1.getSource().name(), quoteMapKey1.getSupplier().name(), marketDataValue -> {
                marketDataValue.setAsk(10);
                marketDataValue.setBid(10);
            });
        });

        return calculateAndPrintRuntime(startTime);
    }

    private long calculateAndPrintRuntime(long startTimeInNanoseconds) {
        return TestUtils.calculateAndPrintRuntime(startTimeInNanoseconds);
    }
}