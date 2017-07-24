package queue4.atTime;

/**
 * Created by hardikd on 20/07/2017.
 */
public class ValuationEnvironmentSaver {
    BooleanFunctionCallRecorder _functionCallRecorder;
    public ValuationEnvironmentSaver(BooleanFunctionCallRecorder functionCallRecorder)
    { _functionCallRecorder = functionCallRecorder;}
    /**
     * Saves the valuation environment to file and sends a config to reset the next time when the valuation environment should be saved
     * @param valuationEnvironment Binary valuation environment
     * @return
     */
    public boolean saveValuationEnvironment(byte[] valuationEnvironment)
    {
       System.out.print("Saved");
        _functionCallRecorder.setFunctionCallRecieved(true);
        return true;
    }


}
