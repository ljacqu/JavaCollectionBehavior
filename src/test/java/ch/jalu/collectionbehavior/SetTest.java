package ch.jalu.collectionbehavior;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
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
        Set<Integer> set = new HashSet<>(Arrays.asList(1, 4, 9, 16, 9));

        assertContainsButNotInOrder(set, 1, 4, 9, 16);

        assertThat(set.contains(null), equalTo(false));
        set.add(null);
        assertThat(set.contains(null), equalTo(true));
    }

    /**
     * {@link LinkedHashSet}: modifiable Set that keeps insertion order. Fully supports null.
     * Elements already added to it are silently ignored.
     */
    @Test
    void testJdkLinkedHashSet() {
        Set<Integer> set = new LinkedHashSet<>(Arrays.asList(1, 4, 9, 16, 9));

        assertThat(set, contains(1, 4, 9, 16));

        assertThat(set.contains(null), equalTo(false));
        set.add(null);
        assertThat(set.contains(null), equalTo(true));
    }

    /**
     * {@link Set#of} produces an immutable Set. Does not support null (not even for {@link Set#contains} etc.).
     * Throws an exception if any element is passed in twice. Random iteration order.
     */
    @Test
    void testJdkSetOf() {
        Integer[] elements = { 1, 4, 9, 16 };
        Set<Integer> set = Set.of(elements);

        elements[2] = -999;
        assertContainsButNotInOrder(set, 1, 4, 9, 16);

        assertThrows(UnsupportedOperationException.class, () -> set.add(777));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(3));

        assertThrows(NullPointerException.class, () -> Set.of(14, null, 16));
        assertThrows(NullPointerException.class, () -> set.contains(null));

        assertThrows(IllegalArgumentException.class, () -> Set.of(14, 15, 14));
    }

    /**
     * {@link Set#copyOf} produces an immutable Set. Does not support null (not even for {@link Set#contains} etc.).
     * Duplicate elements in the original collection are ignored. Random iteration order. Recognizes instances of the
     * same class and avoids unnecessary copying.
     */
    @Test
    void testJdkSetCopyOf() {
        List<Integer> elements = newArrayList(1, 4, 9, 16, 9);
        Set<Integer> set = Set.copyOf(elements);

        elements.remove(4);
        assertContainsButNotInOrder(set, 1, 4, 9, 16);

        assertThat(Set.copyOf(set), sameInstance(set));

        assertThrows(UnsupportedOperationException.class, () -> set.add(777));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(3));

        assertThrows(NullPointerException.class, () -> set.contains(null));

        List<Integer> elementsWithNull = Arrays.asList(1, 4, null, 16);
        assertThrows(NullPointerException.class, () -> Set.copyOf(elementsWithNull));
    }

    /**
     * {@link ImmutableSet#of} produces an immutable Set. Does not support null as elements.
     * Insertion order is kept. Can be instantiated with duplicates
     * (also when using the builder {@link ImmutableSet#builder()}).
     */
    @Test
    void testGuavaImmutableSet() {
        Set<Integer> set = ImmutableSet.of(1, 4, 9, 16, 9);
        assertThat(set, contains(1, 4, 9, 16));

        assertThrows(UnsupportedOperationException.class, () -> set.add(777));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(3));

        assertThrows(NullPointerException.class, () -> ImmutableSet.of(14, null, 16));
        assertThat(set.contains(null), equalTo(false));

        assertThat(ImmutableSet.of(14, 15, 14), contains(14, 15));
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
        List<Integer> elements = newArrayList(1, 4, 9, 16, 9);
        Set<Integer> set = ImmutableSet.copyOf(elements);

        elements.set(2, -999);
        assertThat(set, contains(1, 4, 9, 16));

        assertThat(ImmutableSet.copyOf(set), sameInstance(set));

        assertThrows(UnsupportedOperationException.class, () -> set.add(888));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(9));

        List<Integer> elementsWithNull = newArrayList(1, null, 9);
        assertThrows(NullPointerException.class, () -> ImmutableSet.copyOf(elementsWithNull));
        assertThat(set.contains(null), equalTo(false));
    }

    /**
     * {@link Collections#emptySet()} always returns the same instance: immutable empty Set.
     */
    @Test
    void testJdkCollectionsEmptySet() {
        Set<String> set1 = Collections.emptySet();
        Set<Integer> set2 = Collections.emptySet();

        assertThat(set1, sameInstance(set2));

        assertThat(set1.contains("foo"), equalTo(false));
        assertThat(set1.contains(null), equalTo(false));

        assertThrows(UnsupportedOperationException.class, () -> set1.add("f"));
    }

    /**
     * {@link Collections#unmodifiableSet} wraps a Set into an unmodifiable Set facade. Changes to the backing
     * collection are reflected. Supports null as elements. Iteration order kept from underlying collection.
     */
    @Test
    void testJdkCollectionsUnmodifiableSet() {
        Set<Integer> elements = new LinkedHashSet<>(Arrays.asList(1, 4, 9, 16));
        Set<Integer> set = Collections.unmodifiableSet(elements);

        elements.remove(4);
        assertThat(set, contains(1, 9, 16));

        // Same instance returned in JDK 17, whereas in JDK 11 it always returned a new instance
        assertThat(Collections.unmodifiableSet(set), sameInstance(set));

        assertThrows(UnsupportedOperationException.class, () -> set.add(777));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(1));

        Set<Integer> setWithNull = newHashSet(1, null, 9);
        assertThat(Collections.unmodifiableSet(setWithNull), containsInAnyOrder(1, null, 9));
        assertThat(set.contains(null), equalTo(false));
    }

    /**
     * {@link Collections#singleton} produces an immutable Set with a single element. Supports null.
     */
    @Test
    void testJdkCollectionsSingleton() {
        Set<Integer> set = Collections.singleton(19);

        assertThat(set, contains(19));

        assertThrows(UnsupportedOperationException.class, () -> set.add(22));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(19));

        assertThat(Collections.singleton(null), contains((Object) null));
        assertThat(set.contains(null), equalTo(false));
    }

    private static void assertContainsButNotInOrder(Set<Integer> set, Integer... values) {
        assertThat(set, containsInAnyOrder(values));
        assertThat(set, not(contains(values)));
        // Of course it's still possible that by coincidence the Set has the same order, but we just have to live with this
    }
}
