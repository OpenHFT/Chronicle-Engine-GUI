package jp.mufg.examples;

import jp.mufg.api.*;

import java.util.*;

/**
 * Created by daniels on 20/02/2015.
 */
public class ChronicleMapTesterImpl implements ChronicleMapTester
{
    @Override
    public void onMapMarketDataUpdate(MapMarketDataUpdate mapMarketDataUpdate)
    {
        System.out.println("Received onMapMarketDataUpdate: " + mapMarketDataUpdate);
        System.out.println("onMapMarketDataUpdate Source: " + mapMarketDataUpdate.getSource());
        System.out.println("onMapMarketDataUpdate Exchange: " + mapMarketDataUpdate.getExchange());
        System.out.println("onMapMarketDataUpdate Instrument: " + mapMarketDataUpdate.getInstrument());
    }

    @Override
    public void onMarketDataUpdateMapStringDouble(String someString, Map<String, Double> map)
    {
        System.out.println("Received onMarketDataUpdateMapStringDouble");
        System.out.println("onMarketDataUpdateMapStringDouble string: " + someString);
        System.out.println("onMarketDataUpdateMapStringDouble Map: " + map);
    }

    @Override
    public void onMarketDataUpdateMapStringObject(String someString, Map<String, Object> map)
    {
        System.out.println("Received onMarketDataUpdateMapStringObject");
        System.out.println("onMarketDataUpdateMapStringObject string: " + someString);
        System.out.println("onMarketDataUpdateMapStringObject Map: " + map);
    }
}
