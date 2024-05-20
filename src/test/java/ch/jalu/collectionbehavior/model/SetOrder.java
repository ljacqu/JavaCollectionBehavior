package ch.jalu.collectionbehavior.model;

/**
 * Models different types of element ordering that a Set may have.
 */
public enum SetOrder {

    /**
     * Elements are kept by insertion order, i.e. in the order in which they were added to the set.
     */
    INSERTION_ORDER,

    /**
     * Elements are kept in a sorted order.
     */
    SORTED,

    /**
     * Elements are in random order.
     */
    UNORDERED

}
