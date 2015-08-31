package jp.mufg.api.util;

import net.openhft.chronicle.ExcerptTailer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FromChronicle<T> {
    @NotNull
    private final T instance;
    private final ExcerptTailer tailer;
    private final Map<String, Method> methodMap = new HashMap<>();

    private FromChronicle(@NotNull T instance, ExcerptTailer tailer) {
        this.instance = instance;
        this.tailer = tailer;
        for (Method m : instance.getClass().getMethods())
            if (m.getDeclaringClass() != Object.class)
                methodMap.put(m.getName(), m);
    }

    @NotNull
    public static <T> FromChronicle<T> of(@NotNull T instance, ExcerptTailer tailer) {
        return new FromChronicle<>(instance, tailer);
    }

    public boolean readOne() {
        if (!tailer.nextIndex()) {
            return false;
        }
        MetaData.get().readMarshallable(tailer);
        String methodName = tailer.readUtf8();
        Method m = findMethod(methodName);
        Object[] args = null;
        int len = (int) tailer.readStopBit();
        if (len > 0) {
            args = new Object[len];
            for (int i = 0; i < len; i++)
                args[i] = tailer.readObject();
        }
//        System.out.println("Calling "+methodName+(args == null ? "()" : Arrays.toString(args)));
        try {
            m.invoke(instance, args);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return true;
    }

    private Method findMethod(String metaName) {
        return methodMap.get(metaName);
    }
}
