package jp.mufg.chronicle.map.testclasses;

import java.io.*;


public class QuoteMapKey implements Externalizable
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
        return _hash;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject(_source);
        out.writeObject(_supplier);
        out.writeUTF(_id);
        out.writeObject(_field);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        _source = (MarketDataSource) in.readObject();
        _supplier = (MarketDataSupplier) in.readObject();
        _id = in.readUTF();
        _field = (MarketDataField) in.readObject();
        buildHashCode();
    }
}