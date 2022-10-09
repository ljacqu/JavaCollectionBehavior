package ch.jalu.collectionbehavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CollectionBehaviorTestUtil {

    private CollectionBehaviorTestUtil() {
    }

    // --------------------------
    // Mutability
    // --------------------------

    public static void verifyIsMutable(List<String> emptyList) {
        List<String> list = emptyList;

        // Check that list is empty and populate it with a few more values
        assertThat(list, empty()); // Validate method contract
        Collections.addAll(list, "a", "b", "c");
        assertThat(list, contains("a", "b", "c"));

        // List#add, List#addAll
        list.add("f");
        list.addAll(List.of("a", "b"));
        assertThat(list, contains("a", "b", "c", "f", "a", "b"));

        // List#remove, List#removeAll
        list.remove("b");              // -> a, c, f, a, b
        list.remove(1);             // -> a, f, a, b
        list.removeAll(List.of("a")); // -> f, b
        assertThat(list, contains("f", "b"));

        // List#set(int, Object)
        list.set(1, "a");
        assertThat(list, contains("f", "a"));

        // List#replaceAll
        list.replaceAll(String::toUpperCase);
        assertThat(list, contains("F", "A"));

        // List#sort
        list.sort(Comparator.comparing(Function.identity()));
        assertThat(list, contains("A", "F"));

        // List#removeIf
        list.removeIf(str -> str.equals("A"));
        assertThat(list, contains("F"));

        // List#clear
        list.clear();
        assertThat(list, empty());
    }

    // todo: modif by iterator or sublist

    public static void verifyIsImmutable(List<String> abcdImmutableList, Runnable originModifier) {
        assertThat(abcdImmutableList, contains("a", "b", "c", "d")); // Validate method contract
        originModifier.run();
        assertThat(abcdImmutableList, contains("a", "b", "c", "d"));

        verifyCannotBeModifiedDirectly(abcdImmutableList);
    }

    public static void verifyIsUnmodifiable(List<String> abcdUnmodifiableList, Runnable originModifier) {
        assertThat(abcdUnmodifiableList, contains("a", "b", "c", "d")); // Validate method contract
        originModifier.run();
        assertThat(abcdUnmodifiableList, contains("a", "b", "changed", "d"));

        verifyCannotBeModifiedDirectly(abcdUnmodifiableList);
    }

    public static void verifyCannotBeModifiedDirectly(List<String> list) {
        List<String> copy = new ArrayList<>(list);

        assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.addAll(List.of("foo", "bar")));
        assertThrows(UnsupportedOperationException.class, () -> list.remove("a"));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(3));
        assertThrows(UnsupportedOperationException.class, () -> list.removeAll(Set.of("f", "b")));
        assertThrows(UnsupportedOperationException.class, () -> list.set(3, "foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.replaceAll(String::toUpperCase));
        assertThrows(UnsupportedOperationException.class, () -> list.sort(Comparator.comparing(Function.identity())));
        assertThrows(UnsupportedOperationException.class, () -> list.removeIf(str -> str.equals("f")));
        assertThrows(UnsupportedOperationException.class, () -> list.clear());

        assertThat(list, equalTo(copy));
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
