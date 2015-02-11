package jp.mufg.api;

import org.junit.Test;

import java.util.Map;

import static jp.mufg.api.Util.newSubscription;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilteringDataMartTest {

    @Test
    public void testOnUpdate() throws Exception {
        MarketDataUpdate q1 = Util.newQuote("source", "exchange", "instrument2", 10, 20, 10, 20);
        MarketDataUpdate q2 = Util.newQuote("source2", "exchange2", "instrument2", 10, 20, 10, 20);
        MarketDataUpdate q3 = Util.newQuote("source", "exchangeX", "instrument3", 10, 20, 10, 20);
        MarketDataUpdate q4 = Util.newQuote("source", "exchange", "instrument", 10, 20, 10, 20);
        MarketDataUpdate q5 = Util.newQuote(null, null, null, 10, 20, 10, 20);

        Calculator calculator = createMock(Calculator.class);
        Map<SourceExchangeInstrument, MarketDataUpdate> marketDataMap = createMock(Map.class);
        expect(marketDataMap.put(Util.seiFrom(q1), q1)).andReturn(null);
        expect(marketDataMap.put(Util.seiFrom(q3), q3)).andReturn(null);
        expect(marketDataMap.put(Util.seiFrom(q4), q4)).andReturn(null);

        calculator.calculate();

        replay(calculator);
        replay(marketDataMap);
        FilteringDataMart fdm = new FilteringDataMart("target", marketDataMap, calculator);

        // boot strap
        fdm.calculate();

        fdm.addSubscription(newSubscription("target", "source", "exchange", "instrument2"));
        fdm.addSubscription(newSubscription("target", "source", null, "instrument3"));
        fdm.addSubscription(newSubscription("target", "source", null, "instrument"));
        fdm.addSubscription(newSubscription("no-target", null, null, null));

        assertFalse(fdm.hasChanged());

        fdm.onUpdate(q1);
        fdm.onUpdate(q2);
        fdm.onUpdate(q3);
        fdm.onUpdate(q4);
        fdm.onUpdate(q5);

        assertTrue(fdm.hasChanged());
        fdm.calculate();
        assertFalse(fdm.hasChanged());

        verify(calculator);
        verify(marketDataMap);

        // test removals.
        reset(calculator);
        reset(marketDataMap);
        expect(marketDataMap.put(Util.seiFrom(q4), q4)).andReturn(null);
        calculator.calculate();

        replay(calculator);
        replay(marketDataMap);
        fdm.removeSubscription(newSubscription("target", "source", "exchange", "instrument2"));
        fdm.removeSubscription(newSubscription("target", "source", null, "instrument3"));

        fdm.onUpdate(q1);
        fdm.onUpdate(q2);
        fdm.onUpdate(q3);
        fdm.onUpdate(q4);
        fdm.onUpdate(q5);

        assertTrue(fdm.hasChanged());
        fdm.calculate();
        assertFalse(fdm.hasChanged());

        verify(marketDataMap);
        verify(calculator);
    }
}