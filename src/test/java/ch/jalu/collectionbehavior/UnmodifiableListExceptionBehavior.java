package ch.jalu.collectionbehavior;

public enum UnmodifiableListExceptionBehavior {

    ALWAYS_THROWS,

    COLLECTIONS_SINGLETONLIST,

    COLLECTIONS_EMPTYLIST;

    public Class<? extends Exception> getNonModifyingReplaceAllExceptionOverride() {
        if (this == COLLECTIONS_SINGLETONLIST) {
            return UnsupportedOperationException.class;
        }
        return null;
    }

    public Class<? extends Exception> getNonModifyingRemoveIfExceptionOverride() {
        if (this == COLLECTIONS_SINGLETONLIST) {
            return UnsupportedOperationException.class;
        }
        return null;
    }

    public Class<? extends Exception> getNonModifyingReplaceAllSubListExOverride() {
        if (this == COLLECTIONS_SINGLETONLIST) {
            return UnsupportedOperationException.class;
        }
        return null;
    }

    public Class<? extends Exception> getSortSubListExOverride() {
        if (this == COLLECTIONS_SINGLETONLIST) {
            return UnsupportedOperationException.class;
        }
        return null;
    }
}
