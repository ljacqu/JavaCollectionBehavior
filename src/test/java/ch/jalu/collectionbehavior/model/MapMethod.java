package ch.jalu.collectionbehavior.model;

import java.util.Map;

/**
 * Enum representing various methods available on a {@link Map}.
 */
public enum MapMethod implements CollectionMethod {

    /** {@link Map#put(Object, Object)} */
    PUT,

    /** {@link Map#putAll(Map)} */
    PUT_ALL,

    /** {@link Map#putIfAbsent(Object, Object)} */
    PUT_IF_ABSENT,

    /** {@link Map#remove(Object)} */
    REMOVE,

    /** {@link Map#remove(Object, Object)} */
    REMOVE_KEY_VALUE,

    /** {@link Map#replace(Object, Object)} */
    REPLACE,

    /** {@link Map#replace(Object, Object, Object)} */
    REPLACE_WITH_OLD_VALUE,

    /** {@link Map#replaceAll(java.util.function.BiFunction)} */
    REPLACE_ALL,

    /** {@link Map#clear()} */
    CLEAR,

    /** {@link Map#merge(Object, Object, java.util.function.BiFunction)} */
    MERGE,

    /** {@link Map#compute(Object, java.util.function.BiFunction)} */
    COMPUTE,

    /** {@link Map#computeIfAbsent(Object, java.util.function.Function)} */
    COMPUTE_IF_ABSENT,

    /** {@link Map#computeIfPresent(Object, java.util.function.BiFunction)} */
    COMPUTE_IF_PRESENT
}
