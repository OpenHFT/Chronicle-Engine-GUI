package jp.mufg.api;

import net.openhft.chronicle.Chronicle;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static jp.mufg.api.ChronicleDataMartWrapperTest.createChronicle;
import static jp.mufg.api.Util.newQuote;
import static jp.mufg.api.Util.newSubscription;
import static org.easymock.EasyMock.*;

public class DataMartEngineTest {

    static void addCalculator(String target, @NotNull DataMartEngine engine, Chronicle chronicle, @NotNull Calculator calculator) throws IOException {
        Map<String, MarketDataUpdate> marketDataMap = new HashMap<>();

        calculator.calculate();
        replay(calculator);

        FilteringDataMart fdm = new FilteringDataMart(target,
                marketDataMap, calculator);
        DirectDataMart dataMart = fdm; //PrintAll.of(DirectDataMart.class, fdm);
        engine.add(new ChronicleDataMartWrapper(chronicle, dataMart));
    }

    static void addCalculatorPerf(String target, @NotNull DataMartEngine engine, Chronicle chronicle, @NotNull Calculator calculator) throws IOException {
        Map<String, MarketDataUpdate> marketDataMap = new HashMap<>();

        calculator.calculate();

        FilteringDataMart fdm = new FilteringDataMart(target,
                marketDataMap, calculator);
        DirectDataMart dataMart = fdm; //PrintAll.of(DirectDataMart.class, fdm);
        engine.add(new ChronicleDataMartWrapper(chronicle, dataMart));
    }

    @Test
    public void testAdd() throws Exception {
        DataMartEngine engine = new DataMartEngine();

        Chronicle chronicle = createChronicle("testAdd");

        Calculator calculator = createMock(Calculator.class);
        addCalculator("target", engine, chronicle, calculator);

        Calculator calculator2 = createMock(Calculator.class);
        addCalculator("target2", engine, chronicle, calculator2);

        DataMart writer = new ChronicleDataMartWriter(chronicle);
        writer.addSubscription(newSubscription("target", "one", "source", "exchange", "instrument2"));
        writer.addSubscription(newSubscription("target", "two", "source", "exchange", "instrument3"));
        writer.addSubscription(newSubscription("target", "three", "source", "exchange", "instrument"));

        writer.addSubscription(newSubscription("target2", "one", "source", "exchange", "instrument2"));

        writer.onUpdate(newQuote("source", "exchange", "instrument", 10, 21, 10, 20));
        writer.onUpdate(newQuote("source", "exchange", "instrument2", 13, 22, 10, 20));
        writer.onUpdate(newQuote("source", "exchangeX", "instrument3", 16, 23, 10, 20));

        for (int i = 0; i < 20; i++) {
            Thread.sleep(100);
            try {
                verify(calculator);
                verify(calculator2);
                break;
            } catch (AssertionError keepTrying) {

            }
        }

        engine.shutdown();
        verify(calculator);
        verify(calculator2);
    }

    @Test
    @Ignore("long running test")
    public void testAddPerf() throws Exception {
        DataMartEngine engine = new DataMartEngine();

        Chronicle chronicle = createChronicle("testAdd");

        Calculator calculator = new Calculator() {
            AtomicLong counter = new AtomicLong();
            @Override
            public void calculate() {
                counter.incrementAndGet();
            }
        };

        Calculator calculator2 = new Calculator() {
            AtomicLong counter = new AtomicLong();
            @Override
            public void calculate() {
                counter.incrementAndGet();
            }
        };

        addCalculatorPerf("target", engine, chronicle, calculator);

        addCalculatorPerf("target2", engine, chronicle, calculator2);

        DataMart writer = new ChronicleDataMartWriter(chronicle);
        for(int j=0; j<3; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10_000_000; i += 3) {
                if (i % 1000 == 0) {
                    writer.addSubscription(newSubscription("target", "one", "source", "exchange", "instrument2"));
                    writer.addSubscription(newSubscription("target", "two", "source", "exchange", "instrument3"));
                    writer.addSubscription(newSubscription("target", "three", "source", "exchange", "instrument"));

                    writer.addSubscription(newSubscription("target2", "one", "source", "exchange", "instrument2"));
                }

                writer.onUpdate(newQuote("source", "exchange", "instrument", 10, 21, 10, 20));
                writer.onUpdate(newQuote("source", "exchange", "instrument2", 13, 22, 10, 20));
                writer.onUpdate(newQuote("source", "exchangeX", "instrument3", 16, 23, 10, 20));
            }
            System.out.println("Time for iteration " + j + " was " + (System.currentTimeMillis()-start));
        }


        engine.shutdown();

    }
}