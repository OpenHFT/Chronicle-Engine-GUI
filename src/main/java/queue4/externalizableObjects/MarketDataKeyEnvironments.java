package queue4.externalizableObjects;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

/**
 * Created by cliveh on 10/05/2016.
 */
public class MarketDataKeyEnvironments implements Externalizable{

    private Set<MarketDataKeyEnvironment> _environments;

    public Set<MarketDataKeyEnvironment> get_environments() {
        return _environments;
    }

    public void set_environments(Set<MarketDataKeyEnvironment> _environments) {
        this._environments = _environments;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(_environments);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _environments = (Set<MarketDataKeyEnvironment>)in.readObject();
    }
}
