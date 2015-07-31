package queue;

import net.openhft.chronicle.*;
import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Used to read method invocations for a specified interface that are stored in a Chronicle and invoke these methods on a provided object that implements the interface.
 * <p/>
 * Please note that this class does not support interfaces that have overloaded methods.
 *
 * @param <T> The interface whose method invocations are being read.
 */
public class FromChronicle<T>
{
    @NotNull
    private final T _instance;
    private final ExcerptTailer _tailer;
    private int _sleepInMs;
    private final Map<String, Method> methodMap = new HashMap<>();
    private final Map<String, Object[]> argumentArrays = new HashMap<>(); // A map of re-usable Object arrays for each argument.

    private boolean _isDebuggingEnabled = true;

    /**
     * Creates an instance of this that can be used to read invocations of the methods for a specified interface stored in a Chronicle and
     * invoke these methods on a provided object that implements the interface.
     *
     * @param instance The object whose methods should be invoked when a method invocation is read from Chronicle.
     * @param tailer   The object to use to read method invocations from the Chronicle.
     */
    private FromChronicle(@NotNull T instance, ExcerptTailer tailer, boolean enableDebuggingLog, int sleepInMs)
    {
        _isDebuggingEnabled = enableDebuggingLog;
        _instance = instance;
        _tailer = tailer;
        _sleepInMs = sleepInMs;

        for (Method m : _instance.getClass().getMethods())
        {
            if (m.getDeclaringClass() != Object.class)
            {
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
    public static <T> FromChronicle<T> of(@NotNull T instance, ExcerptTailer tailer, boolean enableDebuggingLog, int sleepInMs)
    {

        return new FromChronicle<>(instance, tailer, enableDebuggingLog, sleepInMs);
    }


    /**
     * Gets an object array that can be used to store the arguments for a specified method.
     *
     * @param methodName The name of the method.
     * @param arraySize  The number of arguments that the method has.
     * @return The array to use
     */
    private Object[] getArgumentArray(String methodName, int arraySize)
    {
        if (!argumentArrays.containsKey(methodName))
        {
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
    private Method findMethod(String methodName)
    {
        return methodMap.getOrDefault(methodName, null);
    }

    /**
     * Reads the next method invocation from the Chronicle.
     *
     * @return true if an object was read. false otherwise.
     */
    public boolean readOne() throws Exception
    {
        // See if there are any objects left to read on the Chronicle

        sleep();

        if (!_tailer.nextIndex())
        {
            return false;
        }

        printDebuggingInfo("FromChronicle readOne - had next index...");

        // Get the name of the method and the meta data about the method

        String methodName = _tailer.readUTFΔ();
        printDebuggingInfo("Method name: " + methodName);
        Method m = findMethod(methodName);


        // Get the number of arguments that the method has

        int len = (int) _tailer.readStopBit();
        Object[] args = null;

        if (len > 0)
        {
            // Get an object array to store the values for the arguments read from the Chronicle
            // and process each argument in turn storing the values in the object array

            args = getArgumentArray(m.getName(), len);

            for (int i = 0; i < len; i++)
            {
                // Read the type of the argument

                char c = _tailer.readChar();

                switch (c)
                {
                    case 'S':
                        args[i] = _tailer.readUTFΔ();
                        printDebuggingInfo("String: " + args[i]);
                        break;
                }
            }
        }


        // Invoke the method and return true to signal that a method was read from the Chronicle

        m.invoke(_instance, args);

        printDebuggingInfo("FromChronicle readOne - successfully invoked!");

        return true;
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