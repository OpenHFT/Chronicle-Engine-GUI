package jp.mufg.api;

import jp.mufg.api.util.FromChronicle;
import jp.mufg.api.util.ToChronicle;
import net.openhft.chronicle.Chronicle;

import java.io.IOException;

public class ChronicleDataMart {
    private final String target;
    private final Chronicle chronicle;
    private final DataMart writer;
    private final FromChronicle<DataMart> reader;
    private final DataMart dataMart;

    public ChronicleDataMart(String target, Chronicle chronicle, DataMart dataMart) throws IOException {
        this.target = target;
        this.chronicle = chronicle;
        writer = ToChronicle.of(DataMart.class, chronicle);
        this.dataMart = dataMart;
        reader = FromChronicle.of(this.dataMart, chronicle.createTailer());
    }

    public boolean runOnce() {
        return reader.readOne();
    }

    public void onIdle() {
        if (dataMart.hasChanged())
            writer.calculate(target);
        else
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
    }
}
