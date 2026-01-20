package ch.jalu.collectionbehavior.method;

import ch.jalu.collectionbehavior.documentation.MethodInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Invocation handler that records the call to a list and saves the method name and the arguments.
 */
public final class MethodInvocationRecorder<C> implements InvocationHandler {

    private final C container;

    private String lastCalledMethod;
    private String lastCalledMethodParameters;
    private String lastArguments;

    public MethodInvocationRecorder(C container) {
        this.container = container;
    }

    public void invoke(MethodCall<C> call, Class<? super C> interfaceType) {
        C proxy = (C) Proxy.newProxyInstance(interfaceType.getClassLoader(),
            new Class[]{ interfaceType }, this);
        call.invoke(proxy);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        lastCalledMethod = method.getDeclaringClass().getSimpleName() + "#" + method.getName();

        if (method.getParameterCount() > 0) {
            lastCalledMethodParameters = Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
        } else {
            lastCalledMethodParameters = "";
        }

        if (args == null) {
            lastArguments = "";
        } else {
            lastArguments = Arrays.stream(args)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        }

        try {
            return method.invoke(container, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IndexOutOfBoundsException iob) {
                throw iob;
            } else if (e.getCause() instanceof NoSuchElementException nse) {
                throw nse;
            } else if (e.getCause() instanceof IllegalStateException ise) {
                throw ise;
            }
            throw e;
        }
    }

    public MethodInvocation getLastMethodCall() {
        return new MethodInvocation(lastCalledMethod, lastCalledMethodParameters, lastArguments);
    }
}
