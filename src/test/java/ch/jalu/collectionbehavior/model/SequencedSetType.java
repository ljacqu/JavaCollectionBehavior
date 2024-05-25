package ch.jalu.collectionbehavior.model;

/**
 * Defines whether a Set type implements the SequencedSet interface.
 */
public enum SequencedSetType {

    /** The type implements SequencedSet. */
    IMPLEMENTS,

    /**
     * The type implements SequencedSet, but methods relating to first and last entries throw an exception
     * because the set has implicit sorting.
     */
    IMPLEMENTS_W_IMPLICIT_ORDERING,

    /** The type does not implement SequencedSet. */
    DOES_NOT_IMPLEMENT

}
