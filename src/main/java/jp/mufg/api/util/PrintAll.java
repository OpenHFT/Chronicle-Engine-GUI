package jp.mufg.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class PrintAll implements InvocationHandler {
    private final Object chained;

    PrintAll(Object chained) {
        this.chained = chained;
    }

    @NotNull
    public static <T> T of(@NotNull Class<T> interfaceType) {
        return of(interfaceType, null);
    }

    @NotNull
    public static <T> T of(@NotNull Class<T> interfaceType, T t) {
        return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, new PrintAll(t));
    }

    @Nullable
    @Override
    public Object invoke(Object proxy, @NotNull Method method, @Nullable Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        System.out.println(method.getName() + (args == null ? "()" : Arrays.toString(args)));
        if (chained != null)
            return method.invoke(chained, args);
        return null;
    }
}
