package queue4.externalizableObjects;

import net.openhft.chronicle.wire.Wires;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import static net.openhft.chronicle.wire.WireType.TEXT;

/**
 * Created by cliveh on 10/05/2016.
 */
public class MarketDataKeyEnvironment  implements Externalizable {
    private Map<MarketDataKey, Map<InstrumentId, Values>> _instrumentIdValuesByKey;

    public Map<MarketDataKey, Map<InstrumentId, Values>> get_instrumentIdValuesByKey() {
        return _instrumentIdValuesByKey;
    }

    public void set_instrumentIdValuesByKey(Map<MarketDataKey, Map<InstrumentId, Values>> _instrumentIdValuesByKey) {
        this._instrumentIdValuesByKey = _instrumentIdValuesByKey;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(_instrumentIdValuesByKey);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _instrumentIdValuesByKey = (Map<MarketDataKey, Map<InstrumentId, Values>>)in.readObject();
    }

    @Override
    public int hashCode() {
//        final int i = HashWire.hash32(this);
        return toString().hashCode();
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
