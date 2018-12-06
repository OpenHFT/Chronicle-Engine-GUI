package jp.mufg.api;

import net.openhft.lang.model.DataValueClasses;
import org.junit.Test;

public class MarketDataUpdateTest {
    @Test
    public void dumpCode() {
        System.setProperty("dvg.dumpCode", "true");
        MarketDataUpdate mdu = DataValueClasses.newDirectInstance(MarketDataUpdate.class);
        mdu.setBid(100);
        mdu.setBidq(1e6);
        mdu.setAsk(102);
        mdu.setAskq(2e6);
        System.out.println(mdu);
    }
}