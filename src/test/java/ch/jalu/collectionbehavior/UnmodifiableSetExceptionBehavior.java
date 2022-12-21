package ch.jalu.collectionbehavior;

/**
 * Behavior types of unmodifiable sets when methods are called that would modify the collection.
 */
public enum UnmodifiableSetExceptionBehavior {

    /** Any modifying method that is called will throw an exception. */
    ALWAYS_THROWS,

    /** Behavior of {@link java.util.Collections#singleton}. */
    COLLECTIONS_SINGLETON,

    /** Behavior of {@link java.util.Collections#emptySet}. */
    COLLECTIONS_EMPTYSET;

    /**
     * Expected exception type when calling {@link java.util.Set#removeIf} with arguments that don't modify the
     * collection. This method returns the expected exception type when the behavior is not consistent with the
     * rest of the methods.
     *
     * @return expected exception type (overridden) or null if the usual behavior is expected
     */
    public Class<? extends Exception> getNonModifyingRemoveIfExceptionOverride() {
        if (this == COLLECTIONS_SINGLETON) {
            return UnsupportedOperationException.class;
        }
        return null;
    }
}
