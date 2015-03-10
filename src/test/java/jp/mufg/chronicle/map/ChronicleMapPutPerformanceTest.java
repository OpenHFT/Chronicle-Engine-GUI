package jp.mufg.chronicle.map;

import jp.mufg.chronicle.map.testclasses.*;
import net.openhft.chronicle.map.*;
import net.openhft.chronicle.tools.*;
import org.junit.*;

import java.io.*;
import java.util.*;

public class ChronicleMapPutPerformanceTest
{
    private String chronicleMapFile = "C:\\LocalFolder\\temp\\Chronicle\\chroniclemap";
    private File file;
    private int noOfPuts = 10000000;
    private Map<QuoteMapKey, Object> marketDataCache;

    @Before
    public void setUp() throws Exception
    {
        ChronicleTools.deleteDirOnExit(chronicleMapFile);

        file = new File(chronicleMapFile);
    }

    @After
    public void tearDown() throws Exception
    {
        ChronicleTools.deleteDirOnExit(chronicleMapFile);
    }

    @Test
    public void testCompareMapPuts() throws IOException
    {
        //Put to Chronicle
        setMarketDataMapToChronicle();

        long putChronicleRuntime = putConfiguredNumberOfKeyValues();

        //Replace in Chronicle
        long replaceChronicleRuntime = replaceConfiguredNumberOfKeyValues();

        //Put to hash map
        setMarketDataMapToHashMap();

        long putHashMapRuntime = putConfiguredNumberOfKeyValues();

        System.out.println("Diff chronicle put - replace: " + (putChronicleRuntime - replaceChronicleRuntime));
        System.out.println("Diff chronicle put - hash put: " + (putChronicleRuntime - putHashMapRuntime));
        System.out.println("Diff chronicle replace - hash put: " + (replaceChronicleRuntime - putHashMapRuntime));
    }

    @Test
    public void testChronicleMapPuts() throws IOException
    {
        setMarketDataMapToChronicle();

        putConfiguredNumberOfKeyValues();
    }

    @Test
    public void testChronicleMapReplace() throws IOException
    {
        setMarketDataMapToChronicle();

        replaceConfiguredNumberOfKeyValues();
    }

    @Test
    public void testHashMapPuts() throws IOException
    {
        setMarketDataMapToHashMap();

        putConfiguredNumberOfKeyValues();
    }

    private long putConfiguredNumberOfKeyValues()
    {
        QuoteMapKey quoteMapKey1 = generateExampleQuoteMapKey1();
        QuoteMapKey quoteMapKey2 = generateExampleQuoteMapKey2();

        long startTime = System.nanoTime();

        for (double i = 0.0; i < noOfPuts; i++)
        {
//            marketDataCache.put(quoteMapKey1, i);
//            marketDataCache.put(quoteMapKey2, i);

            if (i % 2 == 0)
            {
                marketDataCache.put(quoteMapKey1, i);
            }
            else
            {
                marketDataCache.put(quoteMapKey2, i);
            }
        }

        return calculateAndPrintRuntime(startTime);
    }

    private long replaceConfiguredNumberOfKeyValues()
    {

        QuoteMapKey quoteMapKey1 = generateExampleQuoteMapKey1();
        QuoteMapKey quoteMapKey2 = generateExampleQuoteMapKey2();
        marketDataCache.put(quoteMapKey1, 0.0);
        marketDataCache.put(quoteMapKey2, 0.0);

        long startTime = System.nanoTime();

        for (double i = 0.0; i < noOfPuts; i++)
        {
            if (i % 2 == 0)
            {
                marketDataCache.replace(quoteMapKey1, i);
            }
            else
            {
                marketDataCache.replace(quoteMapKey2, i);
            }
        }

        return calculateAndPrintRuntime(startTime);
    }

    private void setMarketDataMapToChronicle() throws IOException
    {
        marketDataCache = ChronicleMapBuilder
                .of(QuoteMapKey.class, Object.class)
                .createPersistedTo(file);
    }

    private void setMarketDataMapToHashMap()
    {
        marketDataCache = new HashMap<>();
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