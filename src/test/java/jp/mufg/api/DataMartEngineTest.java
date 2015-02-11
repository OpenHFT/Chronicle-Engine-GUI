package jp.mufg.api;

import jp.mufg.api.util.ToChronicle;
import net.openhft.chronicle.Chronicle;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static jp.mufg.api.ChronicleDataMartTest.createChronicle;
import static jp.mufg.api.Util.newQuote;
import static jp.mufg.api.Util.newSubscription;
import static org.easymock.EasyMock.*;

public class DataMartEngineTest {

    @Test
    public void testAdd() throws Exception {
        DataMartEngine engine = new DataMartEngine();

        Chronicle chronicle = createChronicle("testAdd");

        Map<SourceExchangeInstrument, MarketDataUpdate> marketDataMap = new HashMap<>();
        Calculator calculator = createMock(Calculator.class);
        calculator.calculate();
        replay(calculator);

        FilteringDataMart fdm = new FilteringDataMart("target",
                marketDataMap, calculator);
        DirectDataMart dataMart = fdm; //PrintAll.of(DirectDataMart.class, fdm);
        engine.add(new ChronicleDataMart(chronicle, dataMart));

        DataMart writer = ToChronicle.of(DirectDataMart.class, chronicle);
        writer.addSubscription(newSubscription("target", "one", "source", "exchange", "instrument2"));
        writer.addSubscription(newSubscription("target", "two", "source", null, "instrument3"));
        writer.addSubscription(newSubscription("target", "three", "source", null, "instrument"));

        writer.onUpdate(newQuote("source", "exchange", "instrument", 10, 21, 10, 20));
        writer.onUpdate(newQuote("source", "exchange", "instrument2", 13, 22, 10, 20));
        writer.onUpdate(newQuote("source", "exchangeX", "instrument3", 16, 23, 10, 20));

        for (int i = 0; i < 10; i++) {
            Thread.sleep(100);
            try {
                verify(calculator);
                break;
            } catch (AssertionError keepTrying) {

            }
        }

        engine.shutdown();
        verify(calculator);
    }
}