package jp.mufg.chronicle.map.testclasses;

/**
 * Created by daniels on 17/03/2015.
 */
public interface MarketDataValue
{
    double getBid();

    void setBid(double bid);

    double getAsk();

    void setAsk(double ask);

    double getMid();

    void setMid(double mid);
}
