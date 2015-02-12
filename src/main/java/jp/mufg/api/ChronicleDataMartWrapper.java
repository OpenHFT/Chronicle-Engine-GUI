package jp.mufg.api;

import jp.mufg.api.util.FromChronicle;
import jp.mufg.api.util.ToChronicle;
import net.openhft.chronicle.Chronicle;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ChronicleDataMartWrapper implements DataMartWrapper {
    @NotNull
    private final DataMart writer;
    private final DirectDataMart dataMart;
    private final Chronicle chronicle;
    private FromChronicle<DataMart> reader;

    public ChronicleDataMartWrapper(Chronicle chronicle,
                                    DirectDataMart dataMart) throws IOException {
        this.chronicle = chronicle;
        writer = ToChronicle.of(DirectDataMart.class, chronicle);
        this.dataMart = dataMart;
    }

    @Override
    public void start() {
        writer.startup(getTarget());
    }

    @Override
    public void stop() {
        writer.stopped(getTarget());
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
