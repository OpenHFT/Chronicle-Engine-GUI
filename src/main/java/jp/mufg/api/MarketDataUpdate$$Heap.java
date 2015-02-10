package jp.mufg.api;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.Copyable;

import static net.openhft.lang.Compare.calcLongHashCode;
import static net.openhft.lang.Compare.isEqual;

public class MarketDataUpdate$$Heap implements MarketDataUpdate, BytesMarshallable, Copyable<jp.mufg.api.MarketDataUpdate> {
    private double _ask;
    private double _askq;
    private double _bid;
    private double _bidq;
    private long _marketTimestamp;
    private java.lang.String _exchange;
    private java.lang.String _instrument;
    private java.lang.String _source;

    public double getAsk() {
        return _ask;
    }

    public void setAsk(double $) {
        _ask = $;
    }

    public double getAskq() {
        return _askq;
    }

    public void setAskq(double $) {
        _askq = $;
    }

    public double getBid() {
        return _bid;
    }

    public void setBid(double $) {
        _bid = $;
    }

    public double getBidq() {
        return _bidq;
    }

    public void setBidq(double $) {
        _bidq = $;
    }

    public long getMarketTimestamp() {
        return _marketTimestamp;
    }

    public void setMarketTimestamp(long $) {
        _marketTimestamp = $;
    }

    public java.lang.String getExchange() {
        return _exchange;
    }

    public void setExchange(java.lang.String $) {
        _exchange = $;
    }

    public java.lang.String getInstrument() {
        return _instrument;
    }

    public void setInstrument(java.lang.String $) {
        _instrument = $;
    }

    public java.lang.String getSource() {
        return _source;
    }

    public void setSource(java.lang.String $) {
        _source = $;
    }

    @SuppressWarnings("unchecked")
    public void copyFrom(jp.mufg.api.MarketDataUpdate from) {
        setAsk(from.getAsk());
        setAskq(from.getAskq());
        setBid(from.getBid());
        setBidq(from.getBidq());
        setMarketTimestamp(from.getMarketTimestamp());
        setExchange(from.getExchange());
        setInstrument(from.getInstrument());
        setSource(from.getSource());
    }

    public void writeMarshallable(Bytes out) {
        out.writeDouble(getAsk());
        out.writeDouble(getAskq());
        out.writeDouble(getBid());
        out.writeDouble(getBidq());
        out.writeLong(getMarketTimestamp());
        out.writeUTFΔ(getExchange());
        out.writeUTFΔ(getInstrument());
        out.writeUTFΔ(getSource());
    }

    public void readMarshallable(Bytes in) {
        _ask = in.readDouble();
        _askq = in.readDouble();
        _bid = in.readDouble();
        _bidq = in.readDouble();
        _marketTimestamp = in.readLong();
        _exchange = in.readUTFΔ();
        _instrument = in.readUTFΔ();
        _source = in.readUTFΔ();
    }

    public int hashCode() {
        long lhc = longHashCode();
        return (int) ((lhc >>> 32) ^ lhc);
    }

    public long longHashCode() {
        return (((((((calcLongHashCode(getAsk())) * 10191 +
                calcLongHashCode(getAskq())) * 10191 +
                calcLongHashCode(getBid())) * 10191 +
                calcLongHashCode(getBidq())) * 10191 +
                calcLongHashCode(getMarketTimestamp())) * 10191 +
                calcLongHashCode(getExchange())) * 10191 +
                calcLongHashCode(getInstrument())) * 10191 +
                calcLongHashCode(getSource());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarketDataUpdate)) return false;
        MarketDataUpdate that = (MarketDataUpdate) o;

        if (!isEqual(getAsk(), that.getAsk())) return false;
        if (!isEqual(getAskq(), that.getAskq())) return false;
        if (!isEqual(getBid(), that.getBid())) return false;
        if (!isEqual(getBidq(), that.getBidq())) return false;
        if (!isEqual(getMarketTimestamp(), that.getMarketTimestamp())) return false;
        if (!isEqual(getExchange(), that.getExchange())) return false;
        if (!isEqual(getInstrument(), that.getInstrument())) return false;
        if (!isEqual(getSource(), that.getSource())) return false;
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MarketDataUpdate{ ");
        sb.append("ask= ").append(getAsk());
        sb.append(", ")
        ;
        sb.append("askq= ").append(getAskq());
        sb.append(", ")
        ;
        sb.append("bid= ").append(getBid());
        sb.append(", ")
        ;
        sb.append("bidq= ").append(getBidq());
        sb.append(", ")
        ;
        sb.append("marketTimestamp= ").append(getMarketTimestamp());
        sb.append(", ")
        ;
        sb.append("exchange= ").append(getExchange());
        sb.append(", ")
        ;
        sb.append("instrument= ").append(getInstrument());
        sb.append(", ")
        ;
        sb.append("source= ").append(getSource());
        sb.append(" }");
        return sb.toString();
    }
}