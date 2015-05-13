package jp.mufg.api;

import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.MaxSize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Subscription extends BytesMarshallable {
    @NotNull
    String getTarget();

    void setTarget(@MaxSize(64) String target);

    @NotNull
    String getSource();

    void setSource(@MaxSize(64) @NotNull String source);

    @NotNull
    String getExchange();

    void setExchange(@MaxSize(64) @Nullable String exchange);

    @NotNull
    String getInstrument();

    void setInstrument(@MaxSize(64) @Nullable String instrument);

    // @GroupId
    @NotNull
    String getSubscriptionId();

    void setSubscriptionId(@MaxSize(64) String subscriptionId);

    public boolean getRetransmit();

    public void setRetransmit(boolean retransmit);
}
