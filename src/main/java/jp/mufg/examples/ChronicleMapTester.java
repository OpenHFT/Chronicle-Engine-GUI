package jp.mufg.examples;

import jp.mufg.api.*;

import java.util.*;

/**
 * Created by daniels on 20/02/2015.
 */
public interface ChronicleMapTester
{
    void onMapMarketDataUpdate(MapMarketDataUpdate mapMarketDataUpdate);

    void onMarketDataUpdateMapStringDouble(String someString, Map<String, Double> map);

    void onMarketDataUpdateMapStringObject(String someString, Map<String, Object> map);

    void onMarketDataUpdateMapEnumObject(String someString, Map<Enum, Object> map);
}