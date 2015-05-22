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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

// TODO need to add expected performance measures.
public class ChronicleMapPutPerformanceTest {
    private static final long RUN_TIME = (long) 5e9;
    private String chronicleMapFile = Jvm.TMP + "/chroniclemap";
    private File file;
    private int noOfPuts = 10_000_000;
    private Map<QuoteMapKey, Double> marketDataCache;
    static final QuoteMapKey[] quoteMapKey = {
            generateExampleQuoteMapKey1(),
            generateExampleQuoteMapKey1b(),
            generateExampleQuoteMapKey2(),
            generateExampleQuoteMapKey2b(),
            generateExampleQuoteMapKey3(),
            generateExampleQuoteMapKey3b(),
            generateExampleQuoteMapKey4(),
            generateExampleQuoteMapKey4b()};

    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp() throws Exception {
        System.out.print("Test " + name.getMethodName() + " - ");
        ChronicleTools.deleteDirOnExit(chronicleMapFile);

        file = new File(chronicleMapFile);
    }

    @After
    public void tearDown() throws Exception {
        ChronicleTools.deleteDirOnExit(chronicleMapFile);
    }

    @Test
    public void testCompareMapPuts() throws IOException {
        //Put to Chronicle
        setMarketDataMapToChronicle();

        long putChronicleRuntime = putConfiguredNumberOfKeyValues();

        //Replace in Chronicle
        long replaceChronicleRuntime = replaceConfiguredNumberOfKeyValues();

        //Put to hash map
        setMarketDataMapToHashMap();

        long putHashMapRuntime = putConfiguredNumberOfKeyValues();

        System.out.println("Ratio chronicle put / replace: " + 100 * putChronicleRuntime / replaceChronicleRuntime / 100.0);
        System.out.println("Ratio chronicle put / hash put: " + 100 * putChronicleRuntime / putHashMapRuntime / 100.0);
        System.out.println("Ratio chronicle replace / hash put: " + 100 * replaceChronicleRuntime / putHashMapRuntime / 100.0);
    }

    @Test
    public void testChronicleMapPuts() throws IOException {
        setMarketDataMapToChronicle();

        putConfiguredNumberOfKeyValues();
    }

    @Test
    public void testChronicleMapReplace() throws IOException {
        setMarketDataMapToChronicle();

        replaceConfiguredNumberOfKeyValues();
    }

    @Test
    public void testHashMapPuts() throws IOException {
        setMarketDataMapToHashMap();

        putConfiguredNumberOfKeyValues();
    }

    @Test
    public void testConcurrentHashMapPuts() throws IOException {
        setMarketDataMapToConcurrentHashMap();

        putConfiguredNumberOfKeyValues();
    }

    @Test
    public void testConcurrentHashMapPutsMany() throws IOException {
        setMarketDataMapToConcurrentHashMap();

        putConfiguredNumberOfKeyValuesMany();
    }

    private long putConfiguredNumberOfKeyValues() {
        long startTime = System.nanoTime();
        int count = 0;
        while (System.nanoTime() - startTime < RUN_TIME) {
            count++;

            IntStream.range(0, noOfPuts).parallel().forEach(i -> {
                QuoteMapKey key = quoteMapKey[i % quoteMapKey.length];
                marketDataCache.put(key, (double) i);
            });
        }
        return TestUtils.calculateAndPrintRuntime(startTime, count);
    }

    private long putConfiguredNumberOfKeyValuesMany() {
        QuoteMapKey quoteMapKeyTemp = generateExampleQuoteMapKey1();
        String id = "MyId";

        long startTime = System.nanoTime();

        int count = 0;
        while (System.nanoTime() - startTime < RUN_TIME) {
            count++;

            IntStream.range(0, noOfPuts).parallel().forEach(i -> {
                quoteMapKeyTemp.updateValues(quoteMapKeyTemp.getSupplier(), quoteMapKeyTemp.getSource(), id + (i % 200), quoteMapKeyTemp.getField());

//            System.out.println(quoteMapKeyTemp.getId());

                if (!marketDataCache.containsKey(quoteMapKeyTemp)) {
                    marketDataCache.put(new QuoteMapKey(quoteMapKeyTemp.getSupplier(), quoteMapKeyTemp.getSource(), id + (i % 200), quoteMapKeyTemp.getField()), (double) i);

                } else {
                    marketDataCache.put(quoteMapKeyTemp, (double) i);
                }

//            System.out.println(marketDataCache.size());
            });
        }
        return TestUtils.calculateAndPrintRuntime(startTime, count);
    }

