package jp.mufg.chronicle.map.testclasses;

import net.openhft.chronicle.hash.replication.TcpTransportAndNetworkConfig;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.WriteContext;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * Created by daniels on 17/03/2015.
 */
public class MapContainer implements AutoCloseable
{
    private final ChronicleMap<MarketDataKey, MarketDataValue> marketDataCache;
    private final MarketDataValue _marketDataValue;
    private final MarketDataKey _marketDataKey;

    public MapContainer(File file) throws IOException
    {
        InetSocketAddress mypc1 = new InetSocketAddress( "mypc",8081);
        InetSocketAddress mypc2 = new InetSocketAddress( "mypc",8082);

        TcpTransportAndNetworkConfig  config1 = TcpTransportAndNetworkConfig.of(8082,
                mypc1,mypc2);

        TcpTransportAndNetworkConfig  config = TcpTransportAndNetworkConfig.of(8080);

        marketDataCache = ChronicleMapBuilder
                .of(MarketDataKey.class, MarketDataValue.class)
                .putReturnsNull(true)
                .removeReturnsNull(true)
                .entries(200) //This is a guide more than a limit
                .replication((byte)1,config)
                .createPersistedTo(file);

        _marketDataValue = marketDataCache.newValueInstance();
        //Copyable marketDataValue = (Copyable) _marketDataValue;
        _marketDataKey = marketDataCache.newKeyInstance();
    }

    public MarketDataValue get(String source, String supplier)
    {

        _marketDataKey.setSource(source);
        _marketDataKey.setSupplier(supplier);
        marketDataCache.getUsing(_marketDataKey, _marketDataValue);

        return _marketDataValue;
    }

    public MarketDataValue put(String source, String supplier, Consumer<MarketDataValue> consumer)
    {
        _marketDataKey.setSource(source);
        _marketDataKey.setSupplier(supplier);

        try (WriteContext<MarketDataKey, MarketDataValue> c = marketDataCache.acquireUsingLocked(_marketDataKey, _marketDataValue))
        {
            consumer.accept(_marketDataValue);
        }

        return _marketDataValue;
    }

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

    public void putAsk(CharSequence source, CharSequence supplier, CharSequence id, double ask)
    {

        _marketDataKey.setSource(source);
        _marketDataKey.setSupplier(supplier);
        _marketDataKey.setId(id);

        try (WriteContext<MarketDataKey, MarketDataValue> context = marketDataCache.acquireUsingLocked(_marketDataKey, _marketDataValue))
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

