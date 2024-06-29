package ch.jalu.collectionbehavior.model;

import java.util.List;

/**
 * Enum representing various methods available on a {@link List}.
 */
public enum ListMethod implements CollectionMethod {

    /** {@link List#add(Object)} */
    ADD,

    /** {@link List#add(int, Object)} */
    ADD_WITH_INDEX,

    /** {@link List#addAll(java.util.Collection)} */
    ADD_ALL,

    /** {@link List#addFirst(Object)} */
    ADD_FIRST,

    /** {@link List#addLast(Object)} */
    ADD_LAST,

    /** {@link List#remove(Object)} */
    REMOVE,

    /** {@link List#remove(int)} */
    REMOVE_INDEX,

    /** {@link List#removeIf(java.util.function.Predicate)} */
    REMOVE_IF,

    /** {@link List#removeAll(java.util.Collection)} */
    REMOVE_ALL,

    /** {@link List#removeFirst()} */
    REMOVE_FIRST,

    /** {@link List#removeLast()} */
    REMOVE_LAST,

    /** {@link List#set(int, Object)} */
    SET,

    /** {@link List#retainAll(java.util.Collection)} */
    RETAIN_ALL,

    /** {@link List#replaceAll(java.util.function.UnaryOperator)} */
    REPLACE_ALL,

    /** {@link List#sort(java.util.Comparator)} */
    SORT,

    /** {@link List#clear()} */
    CLEAR
}
