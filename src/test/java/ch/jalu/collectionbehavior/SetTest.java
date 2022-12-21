package ch.jalu.collectionbehavior;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyIsImmutable;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyIsMutable;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyIsUnmodifiable;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyRejectsNullArgInMethods;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifySupportsNullArgInMethods;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyThrowsOnlyIfSetWouldBeModified;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SetTest {

    /**
     * {@link HashSet}: standard modifiable Set. Fully supports null. Does not keep insertion order.
     * Elements already added to it are silently ignored.
     */
    @Test
    void testJdkHashSet() {
        // Is mutable
        verifyIsMutable(new HashSet<>());

        // Has random order
        Set<Integer> set = new HashSet<>(Arrays.asList(1, 4, 9, 16));
        assertContainsButNotInOrder(set, 1, 4, 9, 16);

        // May contain null
        Set<Long> setWithNull = new HashSet<>();
        setWithNull.add(null); // No exception

        // Null support in methods
        verifySupportsNullArgInMethods(set);
    }

    /**
     * {@link LinkedHashSet}: modifiable Set that keeps insertion order. Fully supports null.
     * Elements already added to it are silently ignored.
     */
    @Test
    void testJdkLinkedHashSet() {
        // Is mutable
        verifyIsMutable(new LinkedHashSet<>());

        // Keeps insertion order
        Set<Integer> set = new LinkedHashSet<>(Arrays.asList(1, 4, 9, 16, 9));
        assertThat(set, contains(1, 4, 9, 16));

        // May contain null
        Set<Long> setWithNull = new LinkedHashSet<>();
        setWithNull.add(null); // No exception

        // Null support in methods
        verifySupportsNullArgInMethods(set);
    }

    /**
     * {@link Set#of} produces an immutable Set. Does not support null (not even for {@link Set#contains} etc.).
     * Throws an exception if any element is passed in twice. Random iteration order.
     */
    @Test
    void testJdkSetOf() {
        // Is immutable
        Integer[] elements = { 1, 4, 9, 16 };
        Set<Integer> set = Set.of(elements);
        verifyIsImmutable(set, () -> elements[2] = -999);

        // Has random order
        assertContainsButNotInOrder(set, 1, 4, 9, 16);

        // May not contain null
        assertThrows(NullPointerException.class, () -> Set.of(14, null, 16));

        // No null support in methods
        verifyRejectsNullArgInMethods(set);
    }

    /**
     * {@link Set#copyOf} produces an immutable Set. Does not support null (not even for {@link Set#contains} etc.).
     * Duplicate elements in the original collection are ignored. Random iteration order. Recognizes instances of the
     * same class and avoids unnecessary copying.
     */
    @Test
    void testJdkSetCopyOf() {
        // Is immutable
        List<Integer> elements = newArrayList(1, 4, 9, 16, 9);
        Set<Integer> set = Set.copyOf(elements);
        verifyIsImmutable(set, () -> elements.remove((Integer) 4));

        // Has random order
        assertContainsButNotInOrder(set, 1, 4, 9, 16);

        // May not contain null
        List<Integer> elementsWithNull = Arrays.asList(1, 4, null, 16);
        assertThrows(NullPointerException.class, () -> Set.copyOf(elementsWithNull));

        // No null support in methods
        verifyRejectsNullArgInMethods(set);

        // Does not create new instances if not needed
        assertThat(Set.copyOf(set), sameInstance(set));
    }

    /**
     * {@link ImmutableSet#of} produces an immutable Set. Does not support null as elements.
     * Insertion order is kept. Can be instantiated with duplicates (also when using the builder,
     * {@link ImmutableSet#builder()}).
     */
    @Test
    void testGuavaImmutableSet() {
        // Is immutable
        Set<Integer> set = ImmutableSet.of(1, 4, 9, 16, 9);
        verifyIsImmutable(set, () -> { /* Noop */ });

        // Keeps insertion order
        assertThat(set, contains(1, 4, 9, 16));

        // May not contain null
        assertThrows(NullPointerException.class, () -> ImmutableSet.of(14, null, 16));

        // Null support in methods
        verifySupportsNullArgInMethods(set);

        // Builder also accepts duplicates
        assertThat(ImmutableSet.builder()
            .add(14)
            .add(15, 15)
            .add(14)
            .add(16)
            .build(), contains(14, 15, 16));
    }

    /**
     * {@link ImmutableSet#copyOf} produces an immutable Set. Null as element is not supported but can be used
     * in {@link Set#contains} etc. Elements are copied. Retains iteration order of the original collection.
     * Recognizes its own instances and avoids unnecessary copies.
     */
    @Test
    void testGuavaImmutableSetCopy() {
        // Is immutable
        List<Integer> elements = newArrayList(1, 4, 9, 16, 9);
        Set<Integer> set = ImmutableSet.copyOf(elements);
        verifyIsImmutable(set, () -> elements.set(2, -999));

        // Keeps insertion order
        assertThat(set, contains(1, 4, 9, 16));

        // May not contain null
        List<Integer> elementsWithNull = newArrayList(1, null, 9);
        assertThrows(NullPointerException.class, () -> ImmutableSet.copyOf(elementsWithNull));

        // Null support in methods
        verifySupportsNullArgInMethods(set);

        // Does not create new instances if not needed
        assertThat(ImmutableSet.copyOf(set), sameInstance(set));
    }

    /**
     * {@link Collections#emptySet()} always returns the same instance: immutable empty Set.
     */
    @Test
    void testJdkCollectionsEmptySet() {
        // Is immutable
        Set<Integer> set1 = Collections.emptySet();
        verifyThrowsOnlyIfSetWouldBeModified(set1, UnmodifiableSetExceptionBehavior.COLLECTIONS_EMPTYSET);

        // Null support in methods
        verifySupportsNullArgInMethods(set1);

        // Always returns the same instance
        Set<String> set2 = Collections.emptySet();
        assertThat(set1, sameInstance(set2));
    }

    /**
     * {@link Collections#unmodifiableSet} wraps a Set into an unmodifiable Set facade. Changes to the backing
     * collection are reflected. Supports null as elements. Iteration order kept from underlying collection.
     */
    @Test
    void testJdkCollectionsUnmodifiableSet() {
        // Is unmodifiable
        Set<Integer> elements = new LinkedHashSet<>(Arrays.asList(1, 4, 9, 16));
        Set<Integer> set = Collections.unmodifiableSet(elements);
        verifyIsUnmodifiable(set, () -> elements.remove(9));

        // Has same order as backing set
        assertThat(set, contains(1, 4, 16));

        // May contain null
        Set<Integer> setWithNull = new HashSet<>(Arrays.asList(2, null));
        Collections.unmodifiableSet(setWithNull); // No exception

        // Null support in methods
        verifySupportsNullArgInMethods(set);

        // Same instance returned in JDK 17, whereas in JDK 11 it always returned a new instance
        assertThat(Collections.unmodifiableSet(set), sameInstance(set));
    }

    /**
     * {@link Collections#singleton} produces an immutable Set with a single element. Supports null.
     */
    @Test
    void testJdkCollectionsSingleton() {
        // Is immutable
        Set<Integer> set = Collections.singleton(4);
        verifyThrowsOnlyIfSetWouldBeModified(set, UnmodifiableSetExceptionBehavior.COLLECTIONS_SINGLETON);

        // May contain null
        Set<Integer> singletonWithNull = Collections.singleton(null);
        assertThat(singletonWithNull.contains(null), equalTo(true));

        // Null support in methods
        verifySupportsNullArgInMethods(set);
    }

    /**
     * {@link Collectors#toSet} makes no guarantees about the returned type or its mutability. For now, it
     * returns a HashSet and therefore also supports null values.
     */
    @Test
    void testJdkCollectorsToSet() {
        // Is mutable (but Javadoc makes no guarantee)
        Set<String> emptySet = Stream.of("f", "g")
            .filter(e -> false)
            .collect(Collectors.toSet());
        assertThat(emptySet.getClass(), equalTo(HashSet.class));
        verifyIsMutable(emptySet);

        // Has random order
        Set<Integer> set = Stream.of(1, 4, 9, 16).collect(Collectors.toSet());
        assertContainsButNotInOrder(set, 1, 4, 9, 16);

        // May contain null
        Set<Integer> setWithNull = Stream.of(3, null, 4)
            .collect(Collectors.toSet());
        assertThat(setWithNull.contains(null), equalTo(true));

        // Null support in methods
        verifySupportsNullArgInMethods(set);
    }

    /**
     * {@link Collectors#toUnmodifiableSet} produces an immutable Set. Null as element is not supported and may not be
     * used as argument in methods like {@link Set#contains}.
     */
    @Test
    void testJdkCollectorsToUnmodifiableSet() {
        // Is immutable
        Set<Integer> set = Stream.of(1, 4, 9, 16)
            .collect(Collectors.toUnmodifiableSet());
        verifyIsImmutable(set, () -> { /* noop */ });

        // Has random order
        assertContainsButNotInOrder(set, 1, 4, 9, 16);

        // May not contain null
        assertThrows(NullPointerException.class, () -> Stream.of(1, null, 16)
            .collect(Collectors.toUnmodifiableSet()));

        // No null support in methods
        verifyRejectsNullArgInMethods(set);
    }

    private static void assertContainsButNotInOrder(Set<Integer> set, Integer... values) {
        assertThat(set, containsInAnyOrder(values));
        assertThat(set, not(contains(values)));
        // Of course it's still possible that by coincidence the Set has the same order, but we just have to live with this
    }
}
