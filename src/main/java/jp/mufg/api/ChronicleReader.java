package jp.mufg.api;

import jp.mufg.api.util.FromChronicle;
import jp.mufg.api.util.PrintAll;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;

import java.io.IOException;

public class ChronicleReader {
    static final String CHRONICLE_BASE = System.getProperty("chronicle", "engine-input");

    public static void main(String... ignored) throws IOException, InterruptedException {
        Chronicle chronicle = ChronicleQueueBuilder.vanilla(CHRONICLE_BASE).build();
        FromChronicle<DataMart> tailer = FromChronicle.of(PrintAll.of(DataMart.class), chronicle.createTailer());
        while (!Thread.interrupted()) {
            if (!tailer.readOne())
                Thread.sleep(50);
        }
        chronicle.close();
    }
}
