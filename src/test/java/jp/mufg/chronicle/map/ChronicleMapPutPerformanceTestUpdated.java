package jp.mufg.chronicle.map;

import jp.mufg.chronicle.map.testclasses.*;
import net.openhft.lang.Jvm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

// TODO add expected performance measures.
public class ChronicleMapPutPerformanceTestUpdated
{
    private String chronicleMapFile = Jvm.TMP + "/chroniclemap2";
    private File file;
    private int noOfPuts = 10000000;
    private MapContainer marketDataCache;
//    private MapContainerEnum marketDataCache;

    @Before
    public void setUp() throws Exception
    {
        file = new File(chronicleMapFile);
        file.delete();
        file.deleteOnExit();

        marketDataCache = new MapContainer(file);
//        marketDataCache = new MapContainerEnum(file);
    }

    @After
    public void tearDown() throws Exception
    {
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
    public void testChronicleMapPuts() throws IOException
    {
        putConfiguredNumberOfKeyValues();

        putConfiguredNumberOfKeyValues();

        putConfiguredNumberOfKeyValues();

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

    private long putConfiguredNumberOfKeyValues()
    {
        QuoteMapKey quoteMapKey1 = generateExampleQuoteMapKey1();

        long startTime = System.nanoTime();


        new StringBuilder();

        for (double i = 0.0; i < noOfPuts; i++)
        {
            marketDataCache.put(quoteMapKey1.getSource().name() , quoteMapKey1.getSupplier().name(), marketDataValue -> {
                marketDataValue.setAsk(10);
                marketDataValue.setBid(10);
            });
        }

        return calculateAndPrintRuntime(startTime);
    }

    private QuoteMapKey generateExampleQuoteMapKey1()
    {
        MarketDataSupplier supplier = MarketDataSupplier.BROADWAY;
        MarketDataSource source = MarketDataSource.BLOOMBERG;
        String id = "Id1";
        MarketDataField field = MarketDataField.BID_PRICE;

        return new QuoteMapKey(supplier, source, id, field);
    }

    private QuoteMapKey generateExampleQuoteMapKey2()
    {
        MarketDataSupplier supplier = MarketDataSupplier.REUTERS;
        MarketDataSource source = MarketDataSource.CME;
        String id = "Id2";
        MarketDataField field = MarketDataField.ASK_PRICE;

        return new QuoteMapKey(supplier, source, id, field);
    }

    private long calculateAndPrintRuntime(long startTimeInNanoseconds)
    {
        long endNanoTime = System.nanoTime();

        long runtimeNanoSeconds = endNanoTime - startTimeInNanoseconds;

        double runtimeMilliseconds = (double)runtimeNanoSeconds / 1000000.0;

        double runtimeSeconds = runtimeMilliseconds / 1000.0;

        System.out.println("Runtime: " + runtimeNanoSeconds + " nanoseconds | "
                + runtimeMilliseconds + " milliseconds | " + runtimeSeconds + " seconds");

        return runtimeNanoSeconds;
    }
}