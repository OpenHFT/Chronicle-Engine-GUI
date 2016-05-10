package queue4.externalizableObjects;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

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
}
