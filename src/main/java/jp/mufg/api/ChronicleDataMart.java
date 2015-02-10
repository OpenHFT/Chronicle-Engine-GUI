package jp.mufg.api;

import jp.mufg.api.util.FromChronicle;
import jp.mufg.api.util.ToChronicle;
import net.openhft.chronicle.Chronicle;

import java.io.IOException;

public class ChronicleDataMart {
    private final Chronicle chronicle;
    private final Calculator writer;
    private final FromChronicle<DataMart> reader;
    private final DataMart dataMart;

    public ChronicleDataMart(Chronicle chronicle, DataMart dataMart) throws IOException {
        this.chronicle = chronicle;
        writer = ToChronicle.of(Calculator.class, chronicle);
        this.dataMart = dataMart;
        reader = FromChronicle.of(this.dataMart, chronicle.createTailer());
    }

    public boolean runOnce() {
        return reader.readOne();
    }

    public void onIdle() {
        if (dataMart.hasChanged())
            writer.calculate();
    }
}
