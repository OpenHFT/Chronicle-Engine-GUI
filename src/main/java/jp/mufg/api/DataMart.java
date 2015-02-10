package jp.mufg.api;

public interface DataMart {
    public void onUpdate(MarketDataUpdate quote);

    public void addSubscription(Subscription subscription);

    public void removeSubscription(Subscription subscription);
}
