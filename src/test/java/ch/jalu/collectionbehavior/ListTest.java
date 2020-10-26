package ch.jalu.collectionbehavior;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListTest {

    /**
     * ArrayList: standard mutable List implementation. Fully supports null.
     */
    @Test
    void testJdkArrayList() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        assertThat(list, contains("a", "b", "c"));
        assertThat(list, instanceOf(RandomAccess.class));

        assertThat(list.contains(null), equalTo(false));
        list.add(null);
        assertThat(list.contains(null), equalTo(true));
    }

    /**
     * {@link List#of} produces a fully immutable list that does not support null,
     * even when null is called on {@link List#contains}. Copies the incoming array.
     */
    @Test
    void testJdkListOf() {
        String[] elements = { "a", "b", "c", "d" };
        List<String> list = List.of(elements);

        elements[2] = "changed";
        assertThat(list, contains("a", "b", "c", "d"));
        assertThat(list, instanceOf(RandomAccess.class));

        assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(3));
        assertThrows(UnsupportedOperationException.class, () -> list.set(3, "foo"));

        assertThrows(NullPointerException.class, () -> List.of("a", null, "c"));
        assertThrows(NullPointerException.class, () -> list.contains(null));
    }

    /**
     * {@link List#copyOf} copies the incoming collection and returns a fully immutable List;
     * recognizes lists of its own class and returns the same instance instead of unnecessarily copying.
     * Does not support null (not even as argument passed into things like {@link List#contains}).
     */
    @Test
    void testJdkListCopyOf() {
        List<String> elements = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        List<String> list = List.copyOf(elements);

        elements.set(2, "changed");
        assertThat(list, contains("a", "b", "c", "d"));
        assertThat(list, instanceOf(RandomAccess.class));

        assertThat(List.copyOf(list), sameInstance(list));

        assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(3));
        assertThrows(UnsupportedOperationException.class, () -> list.set(3, "foo"));

        List<String> listWithNull = Arrays.asList("a", null, "c");
        assertThrows(NullPointerException.class, () -> List.copyOf(listWithNull));
        assertThrows(NullPointerException.class, () -> list.contains(null));
    }

    /**
     * {@link Arrays#asList} wraps an array into the List interface. Allows to change individual elements
     * but elements cannot be added or removed. The array passed into it is not copied! Supports null.
     * Basically wraps an array into a List interface as changes to the array are propagated to the List and
     * vice versa (changing an entry in the List actually changes the backing array).
     */
    @Test
    void testJdkArraysAsList() {
        String[] elements = { "a", "b", "c", "d" };
        List<String> list = Arrays.asList(elements);

        elements[2] = "changed";
        assertThat(list, contains("a", "b", "changed", "d"));
        assertThat(list, instanceOf(RandomAccess.class));

        assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(3));
        list.set(2, "foo");
        assertThat(list, contains("a", "b", "foo", "d"));
        assertThat(elements, arrayContaining("a", "b", "foo", "d")); // backing array changed via list

        assertThat(Arrays.asList("a", null, "c"), contains("a", null, "c"));
        assertThat(list.contains(null), equalTo(false));
    }

    /**
     * {@link ImmutableList#copyOf} returns a fully immutable list. Does not support null as element but
     * null can be passed as argument into methods like {@link List#contains}. Copies the incoming array.
     * Prefer {@link ImmutableList#of} if you are not starting from an array (unlike this test case).
     */
    @Test
    void testGuavaImmutableList() {
        String[] elements = { "a", "b", "c", "d" };
        List<String> list = ImmutableList.copyOf(elements);

        elements[2] = "changed";
        assertThat(list, contains("a", "b", "c", "d"));
        assertThat(list, instanceOf(RandomAccess.class));

        assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(3));
        assertThrows(UnsupportedOperationException.class, () -> list.set(3, "foo"));

        assertThrows(NullPointerException.class, () -> ImmutableList.of("a", null, "c"));
        assertThat(list.contains(null), equalTo(false));
    }

    /**
     * {@link ImmutableList#copyOf(Iterable)} copies the incoming collection and produces a fully immutable
     * List. Recognizes instances of the same class and avoids unnecessary copies. Does not support null as
     * element but accepts null passed as argument into {@link List#contains} etc.
     */
    @Test
    void testGuavaImmutableListCopy() {
        List<String> elements = newArrayList("a", "b", "c", "d");
        List<String> list = ImmutableList.copyOf(elements);

        elements.set(2, "changed");
        assertThat(list, contains("a", "b", "c", "d"));
        assertThat(list, instanceOf(RandomAccess.class));

        assertThat(ImmutableList.copyOf(list), sameInstance(list));

        assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(3));
        assertThrows(UnsupportedOperationException.class, () -> list.set(3, "foo"));

        List<String> elementsWithNull = newArrayList("a", null, "c");
        assertThrows(NullPointerException.class, () -> ImmutableList.copyOf(elementsWithNull));
        assertThat(list.contains(null), equalTo(false));
    }

    /**
     * {@link Collections#emptyList()} returns an immutable empty list. Always the same instance.
     * Nice readable name for when an empty list is desired to be returned.
     */
    @Test
    void testJdkCollectionsEmptyList() {
        List<String> list1 = Collections.emptyList();
        List<Integer> list2 = Collections.emptyList();

        assertThat(list1, sameInstance(list2));
        assertThat(list1, instanceOf(RandomAccess.class));

        assertThat(list1.contains("foo"), equalTo(false));
        assertThat(list1.contains(null), equalTo(false));

        assertThrows(UnsupportedOperationException.class, () -> list1.add("f"));
    }

    /**
     * {@link Collections#unmodifiableList} wraps a List and does not copy its elements. Changes to the
     * original List are propagated but cannot be modified via the returned list itself. Fully supports null
     * as elements or method parameter.
     * Implements RandomAccess only if the underlying List implements it.
     */
    @Test
    void testJdkCollectionsUnmodifiableList() {
        List<String> elements = newArrayList("a", "b", "c", "d");
        List<String> list = Collections.unmodifiableList(elements);

        elements.set(2, "changed");
        assertThat(list, contains("a", "b", "changed", "d"));
        assertThat(list, instanceOf(RandomAccess.class)); // Because wrapped list is RandomAccess, too.

        assertThat(Collections.unmodifiableList(list), not(sameInstance(list)));

        assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(3));
        assertThrows(UnsupportedOperationException.class, () -> list.set(3, "foo"));

        List<String> listWithNull = Arrays.asList("a", null, "c");
        assertThat(Collections.unmodifiableList(listWithNull), contains("a", null, "c"));
        assertThat(list.contains(null), equalTo(false));
    }

    /**
     * {@link Collections#singletonList} provides a list with a single given element. Fully immutable. Supports null.
     */
    @Test
    void testJdkCollectionsSingletonList() {
        List<String> list = Collections.singletonList("test");

        assertThat(list, contains("test"));
        assertThat(list, instanceOf(RandomAccess.class));

        assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> list.set(0, "foo"));

        assertThat(Collections.singletonList(null), contains((Object) null));
        assertThat(list.contains(null), equalTo(false));
    }
}
