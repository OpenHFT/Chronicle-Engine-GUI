package jp.mufg.api;

public interface DataMart {
    public void calculate(String target);

    public boolean hasChanged();

    public String getTarget();

    public void onUpdate(MarketDataUpdate quote);

    public void addSubscription(Subscription subscription);

    public void removeSubscription(Subscription subscription);
}
