package ch.jalu.collectionbehavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CollectionBehaviorTestUtil {

    private CollectionBehaviorTestUtil() {
    }

    // --------------------------
    // Mutability
    // --------------------------

    public static void verifyIsMutable(List<String> emptyList) {
        assertThat(emptyList, empty()); // Validate method contract
        List<String> list = emptyList;

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

        verifyIsMutableBySubListAndIterator(list);
    }

    private static void verifyIsMutableBySubListAndIterator(List<String> list) {
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

        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }

        assertThat(list, empty());
    }

    public static void verifyIsImmutable(List<String> abcdImmutableList, Runnable originModifier) {
        assertThat(abcdImmutableList, contains("a", "b", "c", "d")); // Validate method contract
        originModifier.run();
        assertThat(abcdImmutableList, contains("a", "b", "c", "d"));

        verifyCannotBeModifiedDirectly(abcdImmutableList);
        verifyCannotBeModifiedDirectly(abcdImmutableList.subList(0, 3));
        verifyCannotBeModifiedByIterator(abcdImmutableList);
    }

    public static void verifyIsUnmodifiable(List<String> abcdUnmodifiableList, Runnable originModifier) {
        assertThat(abcdUnmodifiableList, contains("a", "b", "c", "d")); // Validate method contract
        originModifier.run();
        assertThat(abcdUnmodifiableList, contains("a", "b", "changed", "d"));

        verifyCannotBeModifiedDirectly(abcdUnmodifiableList);
        verifyCannotBeModifiedDirectly(abcdUnmodifiableList.subList(0, 3));
        verifyCannotBeModifiedByIterator(abcdUnmodifiableList);
    }

    private static void verifyCannotBeModifiedDirectly(List<String> list) {
        List<String> copy = new ArrayList<>(list);

        assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.add(1, "foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.addAll(List.of("foo", "bar")));
        assertThrows(UnsupportedOperationException.class, () -> list.remove("a"));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(3));
        assertThrows(UnsupportedOperationException.class, () -> list.removeAll(Set.of("f", "b")));
        assertThrows(UnsupportedOperationException.class, () -> list.removeIf(str -> str.equals("f")));
        assertThrows(UnsupportedOperationException.class, () -> list.set(3, "foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.retainAll(Set.of("x")));
        assertThrows(UnsupportedOperationException.class, () -> list.replaceAll(String::toUpperCase));
        assertThrows(UnsupportedOperationException.class, () -> list.sort(Comparator.comparing(Function.identity())));
        assertThrows(UnsupportedOperationException.class, list::clear);

        assertThat(list, equalTo(copy));
    }

    private static void verifyCannotBeModifiedByIterator(List<String> list) {
        assertThat(list.size(), greaterThanOrEqualTo(1));

        Iterator<String> iterator = list.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    public static void verifyThrowsOnlyIfListWouldBeModified(List<String> list) {
        if (list.getClass() != Collections.emptyList().getClass()) {
            assertThat(list, contains("test")); // Validate method contract

            assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
            assertThrows(UnsupportedOperationException.class, () -> list.add(1, "foo"));
            assertThrows(UnsupportedOperationException.class, () -> list.addAll(List.of("foo", "bar")));
            list.remove("a");
            assertThrows(UnsupportedOperationException.class, () -> list.remove("test"));
            assertThrows(UnsupportedOperationException.class, () -> list.remove(3));
            list.removeAll(Set.of("f", "b"));
            // Note the exception on List#removeIf here: it WOULD not change anything, but it is still just instantly rejected
            assertThrows(UnsupportedOperationException.class, () -> list.removeIf(str -> str.equals("f")));

            assertThrows(UnsupportedOperationException.class, () -> list.removeAll(Set.of("f", "test")));
            assertThrows(UnsupportedOperationException.class, () -> list.set(3, "foo"));
            list.retainAll(Set.of("test"));
            assertThrows(UnsupportedOperationException.class, () -> list.retainAll(Set.of("qq")));
            assertThrows(UnsupportedOperationException.class, () -> list.replaceAll(s -> s));
            list.sort(Comparator.comparing(Function.identity()));
            assertThrows(UnsupportedOperationException.class, list::clear);

            assertThat(list, contains("test"));
            verifySubListOfSingletonList(list.subList(0, 1));
            verifyCannotBeModifiedByIterator(list);
        } else {
            assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
            assertThrows(UnsupportedOperationException.class, () -> list.add(1, "foo"));
            assertThrows(UnsupportedOperationException.class, () -> list.addAll(List.of("foo", "bar")));
            list.remove("a");
            assertThrows(UnsupportedOperationException.class, () -> list.remove(3));
            list.removeAll(Set.of("f", "b"));
            list.removeIf(str -> str.equals("f"));
            assertThrows(UnsupportedOperationException.class, () -> list.set(3, "foo"));
            list.retainAll(Set.of("p"));
            list.replaceAll(String::toUpperCase);
            list.sort(Comparator.comparing(Function.identity()));
            list.clear();
        }
    }

    private static void verifySubListOfSingletonList(List<String> subList) {
        assertThrows(UnsupportedOperationException.class, () -> subList.add("foo"));
        assertThrows(UnsupportedOperationException.class, () -> subList.add(1, "foo"));
        assertThrows(UnsupportedOperationException.class, () -> subList.addAll(List.of("foo", "bar")));
        subList.remove("a");
        assertThrows(UnsupportedOperationException.class, () -> subList.remove("test"));
        assertThrows(UnsupportedOperationException.class, () -> subList.remove(0)); // throws IndexOutOfBounds if appropriate
        subList.removeAll(Set.of("f", "b"));
        subList.removeIf(str -> str.equals("f")); // Different from singletonList
        assertThrows(UnsupportedOperationException.class, () -> subList.removeIf(str -> str.equals("test")));

        assertThrows(UnsupportedOperationException.class, () -> subList.removeAll(Set.of("f", "test")));
        assertThrows(UnsupportedOperationException.class, () -> subList.set(0, "foo")); // throws IndexOutOfBounds when appropriate
        subList.retainAll(Set.of("test"));
        assertThrows(UnsupportedOperationException.class, () -> subList.retainAll(Set.of("qq")));
        assertThrows(UnsupportedOperationException.class, () -> subList.replaceAll(s -> s));
        assertThrows(UnsupportedOperationException.class, () -> subList.sort(Comparator.comparing(Function.identity()))); // different from singletonList
        assertThrows(UnsupportedOperationException.class, subList::clear);
    }

    // --------------------------
    // Null support
    // --------------------------

    public static void verifySupportsNullArgInMethods(List<String> list) {
        assertThat(list.contains(null), equalTo(false));
        assertThat(list.indexOf(null), equalTo(-1));
        assertThat(list.lastIndexOf(null), equalTo(-1));

        List<String> listWithNull = Arrays.asList((String) null);
        assertThat(list.containsAll(listWithNull), equalTo(false));
    }

    public static void verifyRejectsNullArgInMethods(List<String> list) {
        assertThrows(NullPointerException.class, () -> list.contains(null));
        assertThrows(NullPointerException.class, () -> list.indexOf(null));
        assertThrows(NullPointerException.class, () -> list.lastIndexOf(null));

        List<String> listWithNull = Arrays.asList((String) null);
        assertThrows(NullPointerException.class, () -> list.containsAll(listWithNull));
        // todo: interesting to note that Arrays.asList("test", null); will not produce an NPE because "test" was
        // already evaluated to false -> should document this in the future
    }
}
