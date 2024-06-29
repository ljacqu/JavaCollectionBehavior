package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.ListCreator;
import ch.jalu.collectionbehavior.model.ListWithBackingDataModifier;
import ch.jalu.collectionbehavior.model.ModificationBehavior;
import ch.jalu.collectionbehavior.model.SetWithBackingDataModifier;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NavigableSet;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the mutability of collections.
 */
public final class CollectionMutabilityVerifier {

    private CollectionMutabilityVerifier() {
    }

    public static void verifyListIsMutable(List<String> list) {
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

    public static void verifyIsMutableBySubList(List<String> list) {
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

    public static void verifyIsMutableByReversedList(List<String> list) {
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

    public static void verifyIsMutableByIteratorAndListIterator(List<String> list) {
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
     * Verifies that the given Set is mutable (incl. verification that it can be modified via iterator).
     *
     * @param emptySet an empty Set instance of the type to test
     */
    public static void verifySetIsMutable(Set<String> emptySet) {
        assertThat(emptySet, empty()); // Validate method contract
        Set<String> set = emptySet;

        // Set#add, Set#addAll
        set.add("a");
        set.add("b");
        set.addAll(List.of("b", "c", "d", "c", "X", "Y"));
        assertThat(set, containsInAnyOrder("a", "b", "c", "d", "X", "Y"));

        // Set#remove, Set#removeAll, Set#removeIf
        set.remove("a"); // b, c, d, X, Y
        set.removeAll(Set.of("c", "ë")); // b, d, X, Y
        set.removeIf(elem -> elem.equals("b")); // d, X, Y
        assertThat(set, containsInAnyOrder("d", "X", "Y"));

        // Set#retainAll
        set.retainAll(Set.of("d", "X"));
        assertThat(set, containsInAnyOrder("d", "X"));

        // Set#clear
        set.clear();
        assertThat(set, empty());
    }

    public static void verifyIsMutableByIterator(Set<String> set) {
        set.add("north");
        set.add("east");
        set.add("south");
        set.add("west");

        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }

        assertThat(set, empty());
    }

    public static void verifyIsMutableBySequencedSetMethods(SequencedSet<String> seqColl, boolean removeOnly) {
        if (!removeOnly) {
            seqColl.add("b");
            seqColl.addFirst("a");
            seqColl.addLast("c");
            assertThat(seqColl, contains("a", "b", "c"));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> seqColl.addFirst("a"));
            assertThrows(UnsupportedOperationException.class, () -> seqColl.addLast("c"));
            seqColl.addAll(List.of("a", "b", "c"));
        }

        seqColl.removeFirst();
        seqColl.removeLast();
        assertThat(seqColl, contains("b"));

        SequencedSet<String> reversed = seqColl.reversed();
        reversed.add("f");
        // Interestingly, if seqColl were a list, we would expect ("f", "b") here
        assertThat(seqColl, contains("b", "f"));

        if (removeOnly) {
            reversed.add("g");
            assertThrows(UnsupportedOperationException.class, () -> reversed.addFirst("g"));
        } else {
            reversed.addFirst("g");
        }

        reversed.remove("f");
        assertThat(reversed, contains("g", "b"));
        assertThat(seqColl, contains("b", "g"));
        reversed.clear();
    }

    public static void verifyIsMutableByNavigableSetMethods(NavigableSet<String> navSet) {
        navSet.addAll(Set.of("a", "e", "i", "o", "u"));
        assertThat(navSet, contains("a", "e", "i", "o", "u"));

        // descendingSet
        navSet.descendingSet().add("k"); // aeikou
        // descendingIterator
        Iterator<String> descendingIterator = navSet.descendingIterator();
        descendingIterator.next();
        descendingIterator.remove(); // removes "u" -> aeiko
        // subSet
        SortedSet<String> subSet = navSet.subSet("e", "o");
        subSet.add("g"); // aeigko
        assertThat(navSet, contains("a", "e", "g", "i", "k", "o"));
        // headSet(E)
        SortedSet<String> headSetExcl = navSet.headSet("m");
        headSetExcl.remove("g");
        headSetExcl.remove("o");
        assertThat(navSet, contains("a", "e", "i", "k", "o"));
        // headSet(E, boolean)
        NavigableSet<String> headSetIncl = navSet.headSet("o", true);
        headSetIncl.add("c");
        headSetIncl.removeFirst();
        assertThat(navSet, contains("c", "e", "i", "k", "o"));
        // tailSet(E)
        SortedSet<String> tailSetIncl = navSet.tailSet("k");
        tailSetIncl.add("l");
        // tailSet(E, boolean)
        SortedSet<String> tailSetExcl = navSet.tailSet("i", false);
        tailSetExcl.removeFirst();
        assertThat(navSet, contains("c", "e", "i", "l", "o"));
        // subSet
        SortedSet<String> subset = navSet.subSet("e", "l");// = subset(e, true, k, false)
        subset.retainAll(Set.of("i"));
        assertThat(navSet, contains("c", "i", "l", "o"));
        navSet.clear();
    }

    public static void unmodifiable_changeToOriginalStructureIsReflectedInList(
                                                              ListWithBackingDataModifier listWithBackingDataModifier) {
        List<String> list = listWithBackingDataModifier.list();
        assertThat(list, contains("a", "b", "c", "d"));
        listWithBackingDataModifier.runBackingDataModifier();
        assertThat(list, contains("a", "changed", "c", "d"));
    }

    public static void immutable_changeToOriginalStructureIsNotReflectedInList(
                                                              ListWithBackingDataModifier listWithBackingDataModifier) {
        List<String> list = listWithBackingDataModifier.list();
        assertThat(list, contains("a", "b", "c", "d"));
        listWithBackingDataModifier.runBackingDataModifier();
        assertThat(list, contains("a", "b", "c", "d"));
    }

    public static void unmodifiable_changeToOriginalStructureIsReflectedInSet(
                                                                SetWithBackingDataModifier setWithBackingDataModifier) {
        Set<String> set = setWithBackingDataModifier.set();
        assertThat(set, contains("a", "b", "c", "d"));
        setWithBackingDataModifier.runBackingDataModifier();
        assertThat(set, contains("a", "b", "c"));
    }

    public static void immutable_changeToOriginalStructureIsNotReflectedInSet(
                                                                SetWithBackingDataModifier setWithBackingDataModifier) {
        Set<String> set = setWithBackingDataModifier.set();
        assertThat(set, containsInAnyOrder("a", "b", "c", "d"));
        setWithBackingDataModifier.runBackingDataModifier();
        assertThat(set, containsInAnyOrder("a", "b", "c", "d"));
    }

    public static void verifyCannotBeModifiedByIterator(Collection<String> coll) {
        Iterator<?> iterator = coll.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    public static void verifyCannotBeModifiedByListIterator(ModificationBehavior mutability,
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
}
