package ch.jalu.collectionbehavior;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;
import java.util.SequencedCollection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyIsImmutable;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyIsMutable;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyIsUnmodifiable;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyRejectsNullArgInMethods;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifySupportsNullArgInMethods;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyThrowsOnlyIfListWouldBeModified;
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

    @Test
    void testListSupertypeIsSequencedCollection() {
        // Since List implements SequencedCollection, every List implementation is a SequencedCollection
        assertThat(SequencedCollection.class.isAssignableFrom(List.class), equalTo(true));
    }

    /**
     * ArrayList: standard modifiable List implementation. Fully supports null.
     */
    @Test
    void testJdkArrayList() {
        // Is mutable
        verifyIsMutable(new ArrayList<>());

        // Implements RandomAccess
        assertThat(new ArrayList<>(), instanceOf(RandomAccess.class));

        // May contain null
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add(null); // no exception

        // Null support in methods
        verifySupportsNullArgInMethods(new ArrayList<>());
    }

    /**
     * LinkedList: mutable, fully supports null.
     */
    @Test
    void testJdkLinkedList() {
        // Is mutable
        verifyIsMutable(new LinkedList<>());

        // Does not implement RandomAccess
        assertThat(new LinkedList<>(), not(instanceOf(RandomAccess.class)));

        // May contain null
        List<String> listWithNull = new LinkedList<>();
        listWithNull.add(null); // no exception

        // Null support in methods
        verifySupportsNullArgInMethods(new LinkedList<>());
    }

    /**
     * {@link List#of} produces an immutable list that does not support null,
     * even when null is called on {@link List#contains}.
     */
    @Test
    void testJdkListOf() {
        // Is immutable
        String[] elements = { "a", "b", "c", "d" };
        List<String> list = List.of(elements);
        verifyIsImmutable(list, () -> elements[2] = "changed");

        // Implements RandomAccess
        assertThat(list, instanceOf(RandomAccess.class));

        // May not contain null
        assertThrows(NullPointerException.class, () -> List.of("a", null, "c"));

        // No null support in methods
        verifyRejectsNullArgInMethods(list);
    }

    /**
     * {@link List#copyOf} copies the incoming collection and returns an immutable List;
     * recognizes lists of its own class and returns the same instance instead of unnecessarily copying.
     * Does not support null (not even as argument passed into things like {@link List#contains}).
     */
    @Test
    void testJdkListCopyOf() {
        // Is immutable
        List<String> elements = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        List<String> list = List.copyOf(elements);
        verifyIsImmutable(list, () -> elements.set(2, "changed"));

        // Implements RandomAccess
        assertThat(list, instanceOf(RandomAccess.class));

        // May not contain null
        List<String> listWithNull = Arrays.asList("a", null, "c");
        assertThrows(NullPointerException.class, () -> List.copyOf(listWithNull));

        // No null support in methods
        verifyRejectsNullArgInMethods(list);

        // Does not create new instances if not needed
        assertThat(List.copyOf(list), sameInstance(list));
    }

    /**
     * {@link Arrays#asList} wraps an array into the List interface. Allows to change individual elements
     * but elements cannot be added or removed. The array passed into it is not copied! Supports null.
     * Basically wraps an array into a List interface as changes to the array are propagated to the List and
     * vice versa (changing an entry in the List actually changes the backing array).
     */
    @Test
    void testJdkArraysAsList() {
        // Is partially modifiable: basically just delegates to the wrapped array, so anything that can be done on
        // the array (changing an existing value, but not adding a new value) is supported
        String[] elements = { "a", "b", "c", "d" };
        List<String> list = Arrays.asList(elements);

        elements[2] = "changed";
        assertThat(list, contains("a", "b", "changed", "d"));
        assertThrows(UnsupportedOperationException.class, () -> list.add("foo"));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(3));

        list.set(2, "foo");
        assertThat(list, contains("a", "b", "foo", "d"));
        assertThat(elements, arrayContaining("a", "b", "foo", "d")); // backing array changed via list

        // Implements RandomAccess
        assertThat(list, instanceOf(RandomAccess.class));

        // May contain null
        assertThat(Arrays.asList("a", null, "c"), contains("a", null, "c"));

        // Null support in methods
        verifySupportsNullArgInMethods(Arrays.asList("a", "z"));
    }

    /**
     * {@link ImmutableList#copyOf} returns an immutable list. Does not support null as element but
     * null can be passed as argument into methods like {@link List#contains}.
     * Prefer {@link ImmutableList#of} if you are not starting from an array (unlike this test case).
     */
    @Test
    void testGuavaImmutableList() {
        // Is immutable
        String[] elements = { "a", "b", "c", "d" };
        List<String> list = ImmutableList.copyOf(elements);
        verifyIsImmutable(list, () -> elements[2] = "changed", UnmodifiableListExceptionBehavior.GUAVA_IMMUTABLE_LIST);

        // Implements RandomAccess
        assertThat(list, instanceOf(RandomAccess.class));

        // May not contain null
        assertThrows(NullPointerException.class, () -> ImmutableList.of("a", null, "c"));

        // Null support in methods
        verifySupportsNullArgInMethods(Arrays.asList("a", "z"));
    }

    /**
     * {@link ImmutableList#copyOf(Iterable)} copies the incoming collection and produces an immutable
     * List. Recognizes instances of the same class and avoids unnecessary copies. Does not support null as
     * element but accepts null passed as argument into {@link List#contains} etc.
     */
    @Test
    void testGuavaImmutableListCopy() {
        // Is immutable
        List<String> elements = newArrayList("a", "b", "c", "d");
        List<String> list = ImmutableList.copyOf(elements);
        verifyIsImmutable(list, () -> elements.set(2, "changed"), UnmodifiableListExceptionBehavior.GUAVA_IMMUTABLE_LIST);

        // Implements RandomAccess
        assertThat(list, instanceOf(RandomAccess.class));

        // May not contain null
        List<String> elementsWithNull = newArrayList("a", null, "c");
        assertThrows(NullPointerException.class, () -> ImmutableList.copyOf(elementsWithNull));

        // Null support in methods
        verifySupportsNullArgInMethods(list);

        // Does not create new instances if not needed
        assertThat(ImmutableList.copyOf(list), sameInstance(list));
    }

    /**
     * {@link Collections#emptyList()} returns an immutable empty list. Always the same instance.
     * Nice readable name for when an empty list is desired to be returned.
     */
    @Test
    void testJdkCollectionsEmptyList() {
        // Is immutable
        List<String> list = Collections.emptyList();
        verifyThrowsOnlyIfListWouldBeModified(list, UnmodifiableListExceptionBehavior.COLLECTIONS_EMPTYLIST);

        // Implements RandomAccess
        assertThat(list, instanceOf(RandomAccess.class));

        // Null support in methods
        verifySupportsNullArgInMethods(list);

        // Is always the same instance
        List<Integer> list2 = Collections.emptyList();
        assertThat(list, sameInstance(list2));
    }

    /**
     * {@link Collections#unmodifiableList} wraps a List into an unmodifiable List, i.e. changes to the
     * original List are propagated but cannot be modified via the returned list itself. Fully supports null
     * as elements or method parameter.
     * Implements RandomAccess only if the underlying List implements it.
     */
    @Test
    void testJdkCollectionsUnmodifiableList() {
        // Is unmodifiable
        List<String> elements = newArrayList("a", "b", "c", "d");
        List<String> list = Collections.unmodifiableList(elements);
        verifyIsUnmodifiable(list, () -> elements.set(2, "changed"));

        // Implements RandomAccess if underlying List implements it
        assertThat(list, instanceOf(RandomAccess.class)); // Because wrapped list is RandomAccess, too.
        assertThat(new LinkedList<>(), not(instanceOf(RandomAccess.class))); // validate assumption
        assertThat(Collections.unmodifiableList(new LinkedList<>()), not(instanceOf(RandomAccess.class)));

        // May contain null
        List<String> listWithNull = Arrays.asList("a", null, "c");
        assertThat(Collections.unmodifiableList(listWithNull), contains("a", null, "c"));

        // Null support in methods
        verifySupportsNullArgInMethods(list);

        // Same instance is returned in JDK  17, whereas in JDK 11 it always created a new instance
        assertThat(Collections.unmodifiableList(list), sameInstance(list));
    }

    /**
     * {@link Collections#singletonList} provides a list with a single given element. Immutable. Supports null.
     */
    @Test
    void testJdkCollectionsSingletonList() {
        // Is immutable
        List<String> list = Collections.singletonList("a");
        verifyThrowsOnlyIfListWouldBeModified(list, UnmodifiableListExceptionBehavior.COLLECTIONS_SINGLETONLIST);

        // Implements RandomAccess
        assertThat(list, instanceOf(RandomAccess.class));

        // May contain null
        assertThat(Collections.singletonList(null), contains((Object) null));

        // Null support in methods
        verifySupportsNullArgInMethods(list);
    }

    /**
     * {@link Collectors#toList} produces an ArrayList, although it makes no guarantees about the returned type,
     * nor about the mutability of the return value.
     * As it returns an ArrayList (for now), null values are supported.
     */
    @Test
    void testJdkCollectorsToList() {
        // Is mutable (but Javadoc makes no guarantees)
        List<String> list = Stream.of("a", "b", "c", "d")
            .filter(str -> false)
            .collect(Collectors.toList());
        assertThat(list.getClass(), equalTo(ArrayList.class));
        verifyIsMutable(list);

        // Implements RandomAccess (but Javadoc makes no guarantees)
        assertThat(list, instanceOf(RandomAccess.class));

        // May contain null
        List<String> listWithNull = Stream.of("a", null, "c")
            .collect(Collectors.toList());
        assertThat(listWithNull, contains("a", null, "c"));

        // Null support in methods
        verifySupportsNullArgInMethods(list);
    }

    /**
     * {@link Collectors#toUnmodifiableList} produces an unmodifiable list. It throws an exception if null is
     * passed to it or to any of its methods.
     */
    @Test
    void testJdkCollectorsToUnmodifiableList() {
        // Is immutable
        List<String> list = Stream.of("a", "b", "c", "d")
            .collect(Collectors.toUnmodifiableList());
        verifyIsImmutable(list, () -> { /* noop */ });

        // Implements RandomAccess (but Javadoc makes no guarantees)
        assertThat(list, instanceOf(RandomAccess.class));

        // May not contain null
        assertThrows(NullPointerException.class, () -> Stream.of("a", null, "c")
            .collect(Collectors.toUnmodifiableList()));

        // No null support in methods
        verifyRejectsNullArgInMethods(list);
    }

    /**
     * {@link Stream#toList} produces an unmodifiable list that supports nulls.
     */
    @Test
    void testJdkStreamToList() {
        // Is immutable
        List<String> list = Stream.of("a", "b", "c", "d").toList();
        verifyIsImmutable(list, () -> { /* noop */ });

        // Implements RandomAccess (but Javadoc makes no guarantees)
        assertThat(list, instanceOf(RandomAccess.class));

        // May contain null
        List<String> listWithNull = Stream.of("a", null, "c")
            .toList();
        assertThat(listWithNull, contains("a", null, "c"));

        // Null support in methods
        verifySupportsNullArgInMethods(list);
    }
}
