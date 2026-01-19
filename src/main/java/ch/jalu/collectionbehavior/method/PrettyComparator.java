package ch.jalu.collectionbehavior.method;

import java.util.Comparator;

/**
 * Comparator with a custom toString for nicer printing.
 *
 * @param <T> the type of objects that may be compared by this comparator
 */
public class PrettyComparator<T> implements Comparator<T> {

    private final Comparator<T> comparator;
    private final String toString;

    public PrettyComparator(Comparator<T> comparator, String toString) {
        this.comparator = comparator;
        this.toString = toString;
    }

    @Override
    public int compare(T o1, T o2) {
        return comparator.compare(o1, o2);
    }

    @Override
    public String toString() {
        return toString;
    }
}
