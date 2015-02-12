package jp.mufg.api.util;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptCommon;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by mat on 10/02/2015.
 */
public class MetaData implements BytesMarshallable {
    static final ThreadLocal<MetaData> META_DATA_THREAD_LOCAL = new ThreadLocal<MetaData>() {
        @NotNull
        @Override
        protected MetaData initialValue() {
            return new MetaData();
        }
    };
    static final Map<Chronicle, Byte> CHRONICLE_TO_ID =
            Collections.synchronizedMap(new WeakHashMap<Chronicle, Byte>());
    int timeCount = 0;
    @NotNull
    long[] times = new long[20];
    int sourceCount = 0;
    @NotNull
    byte[] sourceIds = new byte[10];
    @NotNull
    long[] sources = new long[10];

    public static MetaData get() {
        return META_DATA_THREAD_LOCAL.get();
    }

    public static void setId(Chronicle c, byte id) {
        CHRONICLE_TO_ID.put(c, id);
    }

    @Override
    public void readMarshallable(@NotNull Bytes bytes) throws IllegalStateException {
        // support future schema changes by length.
        int length = bytes.readUnsignedShort();
        long limit = bytes.limit();
        long limit2 = bytes.position() + length;
        assert limit2 <= limit;
        // TODO doesn't work due to JLANG-51
//        bytes.limit(limit2);
        try {
            readMarshallable0(bytes);
            assert bytes.position() <= limit2;
        } finally {
            bytes.limit(limit);
            bytes.position(limit2);
        }
    }

    private void readMarshallable0(Bytes bytes) {
        timeCount = (int) bytes.readStopBit();
        for (int i = 0; i < timeCount; i++) {
            times[i] = bytes.readLong();
        }
        times[timeCount++] = System.nanoTime();
        sourceCount = (int) bytes.readStopBit();
        bytes.read(sourceIds, 0, sourceCount);
        for (int i = 0; i < sourceCount; i++)
            sources[i] = bytes.readLong();
        if (bytes instanceof ExcerptCommon) {
            ExcerptCommon excerpt = (ExcerptCommon) bytes;
            Byte id = CHRONICLE_TO_ID.get(excerpt.chronicle());
            sourceIds[sourceCount] = id == null ? (byte) -1 : id;
            sources[sourceCount++] = excerpt.index();
        }
    }

    @Override
    public void writeMarshallable(@NotNull Bytes bytes) {
        long position = bytes.position();
        bytes.writeUnsignedShort(0);


        bytes.writeStopBit(timeCount + 1);
            for (int i = 0; i < timeCount; i++) {
                bytes.writeLong(times[i]);
            }
        bytes.writeLong(System.nanoTime());

        bytes.writeStopBit(sourceCount);
        bytes.write(sourceIds, 0, sourceCount);
            for (int i = 0; i < sourceCount; i++) {
                bytes.writeLong(sources[i]);
            }


        long length = bytes.position() - position - 2;
        if (length >= 1 << 16)
            throw new AssertionError("length: " + length);
        bytes.writeUnsignedShort(position, (int) length);
    }

    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MetaData times,");
        if (timeCount > 0)
            sb.append(times[0] / 1000);
        for (int i = 1; i < timeCount; i++)
            sb.append(",").append((times[i] - times[i - 1]) / 1000);
        sb.append(", sources");
        for (int i = 0; i < sourceCount; i++) {
            sb.append(", ").append(sourceIds[i])
                    .append(",").append(Long.toHexString(sources[i]));
        }
        return sb.toString();
    }
}