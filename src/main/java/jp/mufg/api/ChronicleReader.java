package jp.mufg.api;

import jp.mufg.api.util.PrintAll;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;

import java.io.IOException;

import static net.openhft.chronicle.core.Jvm.pause;

public class ChronicleReader {
    static final String CHRONICLE_BASE = System.getProperty("chronicle", "engine-input");

    public static void main(String... ignored) throws IOException, InterruptedException {
        Chronicle chronicle = ChronicleQueueBuilder.vanilla(CHRONICLE_BASE).build();
        ChronicleDataMartReader tailer = ChronicleDataMartReader.of(
                PrintAll.of(DataMart.class), chronicle.createTailer());
        while (!Thread.interrupted()) {
            if (!tailer.readOne())
                pause(50);
        }
        chronicle.close();
    }
}
