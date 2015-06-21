package jp.mufg.api;

import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.MaxSize;
import org.jetbrains.annotations.NotNull;

public interface MarketDataUpdate extends BytesMarshallable {
    @NotNull
    String getSource();

    void setSource(@MaxSize(16) String source);

    @NotNull
    String getExchange();

    void setExchange(@MaxSize(16) String exchange);

    @NotNull
    String getInstrument();

    void setInstrument(@MaxSize(16) String instrument);

    double getBid();

    void setBid(double bid);

    double getAsk();

    void setAsk(double ask);

    double getBidq();

    void setBidq(double bidq);

    double getAskq();

    void setAskq(double askq);

    boolean getRetransmit();

    void setRetransmit(boolean retransmit);

    long getMarketTimestamp();

    void setMarketTimestamp(long marketTimestamp);
}
