package ch.jalu.collectionbehavior.model;

/**
 * Defines whether a list type (or list returned from a method) implements RandomAccess.
 */
public enum RandomAccessType {

    /** The type implements RandomAccess. */
    IMPLEMENTS,

    /**
     * Applicable to methods that create a list based on another list: the returned list <b>preserves</b> RandomAccess.
     * In other words, if the provided list implements RandomAccess, so does the returned list.
     */
    PRESERVES,

    /**
     * The type does <b>not</b> implement RandomAccess.
     */
    DOES_NOT_IMPLEMENT

}
