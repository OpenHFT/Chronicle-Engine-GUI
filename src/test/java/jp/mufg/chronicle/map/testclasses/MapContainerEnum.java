package jp.mufg.chronicle.map.testclasses;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.WriteContext;

import java.io.File;
import java.io.IOException;

/**
 * Created by daniels on 17/03/2015.
 */
public class MapContainerEnum implements AutoCloseable
{
    private final ChronicleMap<MarketDataKeyEnum, MarketDataValue> marketDataCache;
    private final MarketDataValue _marketDataValue;
    private final MarketDataKeyEnum _marketDataKey;

    public MapContainerEnum(File file) throws IOException
    {
        marketDataCache = ChronicleMapBuilder
                .of(MarketDataKeyEnum.class, MarketDataValue.class)
                .putReturnsNull(true)
                .removeReturnsNull(true)
                .entries(200) //This is a guide more than a limit
                .createPersistedTo(file);

        _marketDataValue = marketDataCache.newValueInstance();
        _marketDataKey = marketDataCache.newKeyInstance();
    }

    public static void main(String[] args) throws Exception
    {
        File file = File.createTempFile("chronicle", "chron");
        MapContainerEnum mapContainerEnum = new MapContainerEnum(file);
        mapContainerEnum.close();
    }

//    public MarketDataValue get(String source, String supplier)
//    {
//
//        _marketDataKey.setSource(source);
//        _marketDataKey.setSupplier(supplier);
//        marketDataCache.getUsing(_marketDataKey, _marketDataValue);
//
//        return _marketDataValue;
//    }

//    public MarketDataValue putBid(String source, String supplier, double bid)
//    {
//
//        _marketDataKey.setSource(source);
//        _marketDataKey.setSupplier(supplier);
//
//
//        try (WriteContext<MarketDataKey, MarketDataValue> marketDataKeyMarketDataValueWriteContext = marketDataCache.acquireUsingLocked(_marketDataKey, _marketDataValue))
//        {
//            _marketDataValue.setBid(bid);
//        }
//
//        return _marketDataValue;
//    }

//    public void putAsk(String source, String supplier, double ask)
//    {
//
//        _marketDataKey.setSource(source);
//        _marketDataKey.setSupplier(supplier);
//
//
//        marketDataCache.acquireUsing(_marketDataKey, _marketDataValue);
//        _marketDataValue.setAsk(ask);
//
//    }

    public void putAsk(MarketDataSource source, MarketDataSupplier supplier, CharSequence id, double ask)
    {

        _marketDataKey.setSource(source);
        _marketDataKey.setSupplier(supplier);
        _marketDataKey.setId(id);

        try (WriteContext<MarketDataKeyEnum, MarketDataValue> context = marketDataCache.acquireUsingLocked(_marketDataKey, _marketDataValue))
        {
            _marketDataValue.setAsk(ask);
        }
    }

    @Override
    public void close() throws Exception
    {
        marketDataCache.close();
    }
}

