package jp.mufg.api;

import net.openhft.lang.model.constraints.MaxSize;

public interface SourceExchangeInstrument {
    public String getSource();

    public void setSource(@MaxSize(16) String source);

    public String getExchange();

    public void setExchange(@MaxSize(16) String exchange);

    public String getInstrument();

    public void setInstrument(@MaxSize(16) String instrument);

}
