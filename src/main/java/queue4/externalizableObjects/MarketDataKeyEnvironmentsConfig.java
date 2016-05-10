package queue4.externalizableObjects;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created by cliveh on 10/05/2016.
 */
public class MarketDataKeyEnvironmentsConfig implements ConfigSetting{

    private String _id;
    private String _executor;
    protected boolean _isRetransmit;

    private MarketDataKeyEnvironments _marketDataKeyEnvironments;

    public MarketDataKeyEnvironments get_marketDataKeyEnvironments() {
        return _marketDataKeyEnvironments;
    }

    public void set_marketDataKeyEnvironments(MarketDataKeyEnvironments _marketDataKeyEnvironments) {
        this._marketDataKeyEnvironments = _marketDataKeyEnvironments;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(_marketDataKeyEnvironments);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _marketDataKeyEnvironments = (MarketDataKeyEnvironments)in.readObject();
    }

    @Override
    public String getExecutor() {
        return _executor;
    }

    @Override
    public void setExecutor(String executor) {
        _executor = executor;
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public void setId(String id) {
        _id = id;
    }

    @Override
    public boolean isRetransmit() {
        return _isRetransmit;
    }

    @Override
    public void setRetransmit(boolean isRetransmit) {
        _isRetransmit = isRetransmit;
    }
}
