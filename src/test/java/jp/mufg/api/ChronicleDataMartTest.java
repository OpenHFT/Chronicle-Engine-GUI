package jp.mufg.api;

import jp.mufg.api.util.MetaData;
import jp.mufg.api.util.PrintAll;
import jp.mufg.api.util.ToChronicle;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.tools.ChronicleTools;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static jp.mufg.api.Util.newQuote;
import static jp.mufg.api.Util.newSubscription;
import static org.easymock.EasyMock.*;

public class ChronicleDataMartTest {
    @Test
    public void testHasChanged() throws IOException {
        String basePath = "testHasChanged-" + System.nanoTime();
        ChronicleTools.deleteDirOnExit(basePath);

        Chronicle chronicle = ChronicleQueueBuilder.vanilla(basePath).build();
        MetaData.setId(chronicle, (byte) 111);

        Map<SourceExchangeInstrument, MarketDataUpdate> marketDataMap = new HashMap<>();
        Calculator calculator = createMock(Calculator.class);
        calculator.calculate();
        replay(calculator);

        ChronicleDataMart chronicleDataMart = new ChronicleDataMart(chronicle,
                PrintAll.of(DataMart.class, new FilteringDataMart("target", marketDataMap, calculator)));

        DataMart writer = ToChronicle.of(DataMart.class, chronicle);
        writer.addSubscription(newSubscription("target", "source", "exchange", "instrument2"));
        writer.addSubscription(newSubscription("target", "source", null, "instrument3"));
        writer.addSubscription(newSubscription("target", "source", null, "instrument"));

        writer.onUpdate(newQuote("source", "exchange", "instrument", 10, 21, 10, 20));
        writer.onUpdate(newQuote("source", "exchange", "instrument2", 13, 22, 10, 20));
        writer.onUpdate(newQuote("source", "exchangeX", "instrument3", 16, 23, 10, 20));

        for (int i = 0; i < 2; i++) {
            while (chronicleDataMart.runOnce()) {

            }
            chronicleDataMart.onIdle();
        }
        verify(calculator);
        chronicle.close();
    }

}