package jp.mufg.api;

import net.openhft.lang.model.constraints.MaxSize;
import org.jetbrains.annotations.NotNull;

public interface SourceExchangeInstrument {
    @NotNull
    String getSource();

    void setSource(@MaxSize(16) String source);

    @NotNull
    String getExchange();

    void setExchange(@MaxSize(16) String exchange);

    @NotNull
    String getInstrument();

    void setInstrument(@MaxSize(16) String instrument);
}
