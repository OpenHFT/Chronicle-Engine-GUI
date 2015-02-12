package jp.mufg.api;

import jp.mufg.api.util.MetaData;
import jp.mufg.api.util.ToChronicle;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EngineMain {
    static final String CHRONICLE_BASE = System.getProperty("chronicle", "engine-input");
    static final String TARGET = System.getProperty("target", "target");
    static final String CALCULATORS = System.getProperty("calculators");

    public static void main(String... ignored) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Chronicle chronicle = ChronicleQueueBuilder.vanilla(CHRONICLE_BASE).build();
        MetaData.setId(chronicle, (byte) 1);

        DataMartEngine engine = new DataMartEngine();
        // create the boot strap
        Map<SourceExchangeInstrument, MarketDataUpdate> mduMap = new HashMap<>();
        Map<SubscriptionKey, Subscription> subMap = new HashMap<>();

        DataMart writer = ToChronicle.of(DataMart.class, chronicle);
        BootstrapDataMart bootstrap = new BootstrapDataMart(mduMap, subMap, writer);
        engine.add(new ChronicleDataMartWrapper(chronicle, bootstrap));

        for (String name : CALCULATORS.split(",")) {
            Calculator calculator = (Calculator) Class.forName(name)
                    .newInstance();
            DirectDataMart dataMart = new FilteringDataMart(TARGET, new HashMap<>(), calculator);
            engine.add(new ChronicleDataMartWrapper(chronicle, dataMart));
        }
    }
}
