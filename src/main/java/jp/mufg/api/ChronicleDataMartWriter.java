package jp.mufg.api;

import jp.mufg.api.util.MetaData;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.lang.io.serialization.BytesMarshallable;

import java.io.IOException;

public class ChronicleDataMartWriter implements DataMart {
    private final Chronicle chronicle;

    public ChronicleDataMartWriter(Chronicle chronicle) {
        this.chronicle = chronicle;
    }

    @Override
    public void calculate(String target) {
        writeCmdString("calculate", target);
    }

    private void writeCmdString(String cmd, String target) {
        ExcerptAppender appender = appender();
        appender.startExcerpt();
        MetaData.get().writeMarshallable(appender);
        appender.writeEnum(cmd);
        appender.writeEnum(target);
        appender.finish();
    }

    private void writeCmdMarshallable(String cmd, BytesMarshallable bm) {
        ExcerptAppender appender = appender();
        appender.startExcerpt();
        MetaData.get().writeMarshallable(appender);
        appender.writeEnum(cmd);
        bm.writeMarshallable(appender);
        appender.finish();
    }

    private ExcerptAppender appender() {
        try {
            return chronicle.createAppender();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void startup(String target) {
        writeCmdString("startup", target);
    }

    @Override
    public void stopped(String target) {
        writeCmdString("stopped", target);
    }

    @Override
    public void onUpdate(MarketDataUpdate quote) {
        writeCmdMarshallable("onUpdate", quote);
    }

    @Override
    public void addSubscription(Subscription subscription) {
        writeCmdMarshallable("addSubscription", subscription);
    }

    @Override
    public void removeSubscription(Subscription subscription) {
        writeCmdMarshallable("removeSubscription", subscription);
    }
}
