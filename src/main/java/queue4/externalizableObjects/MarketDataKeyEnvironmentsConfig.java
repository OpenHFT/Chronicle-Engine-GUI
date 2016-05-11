package queue4.externalizableObjects;

import net.openhft.chronicle.wire.Wires;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import static net.openhft.chronicle.wire.WireType.TEXT;

/**
 * Created by cliveh on 10/05/2016.
 */
public class MarketDataKeyEnvironmentsConfig implements ConfigSetting{

    protected boolean _isRetransmit;
    private String _id;
    private String _executor;
    private MarketDataKeyEnvironments _marketDataKeyEnvironments;

    public MarketDataKeyEnvironments get_marketDataKeyEnvironments() {
        return _marketDataKeyEnvironments;
    }

    public void set_marketDataKeyEnvironments(MarketDataKeyEnvironments _marketDataKeyEnvironments) {
        this._marketDataKeyEnvironments = _marketDataKeyEnvironments;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(_id);
        out.writeUTF(_executor);
        out.writeBoolean(_isRetransmit);
        out.writeObject(_marketDataKeyEnvironments);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _id = in.readUTF();
        _executor = in.readUTF();
        _isRetransmit = in.readBoolean();
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


    @Override
    public String toString() {
        return TEXT.asString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return Wires.isEquals(this, obj);
    }

}
