package jp.mufg.api;

import net.openhft.lang.model.DataValueClasses;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class FilteringDataMartTest {

    public static Subscription newSubscription(String target, String source, String exchange, String instrument) {
        Subscription subscription = DataValueClasses.newInstance(Subscription.class);
        subscription.setTarget(target);
        subscription.setSource(source);
        subscription.setExchange(exchange);
        subscription.setInstrument(instrument);
        return subscription;
    }

    @Test
    public void testOnUpdate() throws Exception {
        MarketDataUpdate q1 = newQuote("source", "exchange", "instrument2", 10, 20, 10, 20);
        MarketDataUpdate q2 = newQuote("source2", "exchange2", "instrument2", 10, 20, 10, 20);
        MarketDataUpdate q3 = newQuote("source", "exchangeX", "instrument3", 10, 20, 10, 20);
        MarketDataUpdate q4 = newQuote("source", "exchange", "instrument", 10, 20, 10, 20);
        MarketDataUpdate q5 = newQuote(null, null, null, 10, 20, 10, 20);

        DataMart dataMart = createMock(DataMart.class);
        dataMart.onUpdate(q1);
        dataMart.onUpdate(q3);
        dataMart.onUpdate(q4);

        replay(dataMart);
        FilteringDataMart fdm = new FilteringDataMart("target", dataMart);
        fdm.addSubscription(newSubscription("target", "source", "exchange", "instrument2"));
        fdm.addSubscription(newSubscription("target", "source", null, "instrument3"));
        fdm.addSubscription(newSubscription("target", "source", null, "instrument"));
        fdm.addSubscription(newSubscription("no-target", null, null, null));

        fdm.onUpdate(q1);
        fdm.onUpdate(q2);
        fdm.onUpdate(q3);
        fdm.onUpdate(q4);
        fdm.onUpdate(q5);

        verify(dataMart);

        // test removals.
        reset(dataMart);
        dataMart.onUpdate(q4);

        replay(dataMart);
        fdm.removeSubscription(newSubscription("target", "source", "exchange", "instrument2"));
        fdm.removeSubscription(newSubscription("target", "source", null, "instrument3"));

        fdm.onUpdate(q1);
        fdm.onUpdate(q2);
        fdm.onUpdate(q3);
        fdm.onUpdate(q4);
        fdm.onUpdate(q5);

        verify(dataMart);
    }

    private MarketDataUpdate newQuote(String source, String exchange, String instrument,
                                      double bid, double ask, double bidq, double askq) {
        MarketDataUpdate update = DataValueClasses.newInstance(MarketDataUpdate.class);
        update.setSource(source);
        update.setExchange(exchange);
        update.setInstrument(instrument);
        update.setBid(bid);
        update.setBidq(bidq);
        update.setAsk(ask);
        update.setAskq(askq);
        return update;
    }
}