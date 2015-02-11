package jp.mufg.api;

import jp.mufg.api.util.ManyFromChronicle;
import jp.mufg.api.util.ToChronicle;
import net.openhft.chronicle.Chronicle;

import java.io.IOException;
import java.util.List;

public class ChronicleDataMart {
    private final DataMart writer;
    private final ManyFromChronicle<DataMart> reader;
    private final List<DataMart> instances;

    public ChronicleDataMart(Chronicle chronicle,
                             List<DataMart> instances) throws IOException {
        writer = ToChronicle.of(DataMart.class, chronicle);
        this.instances = instances;
        reader = ManyFromChronicle.of(this.instances, chronicle.createTailer());
    }

    public boolean runOnce() {
        return reader.readOne();
    }

    public boolean onIdle() {
        boolean busy = false;
        for (DataMart dataMart : instances) {
            if (dataMart.hasChanged()) {
                writer.calculate(dataMart.getTarget());
                busy = true;
            }
        }
        return busy;
    }
}
