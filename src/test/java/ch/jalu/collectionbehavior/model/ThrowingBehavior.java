package ch.jalu.collectionbehavior.model;

public enum ThrowingBehavior {

    /** Always throws an UnsupportedOperationException. */
    ALWAYS_THROWS,

    /** Throws an UnsupportedOperationException only if the collection would be modified by the call. */
    THROW_ONLY_IF_CHANGE,

    /**
     * Throws an IndexOutOfBoundsException if the index is invalid; otherwise throws an
     * UnsupportedOperationException if the collection would be modified by the call.
     */
    THROW_INDEX_OUT_OF_BOUNDS_OR_IF_CHANGE

}
