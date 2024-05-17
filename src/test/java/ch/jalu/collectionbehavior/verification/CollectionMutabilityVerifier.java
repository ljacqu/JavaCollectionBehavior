package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.ListCreator;
import ch.jalu.collectionbehavior.model.ListModificationBehavior;
import ch.jalu.collectionbehavior.model.ListWithBackingDataModifier;
import org.junit.jupiter.api.DynamicTest;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Verifies the mutability of collections.
 */
public final class CollectionMutabilityVerifier {

    private CollectionMutabilityVerifier() {
    }

    /**
     * Returns tests to run for a list creator whose type should be fully modifiable.
     *
     * @param listCreator list creator for a modifiable type
     * @return tests to run
     */
    public static List<DynamicTest> createTestsForMutableAssertions(ListCreator listCreator) {
        List<String> emptyList = listCreator.createList();
        return List.of(
            dynamicTest("mutable", () -> verifyListIsMutable(emptyList)),
            dynamicTest("mutable_iterators", () -> verifyIsMutableByIteratorAndListIterator(emptyList)),
            dynamicTest("mutable_subList", () -> verifyIsMutableBySubList(emptyList)),
            dynamicTest("mutable_reversed", () -> verifyIsMutableByReversedList(emptyList)));
    }

    private static void verifyListIsMutable(List<String> list) {
        assertThat(list, empty()); // Validate method contract

        // List#add, List#addAll, List#addFirst, List#addLast
        list.add("a");
        list.add("b");
        list.add("f");
        list.add(2, "c");
        assertThat(list, contains("a", "b", "c", "f"));
        list.addAll(List.of("a", "b", "Y", "X"));
        list.addFirst("e");
        list.addLast("b");
        assertThat(list, contains("e", "a", "b", "c", "f", "a", "b", "Y", "X", "b"));

        // List#remove, List#removeAll, List#removeIf, List#removeFirst, List#removeLast
        list.remove("b");                      // e, a, c, f, a, b, Y, X, b
        list.remove(2);                        // e, a, f, a, b, Y, X, b
        list.removeAll(List.of("a"));          // e, f, b, Y, X, b
        assertThat(list, contains("e", "f", "b", "Y", "X", "b"));
        list.removeIf(str -> str.equals("Y")); // e, f, b, X, b
        list.removeFirst(); // f, b, X, b
        list.removeLast(); // f, b, X
        assertThat(list, contains("f", "b", "X"));

        // List#set(int, Object)
        list.set(1, "a");
        assertThat(list, contains("f", "a", "X"));

        // List#retainAll, List#replaceAll
        list.retainAll(Set.of("f", "a", "m"));
        list.replaceAll(String::toUpperCase);
        assertThat(list, contains("F", "A"));

        // List#sort
        list.sort(Comparator.comparing(Function.identity()));
        assertThat(list, contains("A", "F"));

        // List#clear
        list.clear();
        assertThat(list, empty());
    }

    private static void verifyIsMutableBySubList(List<String> list) {
        assertThat(list, empty()); // Validate method contract

        list.add("north");
        list.add("east");
        list.add("south");
        list.add("west");

        List<String> subList = list.subList(1, 3); // east, south
        subList.remove("south"); // east
        subList.add("best"); // east, best
        subList.addAll(List.of("crest")); // east, best, crest
        subList.remove(0); // best, crest
        assertThat(list, contains("north", "best", "crest", "west"));

        subList.sort(Comparator.comparing(String::length).reversed()); // crest, best
        assertThat(list, contains("north", "crest", "best", "west"));

        subList.clear();
        assertThat(list, contains("north", "west"));
        list.clear();
    }

    private static void verifyIsMutableByReversedList(List<String> list) {
        assertThat(list, empty()); // Validate method contract

        List<String> reversed = list.reversed();
        reversed.addAll(List.of("e", "n", "t"));
        reversed.addFirst("k"); // k, e, n, t
        assertThat(list, contains("t", "n", "e", "k"));

        reversed.removeLast(); // k, e, n
        reversed.replaceAll(String::toUpperCase); // K, E, N
        reversed.sort(Comparator.naturalOrder()); // E, K, N
        assertThat(list, contains("N", "K", "E"));

        reversed.clear();
        assertThat(list, empty());
    }

    private static void verifyIsMutableByIteratorAndListIterator(List<String> list) {
        list.add("north");
        list.add("west");
        assertThat(list, contains("north", "west"));

        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }

        ListIterator<String> listIterator = list.listIterator();
        listIterator.add("foo");

