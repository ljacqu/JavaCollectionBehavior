package ch.jalu.collectionbehavior;

import java.util.List;
import java.util.SequencedCollection;

public enum ListDerivedType {

    /** Actual list type being described. */
    MAIN_TYPE,

    /** Type from the list's {@link List#subList}. */
    SUBLIST,

    /** Type from the list's {@link SequencedCollection#reversed}. */
    REVERSED

}
