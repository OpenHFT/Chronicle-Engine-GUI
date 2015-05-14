package jp.mufg.chronicle.map.testclasses;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.NotNull;


public class QuoteMapKey implements BytesMarshallable
{
    private MarketDataSource _source;
    private MarketDataSupplier _supplier;
    private String _id;
    private MarketDataField _field;
    private int _hash;

    public QuoteMapKey()
    {
    }

    public QuoteMapKey(MarketDataSupplier supplier, MarketDataSource source, String id, MarketDataField field)
    {
        updateValues(supplier, source, id, field);
    }

    public void updateValues(MarketDataSupplier supplier, MarketDataSource source, String id, MarketDataField field)
    {
        // Store the information

        _source = source;
        _supplier = supplier;
        _id = id;
        _field = field;
        buildHashCode();
    }

    public MarketDataSupplier getSupplier()
    {
        return _supplier;
    }

    public MarketDataSource getSource()
    {
        return _source;
    }

    public String getId()
    {
        return _id;
    }

    public MarketDataField getField()
    {
        return _field;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj != null) && (obj instanceof QuoteMapKey))
        {
            QuoteMapKey qmk = (QuoteMapKey) obj;

            return qmk._field.equals(_field) && qmk._source.equals(_source) && qmk._supplier.equals(_supplier) && qmk._id.equals(_id);
        }
        else
        {
            return false;
        }
    }

    private void buildHashCode()
    {
        _hash = 17 + _supplier.hashCode();
        _hash = _hash * 31 + _source.hashCode();
        _hash = _hash * 13 + _id.hashCode();
        _hash = _hash * 37 + _field.hashCode();
    }

    @Override
    public int hashCode()
    {
        if (_hash == 0)
            buildHashCode();
        return _hash;
    }

    @Override
    public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
        _source = in.readEnum(MarketDataSource.class);
        _supplier = in.readEnum(MarketDataSupplier.class);
        _id = in.readEnum(String.class);
        _field = in.readEnum(MarketDataField.class);
    }

    @Override
    public void writeMarshallable(@NotNull Bytes out) {
        out.writeEnum(_source);
        out.writeEnum(_supplier);
        out.write8bitText(_id);
        out.writeEnum(_field);
    }
}