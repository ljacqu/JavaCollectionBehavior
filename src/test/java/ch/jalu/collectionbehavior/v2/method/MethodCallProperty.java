package ch.jalu.collectionbehavior.v2.method;

/**
 * Properties to annotate various method calls that are being tested.
 */
public enum MethodCallProperty {

    /** The call is made with {@code null} as argument. */
    NULL_ARGUMENT,

    /** The call has an empty collection as its argument. */
    EMPTY_COLLECTION_ARGUMENT,

    /** The call is to a method that never modifies the collection. */
    READ_METHOD,

}
