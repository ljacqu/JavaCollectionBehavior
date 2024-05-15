package ch.jalu.collectionbehavior.model;

import ch.jalu.collectionbehavior.ListDerivedType;

import java.util.NoSuchElementException;

public enum ListBehaviorType {

    DEFAULT,
    COLLECTIONS_SINGLETONLIST,
    COLLECTIONS_EMPTYLIST,
    ARRAYS_ASLIST,
    GUAVA_IMMUTABLE_LIST;


    // eo = exception override

    public Class<? extends Exception> eoNonModifyingRemoveIf(ListDerivedType listDerivedType) {
        if (this == GUAVA_IMMUTABLE_LIST && listDerivedType == ListDerivedType.REVERSED) {
            return UnsupportedOperationException.class;
        } else if (this == COLLECTIONS_SINGLETONLIST
                    && (listDerivedType == ListDerivedType.MAIN_TYPE || listDerivedType == ListDerivedType.REVERSED)) {
            return UnsupportedOperationException.class;
        }
        return null;
    }

    public Class<? extends Exception> eoNonModifyingReplaceAll(ListDerivedType listDerivedType) {
        if (this == GUAVA_IMMUTABLE_LIST && listDerivedType == ListDerivedType.REVERSED) {
            return UnsupportedOperationException.class;
        } else if (this == COLLECTIONS_SINGLETONLIST) {
            return UnsupportedOperationException.class;
        }
        return null;
    }

    public Class<? extends Exception> eoRemoveFirstLast() {
        if (this == COLLECTIONS_EMPTYLIST) {
            return NoSuchElementException.class;
        }
        return null;
    }

    public Class<? extends Exception> eoSort(ListDerivedType listDerivedType) {
        if (this == COLLECTIONS_SINGLETONLIST && listDerivedType == ListDerivedType.SUBLIST) {
            return UnsupportedOperationException.class;
        }
        return null;
    }
}
