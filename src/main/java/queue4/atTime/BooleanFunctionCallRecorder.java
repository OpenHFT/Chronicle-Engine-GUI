package queue4.atTime;

/**
 * Created by hardikd on 20/07/2017.
 */
public class BooleanFunctionCallRecorder {
    boolean _functionCallRecieved;

    public BooleanFunctionCallRecorder() {
        _functionCallRecieved = false;
    }

    public boolean getfunctionCallRecieved() {
        return _functionCallRecieved;
    }

    public void setFunctionCallRecieved(boolean _functionCallRecieved) {
        this._functionCallRecieved = _functionCallRecieved;
    }
}
