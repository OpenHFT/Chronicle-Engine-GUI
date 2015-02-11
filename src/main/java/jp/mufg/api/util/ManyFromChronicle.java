package jp.mufg.api.util;

import net.openhft.chronicle.ExcerptTailer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManyFromChronicle<T> {
    @NotNull
    private final List<T> instances;
    private final ExcerptTailer tailer;
    private final Map<String, Method> methodMap = new HashMap<>();

    private ManyFromChronicle(@NotNull List<T> instances, ExcerptTailer tailer) {
        this.instances = instances;
        this.tailer = tailer;
        for (Method m : instances.get(0).getClass().getMethods())
            if (m.getDeclaringClass() != Object.class)
                methodMap.put(m.getName(), m);
    }

    @NotNull
    public static <T> ManyFromChronicle<T> of(@NotNull List<T> instances, ExcerptTailer tailer) {
        return new ManyFromChronicle<>(instances, tailer);
    }

    public boolean readOne() {
        if (!tailer.nextIndex()) {
            return false;
        }
        MetaData.get().readMarshallable(tailer);
        String methodName = tailer.readUTFΔ();
        Method m = findMethod(methodName);
        Object[] args = null;
        int len = (int) tailer.readStopBit();
        if (len > 0) {
            args = new Object[len];
            for (int i = 0; i < len; i++)
                args[i] = tailer.readObject();
        }
        try {
            for (T instance : instances) {
                m.invoke(instance, args);
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (RuntimeException re) {
            System.err.println("Calling " + methodName + (args == null ? "()" : Arrays.toString(args)));
            throw re;
        }
        return true;
    }

    private Method findMethod(String metaName) {
        return methodMap.get(metaName);
    }
}
