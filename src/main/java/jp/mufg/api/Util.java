package jp.mufg.api;

import org.jetbrains.annotations.NotNull;

import static net.openhft.lang.model.DataValueClasses.newInstance;

// TODO these methods should be migrated to their classes once JLANG-54 is fixed
public enum Util {
    ;

    public static SubscriptionKey keyFor(Subscription subscription) {
        SubscriptionKey key = newInstance(SubscriptionKey.class);
        key.setTarget(subscription.getTarget());
        key.setSubscriptionId(subscription.getSubscriptionId());
        return key;
    }
    public static SourceExchangeInstrument seiFrom(@NotNull MarketDataUpdate quote) {
        String source = quote.getSource();
        String instrument = quote.getInstrument();
        String exchange = quote.getExchange();
        return seiOf(source, instrument, exchange);
    }

    public static SourceExchangeInstrument seiFrom(@NotNull Subscription subscription) {
        String source = subscription.getSource();
        String instrument = subscription.getInstrument();
        String exchange = subscription.getExchange();
        return seiOf(source, instrument, exchange);
    }

    public static SourceExchangeInstrument seiOf(@NotNull String source, @NotNull String instrument, @NotNull String exchange) {
        SourceExchangeInstrument key = newInstance(SourceExchangeInstrument.class);
        key.setSource(source);
        key.setInstrument(instrument);
        key.setExchange(exchange);
        return key;
    }

    public static Subscription newSubscription(@NotNull String target, @NotNull String subscriptionId,
                                               @NotNull String source, @NotNull String exchange, @NotNull String instrument) {
        Subscription subscription = newInstance(Subscription.class);
        subscription.setTarget(target);
        subscription.setSubscriptionId(subscriptionId);
        subscription.setSource(source);
        subscription.setExchange(exchange);
        subscription.setInstrument(instrument);
        return subscription;
    }

    static MarketDataUpdate newQuote(MarketDataUpdate update,
                                     @NotNull String source, @NotNull String exchange,
                                     @NotNull String instrument,
                                     double bid, double ask, double bidq, double askq) {
        update.setSource(source);
        update.setExchange(exchange);
        update.setInstrument(instrument);
        update.setBid(bid);
        update.setBidq(bidq);
        update.setAsk(ask);
        update.setAskq(askq);
        return update;
    }

    static MarketDataUpdate newQuote(@NotNull String source, @NotNull String exchange,
                                     @NotNull String instrument,
                                     double bid, double ask, double bidq, double askq) {
        MarketDataUpdate update = newInstance(MarketDataUpdate.class);
        update.setSource(source);
        update.setExchange(exchange);
        update.setInstrument(instrument);
        update.setBid(bid);
        update.setBidq(bidq);
        update.setAsk(ask);
        update.setAskq(askq);
        return update;
    }

    public static SourceExchangeInstrument copyTo(MarketDataUpdate quote, SourceExchangeInstrument sei) {
        sei.setSource(quote.getSource());
        sei.setInstrument(quote.getInstrument());
        sei.setExchange(quote.getExchange());
        return sei;
    }
}
