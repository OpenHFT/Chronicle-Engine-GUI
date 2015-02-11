package jp.mufg.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FilteringDataMart implements DirectDataMart {
    final String target;
    final Map<SourceExchangeInstrument, String> sourceToId = new HashMap<>();
    final Map<String, SourceExchangeInstrument> idToSource = new HashMap<>();

    private final Map<String, MarketDataUpdate> marketDataMap;
    private final Calculator calculator;
    private boolean changed = false;

    public FilteringDataMart(String target,
                             Map<String, MarketDataUpdate> marketDataMap,
                             Calculator calculator) {
        this.target = target;
        this.marketDataMap = marketDataMap;
        this.calculator = calculator;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public void onUpdate(@NotNull MarketDataUpdate quote) {
        SourceExchangeInstrument sei = Util.seiFrom(quote);
        String generalId = sourceToId.get(sei);
        if (generalId == null)
            return;
        MarketDataUpdate previous = marketDataMap.get(generalId);
        if (newer(previous, quote)) {
            marketDataMap.put(generalId, quote);
            changed = true;
        }
    }

    private boolean newer(@Nullable MarketDataUpdate previous, @NotNull MarketDataUpdate quote) {
        return previous == null || !quote.getRetransmit();
    }

    public boolean hasChanged() {
        return changed;
    }

    @Override
    public void calculate(String target) {
        if (!this.target.equals(target))
            return;
        if (changed)
            calculator.calculate();
        changed = false;
    }

    @Override
    public void addSubscription(@NotNull Subscription subscription) {
        if (!subscription.getTarget().equals(target))
            return;

        String subscriptionId = subscription.getSubscriptionId();
        remove(subscriptionId);
        SourceExchangeInstrument sei = Util.seiOf(subscription.getSource(),
                subscription.getInstrument(), subscription.getExchange());
        try {
            sourceToId.put(sei, subscriptionId);
        } catch (Exception e) {
            System.out.println("sei: " + sei);
            throw e;
        }
        idToSource.put(subscriptionId, sei);
    }

    @Override
    public void removeSubscription(@NotNull Subscription subscription) {
        if (!subscription.getTarget().equals(target))
            return;

        String subscriptionId = subscription.getSubscriptionId();
        remove(subscriptionId);
    }

    private void remove(String subscriptionId) {
        SourceExchangeInstrument sei = idToSource.remove(subscriptionId);
        if (sei != null)
            sourceToId.remove(sei);
    }
}


