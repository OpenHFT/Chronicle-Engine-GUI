package jp.mufg.api;

import net.openhft.lang.model.constraints.MaxSize;
import org.jetbrains.annotations.NotNull;

public interface SourceExchangeInstrument {
    @NotNull
    public String getSource();

    public void setSource(@MaxSize(16) String source);

    @NotNull
    public String getExchange();

    public void setExchange(@MaxSize(16) String exchange);

    @NotNull
    public String getInstrument();

    public void setInstrument(@MaxSize(16) String instrument);
}
