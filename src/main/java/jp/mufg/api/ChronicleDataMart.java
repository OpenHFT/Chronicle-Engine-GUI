package jp.mufg.api;

import jp.mufg.api.util.FromChronicle;
import jp.mufg.api.util.ToChronicle;
import net.openhft.chronicle.Chronicle;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ChronicleDataMart implements DataMartWrapper {
    @NotNull
    private final DataMart writer;
    private final DirectDataMart dataMart;
    private final Chronicle chronicle;
    private FromChronicle<DataMart> reader;

    public ChronicleDataMart(Chronicle chronicle,
                             DirectDataMart dataMart) throws IOException {
        this.chronicle = chronicle;
        writer = ToChronicle.of(DirectDataMart.class, chronicle);
        this.dataMart = dataMart;
    }

    @Override
    public boolean runOnce() {
        try {
            if (reader == null)
                reader = FromChronicle.of(dataMart, chronicle.createTailer());
            return reader.readOne();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public boolean onIdle() {
        boolean busy = false;
            if (dataMart.hasChanged()) {
                writer.calculate(dataMart.getTarget());
                busy = true;
            }
        return busy;
    }

    @Override
    public String getTarget() {
        return dataMart.getTarget();
    }
}
