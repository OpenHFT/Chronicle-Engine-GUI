package jp.mufg.api;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.Byteable;
import net.openhft.lang.model.Copyable;

import static net.openhft.lang.Compare.calcLongHashCode;
import static net.openhft.lang.Compare.isEqual;

public class MarketDataUpdate$$Native implements MarketDataUpdate, BytesMarshallable, Byteable, Copyable<jp.mufg.api.MarketDataUpdate> {
    private static final int ASK = 0;
    private static final int ASKQ = 8;
    private static final int BID = 16;
    private static final int BIDQ = 24;
    private static final int MARKETTIMESTAMP = 32;
    private static final int EXCHANGE = 40;
    private static final int INSTRUMENT = 56;
    private static final int SOURCE = 72;


    private Bytes _bytes;
    private long _offset;

    public double getAsk() {
        return _bytes.readDouble(_offset + ASK);
    }

    public void setAsk(double $) {
        _bytes.writeDouble(_offset + ASK, $);
    }

    public double getAskq() {
        return _bytes.readDouble(_offset + ASKQ);
    }

    public void setAskq(double $) {
        _bytes.writeDouble(_offset + ASKQ, $);
    }

    public double getBid() {
        return _bytes.readDouble(_offset + BID);
    }

    public void setBid(double $) {
        _bytes.writeDouble(_offset + BID, $);
    }

    public double getBidq() {
        return _bytes.readDouble(_offset + BIDQ);
    }

    public void setBidq(double $) {
        _bytes.writeDouble(_offset + BIDQ, $);
    }

    public long getMarketTimestamp() {
        return _bytes.readLong(_offset + MARKETTIMESTAMP);
    }

    public void setMarketTimestamp(long $) {
        _bytes.writeLong(_offset + MARKETTIMESTAMP, $);
    }

    public java.lang.String getExchange() {
        return _bytes.readUTFΔ(_offset + EXCHANGE);
    }

    public void setExchange(java.lang.String $) {
        _bytes.writeUTFΔ(_offset + EXCHANGE, 16, $);
    }

    public java.lang.String getInstrument() {
        return _bytes.readUTFΔ(_offset + INSTRUMENT);
    }

    public void setInstrument(java.lang.String $) {
        _bytes.writeUTFΔ(_offset + INSTRUMENT, 16, $);
    }

    public java.lang.String getSource() {
        return _bytes.readUTFΔ(_offset + SOURCE);
    }

    public void setSource(java.lang.String $) {
        _bytes.writeUTFΔ(_offset + SOURCE, 16, $);
    }

    @Override
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

    @Override
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

    @Override
    public void readMarshallable(Bytes in) {
        setAsk(in.readDouble());
        setAskq(in.readDouble());
        setBid(in.readDouble());
        setBidq(in.readDouble());
        setMarketTimestamp(in.readLong());
        setExchange(in.readUTFΔ());
        setInstrument(in.readUTFΔ());
        setSource(in.readUTFΔ());
    }

    @Override
    public void bytes(Bytes bytes, long offset) {
        this._bytes = bytes;
        this._offset = offset;
    }

    @Override
    public Bytes bytes() {
        return _bytes;
    }

    @Override
    public long offset() {
        return _offset;
    }

    @Override
    public int maxSize() {
        return 88;
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
        if (_bytes == null) return "bytes is null";
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
