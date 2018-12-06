package jp.mufg.api;

import jp.mufg.api.util.MetaData;
import net.openhft.chronicle.ExcerptTailer;
import org.jetbrains.annotations.NotNull;

import static net.openhft.lang.model.DataValueClasses.newInstance;

public class ChronicleDataMartReader {
    //TODO Reuse this object when ChronicleMap is used.
//    final MarketDataUpdate mdu = DataValueClasses.newInstance(MarketDataUpdate.class);
    final Subscription sub = newInstance(Subscription.class);
    @NotNull
    private final DataMart instance;
    private final ExcerptTailer tailer;

    private ChronicleDataMartReader(@NotNull DataMart instance, ExcerptTailer tailer) {
        this.instance = instance;
        this.tailer = tailer;
    }

    @NotNull
    public static ChronicleDataMartReader of(@NotNull DataMart instance, ExcerptTailer tailer) {
        return new ChronicleDataMartReader(instance, tailer);
    }

    public boolean readOne() {
        if (!tailer.nextIndex()) {
            return false;
        }
        MetaData.get().readMarshallable(tailer);
        String methodName = tailer.readEnum(String.class);
        assert methodName != null;
        switch (methodName) {
            //void calculate(String target);

            case "calculate": {
                instance.calculate(tailer.readEnum(String.class));
                break;
            }

            //default void startup(String target) {
            case "startup": {
                instance.startup(tailer.readEnum(String.class));
                break;
            }

            //void onUpdate(MarketDataUpdate quote);
            case "onUpdate": {
                //TODO Reuse this object when ChronicleMap is used.
                MarketDataUpdate mdu = newInstance(MarketDataUpdate.class);
                mdu.readMarshallable(tailer);
                instance.onUpdate(mdu);
                break;
            }
            //void addSubscription(Subscription subscription);
            case "addSubscription": {
                sub.readMarshallable(tailer);
                instance.addSubscription(sub);
                break;
            }
            //void removeSubscription(Subscription subscription);
            case "removeSubscription": {
                sub.readMarshallable(tailer);
                instance.removeSubscription(sub);
                break;
            }
            //default void stopped(String target) {
            case "stopped": {
                instance.stopped(tailer.readEnum(String.class));
                break;
            }

            default:
                throw new AssertionError("Unhandled message " + methodName);
        }
        return true;
    }
}
