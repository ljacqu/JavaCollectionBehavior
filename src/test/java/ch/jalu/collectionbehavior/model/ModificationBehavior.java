package ch.jalu.collectionbehavior.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Defines the expected behavior of a collection type with regard to how it can be modified, or which exceptions are
 * thrown if the collection is unmodifiable.
 * <p>
 * For example, certain unmodifiable list implementations only throw an exception if the call would effectively
 * modify the list, while others unconditionally throw in methods like {@code List#remove}.
 */
public final class ModificationBehavior {

    /**
     * True if the described type is <b>immutable</b>. False does not imply that the type is modifiable! This flag
     * defines whether a collection cannot be changed in any way, unlike Java's definition of "unmodifiable".
     */
    public final boolean isImmutable;

    /**
     * If true, the expected behavior is that an {@link UnsupportedOperationException} is thrown when indexes are
     * referenced that are out of bounds. If false, the usual {@link IndexOutOfBoundsException} is expected.
     * This only applies to lists (sets and maps have no methods referencing indexes).
     */
    public boolean throwsUnsupportedOperationExceptionForInvalidIndex;

    /**
     * Throws an exception if a call is made that would modify an entry in the collection. If the size of the collection
     * changes with the processed modification, {@link #throwsOnSizeModification} should be considered instead.
     */
    public final boolean throwsOnModification;

    /**
     * Throws an exception if a call leads to the collection's size changing (i.e. adding or removing entries).
     */
    public final boolean throwsOnSizeModification;

    /**
     * If true, an exception is expected from methods that can modify the collection even if the supplied arguments
     * would not modify it.
     */
    public final boolean throwsOnNonModifyingModificationMethods;

    /**
     * Contains (method, effect) pairs that are expected to throw an exception that is not in line with the rules
     * generally defined in this behavior object. This acts to document exceptions on types that have false for
     * {@link #throwsOnNonModifyingModificationMethods}.
     */
    private final Table<CollectionMethod, MethodCallEffect, Class<? extends Exception>> exceptionExceptions =
        HashBasedTable.create();

    private ModificationBehavior(boolean isImmutable, boolean throwsOnModification,
                                 boolean throwsOnSizeModification, boolean throwsOnNonModifyingModificationMethods) {
        this.isImmutable = isImmutable;
        this.throwsOnModification = throwsOnModification;
        this.throwsOnSizeModification = throwsOnSizeModification;
        this.throwsOnNonModifyingModificationMethods = throwsOnNonModifyingModificationMethods;
    }

    /**
     * Returns a behavior definition object that expects the collection to be fully modifiable.
     *
     * @return modifiable behavior object
     */
    public static ModificationBehavior mutable() {
        return new ModificationBehavior(false, false, false, false);
    }

    /**
     * Creates a builder to create a behavior definition for an immutable collection type.
     *
     * @return builder for immutable type
     */
    public static Builder immutable() {
        return new Builder(true);
    }

    /**
     * Creates a builder to create a behavior definition for an unmodifiable collection type. "Unmodifiable" differs
     * from "immutable" in that an unmodifiable collection can be changed by altering its backing structure.
     *
     * @return builder for unmodifiable type
     */
    public static Builder unmodifiable() {
        return new Builder(false);
    }

    /**
     * Registers methods that throw the given exception for cases that are not in line with the general behavior.
     * This method returns a builder to which the methods can be passed.
     *
     * @param expectedException exception type that some methods throw (special cases)
     * @return exception registration builder to pass methods to
     */
    public ExceptionRegistration butThrows(Class<? extends Exception> expectedException) {
        return new ExceptionRegistration(expectedException);
    }

    /**
     * For list methods taking an index, if the provided index is out of bounds, an
     * {@link UnsupportedOperationException} is expected to be thrown instead of the usual
     * {@link IndexOutOfBoundsException}.
     *
     * @return this instance, for chaining
     */
    public ModificationBehavior throwsUnsupportedOpForIndexOutOfBounds() {
        this.throwsUnsupportedOperationExceptionForInvalidIndex = true;
        return this;
    }

    /**
     * Returns a "manual" expected exception for the given method, if applicable. Used when single methods behave
     * differently from the usual behavior.
     *
     * @param method the method to check for
     * @param effect the effect the method has (or would have) on the list
     * @return the expected exception if it does not conform to the behavior generally defined, otherwise null
     */
    public Class<? extends Exception> getExpectedException(CollectionMethod method, MethodCallEffect effect) {
        return exceptionExceptions.get(method, effect);
    }

    /**
     * @return true if this behavior definition is for a modifiable collection; false if it's unmodifiable
     */
    public boolean isMutable() {
        return !throwsOnModification && !throwsOnSizeModification;
    }

    public final class ExceptionRegistration {

        private final Class<? extends Exception> expectedException;

        private ExceptionRegistration(Class<? extends Exception> expectedException) {
            this.expectedException = expectedException;
        }

        /**
         * The exception is thrown for the given methods, when the effect of the method is as specified.
         *
         * @param effect the effect the methods have on the collection
         * @param methods the methods for which the exception should be registered
         * @return modification behavior object this object originated from
         */
        public ModificationBehavior on(MethodCallEffect effect, CollectionMethod... methods) {
            for (CollectionMethod method : methods) {
                exceptionExceptions.put(method, effect, expectedException);
            }
            return ModificationBehavior.this;
        }
    }

    /** Builder for unmodifiable/immutable behavior definitions. */
    public static final class Builder {

        private final boolean isImmutable;

        Builder(boolean isImmutable) {
            this.isImmutable = isImmutable;
        }

        /**
         * Methods that (potentially) modify the collection are expected to always throw an exception.
         *
         * @return behavior definition
         */
        public ModificationBehavior alwaysThrows() {
            return new ModificationBehavior(isImmutable,true, true, true);
        }

        /**
         * Methods that (potentially) modify the collection are expected to only throw an exception if the arguments
         * would actually lead to a modification.
         *
         * @return behavior definition
         */
        public ModificationBehavior throwsIfWouldBeModified() {
            return new ModificationBehavior(isImmutable, true, true, false);
        }

        /**
         * Creates a behavior definition that only throws exceptions on methods that add or remove entries. Existing
         * entries can be modified without exceptions.
         *
         * @return behavior definition
         */
        public ModificationBehavior throwsOnSizeModification() {
            Preconditions.checkState(!isImmutable);
            return new ModificationBehavior(isImmutable, false, true, false);
        }
    }
}
