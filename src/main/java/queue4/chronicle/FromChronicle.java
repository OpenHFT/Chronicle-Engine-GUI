package queue4.chronicle;


import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import org.jetbrains.annotations.NotNull;
import queue4.externalizableObjects.MarketDataField;
import queue4.externalizableObjects.MarketDataSource;
import queue4.externalizableObjects.MarketDataSupplier;
import queue4.externalizableObjects.MarketDataType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to read method invocations for a specified interface that are stored in a Chronicle and invoke these methods on a provided object that implements the interface.
 * <p/>
 * Please note that instances of this class should not be shared across threads as instances of ExcerptTailer are aware of what thread they are called in.
 * <p/>
 * Please note that this class does not support interfaces that have overloaded methods.
 *
 * @param <T> The interface whose method invocations are being read.
 */
public class FromChronicle<T> {
    private final T _instance;
    private final ExcerptTailer _tailer;
    private final Map<String, Method> methodMap = new HashMap<>();
    private final Map<String, Object[]> argumentArrays = new HashMap<>(); // A map of re-usable Object arrays for each argument.
    // Number of arguments for each method call should not change
    // at runtime which is why this is possible.


    /**
     * Creates an instance of this that can be used to read invocations of the methods for a specified interface stored in a Chronicle and
     * invoke these methods on a provided object that implements the interface.
     *
     * @param instance The object whose methods should be invoked when a method invocation is read from Chronicle.
     * @param tailer   The object to use to read method invocations from the Chronicle.
     */
    private FromChronicle(@NotNull T instance, ExcerptTailer tailer) {
        _instance = instance;
        _tailer = tailer;

        for (Method m : _instance.getClass().getMethods()) {
            if (m.getDeclaringClass() != Object.class) {
                methodMap.put(m.getName(), m);
            }
        }
    }


    /**
     * Creates an instance of this that can be used to read invocations of the methods for a specified interface stored in a Chronicle and
     * invoke these methods on a provided object that implements the interface.
     *
     * @param instance The object whose methods should be invoked when a method invocation is read from Chronicle.
     * @param tailer   The object to use to read method invocations from the Chronicle.
     * @param <T>      The interface whose method invocations are being read.
     * @return The instance of this that can be used to read method invocations.
     */
    @NotNull
    public static <T> FromChronicle<T> of(@NotNull T instance, ExcerptTailer tailer) {
        return new FromChronicle<>(instance, tailer);
    }


    /**
     * Gets an object array that can be used to store the arguments for a specified method.
     *
     * @param methodName The name of the method.
     * @param arraySize  The number of arguments that the method has.
     * @return The array to use
     */
    private Object[] getArgumentArray(String methodName, int arraySize) {
        if (!argumentArrays.containsKey(methodName)) {
            argumentArrays.put(methodName, new Object[arraySize]);
        }

        Object[] objects = argumentArrays.get(methodName);
        Arrays.fill(objects, null);

        return objects;
    }


    /**
     * Retrieves the metadata about a specified method.
     *
     * @param methodName The name of the method.
     * @return The metadata or null if there was no method with the provided name.
     */
    private Method findMethod(String methodName) {
        return methodMap.getOrDefault(methodName, null);
    }


    /**
     * Reads the next method invocation from the Chronicle.
     *
     * @return true if an object was read. false otherwise.
     */
    public boolean readOne() throws Exception {
        // See if there are any objects left to read on the Chronicle

        //FIXME CH Not sure how to handle this
//        if (!_tailer.nextIndex())
//        {
//            return false;
//        }


        // Read any meta-data is added before each event is written to the Chronicle

        //TODO Re-add metadata
        //MetaData.get().readMarshallable(tailer);

        try (final DocumentContext dc = _tailer.readingDocument()) {
            // Get the name of the method and the meta data about the method
            Bytes<?> bytes = dc.wire().bytes();
            String methodName = bytes.readUtf8();
            Method m = findMethod(methodName);


            // Get the number of arguments that the method has

            int len = (int) bytes.readStopBit();
            Object[] args = null;

            if (len > 0) {
                // Get an object array to store the values for the arguments read from the Chronicle
                // and process each argument in turn storing the values in the object array

                args = getArgumentArray(m.getName(), len);

                for (int i = 0; i < len; i++) {
                    // Read the type of the argument

                    int typeInt = bytes.readUnsignedByte();

                    switch (typeInt) {
                        case 'S':
                            args[i] = bytes.readUtf8();
                            break;

                        case '1':
                            args[i] = bytes.readEnum(MarketDataSupplier.class);
                            break;

                        case '2':
                            args[i] = bytes.readEnum(MarketDataSource.class);
                            break;

                        case '3':
                            args[i] = bytes.readEnum(MarketDataField.class);
                            break;

                        case '4':
                            args[i] = bytes.readEnum(MarketDataType.class);
                            break;

                        case 'E':
                            args[i] = bytes.readEnum(Enum.class);
                            break;

                        case 'D':
                            args[i] = bytes.readDouble();
                            break;

                        case 'I':
                            args[i] = bytes.readInt();
                            break;

                        case 'L':
                            args[i] = bytes.readLong();
                            break;

                        case 'C':
                            args[i] = (char) bytes.readUnsignedByte();
                            break;

                        case 'F':
                            args[i] = bytes.readFloat();
                            break;

                        case 'T':
                            args[i] = bytes.readShort();
                            break;

                        case 'B':

                            // Deal with a byte array; first read the length of the argument
                            // array then read in the bytes that have been written

                            int byteArraySize = bytes.readInt();
                            byte[] bytesStore = new byte[byteArraySize];
                            int read = bytes.read(bytesStore);
                            args[i] = bytesStore;
                            break;

                        case 'X':
                            args[i] = dc.wire().getValueIn()
                                    .object();
                            break;

                        default:
                            throw new IORuntimeException("Unrecognised token " + typeInt + " '" + (char) typeInt + "'");
                    }
                }
            }


            // Invoke the method and return true to signal that a method was read from the Chronicle

            m.invoke(_instance, args);
        }

        return true;
    }
}