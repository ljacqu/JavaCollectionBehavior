package ch.jalu.collectionbehavior.v2.method;

/**
 * Categorizes the effect a method has on a collection.
 */
public enum CallEffect {

    /**
     * The collection is modified, but its size has not changed.
     */
    MODIFYING,

    /**
     * The collection's size was changed, i.e. it was modified by adding or removing at least one entry.
     */
    SIZE_ALTERING,

    /**
     * The call did not produce any modification to the collection.
     */
    NON_MODIFYING,

    /**
     * An {@link IndexOutOfBoundsException} was thrown.
     */
    INDEX_OUT_OF_BOUNDS,

    /**
     * An {@link java.util.NoSuchElementException} was thrown.
     */
    NO_SUCH_ELEMENT,

}