    private long replaceConfiguredNumberOfKeyValues() {
        for (QuoteMapKey key : quoteMapKey)
            marketDataCache.put(key, 0.0);

        long startTime = System.nanoTime();

        int count = 0;
        while (System.nanoTime() - startTime < RUN_TIME) {
            count++;

            IntStream.range(0, noOfPuts).parallel().forEach(i -> {
                QuoteMapKey key = quoteMapKey[i % quoteMapKey.length];
                marketDataCache.replace(key, (double) i);
            });
        }

        return TestUtils.calculateAndPrintRuntime(startTime, count);
    }

    private void setMarketDataMapToChronicle() throws IOException {
        marketDataCache = ChronicleMapBuilder
                .of(QuoteMapKey.class, Double.class).
                        putReturnsNull(true).
                        removeReturnsNull(true)
                .createPersistedTo(file);
    }

    private void setMarketDataMapToHashMap() {
        marketDataCache = new HashMap<>();
    }

    private void setMarketDataMapToConcurrentHashMap() {
        marketDataCache = new ConcurrentHashMap<>();
    }

    private static QuoteMapKey generateExampleQuoteMapKey1() {
        MarketDataSupplier supplier = MarketDataSupplier.BROADWAY;
        MarketDataSource source = MarketDataSource.BLOOMBERG;
        String id = "Id1";
        MarketDataField field = MarketDataField.ASK_PRICE;

        return new QuoteMapKey(supplier, source, id, field);
    }

    private static QuoteMapKey generateExampleQuoteMapKey1b() {
        MarketDataSupplier supplier = MarketDataSupplier.BROADWAY;
        MarketDataSource source = MarketDataSource.BLOOMBERG;
        String id = "Id1";
        MarketDataField field = MarketDataField.BID_PRICE;

        return new QuoteMapKey(supplier, source, id, field);
    }

    private static QuoteMapKey generateExampleQuoteMapKey2() {
        MarketDataSupplier supplier = MarketDataSupplier.REUTERS;
        MarketDataSource source = MarketDataSource.CME;
        String id = "Id2";
        MarketDataField field = MarketDataField.ASK_PRICE;

        return new QuoteMapKey(supplier, source, id, field);
    }

    private static QuoteMapKey generateExampleQuoteMapKey2b() {
        MarketDataSupplier supplier = MarketDataSupplier.REUTERS;
        MarketDataSource source = MarketDataSource.CME;
        String id = "Id2";
        MarketDataField field = MarketDataField.BID_PRICE;

        return new QuoteMapKey(supplier, source, id, field);
    }

    private static QuoteMapKey generateExampleQuoteMapKey3() {
        MarketDataSupplier supplier = MarketDataSupplier.INTERNAL_MODEL;
        MarketDataSource source = MarketDataSource.CME;
        String id = "Id3";
        MarketDataField field = MarketDataField.ASK_PRICE;

        return new QuoteMapKey(supplier, source, id, field);
    }

    private static QuoteMapKey generateExampleQuoteMapKey3b() {
        MarketDataSupplier supplier = MarketDataSupplier.INTERNAL_MODEL;
        MarketDataSource source = MarketDataSource.CME;
        String id = "Id3";
        MarketDataField field = MarketDataField.BID_PRICE;

        return new QuoteMapKey(supplier, source, id, field);
    }

    private static QuoteMapKey generateExampleQuoteMapKey4() {
        MarketDataSupplier supplier = MarketDataSupplier.BLOOMBERG;
        MarketDataSource source = MarketDataSource.BLOOMBERG;
        String id = "Id4";
        MarketDataField field = MarketDataField.ASK_PRICE;

        return new QuoteMapKey(supplier, source, id, field);
    }

    private static QuoteMapKey generateExampleQuoteMapKey4b() {
        MarketDataSupplier supplier = MarketDataSupplier.BLOOMBERG;
        MarketDataSource source = MarketDataSource.BLOOMBERG;
        String id = "Id4";
        MarketDataField field = MarketDataField.BID_PRICE;

        return new QuoteMapKey(supplier, source, id, field);
    }
}