package jp.mufg.chronicle.map.testclasses;

import net.openhft.lang.model.constraints.MaxSize;

/**
 * Created by daniels on 17/03/2015.
 */
public interface MarketDataKey
{

    CharSequence getId();
    void setId(@MaxSize(15) CharSequence source);

    CharSequence getSource();

     void setSource(@MaxSize(20) CharSequence source);

     CharSequence getSupplier();

     void setSupplier(@MaxSize(20) CharSequence supplier);
}
