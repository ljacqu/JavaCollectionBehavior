package ch.jalu.collectionbehavior.v2.documentation;

import ch.jalu.collectionbehavior.v2.method.CallEffect;

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

    public MethodBehavior(MethodInvocation methodInvocation, CallEffect effect, String exception) {
        this.methodInvocation = methodInvocation;
        this.effect = effect;
        this.exception = exception;
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
