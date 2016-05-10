package queue4.externalizableObjects;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created by cliveh on 10/05/2016.
 */
public class InstrumentId implements Externalizable, Comparable<InstrumentId> {

    private int _id;
    private char _charId;
    private short _shortId;
    private long _longId;
    private double _doubleId;
    private float _floatId;
    private String _stringId;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public char get_charId() {
        return _charId;
    }

    public void set_charId(char _charId) {
        this._charId = _charId;
    }

    public long get_longId() {
        return _longId;
    }

    public short get_shortId() {
        return _shortId;
    }

    public void set_shortId(short _shortId) {
        this._shortId = _shortId;
    }

    public void set_longId(long _longId) {
        this._longId = _longId;
    }

    public double get_doubleId() {
        return _doubleId;
    }

    public void set_doubleId(double _doubleId) {
        this._doubleId = _doubleId;
    }

    public float get_floatId() {
        return _floatId;
    }

    public void set_floatId(float _floatId) {
        this._floatId = _floatId;
    }

    public String get_stringId() {
        return _stringId;
    }

    public void set_stringId(String _stringId) {
        this._stringId = _stringId;
    }

    @Override
    public int compareTo(InstrumentId o) {
        return Integer.compare(_id, o.get_id());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(_id);
        out.writeChar(_charId);
        out.writeShort(_shortId);
        out.writeLong(_longId);
        out.writeDouble(_doubleId);
        out.writeFloat(_floatId);
        out.writeUTF(_stringId);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _id = in.readInt();
        _charId = in.readChar();
        _shortId = in.readShort();
        _longId = in.readLong();
        _doubleId = in.readDouble();
        _floatId = in.readFloat();
    }
}
