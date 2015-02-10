package jp.mufg.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Subscription {
    String getTarget();

    void setTarget(String target);

    String getSource();

    void setSource(@NotNull String source);

    String getExchange();

    void setExchange(@Nullable String exchange);

    String getInstrument();

    void setInstrument(@Nullable String instrument);
}
