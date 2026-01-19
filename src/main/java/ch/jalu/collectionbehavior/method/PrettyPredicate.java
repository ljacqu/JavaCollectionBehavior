package ch.jalu.collectionbehavior.method;

import java.util.function.Predicate;

/**
 * Predicate with a custom toString for nicer printing.
 *
 * @param <T> the type of the input to the predicate
 */
public class PrettyPredicate<T> implements Predicate<T> {

    private final Predicate<T> predicate;
    private final String toString;

    public PrettyPredicate(Predicate<T> predicate, String toString) {
        this.predicate = predicate;
        this.toString = toString;
    }

    @Override
    public boolean test(T t) {
        return predicate.test(t);
    }

    @Override
    public String toString() {
        return toString;
    }
}
