package ch.jalu.collectionbehavior;

public enum UnmodifiableSetExceptionBehavior {


    ALWAYS_THROWS,

    COLLECTIONS_SINGLETON,

    COLLECTIONS_EMPTYSET;

    public Class<? extends Exception> getNonModifyingRemoveIfExceptionOverride() {
        if (this == COLLECTIONS_SINGLETON) {
            return UnsupportedOperationException.class;
        }
        return null;
    }
}
