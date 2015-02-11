package jp.mufg.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Subscription {
    @NotNull
    String getTarget();
    void setTarget(String target);

    @NotNull
    String getSource();
    void setSource(@NotNull String source);

    @NotNull
    String getExchange();
    void setExchange(@Nullable String exchange);

    @NotNull
    String getInstrument();
    void setInstrument(@Nullable String instrument);

    // @GroupId
    @NotNull
    String getSubscriptionId();

    void setSubscriptionId(String subscriptionId);
}
