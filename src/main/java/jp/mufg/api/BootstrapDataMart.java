package jp.mufg.api;

import java.util.Map;

public class BootstrapDataMart implements DirectDataMart {
    private final Map<SourceExchangeInstrument, MarketDataUpdate> mduMap;
    private final Map<SubscriptionKey, Subscription> subMap;
    private final DataMart writer;

    public BootstrapDataMart(Map<SourceExchangeInstrument, MarketDataUpdate> mduMap,
                             Map<SubscriptionKey, Subscription> subMap,
                             DataMart writer) {
        this.mduMap = mduMap;
        this.subMap = subMap;
        this.writer = writer;
    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public String getTarget() {
        return "bootstrap";
    }

    @Override
    public void calculate(String target) {
    }

    @Override
    public void startup(String target) {
        for (Subscription subscription : subMap.values()) {
            if (subscription.getTarget().equals(target))
                writer.addSubscription(subscription);
        }
    }

    @Override
    public void onUpdate(MarketDataUpdate quote) {
        // ignore our retransmitted updates
        if (!quote.getRetransmit())
            mduMap.put(Util.seiFrom(quote), quote);
    }

    @Override
    public void addSubscription(Subscription subscription) {
        subMap.put(Util.keyFor(subscription), subscription);

        MarketDataUpdate mdu = mduMap.get(Util.seiFrom(subscription));
        if (mdu == null)
            return;
        mdu.setRetransmit(true);
        writer.onUpdate(mdu);
    }

    @Override
    public void removeSubscription(Subscription subscription) {
        subMap.remove(Util.keyFor(subscription), subscription);
    }
}
