package jp.mufg.chronicle.map.testclasses;

import net.openhft.lang.model.constraints.MaxSize;

/**
 * Created by daniels on 17/03/2015.
 */
public interface MarketDataKeyEnum
{

    CharSequence getId();
    void setId(@MaxSize(15) CharSequence source);

    MarketDataSource getSource();

     void setSource(MarketDataSource source);

     MarketDataSupplier getSupplier();

     void setSupplier(MarketDataSupplier supplier);
}
