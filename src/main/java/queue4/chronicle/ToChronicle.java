package queue4.chronicle;


import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import queue4.externalizableObjects.MarketDataField;
import queue4.externalizableObjects.MarketDataSource;
import queue4.externalizableObjects.MarketDataSupplier;
import queue4.externalizableObjects.MarketDataType;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Creates proxy objects that can be used to send events to a Chronicle.
 */
public class ToChronicle implements InvocationHandler {
    private final SingleChronicleQueue _chronicle;
    private ExcerptAppender _appender;
    private Long _maxMessageSize = null;


    /**
     * @param chronicle The Chronicle to use.
     */
    public ToChronicle(SingleChronicleQueue chronicle, Long maxMessageSize) {
        _chronicle = chronicle;
        _maxMessageSize = maxMessageSize;
    }


    /**
     * Creates a proxy object for a specified interface.
     *
     * @param interfaceType The type that a proxy should be created for.
     * @param chronicle     The chronicle to send events to.
     * @param <T>           The type of interface to create a proxy for. Please note that this class does not support interfaces that have overloaded methods.
     * @return The proxy object.
     * @throws IOException If there were any issues creating the proxy class.
     */
    @NotNull
    public static <T> T of(@NotNull Class<T> interfaceType, SingleChronicleQueue chronicle) throws IOException {
        return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, new ToChronicle(chronicle, null));
    }


    @NotNull
    public static <T> T of(@NotNull Class<T> interfaceType, SingleChronicleQueue chronicle, long maxMessageSize) throws IOException {
        return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, new ToChronicle(chronicle, maxMessageSize));
    }


    /**
     * @see InvocationHandler
     */
    @Nullable
    @Override
    public synchronized Object invoke(Object proxy, @NotNull Method method, @Nullable Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        } else {
            // Lazily create the appender
            if (_appender == null) {
                _appender = _chronicle.createAppender();//  TODO .methodWriter(); and pass in the interface want to write
            }

            //Only need to set for large messages
            // FIXME How to handle the below?  Is this still required?
//            if (_maxMessageSize != null)
//            {
//                _appender.startExcerpt(_maxMessageSize);
//            }
//            else
//            {
//                _appender.startExcerpt();
//            }

            try (final DocumentContext dc = _appender.writingDocument()) {

                // Add any meta-data that we would like added before each event is written to the Chronicle

                //TODO Meta Data not required as queue4 takes care of this
                //MetaData.get().writeMarshallable(appender);


                // Write the name of the method being invoked to Chronicle
                Bytes<?> bytes = dc.wire().bytes();
                bytes.writeUtf8(method.getName());


                // Write the arguments for the method being invoked

                if (args == null) {
                    // Write the number of arguments that the method has
                    bytes.writeStopBit(0);
                } else {
                    // Write the number of arguments that the method has
                    bytes.writeStopBit(args.length);


                    // Add the type and value for each method argument

                    for (Object arg : args) {
                        if (arg instanceof String) {
                            bytes.writeUnsignedByte('S');
                            bytes.writeUtf8((String) arg);
                        } else if (arg instanceof Enum) {
                            if (arg instanceof MarketDataSupplier) {
                                bytes.writeUnsignedByte('1');
                            } else if (arg instanceof MarketDataSource) {
                                bytes.writeUnsignedByte('2');
                            } else if (arg instanceof MarketDataField) {
                                bytes.writeUnsignedByte('3');
                            } else if (arg instanceof MarketDataType) {
                                bytes.writeUnsignedByte('4');
                            } else {
                                bytes.writeUnsignedByte('E');
                            }

                            bytes.writeEnum((Enum) arg);
                        } else if (arg instanceof byte[]) {
                            bytes.writeUnsignedByte('B');
                            bytes.writeInt(((byte[]) arg).length);
                            bytes.write((byte[]) arg);
                        } else if (arg instanceof Integer) {
                            bytes.writeUnsignedByte('I');
                            bytes.writeInt((int) arg);
                        } else if (arg instanceof Long) {
                            bytes.writeUnsignedByte('L');
                            bytes.writeLong((long) arg);
                        } else if (arg instanceof Character) {
                            bytes.writeUnsignedByte('C');
                            bytes.writeInt((char) arg);
                        } else if (arg instanceof Float) {
                            bytes.writeUnsignedByte('F');
                            bytes.writeFloat((float) arg);
                        } else if (arg instanceof Short) {
                            bytes.writeUnsignedByte('T');
                            bytes.writeShort((short) arg);
                        } else {
                            bytes.writeUnsignedByte('X');
                            dc.wire().getValueOut().object(arg);
                        }
                    }
                }
            }
            return null;
        }
    }
}