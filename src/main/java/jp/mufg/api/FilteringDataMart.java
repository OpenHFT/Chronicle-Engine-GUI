package jp.mufg.api;

import net.openhft.lang.model.DataValueClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FilteringDataMart implements DataMart {
    static final Logger LOGGER = LoggerFactory.getLogger(FilteringDataMart.class);
    final String target;
    final Map<String, SubscriptionSet> sources = new HashMap<>();
    private final Map<SourceExchangeInstrument, MarketDataUpdate> marketDataMap;
    private final DataMart dataMart;

    public FilteringDataMart(String target, Map<SourceExchangeInstrument, MarketDataUpdate> marketDataMap, DataMart dataMart) {
        this.target = target;
        this.marketDataMap = marketDataMap;
        this.dataMart = dataMart;
    }

    @Override
    public void onUpdate(MarketDataUpdate quote) {
        SubscriptionSet subscriptionSet = sources.get(quote.getSource());
        if (subscriptionSet == null || !subscriptionSet.matches(quote.getExchange(), quote.getInstrument()))
            return;
        SourceExchangeInstrument key = DataValueClasses.newInstance(SourceExchangeInstrument.class);
        key.setSource(quote.getSource());
        key.setInstrument(quote.getInstrument());
        key.setExchange(quote.getExchange());
        marketDataMap.put(key, quote);
        dataMart.onUpdate(quote);
    }

    @Override
    public void addSubscription(Subscription subscription) {
        if (!subscription.getTarget().equals(target))
            return;

        String source = subscription.getSource();
        SubscriptionSet subscriptions = sources.computeIfAbsent(source, s -> new SubscriptionSet());
        subscriptions.add(subscription);
    }

    @Override
    public void removeSubscription(Subscription subscription) {
        SubscriptionSet subscriptionSet = sources.get(subscription.getSource());
        if (subscriptionSet == null)
            return;
        subscriptionSet.remove(subscription);
    }

    class SubscriptionSet {
        Set<String> allExchangesForInstrument = new HashSet<>();
        Map<String, Set<String>> exchangesForInstrument = new HashMap<>();

        public void add(Subscription subscription) {
            String instrument = subscription.getInstrument();
            String exchange = subscription.getExchange();
            if (exchange == null) {
                exchangesForInstrument.remove(instrument);
                allExchangesForInstrument.add(instrument);
            } else if (!allExchangesForInstrument.contains(instrument)) {
                Set<String> exchanges = exchangesForInstrument.computeIfAbsent(instrument, i -> new HashSet<>());
                exchanges.add(exchange);
            }
        }

        public boolean matches(String exchange, String instrument) {
            if (allExchangesForInstrument.contains(instrument))
                return true;
            Set<String> exchanges = exchangesForInstrument.get(instrument);
            return exchanges != null && exchanges.contains(exchange);
        }

        public void remove(Subscription subscription) {
            String instrument = subscription.getInstrument();
            String exchange = subscription.getExchange();
            if (exchange == null) {
                allExchangesForInstrument.remove(instrument);
                exchangesForInstrument.remove(instrument);
            } else {
                Set<String> exchanges = exchangesForInstrument.get(instrument);
                if (exchanges != null) {
                    exchanges.remove(exchange);
                    if (exchanges.isEmpty())
                        exchangesForInstrument.remove(instrument);
                }
                if (allExchangesForInstrument.contains(instrument)) {
                    LOGGER.warn("Attempt to remove " + instrument + " for " + exchange + " when it is in all exchanges");
                }
            }
        }
    }
}


