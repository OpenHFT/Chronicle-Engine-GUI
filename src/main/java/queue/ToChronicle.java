package queue;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Creates proxy objects that can be used to send events to a Chronicle.
 */
public class ToChronicle implements InvocationHandler
{
    private final Chronicle _chronicle;
    private int _sleepInMs;
    private ExcerptAppender _appender;
    private boolean _isDebuggingEnabled = false;

    /**
     * @param chronicle The Chronicle to use.
     */
    public ToChronicle(Chronicle chronicle, boolean enableDebuggingLog, int sleepInMs)
    {
        _isDebuggingEnabled = enableDebuggingLog;
        _chronicle = chronicle;
        _sleepInMs = sleepInMs;
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
    public static <T> T of(@NotNull Class<T> interfaceType, Chronicle chronicle, boolean enableDebuggingLog, int sleepInMs) throws IOException
    {
        return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, new ToChronicle(chronicle, enableDebuggingLog, sleepInMs));
    }

    /**
     * @see InvocationHandler
     */
    @Nullable
    @Override
    public synchronized Object invoke(Object proxy, @NotNull Method method, @Nullable Object[] args) throws Throwable
    {
        printDebuggingInfo("ToChronicle Invoke method ' " + method + "' Thread Id: " + Thread.currentThread().getId());

        sleep();

        if (method.getDeclaringClass() == Object.class)
        {
            return method.invoke(this, args);
        }
        else
        {
            // Lazily create the appender

            if (_appender == null)
            {
                _appender = _chronicle.createAppender();
            }

            _appender.startExcerpt();

            // Write the name of the method being invoked to Chronicle

            printDebuggingInfo("Method name: " + method.getName());
            _appender.writeUtf8(method.getName());


            // Write the arguments for the method being invoked

            if (args == null)
            {
                // Write the number of arguments that the method has

                _appender.writeStopBit(0);
            }
            else
            {
                // Write the number of arguments that the method has

                _appender.writeStopBit(args.length);


                // Add the type and value for each method argument

                for (Object arg : args)
                {
                    //We are only dealing with strings in this example
                    if (arg instanceof String)
                    {
                        _appender.writeChar('S');
                        _appender.writeUtf8((String) arg);

                        printDebuggingInfo("String: " + arg);
                    }
                }
            }


            // Write the object to Chronicle

            _appender.finish();

            printDebuggingInfo("ToChronicle was invoked!");
            return null;
        }
    }

    private void sleep()
    {
        if (_sleepInMs > 0)
        {
            try
            {
                Thread.sleep(_sleepInMs);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void printDebuggingInfo(String info)
    {
        if(_isDebuggingEnabled)
        {
            System.out.println(info);
        }
    }
}