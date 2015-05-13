package jp.mufg.chronicle.map;

import ddp.api.TestUtils;
import jp.mufg.chronicle.map.testclasses.MarketDataField;
import jp.mufg.chronicle.map.testclasses.MarketDataSource;
import jp.mufg.chronicle.map.testclasses.MarketDataSupplier;
import jp.mufg.chronicle.map.testclasses.QuoteMapKey;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.tools.ChronicleTools;
import net.openhft.lang.Jvm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChronicleMapPutPerformanceTest
{
    private String chronicleMapFile = Jvm.TMP + "/Chronicle/chroniclemap";
    private File file;
    private int noOfPuts = 10_000_000;
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

        putConfiguredNumberOfKeyValues();

        putConfiguredNumberOfKeyValues();
    }

    @Test
     public void testConcurrentHashMapPuts() throws IOException
{
    setMarketDataMapToConcurrentHashMap();

    putConfiguredNumberOfKeyValues();

    putConfiguredNumberOfKeyValues();

    putConfiguredNumberOfKeyValues();
}

    @Test
    public void testConcurrentHashMapPutsMany() throws IOException
    {
        setMarketDataMapToConcurrentHashMap();

        putConfiguredNumberOfKeyValuesMany();

        putConfiguredNumberOfKeyValuesMany();

        putConfiguredNumberOfKeyValuesMany();
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

        return TestUtils.calculateAndPrintRuntime(startTime);
    }

    private long putConfiguredNumberOfKeyValuesMany()
    {
        QuoteMapKey quoteMapKeyTemp = generateExampleQuoteMapKey1();
        String id = "MyId";

        long startTime = System.nanoTime();

        for (double i = 0.0; i < noOfPuts; i++)
        {
            quoteMapKeyTemp.updateValues(quoteMapKeyTemp.getSupplier(), quoteMapKeyTemp.getSource(), id + (i % 200), quoteMapKeyTemp.getField());

//            System.out.println(quoteMapKeyTemp.getId());

            if (!marketDataCache.containsKey(quoteMapKeyTemp))
            {
                marketDataCache.put(new QuoteMapKey(quoteMapKeyTemp.getSupplier(), quoteMapKeyTemp.getSource(), id + (i % 200), quoteMapKeyTemp.getField()), i);
            }
            else
            {
                marketDataCache.put(quoteMapKeyTemp, i);
            }

//            System.out.println(marketDataCache.size());
        }

        return TestUtils.calculateAndPrintRuntime(startTime);
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

        return TestUtils.calculateAndPrintRuntime(startTime);
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

    private void setMarketDataMapToConcurrentHashMap()
    {
        marketDataCache = new ConcurrentHashMap<>();
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
}