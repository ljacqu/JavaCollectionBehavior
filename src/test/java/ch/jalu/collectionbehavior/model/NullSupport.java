package ch.jalu.collectionbehavior.model;

/**
 * Defines the null support of a collection.
 */
public enum NullSupport {

    /**
     * Full null support: collection can hold null as element, methods can be called with null arguments
     * (where reasonable).
     */
    FULL,

    /**
     * Null elements may not be part of the collection, but methods can be called with null
     * (e.g. {@code list.contains(null)}).
     */
    ARGUMENTS,

    /**
     * Null elements may not be part of the collection, and calling methods with null arguments also results
     * in an exception (e.g. {@code list.contains(null)}).
     */
    REJECT

}