        assertThat(list, contains("foo"));
        list.clear();
    }

    /**
     * Returns tests to run to verify that the list type of the list creator is unmodifiable (and immutable,
     * if specified by the expected {@code mutability}).
     *
     * @param listCreator list creator whose list type should be tested
     * @param mutability expected mutability behavior of the list
     * @param mutabilitySubList expected mutability behavior of the list's subList type
     * @param mutabilityReversed expected mutability behavior of the list's reversed type
     * @return tests to run to verify the list type is unmodifiable (and immutable, if applicable)
     */
    public static List<DynamicTest> createTestsForUnmodifiableAssertions(ListCreator listCreator,
                                                                         ListModificationBehavior mutability,
                                                                         ListModificationBehavior mutabilitySubList,
                                                                         ListModificationBehavior mutabilityReversed) {
        DynamicTest testForImmutabilityType = mutability.isImmutable
            ? createTestForImmutabilityIfApplicable(listCreator)
            : createTestForListModifiableByBackingStructure(listCreator);

        List<String> list = listCreator.createListWithAbcdOrSubset();
        return Stream.of(
                testForImmutabilityType,
                dynamicTest("unmodifiable",
                    () -> verifyListExceptionBehavior(list, mutability)),
                dynamicTest("unmodifiable_subList",
                    () -> verifyListExceptionBehavior(list.subList(0, list.size()), mutabilitySubList)),
                dynamicTest("unmodifiable_reversed",
                    () -> verifyListExceptionBehavior(list.reversed(), mutabilityReversed)),

                createTestForIteratorModificationIfApplicable(listCreator, list),

                dynamicTest("unmodifiable_listIterator",
                    () -> verifyCannotBeModifiedByListIterator(mutability, listCreator, list)))
            .filter(Objects::nonNull)
            .toList();
    }

    private static DynamicTest createTestForListModifiableByBackingStructure(ListCreator listCreator) {
        ListWithBackingDataModifier listWithBackingDataModifier =
            listCreator.createListWithBackingDataModifier("a", "b", "c", "d").orElseThrow();

        return dynamicTest("unmodifiable_changeToOriginalStructureReflectedInList", () -> {
            List<String> list = listWithBackingDataModifier.list();
            assertThat(list, contains("a", "b", "c", "d"));
            listWithBackingDataModifier.runBackingDataModifier();
            assertThat(list, contains("a", "changed", "c", "d"));
        });
    }

    private static DynamicTest createTestForImmutabilityIfApplicable(ListCreator listCreator) {
        ListWithBackingDataModifier listWithBackingDataModifier =
            listCreator.createListWithBackingDataModifier("a", "b", "c", "d").orElse(null);
        if (listWithBackingDataModifier == null) {
            return null;
        }

        return dynamicTest("immutable_originalElementDoesNotChangeList", () -> {
            List<String> list = listWithBackingDataModifier.list();
            assertThat(list, contains("a", "b", "c", "d"));
            listWithBackingDataModifier.runBackingDataModifier();
            assertThat(list, contains("a", "b", "c", "d"));
        });
    }

    private static DynamicTest createTestForIteratorModificationIfApplicable(ListCreator listCreator,
                                                                             List<String> list) {
        if (listCreator.getSizeLimit() == 0) {
            return null;
        }

        return dynamicTest("unmodifiable_iterator", () -> {
            Iterator<?> iterator = list.iterator();
            iterator.next();
            assertThrows(UnsupportedOperationException.class, iterator::remove);
        });
    }

    private static void verifyCannotBeModifiedByListIterator(ListModificationBehavior mutability,
                                                             ListCreator listCreator, List<String> list) {
        ListIterator<String> listIterator = list.listIterator();
        assertThrows(UnsupportedOperationException.class, () -> listIterator.add("test"));

        if (listCreator.getSizeLimit() > 0) {
            assertThat(list.size(), greaterThanOrEqualTo(1)); // Validate method contract

            listIterator.next();
            assertThrows(UnsupportedOperationException.class, () -> listIterator.remove());
            if (mutability.throwsOnModification) {
                assertThrows(UnsupportedOperationException.class, () -> listIterator.set("test"));
            } else {
                assertDoesNotThrow(() -> listIterator.set("test"));
            }
        }
    }

    private static void verifyListExceptionBehavior(List<String> listToVerify,
                                                    ListModificationBehavior expectedBehavior) {
        new ListModificationVerifier(listToVerify, expectedBehavior).testMethods();
    }
}
