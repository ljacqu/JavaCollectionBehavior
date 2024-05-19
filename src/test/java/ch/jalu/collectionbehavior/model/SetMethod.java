package ch.jalu.collectionbehavior.model;

import java.util.SequencedSet;
import java.util.Set;

/**
 * Enum representing various methods available on a {@link Set} and its extensions.
 */
public enum SetMethod implements CollectionMethod {

    /** {@link Set#add(Object)} */
    ADD,

    /** {@link Set#addAll(java.util.Collection)} */
    ADD_ALL,

    /** {@link Set#remove(Object)} */
    REMOVE,

    /** {@link Set#removeIf(java.util.function.Predicate)} */
    REMOVE_IF,

    /** {@link Set#removeAll(java.util.Collection)} */
    REMOVE_ALL,

    /** {@link Set#retainAll(java.util.Collection)} */
    RETAIN_ALL,

    /** {@link Set#clear()} */
    CLEAR,


    // SequencedSet

    /** {@link SequencedSet#addFirst(Object)} */
    ADD_FIRST,

    /** {@link SequencedSet#addLast(Object)} */
    ADD_LAST,

    /** {@link SequencedSet#removeFirst()} */
    REMOVE_FIRST,

    /** {@link SequencedSet#removeLast()} */
    REMOVE_LAST

}
