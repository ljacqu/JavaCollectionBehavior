package ch.jalu.collectionbehavior.method;

import ch.jalu.collectionbehavior.documentation.MethodInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Invocation handler that records the call to a container and saves the method name and the arguments.
 */
final class MethodInvocationRecorder<C> implements InvocationHandler {

    private final C container;

    private String lastCalledMethod;
    private String lastCalledMethodParameters;
    private String lastArguments;

    MethodInvocationRecorder(C container) {
        this.container = container;
    }

    void invoke(MethodCall<C> call, Class<? super C> interfaceType) {
        C proxy = (C) Proxy.newProxyInstance(interfaceType.getClassLoader(),
            new Class[]{ interfaceType }, this);
        call.invoke(proxy);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        lastCalledMethod = method.getDeclaringClass().getSimpleName() + "#" + method.getName();
        lastCalledMethodParameters = toCommaSeparatedList(method.getParameterTypes(), Class::getSimpleName);
        lastArguments = args == null ? "" : toCommaSeparatedList(args, String::valueOf);

        try {
            return method.invoke(container, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IndexOutOfBoundsException
                || cause instanceof NoSuchElementException
                || cause instanceof IllegalStateException) {
                throw cause;
            }
            throw e;
        }
    }

    public MethodInvocation getLastMethodCall() {
        return new MethodInvocation(lastCalledMethod, lastCalledMethodParameters, lastArguments);
    }

    private static <T> String toCommaSeparatedList(T[] args, Function<T, String> mapper) {
        return Arrays.stream(args)
            .map(mapper)
            .collect(Collectors.joining(", "));
    }
}
