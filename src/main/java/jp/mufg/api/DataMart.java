package jp.mufg.api;

public interface DataMart {
    void calculate(String target);

    default void startup(String target) {
    }

    void onUpdate(MarketDataUpdate quote);

    void addSubscription(Subscription subscription);

    void removeSubscription(Subscription subscription);

    default void stopped(String target) {
    }
}
