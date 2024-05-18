package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.ListModificationBehavior;
import ch.jalu.collectionbehavior.model.MethodCallEffect;
import ch.jalu.collectionbehavior.model.SetMethod;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class SetModificationVerifier {

    private final Set<String> originalSet;
    private final ListModificationBehavior expectedBehavior;

    SetModificationVerifier(Set<String> originalSet, ListModificationBehavior expectedBehavior) {
        this.originalSet = originalSet;
        this.expectedBehavior = expectedBehavior;
    }

    void testMethods() {
        test(SetMethod.ADD, set -> set.add("a"));
        test(SetMethod.ADD, set -> set.add("foo"));
        test(SetMethod.ADD_ALL, set -> set.addAll(List.of("a", "b")));
        test(SetMethod.ADD_ALL, set -> set.addAll(List.of("foo", "bar")));
        test(SetMethod.REMOVE, set -> set.remove("zzz"));
        test(SetMethod.REMOVE, set -> set.remove("a"));
        test(SetMethod.REMOVE_IF, set -> set.removeIf(str -> str.equals("zzz")));
        test(SetMethod.REMOVE_IF, set -> set.removeIf(str -> str.equals("a")));
        test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of("fff", "xxx")));
        test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of("fff", "a")));
        test(SetMethod.RETAIN_ALL, set -> set.retainAll(Set.of("a")));
        test(SetMethod.RETAIN_ALL, set -> set.retainAll(Set.of("qqq")));
        test(SetMethod.CLEAR, set -> set.clear());

        if (originalSet instanceof SequencedSet<String>) {
            test(SetMethod.ADD_FIRST, set -> ((SequencedSet<String>) set).addFirst("a"));
            test(SetMethod.ADD_FIRST, set -> ((SequencedSet<String>) set).addFirst("foo"));
            test(SetMethod.ADD_LAST, set -> ((SequencedSet<String>) set).addLast("a"));
            test(SetMethod.ADD_LAST, set -> ((SequencedSet<String>) set).addLast("foo"));
            test(SetMethod.REMOVE_FIRST, set -> ((SequencedSet<String>) set).removeFirst());
            test(SetMethod.REMOVE_LAST, set -> ((SequencedSet<String>) set).removeLast());
        }
    }

    private void test(SetMethod method, Consumer<Set<String>> action) {
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
    }

    private MethodCallEffect determineMethodCallEffect(Consumer<Set<String>> action) {
        // Need a SequencedSet here, because we also test its methods with this code
        Set<String> copy = new LinkedHashSet<>(originalSet);

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
