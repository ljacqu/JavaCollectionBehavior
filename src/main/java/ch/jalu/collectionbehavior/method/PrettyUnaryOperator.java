package ch.jalu.collectionbehavior.method;

import java.util.function.UnaryOperator;

/**
 * Unary operator with a custom toString for nicer printing.
 *
 * @param <T> the type of the operand and result of the operator
 */
public class PrettyUnaryOperator<T> implements UnaryOperator<T> {

    private final UnaryOperator<T> function;
    private final String toString;

    public PrettyUnaryOperator(UnaryOperator<T> function, String toString) {
        this.function = function;
        this.toString = toString;
    }

    @Override
    public T apply(T t) {
        return function.apply(t);
    }

    @Override
    public String toString() {
        return toString;
    }
}
