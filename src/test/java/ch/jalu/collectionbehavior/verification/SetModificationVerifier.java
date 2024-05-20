package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.MethodCallEffect;
import ch.jalu.collectionbehavior.model.ModificationBehavior;
import ch.jalu.collectionbehavior.model.SetMethod;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SetModificationVerifier {

    private SetModificationVerifier() {
    }

    public static void testMethods(Set<String> originalSet, ModificationBehavior expectedBehavior) {
        new SetMethodVerifier<>(originalSet, LinkedHashSet::new, expectedBehavior)
            .test(SetMethod.ADD, set -> set.add("a"))
            .test(SetMethod.ADD, set -> set.add("foo"))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of("a", "b")))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of("foo", "bar")))
            .test(SetMethod.REMOVE, set -> set.remove("zzz"))
            .test(SetMethod.REMOVE, set -> set.remove("a"))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(str -> str.equals("zzz")))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(str -> str.equals("a")))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of("fff", "xxx")))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of("fff", "a")))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(Set.of("a")))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(Set.of("qqq")))
            .test(SetMethod.CLEAR, set -> set.clear());
    }

    public static void testMethodsForSequencedSet(SequencedSet<String> originalSet, ModificationBehavior expectedBehavior) {
        new SetMethodVerifier<>(originalSet, LinkedHashSet::new, expectedBehavior)
            .test(SetMethod.ADD_FIRST, set -> set.addFirst("a"))
            .test(SetMethod.ADD_FIRST, set -> set.addFirst("foo"))
            .test(SetMethod.ADD_LAST, set -> set.addLast("a"))
            .test(SetMethod.ADD_LAST, set -> set.addLast("foo"))
            .test(SetMethod.REMOVE_FIRST, set -> set.removeFirst())
            .test(SetMethod.REMOVE_LAST, set -> set.removeLast());
    }

    public static void testMethodsForReversedSet(SequencedSet<String> reversedSet, ModificationBehavior expectedBehavior) {
        new SetMethodVerifier<>(reversedSet, LinkedHashSet::new, expectedBehavior)
            .test(SetMethod.ADD, set -> set.add("a"))
            .test(SetMethod.ADD, set -> set.add("foo"))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of("a", "b")))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of("foo", "bar")))
            .test(SetMethod.REMOVE, set -> set.remove("zzz"))
            .test(SetMethod.REMOVE, set -> set.remove("a"))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(str -> str.equals("zzz")))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(str -> str.equals("a")))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of("fff", "xxx")))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of("fff", "a")))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(Set.of("a")))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(Set.of("qqq")))
            .test(SetMethod.CLEAR, set -> set.clear())
            .test(SetMethod.ADD_FIRST, set -> set.addFirst("a"))
            .test(SetMethod.ADD_FIRST, set -> set.addFirst("foo"))
            .test(SetMethod.ADD_LAST, set -> set.addLast("a"))
            .test(SetMethod.ADD_LAST, set -> set.addLast("foo"))
            .test(SetMethod.REMOVE_FIRST, set -> set.removeFirst())
            .test(SetMethod.REMOVE_LAST, set -> set.removeLast());
    }

    private static final class SetMethodVerifier<S extends Set<String>> {

        private final S originalSet;
        private final Function<S, S> copyFunction;
        private final ModificationBehavior expectedBehavior;

        private SetMethodVerifier(S originalSet, Function<S, S> copyFunction, ModificationBehavior expectedBehavior) {
            this.originalSet = originalSet;
            this.copyFunction = copyFunction;
            this.expectedBehavior = expectedBehavior;
        }

        private SetMethodVerifier<S> test(SetMethod method, Consumer<S> action) {
            MethodCallEffect effect = determineMethodCallEffect(action);
            Class<? extends Exception> expectedException = expectedBehavior.getExpectedException(method);
            if (expectedException == null) {
                expectedException = determineExpectedException(effect);
            }

            if (expectedException != null) {
                assertThrows(
                    expectedException, () -> action.accept(originalSet),
                    () -> method + " (" + effect + ")");
            } else {
                assertDoesNotThrow(
                    () -> action.accept(originalSet),
                    () -> method + " (" + effect + ")");
            }
            return this;
        }

        private MethodCallEffect determineMethodCallEffect(Consumer<S> action) {
            // Need a SequencedSet here, because we also test its methods with this code
            S copy = copyFunction.apply(originalSet);

            try {
                action.accept(copy);
            } catch (IndexOutOfBoundsException e) {
                return MethodCallEffect.INDEX_OUT_OF_BOUNDS;
            } catch (NoSuchElementException e) {
                return MethodCallEffect.NO_SUCH_ELEMENT;
            }

            if (copy.size() != originalSet.size()) {
                return MethodCallEffect.SIZE_ALTERING;
            }
            return copy.equals(originalSet) ? MethodCallEffect.NON_MODIFYING : MethodCallEffect.MODIFYING;
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
}
