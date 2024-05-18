package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.ListMethod;
import ch.jalu.collectionbehavior.model.ModificationBehavior;
import ch.jalu.collectionbehavior.model.MethodCallEffect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ListModificationVerifier {

    private final List<String> originalList;
    private final ModificationBehavior expectedBehavior;

    ListModificationVerifier(List<String> originalList, ModificationBehavior expectedBehavior) {
        this.originalList = originalList;
        this.expectedBehavior = expectedBehavior;
    }

    void testMethods() {
        test(ListMethod.ADD, list -> list.add("foo"));
        test(ListMethod.ADD_WITH_INDEX, list -> list.add(1, "foo"));
        test(ListMethod.ADD_WITH_INDEX, list -> list.add(3, "foo"));
        test(ListMethod.ADD_ALL, list -> list.addAll(List.of("foo", "bar")));
        test(ListMethod.ADD_FIRST, list -> list.addFirst("foo"));
        test(ListMethod.ADD_LAST, list -> list.addLast("foo"));
        test(ListMethod.REMOVE, list -> list.remove("zzz"));
        test(ListMethod.REMOVE, list -> list.remove("a"));
        test(ListMethod.REMOVE_INDEX, list -> list.remove(0));
        test(ListMethod.REMOVE_INDEX, list -> list.remove(3));
        test(ListMethod.REMOVE_IF, list -> list.removeIf(str -> str.equals("zzz")));
        test(ListMethod.REMOVE_IF, list -> list.removeIf(str -> str.equals("a")));
        test(ListMethod.REMOVE_ALL, list -> list.removeAll(Set.of("fff", "xxx")));
        test(ListMethod.REMOVE_ALL, list -> list.removeAll(Set.of("fff", "a")));
        test(ListMethod.REMOVE_FIRST, list -> list.removeFirst());
        test(ListMethod.REMOVE_LAST, list -> list.removeLast());
        test(ListMethod.SET, list -> list.set(0, "foo"));
        test(ListMethod.SET, list -> list.set(3, "foo"));
        test(ListMethod.RETAIN_ALL, list -> list.retainAll(Set.of("a")));
        test(ListMethod.RETAIN_ALL, list -> list.retainAll(Set.of("qqq")));
        test(ListMethod.REPLACE_ALL, list -> list.replaceAll(s -> s));
        test(ListMethod.REPLACE_ALL, list -> list.replaceAll(String::toUpperCase));
        test(ListMethod.SORT, list -> list.sort(Comparator.naturalOrder()));
        test(ListMethod.SORT, list -> list.sort(Comparator.<String>naturalOrder().reversed()));
        test(ListMethod.CLEAR, list -> list.clear());
    }

    private void test(ListMethod method, Consumer<List<String>> action) {
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
    }

    private MethodCallEffect determineMethodCallEffect(Consumer<List<String>> action) {
        List<String> copy = new ArrayList<>(originalList);

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
