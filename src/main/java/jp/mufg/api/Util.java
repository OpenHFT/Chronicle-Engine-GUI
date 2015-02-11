package jp.mufg.api;

import net.openhft.lang.model.DataValueClasses;

public enum Util {
    ;

    public static SourceExchangeInstrument seiFrom(MarketDataUpdate quote) {
        String source = quote.getSource();
        String instrument = quote.getInstrument();
        String exchange = quote.getExchange();
        return seiOf(source, instrument, exchange);
    }

    public static SourceExchangeInstrument seiOf(String source, String instrument, String exchange) {
        SourceExchangeInstrument key = DataValueClasses.newInstance(SourceExchangeInstrument.class);
        key.setSource(source);
        key.setInstrument(instrument);
        key.setExchange(exchange);
        return key;
    }

    public static Subscription newSubscription(String target, String source, String exchange, String instrument) {
        Subscription subscription = DataValueClasses.newInstance(Subscription.class);
        subscription.setTarget(target);
        subscription.setSource(source);
        subscription.setExchange(exchange);
        subscription.setInstrument(instrument);
        return subscription;
    }

    static MarketDataUpdate newQuote(String source, String exchange, String instrument,
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
    // TODO these methods should be migrated to their classes once JLANG-54 is fixed

}
