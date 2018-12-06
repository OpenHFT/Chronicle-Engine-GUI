package queue4.externalizableObjects;

import net.openhft.chronicle.wire.HashWire;
import net.openhft.chronicle.wire.Wires;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import static net.openhft.chronicle.wire.WireType.TEXT;

/**
 * Created by cliveh on 10/05/2016.
 */
public class MarketDataKey implements Externalizable {

    private String _producer;
    private MarketDataSource _source;
    private MarketDataSupplier _supplier;
    private MarketDataType _type;
    private String _marketDataId;
    private MarketDataField _field;

    public MarketDataKey() {
    }

    /**
     * @param producer     The producer.
     * @param supplier     The supplier.
     * @param source       The source.
     * @param type         The type.
     * @param marketDataId The market identifier.
     * @param field        The field.
     */
    public MarketDataKey(String producer, MarketDataSupplier supplier, MarketDataSource source, MarketDataType type, String marketDataId, MarketDataField field) {
        updateValues(producer, supplier, source, type, marketDataId, field);
    }

    /**
     * Sets the values and re-calculates the hash code.
     *
     * @param producer     The producer.
     * @param supplier     The supplier.
     * @param source       The source.
     * @param type         The type.
     * @param marketDataId The ID.
     * @param field        The field.
     */
    public final void updateValues(String producer, MarketDataSupplier supplier, MarketDataSource source, MarketDataType type, String marketDataId, MarketDataField field) {
        _producer = (producer == null) ? "" : producer;
        _source = source;
        _supplier = supplier;
        _type = type;
        _marketDataId = marketDataId;
        _field = field;
    }

    public String get_producer() {
        return _producer;
    }

    public void set_producer(String _producer) {
        this._producer = _producer;
    }

    public MarketDataSource get_source() {
        return _source;
    }

    public void set_source(MarketDataSource _source) {
        this._source = _source;
    }

    public MarketDataSupplier get_supplier() {
        return _supplier;
    }

    public void set_supplier(MarketDataSupplier _supplier) {
        this._supplier = _supplier;
    }

    public MarketDataType get_type() {
        return _type;
    }

    public void set_type(MarketDataType _type) {
        this._type = _type;
    }

    public String get_marketDataId() {
        return _marketDataId;
    }

    public void set_marketDataId(String _marketDataId) {
        this._marketDataId = _marketDataId;
    }

    public MarketDataField get_field() {
        return _field;
    }

    public void set_field(MarketDataField _field) {
        this._field = _field;
    }

    /**
     * @see java.io.Externalizable
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(_producer);
        out.writeObject(_source);
        out.writeObject(_supplier);
        out.writeObject(_type);
        out.writeUTF(_marketDataId);
        out.writeObject(_field);
    }

    /**
     * @see java.io.Externalizable
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _producer = in.readUTF();
        _source = (MarketDataSource) in.readObject();
        _supplier = (MarketDataSupplier) in.readObject();
        _type = (MarketDataType) in.readObject();
        _marketDataId = in.readUTF();
        _field = (MarketDataField) in.readObject();
    }

    @Override
    public int hashCode() {
        return HashWire.hash32(this);
    }

    @Override
    public String toString() {
        return TEXT.asString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return Wires.isEquals(this, obj);
    }
}
