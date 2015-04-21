package jp.mufg.chronicle.map.testclasses;

/**
 * Created by daniels on 17/03/2015.
 */
public interface MarketDataValue
{
    public double getBid();

    public void setBid(double bid);

    public double getAsk();

    public void setAsk(double ask);

    public double getMid();

    public void setMid(double mid);
}
