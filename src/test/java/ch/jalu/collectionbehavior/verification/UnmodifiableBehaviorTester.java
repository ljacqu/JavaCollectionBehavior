package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.CollectionMethod;
import ch.jalu.collectionbehavior.model.MethodCallEffect;
import ch.jalu.collectionbehavior.model.ModificationBehavior;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnmodifiableBehaviorTester<C extends Collection<String>> {

    private final C originalList;
    private final Function<C, C> copyFunction;
    private final ModificationBehavior expectedBehavior;

    UnmodifiableBehaviorTester(C originalList, Function<C, C> copyFunction, ModificationBehavior expectedBehavior) {
        this.originalList = originalList;
        this.copyFunction = copyFunction;
        this.expectedBehavior = expectedBehavior;
    }

    UnmodifiableBehaviorTester<C> test(CollectionMethod method, Consumer<C> action) {
        MethodCallEffect effect = determineMethodCallEffect(action);
        Class<? extends Exception> expectedException = expectedBehavior.getExpectedException(method);
        if (expectedException == null) {
            expectedException = determineExpectedException(effect);
        }

        if (expectedException != null) {
            assertThrows(
                expectedException, () -> action.accept(originalList),
                () -> method + " (" + effect + ")");
        } else {
            assertDoesNotThrow(
                () -> action.accept(originalList),
                () -> method + " (" + effect + ")");
        }
        return this;
    }

    private MethodCallEffect determineMethodCallEffect(Consumer<C> action) {
        C copy = copyFunction.apply(originalList);

        try {
            action.accept(copy);
        } catch (IndexOutOfBoundsException e) {
            return MethodCallEffect.INDEX_OUT_OF_BOUNDS;
        } catch (NoSuchElementException e) {
            return MethodCallEffect.NO_SUCH_ELEMENT;
        }

        if (copy.size() != originalList.size()) {
            return MethodCallEffect.SIZE_ALTERING;
        }
        return copy.equals(originalList) ? MethodCallEffect.NON_MODIFYING : MethodCallEffect.MODIFYING;
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
