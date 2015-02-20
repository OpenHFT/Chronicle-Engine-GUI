package jp.mufg.examples;

import net.openhft.lang.model.constraints.*;

import java.util.*;

/**
 * Created by daniels on 20/02/2015.
 */
public interface MapMarketDataUpdate
{
    String getSource();

    void setSource(String source);

    String getExchange();

    void setExchange(String exchange);

    String getInstrument();

    void setInstrument(String instrument);

    Map<String, Double> getMap();

    void setMap(Map<String, Double> map);

//    List<String> getList();
//
//    void setList(List<String> list);
}