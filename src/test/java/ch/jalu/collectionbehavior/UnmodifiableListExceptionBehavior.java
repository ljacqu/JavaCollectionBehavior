package ch.jalu.collectionbehavior;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.SequencedCollection;

/**
 * Behavior types of unmodifiable lists when methods are called that would modify the collection.
 */
public enum UnmodifiableListExceptionBehavior {

    /** Any call to a modifying method throws an exception. */
    ALWAYS_THROWS,

    /** Behavior of {@link java.util.Collections#singletonList}. */
    COLLECTIONS_SINGLETONLIST,

    /** Behavior of {@link Collections#emptyList}. */
    COLLECTIONS_EMPTYLIST,

    /** Behavior of Guava's ImmutableList. */
    GUAVA_IMMUTABLE_LIST;

    /**
     * Overrides the expected exception (if not null) for a call to {@link java.util.List#replaceAll} that would
     * not modify the collection.
     *
     * @param listDerivedType list type being tested
     * @return expected exception if not consistent with usual behavior
     */
    public Class<? extends Exception> getNonModifyingReplaceAllExceptionOverride(ListDerivedType listDerivedType) {
        if (this == COLLECTIONS_SINGLETONLIST) {
            return UnsupportedOperationException.class;
        } else if (this == GUAVA_IMMUTABLE_LIST && listDerivedType == ListDerivedType.REVERSED) {
            return UnsupportedOperationException.class;
        }
        return null;
    }

    /**
     * Overrides the expected exception (if not null) for a call to {@link java.util.List#removeIf} that would
     * not modify the collection.
     *
     * @param listDerivedType list type being tested
     * @return expected exception if not consistent with usual behavior
     */
    public Class<? extends Exception> getNonModifyingRemoveIfExceptionOverride(ListDerivedType listDerivedType) {
        if (this == COLLECTIONS_SINGLETONLIST && listDerivedType != ListDerivedType.SUBLIST) {
            return UnsupportedOperationException.class;
        } else if (this == GUAVA_IMMUTABLE_LIST && listDerivedType == ListDerivedType.REVERSED) {
            return UnsupportedOperationException.class;
        }
        return null;
    }

    /**
     * Overrides the expected exception (if not null) for a call to {@link java.util.List#sort} on a list's sublist.
     *
     * @param listDerivedType list type being tested
     * @return expected exception if not consistent with usual behavior
     */
    public Class<? extends Exception> getSortExceptionOverride(ListDerivedType listDerivedType) {
        if (this == COLLECTIONS_SINGLETONLIST && listDerivedType == ListDerivedType.SUBLIST) {
            return UnsupportedOperationException.class;
        }
        return null;
    }

    /**
     * Overrides the expected exception (if not null) when {@link SequencedCollection#removeFirst()} and
     * {@link SequencedCollection#removeLast()} are called.
     *
     * @return expected exception if not consistent with usual behavior
     */
    public Class<? extends Exception> getRemoveFirstLastExceptionOverride() {
        if (this == COLLECTIONS_EMPTYLIST) {
            return NoSuchElementException.class;
        }
        return null;
    }
}
