package ch.jalu.collectionbehavior;

import java.util.Collections;

/**
 * Behavior types of unmodifiable lists when methods are called that would modify the collection.
 */
public enum UnmodifiableListExceptionBehavior {

    /** Any call to a modifying method throws an exception. */
    ALWAYS_THROWS,

    /** Behavior of {@link java.util.Collections#singletonList}. */
    COLLECTIONS_SINGLETONLIST,

    /** Behavior of {@link Collections#emptyList}. */
    COLLECTIONS_EMPTYLIST;

    /**
     * Overrides the expected exception (if not null) for a call to {@link java.util.List#replaceAll} that would
     * not modify the collection.
     *
     * @param listContext list type being tested
     * @return expected exception if not consistent with usual behavior
     */
    public Class<? extends Exception> getNonModifyingReplaceAllExceptionOverride(ListContext listContext) {
        if (this == COLLECTIONS_SINGLETONLIST) {
            return UnsupportedOperationException.class;
        }
        return null;
    }

    /**
     * Overrides the expected exception (if not null) for a call to {@link java.util.List#removeIf} that would
     * not modify the collection.
     *
     * @param listContext list type being tested
     * @return expected exception if not consistent with usual behavior
     */
    public Class<? extends Exception> getNonModifyingRemoveIfExceptionOverride(ListContext listContext) {
        if (this == COLLECTIONS_SINGLETONLIST && listContext != ListContext.SUBLIST) {
            return UnsupportedOperationException.class;
        }
        return null;
    }

    /**
     * Overrides the expected exception (if not null) for a call to {@link java.util.List#sort} on a list's sublist.
     *
     * @param listContext list type being tested
     * @return expected exception if not consistent with usual behavior
     */
    public Class<? extends Exception> getSortExceptionOverride(ListContext listContext) {
        if (this == COLLECTIONS_SINGLETONLIST && listContext == ListContext.SUBLIST) {
            return UnsupportedOperationException.class;
        }
        return null;
    }
}
