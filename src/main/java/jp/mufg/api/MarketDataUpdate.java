package jp.mufg.api;

import net.openhft.lang.model.constraints.MaxSize;

public interface MarketDataUpdate {
    public String getSource();

    public void setSource(@MaxSize(16) String source);

    public String getExchange();

    public void setExchange(@MaxSize(16) String exchange);

    public String getInstrument();

    public void setInstrument(@MaxSize(16) String instrument);

    public double getBid();

    public void setBid(double bid);

    public double getAsk();

    public void setAsk(double ask);

    public double getBidq();

    public void setBidq(double bidq);

    public double getAskq();

    public void setAskq(double askq);

    public long getMarketTimestamp();

    public void setMarketTimestamp(long marketTimestamp);
}
