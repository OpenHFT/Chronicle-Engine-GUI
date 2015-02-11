package jp.mufg.api;

import net.openhft.lang.model.DataValueClasses;
import org.jetbrains.annotations.NotNull;

// TODO these methods should be migrated to their classes once JLANG-54 is fixed
public enum Util {
    ;

    public static SourceExchangeInstrument seiFrom(@NotNull MarketDataUpdate quote) {
        String source = quote.getSource();
        String instrument = quote.getInstrument();
        String exchange = quote.getExchange();
        return seiOf(source, instrument, exchange);
    }

    public static SourceExchangeInstrument seiOf(@NotNull String source, @NotNull String instrument, @NotNull String exchange) {
        SourceExchangeInstrument key = DataValueClasses.newInstance(SourceExchangeInstrument.class);
        key.setSource(source);
        key.setInstrument(instrument);
        key.setExchange(exchange);
        return key;
    }

    public static Subscription newSubscription(@NotNull String target, @NotNull String subscriptionId,
                                               @NotNull String source, @NotNull String exchange, @NotNull String instrument) {
        Subscription subscription = DataValueClasses.newInstance(Subscription.class);
        subscription.setTarget(target);
        subscription.setSubscriptionId(subscriptionId);
        subscription.setSource(source);
        subscription.setExchange(exchange);
        subscription.setInstrument(instrument);
        return subscription;
    }

    static MarketDataUpdate newQuote(@NotNull String source, @NotNull String exchange,
                                     @NotNull String instrument,
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
