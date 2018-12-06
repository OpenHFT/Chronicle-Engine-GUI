package jp.mufg.chronicle.queue.testclasses;

import java.util.Map;

/**
 * Created by daniels on 20/02/2015.
 */
public interface MapFieldDataValueClass
{
    String getSomeString();

    void setSomeString(String source);

    Map<String, Double> getMap();

    void setMap(Map<String, Double> map);
}