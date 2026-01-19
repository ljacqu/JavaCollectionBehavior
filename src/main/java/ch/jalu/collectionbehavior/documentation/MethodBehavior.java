package ch.jalu.collectionbehavior.documentation;

import ch.jalu.collectionbehavior.method.CallEffect;
import ch.jalu.collectionbehavior.method.MethodCallProperty;

import java.util.Set;

/**
 * Records a method call on a collection and the result it led to.
 */
public class MethodBehavior {

    /** The method invocation (method name and the arguments). */
    private final MethodInvocation methodInvocation;

    /** The effect the method call has on a normal mutable list. */
    // The effect is computed by copying the list being tested as an ArrayList and then checking for changes. As such,
    // the effect here might differ for the same method invocation depending on the list if it has size constraints.
    // E.g. Collections.emptyList() copies to an empty ArrayList, which may throw NoSuchElementException.
    private final CallEffect effect;

    /** If the list threw an exception for the method, the exception name. */
    private final String exception;

    private final Set<MethodCallProperty> properties;

    public MethodBehavior(MethodInvocation methodInvocation, CallEffect effect, String exception,
                          Set<MethodCallProperty> properties) {
        this.methodInvocation = methodInvocation;
        this.effect = effect;
        this.exception = exception;
        this.properties = properties;
    }

    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    public CallEffect getEffect() {
        return effect;
    }

    public String getException() {
        return exception;
    }

    public Set<MethodCallProperty> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        if (exception != null) {
            return methodInvocation.methodName() + "(" + methodInvocation.arguments() + ") "
                + exception + " " + effect;
        }
        return methodInvocation.methodName() + "(" + methodInvocation.arguments() + ") " + effect;
    }
}
