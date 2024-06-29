package ch.jalu.collectionbehavior.model;

/**
 * Categorizes the effect a method call has on a collection.
 */
public enum MethodCallEffect {

    /**
     * The method call modified one or more entries. Note that {@link #SIZE_ALTERING} is used
     * if the size of the collection changed due to the call.
     */
    MODIFYING,

    /**
     * The method call added or removed entries in the collection.
     */
    SIZE_ALTERING,

    /**
     * The method call did not modify anything.
     */
    NON_MODIFYING,

    /**
     * The method call threw an index out of bounds exception.
     */
    INDEX_OUT_OF_BOUNDS,

    /**
     * The method call threw {@link java.util.NoSuchElementException}. (This should only happen on empty
     * collections as this scenario is otherwise not interesting to test.)
     */
    NO_SUCH_ELEMENT

}
