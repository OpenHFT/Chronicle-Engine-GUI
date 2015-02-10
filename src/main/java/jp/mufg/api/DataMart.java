package jp.mufg.api;

public interface DataMart extends Calculator {
    public boolean hasChanged();

    public void onUpdate(MarketDataUpdate quote);

    public void addSubscription(Subscription subscription);

    public void removeSubscription(Subscription subscription);
}
