package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.ListCreator;
import ch.jalu.collectionbehavior.model.ListExpectedBehavior;
import ch.jalu.collectionbehavior.model.ListWithBackingDataModifier;
import org.junit.jupiter.api.DynamicTest;

import java.util.Collection;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public final class CollectionMutabilityVerifier {

    public static List<DynamicTest> createTestsForMutableAssertions(ListCreator listCreator) {
        List<String> emptyList = listCreator.createList();
        return List.of(
            dynamicTest("mutable", () -> verifyListIsMutable(emptyList)),
            dynamicTest("mutable_iterators", () -> verifyIsMutableByIteratorAndListIterator(emptyList)),
            dynamicTest("mutable_subList", () -> verifyIsMutableBySubList(emptyList)));
    }

    private static void verifyListIsMutable(List<String> list) {
        assertThat(list, empty()); // Validate method contract

        // List#add, List#addAll
        list.add("a");
        list.add("b");
        list.add("f");
        list.add(2, "c"); // a, b, c, f
        list.addAll(List.of("a", "b", "Y", "X"));
        assertThat(list, contains("a", "b", "c", "f", "a", "b", "Y", "X"));

        // List#remove, List#removeAll, List#removeIf
        list.remove("b");                      // a, c, f, a, b, Y, X
        list.remove(1);                        // a, f, a, b, Y, X
        list.removeAll(List.of("a"));          // f, b, Y, X
        list.removeIf(str -> str.equals("Y")); // f, b, X
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

    public static List<DynamicTest> createTestsForImmutableAssertions(ListCreator listCreator,
                                                                      ListExpectedBehavior unmodifiableBehavior,
                                                                      ListExpectedBehavior unmodifiableBehaviorSubList,
                                                                      ListExpectedBehavior unmodifiableBehaviorReversed) {
        List<String> list = listCreator.createListWithAbcdOrSubset();

        return Stream.of(
            createTestForImmutabilityIfApplicable(listCreator),
            dynamicTest("immutable",
                () -> verifyListExceptionBehavior(list, unmodifiableBehavior)),
            dynamicTest("immutable_subList",
                () -> verifyListExceptionBehavior(list.subList(0, list.size()), unmodifiableBehaviorSubList)),
            dynamicTest("immutable_reversed",
                () -> verifyListExceptionBehavior(list.reversed(), unmodifiableBehaviorReversed)),
            dynamicTest("immutable_iterator",
                () -> verifyCannotBeModifiedByIterator(list)),
            dynamicTest("immutable_listIterator",
                () -> verifyCannotBeModifiedByListIterator(list)))
            .filter(Objects::nonNull)
            .toList();
    }

    public static List<DynamicTest> createTestsForUnmodifiableAssertions(ListCreator listCreator,
                                                                         ListExpectedBehavior unmodifiableBehavior,
                                                                         ListExpectedBehavior unmodifiableBehaviorSubList,
                                                                         ListExpectedBehavior unmodifiableBehaviorReversed) {
        List<String> list = listCreator.createListWithAbcdOrSubset();

        return Stream.of(
                createTestForListModifiableByBackingStructure(listCreator),
                dynamicTest("unmodifiable",
                    () -> verifyListExceptionBehavior(list, unmodifiableBehavior)),
                dynamicTest("unmodifiable_subList",
                    () -> verifyListExceptionBehavior(list.subList(0, list.size()), unmodifiableBehaviorSubList)),
                dynamicTest("unmodifiable_reversed",
                    () -> verifyListExceptionBehavior(list.reversed(), unmodifiableBehaviorReversed)),
                dynamicTest("unmodifiable_iterator",
                    () -> verifyCannotBeModifiedByIterator(list)),
                dynamicTest("unmodifiable_listIterator",
                    () -> verifyCannotBeModifiedByListIterator(list)))
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

    private static void verifyCannotBeModifiedByIterator(Collection<?> set) {
        assertThat(set.size(), greaterThanOrEqualTo(1));

        Iterator<?> iterator = set.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    private static void verifyCannotBeModifiedByListIterator(List<String> list) {
        assertThat(list.size(), greaterThanOrEqualTo(1));

        ListIterator<String> listIterator = list.listIterator();
        assertThrows(UnsupportedOperationException.class, () -> listIterator.set("test"));
        assertThrows(UnsupportedOperationException.class, () -> listIterator.add("test"));
        assertThrows(UnsupportedOperationException.class, () -> listIterator.remove());
    }

    private static void verifyListExceptionBehavior(List<String> listToVerify,
                                                    ListExpectedBehavior expectedBehavior) {
        new ListModificationVerifier(listToVerify, expectedBehavior).testMethods();
    }
}