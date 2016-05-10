package queue4.externalizableObjects;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

/**
 * Created by cliveh on 10/05/2016.
 */
public class Values implements Externalizable {

    private double _value1;
    private List<Double> _values;


    public double get_value1() {
        return _value1;
    }

    public void set_value1(double value1) {
        this._value1 = value1;
    }

    public List<Double> get_values() {
        return _values;
    }

    public void set_values(List<Double> values) {
        this._values = _values;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(_value1);
        out.writeObject(_values);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _value1 = in.readDouble();
        _values = (List<Double>)in.readObject();
    }
}
