package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.CollectionMethod;
import ch.jalu.collectionbehavior.model.MethodCallEffect;
import ch.jalu.collectionbehavior.model.ModificationBehavior;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class UnmodifiableContainerBehaviorTester<C> {

    private final C originalContainer;
    private final Function<C, C> copyFunction;
    private final ModificationBehavior expectedBehavior;
    private final boolean copyOriginalForEqualsCheck;

    UnmodifiableContainerBehaviorTester(C originalContainer, Function<C, C> copyFunction,
                                        ModificationBehavior expectedBehavior) {
        this(originalContainer, copyFunction, expectedBehavior, false);
    }

    UnmodifiableContainerBehaviorTester(C originalContainer, Function<C, C> copyFunction,
                                        ModificationBehavior expectedBehavior, boolean copyOriginalForEqualsCheck) {
        this.originalContainer = originalContainer;
        this.copyFunction = copyFunction;
        this.expectedBehavior = expectedBehavior;
        this.copyOriginalForEqualsCheck = copyOriginalForEqualsCheck;
    }

    UnmodifiableContainerBehaviorTester<C> test(CollectionMethod method, Consumer<C> action) {
        MethodCallEffect effect = determineMethodCallEffect(action);
        Class<? extends Exception> manuallyDefinedException = expectedBehavior.getExpectedException(method, effect);
        Class<? extends Exception> expectedException = determineExpectedException(effect);
        if (manuallyDefinedException != null) {
            if (manuallyDefinedException.equals(expectedException)) {
                throw new IllegalStateException("Exception for " + method + ", " + effect
                    + " was registered, but it is in line with the defined behavior!");
            }
            expectedException = manuallyDefinedException;
        }

        if (expectedException != null) {
            assertThrows(
                expectedException, () -> action.accept(originalContainer),
                () -> method + " (" + effect + ")");
        } else {
            assertDoesNotThrow(
                () -> action.accept(originalContainer),
                () -> method + " (" + effect + ")");
        }
        return this;
    }

    protected abstract int getSize(C container);

    private MethodCallEffect determineMethodCallEffect(Consumer<C> action) {
        C copy = copyFunction.apply(originalContainer);

        try {
            action.accept(copy);
        } catch (IndexOutOfBoundsException e) {
            return MethodCallEffect.INDEX_OUT_OF_BOUNDS;
        } catch (NoSuchElementException e) {
            return MethodCallEffect.NO_SUCH_ELEMENT;
        }

        if (getSize(copy) != getSize(originalContainer)) {
            return MethodCallEffect.SIZE_ALTERING;
        }

        boolean copyIsEqualToOriginal = copyOriginalForEqualsCheck
            ? copy.equals(copyFunction.apply(originalContainer))
            : copy.equals(originalContainer);
        return copyIsEqualToOriginal ? MethodCallEffect.NON_MODIFYING : MethodCallEffect.MODIFYING;
    }

    private Class<? extends Exception> determineExpectedException(MethodCallEffect effect) {
        return switch (effect) {
            case MODIFYING -> expectedBehavior.throwsOnModification
                ? UnsupportedOperationException.class
                : null;

            case SIZE_ALTERING -> expectedBehavior.throwsOnSizeModification
                ? UnsupportedOperationException.class
                : null;

            case NON_MODIFYING -> expectedBehavior.throwsOnNonModifyingModificationMethods
                ? UnsupportedOperationException.class
                : null;

            case INDEX_OUT_OF_BOUNDS -> expectedBehavior.throwsUnsupportedOperationExceptionForInvalidIndex
                ? UnsupportedOperationException.class
                : IndexOutOfBoundsException.class;

            case NO_SUCH_ELEMENT -> NoSuchElementException.class;
        };
    }
}
