package queue4.externalizableObjects;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.TreeSet;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Represents configuration information for a change of system date.
 */
public class MiscellaneousTypesConfig implements ConfigSetting {
    private final static int BUFFER_SIZE = 2048;

    private String _id;
    private boolean _isRetransmit;
    private String _executor;
    private ZonedDateTime _systemDate;
    private double[] _doublePcaMatrixData;
    private int[] _intPcaMatrixData;
    private long[] _longPcaMatrixData;
    private float[] _floatPcaMatrixData;
    private boolean[] _isPcaMatrixData;
    private TreeSet<SwapId> _orderedTenors;
    private String _valuationEnvironment;

    @Override
    public String getExecutor() {
        return _executor;
    }

    /**
     * Sets the executor for this configuration information.
     *
     * @param executor The executor.
     */
    public void setExecutor(String executor) {
        _executor = executor;
    }

    @Override
    public String getId() {
        return _id;
    }

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

    /**
     * Gets the system date for this.
     */
    public ZonedDateTime getSystemDate() {
        return _systemDate;
    }

    /**
     * Sets the system date.
     *
     * @param systemDate The system date.
     */
    public void setSystemDate(ZonedDateTime systemDate) {
        _systemDate = systemDate;
    }

    public double[] getDoublePcaMatrixData() {
        return _doublePcaMatrixData;
    }

    public void setDoublePcaMatrixData(double[] doublePcaMatrixData) {
        _doublePcaMatrixData = doublePcaMatrixData;
    }

    public int[] getIntPcaMatrixData() {
        return _intPcaMatrixData;
    }

    public void setIntPcaMatrixData(int[] intPcaMatrixData) {
        _intPcaMatrixData = intPcaMatrixData;
    }

    public long[] getLongPcaMatrixData() {
        return _longPcaMatrixData;
    }

    public void setLongPcaMatrixData(long[] longPcaMatrixData) {
        _longPcaMatrixData = longPcaMatrixData;
    }

    public float[] getFloatPcaMatrixData() {
        return _floatPcaMatrixData;
    }

    public void setFloatPcaMatrixData(float[] floatPcaMatrixData) {
        _floatPcaMatrixData = floatPcaMatrixData;
    }

    public boolean[] getIsPcaMatrixData() {
        return _isPcaMatrixData;
    }

    public void setIsPcaMatrixData(boolean[] isPcaMatrixData) {
        _isPcaMatrixData = isPcaMatrixData;
    }

    public TreeSet<SwapId> getOrderedTenors() {
        return _orderedTenors;
    }

    public void setOrderedTenors(TreeSet<SwapId> orderedTenors) {
        _orderedTenors = orderedTenors;
    }

    public String getValuationEnvironment() {
        return _valuationEnvironment;
    }

    public void setValuationEnvironment(String valuationEnvironment) {
        _valuationEnvironment = valuationEnvironment;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(_id);
        out.writeUTF(_executor);
        out.writeBoolean(_isRetransmit);

        out.writeObject(_systemDate);

        out.writeObject(_doublePcaMatrixData);
        out.writeObject(_intPcaMatrixData);
        out.writeObject(_longPcaMatrixData);
        out.writeObject(_floatPcaMatrixData);
        out.writeObject(_isPcaMatrixData);

        out.writeObject(_orderedTenors);

        byte[] bytes = _valuationEnvironment.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        dos.write(bytes);
        dos.close();

        byte[] compressedArray = baos.toByteArray();
        // Write the number of bytes and the bytes

        out.writeInt(compressedArray.length);
        out.write(compressedArray);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _id = in.readUTF();
        _executor = in.readUTF();
        _isRetransmit = in.readBoolean();
        _systemDate = (ZonedDateTime) in.readObject();

        _doublePcaMatrixData = (double[]) in.readObject();
        _intPcaMatrixData = (int[]) in.readObject();
        _longPcaMatrixData = (long[]) in.readObject();
        _floatPcaMatrixData = (float[]) in.readObject();
        _isPcaMatrixData = (boolean[]) in.readObject();

        _orderedTenors = (TreeSet<SwapId>) in.readObject();

        // Read the size of the byte array that was put on the queue to represent the close valuation environment.
        int readBytes = in.readInt();

        if (readBytes == 0) {
            _valuationEnvironment = null;

        } else {
            byte[] inputBytes = new byte[readBytes];

            int len0 = in.read(inputBytes);
            assert len0 == readBytes;

            try (InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(inputBytes));
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[BUFFER_SIZE];
                for (int len; (len = iis.read(buffer)) > 0; )
                    baos.write(buffer, 0, len);
                byte[] output = baos.toByteArray();
                _valuationEnvironment = new String(output, StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * See java.lang.Object.
     */
    @Override
    public final int hashCode() {
        int hashCode = 17;
        hashCode = hashCode * 31 + (_isRetransmit ? 1 : 0);
        hashCode = hashCode * 31 + _id.hashCode();
        hashCode = hashCode * 31 + _executor.hashCode();
        hashCode = hashCode * 31 + _systemDate.hashCode();

        return hashCode;
    }

    /**
     * See java.lang.Object.
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            if (o instanceof ZonedDateTime) {
                MiscellaneousTypesConfig zdtc = (MiscellaneousTypesConfig) o;

                return (_isRetransmit == zdtc._isRetransmit) && (_id == zdtc._id) && _executor.equals(zdtc._executor) && _systemDate.equals(zdtc._systemDate);
            } else {
                return false;
            }
        }
    }
}
