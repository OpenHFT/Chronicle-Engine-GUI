package jp.mufg.api;

import org.jetbrains.annotations.NotNull;

public interface SubscriptionKey {
    @NotNull
    String getTarget();

    void setTarget(String target);

    @NotNull
    String getSubscriptionId();

    void setSubscriptionId(@NotNull String subscriptionId);
}
