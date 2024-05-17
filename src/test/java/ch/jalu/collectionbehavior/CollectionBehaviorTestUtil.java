package ch.jalu.collectionbehavior;

import ch.jalu.collectionbehavior.model.ThrowingBehavior;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Util for verifying collection behavior, such as immutability and null support.
 */
public final class CollectionBehaviorTestUtil {

    private CollectionBehaviorTestUtil() {
    }

    // --------------------------
    // Mutability
    // --------------------------

    /**
     * Verifies that the given Set is mutable (incl. verification that it can be modified via iterator).
     *
     * @param emptySet an empty Set instance of the type to test
     */
    public static void verifyIsMutable(Set<String> emptySet) {
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

        verifyIsMutableByIterator(set);
        if (set instanceof SequencedSet<String> seqColl) {
            verifyIsMutableBySequencedCollectionMethods(seqColl);
        }
    }

    private static void verifyIsMutableByIterator(Set<String> set) {
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

    private static void verifyIsMutableBySequencedCollectionMethods(SequencedCollection<String> seqColl) {
        seqColl.add("b");
        seqColl.addFirst("a");
        seqColl.addLast("c");
        assertThat(seqColl, contains("a", "b", "c"));

        seqColl.removeFirst();
        seqColl.removeLast();
        assertThat(seqColl, contains("b"));

        SequencedCollection<String> reversedCollection = seqColl.reversed();
        reversedCollection.add("f");
        if (seqColl instanceof List) {
            assertThat(seqColl, contains("f", "b"));
        } else {
            assertThat(seqColl, contains("b", "f"));
        }
        reversedCollection.clear();
    }

    /**
     * Verifies that the given Set is immutable (incl. iterator).
     *
     * @param immutableSet the immutable Set to verify
     * @param originModifier callback to modify the originating structure (as to ensure that the set does not change)
     */
    public static void verifyIsImmutable(Set<Integer> immutableSet, Runnable originModifier) {
        assertThat(immutableSet, containsInAnyOrder(1, 4, 9, 16)); // Validate method contract
        originModifier.run();
        assertThat(immutableSet, containsInAnyOrder(1, 4, 9, 16));

        verifySetExceptionBehavior(immutableSet, UnmodifiableSetExceptionBehavior.ALWAYS_THROWS);
        verifyCannotBeModifiedByIterator(immutableSet);
        if (immutableSet instanceof SequencedSet<Integer> seqColl) {
            verifyCannotBeModifiedBySequencedCollectionMethods(seqColl);
            verifyCannotBeModifiedBySequencedCollectionMethods(seqColl.reversed());
        }
    }

    /**
     * Verifies that the given Set is unmodifiable: it cannot be modified directly but changes to the underlying
     * structure (triggered by the given {@code originModifier}) are reflected.
     *
     * @param unmodifiableSet the set to verify (with entries 1, 4, 9, 16)
     * @param originModifier callback that the changes the origin by removing 9
     */
    public static void verifyIsUnmodifiable(Set<Integer> unmodifiableSet, Runnable originModifier) {
        assertThat(unmodifiableSet, containsInAnyOrder(1, 4, 9, 16)); // Validate method contract
        originModifier.run();
        assertThat(unmodifiableSet, containsInAnyOrder(1, 4, 16));

        verifySetExceptionBehavior(unmodifiableSet, UnmodifiableSetExceptionBehavior.ALWAYS_THROWS);
        verifyCannotBeModifiedByIterator(unmodifiableSet);
        if (unmodifiableSet instanceof SequencedSet<Integer> seqColl) {
            verifyCannotBeModifiedBySequencedCollectionMethods(seqColl);
        }
    }

    private static void verifyCannotBeModifiedByIterator(Collection<?> set) {
        assertThat(set.size(), greaterThanOrEqualTo(1));

        Iterator<?> iterator = set.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    private static <T> void verifyCannotBeModifiedBySequencedCollectionMethods(SequencedCollection<T> seqColl) {
        T entry = seqColl.getFirst();

        assertThrows(UnsupportedOperationException.class, () -> seqColl.addFirst(entry));
        assertThrows(UnsupportedOperationException.class, () -> seqColl.addLast(entry));
        assertThrows(UnsupportedOperationException.class, seqColl::removeFirst);
        assertThrows(UnsupportedOperationException.class, seqColl::removeLast);

        SequencedCollection<T> reversedCollection = seqColl.reversed();
        assertThrows(UnsupportedOperationException.class, () -> reversedCollection.addFirst(entry));
        assertThrows(UnsupportedOperationException.class, () -> reversedCollection.addLast(entry));
        assertThrows(UnsupportedOperationException.class, reversedCollection::removeFirst);
        assertThrows(UnsupportedOperationException.class, reversedCollection::removeLast);
    }

    /**
     * Verifies that the given set is unmodifiable; however, an exception is generally only expected if the
     * structure would be modified by the call. This behavior is not fully consistent and exceptions to the
     * expected behavior are derived from the given {@code exceptionBehavior}.
     *
     * @param set the set to verify
     * @param exceptionBehavior the exception behavior that is expected
     */
    public static void verifyThrowsOnlyIfSetWouldBeModified(Set<Integer> set,
                                                            UnmodifiableSetExceptionBehavior exceptionBehavior) {
        if (Collections.<Integer>emptySet() != set) {
            assertThat(set, contains(4));
        }
        Set<Integer> copy = new HashSet<>(set);

        verifySetExceptionBehavior(set, exceptionBehavior);
        assertThat(set, equalTo(copy));
        if (set instanceof SequencedCollection<?>) {
            // This method is only used for sets that aren't sequenced, so we don't bother with assertions for now
            throw new IllegalStateException("Unexpected SequencedCollection: " + set.getClass().getName());
        }
    }

    private static void verifySetExceptionBehavior(Set<Integer> setToVerify,
                                                   UnmodifiableSetExceptionBehavior exceptionBehavior) {
        Class<? extends Exception> removeIfExOverride = exceptionBehavior.getNonModifyingRemoveIfExceptionOverride();

        ThrowingBehavior throwingBehavior = switch (exceptionBehavior) {
            case ALWAYS_THROWS ->
                ThrowingBehavior.ALWAYS_THROWS;
            case COLLECTIONS_SINGLETON, COLLECTIONS_EMPTYSET ->
                ThrowingBehavior.THROW_ONLY_IF_CHANGE;
        };

        new SetVerifier(setToVerify, throwingBehavior)
            .test(set -> set.add(23))
            .test(set -> set.add(4), UnsupportedOperationException.class)
            .test(set -> set.addAll(List.of(8, 24)))
            .test(set -> set.remove(3))
            .test(set -> set.remove(4))
            .test(set -> set.removeIf(elem -> elem == 0), removeIfExOverride)
            .test(set -> set.removeIf(elem -> elem == 4))
            .test(set -> set.removeAll(Set.of(2, 4)))
            .test(set -> set.removeAll(Set.of(2, 3)))
            .test(set -> set.retainAll(Set.of(4, 9)))
            .test(set -> set.retainAll(Set.of(11, 12)))
            .test(set -> set.clear());
    }

    // --------------------------
    // Null support
    // --------------------------

    /**
     * Verifies that null can be supplied as argument to all methods that do not modify the collection.
     *
     * @param list the list to test (may not contain null as entry)
     */
    public static void verifySupportsNullArgInMethods(List<String> list) {
        assertThat(list.contains(null), equalTo(false));
        assertThat(list.indexOf(null), equalTo(-1));
        assertThat(list.lastIndexOf(null), equalTo(-1));

        List<String> listWithNull = Collections.singletonList(null);
        assertThat(list.containsAll(listWithNull), equalTo(false));
    }

    /**
     * Verifies that null can be supplied as argument to all methods that do not modify the collection.
     *
     * @param set the set to test (may not contain null as entry)
     */
    public static void verifySupportsNullArgInMethods(Set<Integer> set) {
        assertThat(set.contains(null), equalTo(false));

        List<Integer> listWithNull = Collections.singletonList(null);
        assertThat(set.containsAll(listWithNull), equalTo(false));
    }

    /**
     * Verifies that a NullPointerException is thrown by all methods that don't modify the collection
     * if null is supplied as argument.
     *
     * @param list the list to test
     */
    public static void verifyRejectsNullArgInMethods(List<String> list) {
        assertThrows(NullPointerException.class, () -> list.contains(null));
        assertThrows(NullPointerException.class, () -> list.indexOf(null));
        assertThrows(NullPointerException.class, () -> list.lastIndexOf(null));

        List<String> listWithNull = Collections.singletonList(null);
        assertThrows(NullPointerException.class, () -> list.containsAll(listWithNull));

        // Exception: if the collection knows it doesn't contain everything due to an element preceding null,
        // no exception will be thrown
        assertThat(list.containsAll(Arrays.asList("qqqq", null)), equalTo(false));
    }

    /**
     * Verifies that a NullPointerException is thrown by all methods that don't modify the collection
     * if null is supplied as argument.
     *
     * @param set the set to test
     */
    public static void verifyRejectsNullArgInMethods(Set<Integer> set) {
        assertThrows(NullPointerException.class, () -> set.contains(null));

        List<String> listWithNull = Collections.singletonList(null);
        assertThrows(NullPointerException.class, () -> set.containsAll(listWithNull));

        // Exception: if the collection knows it doesn't contain everything due to an element preceding null,
        // no exception will be thrown
        assertThat(set.containsAll(Arrays.asList(-555, null)), equalTo(false));
    }

    // --------------------------
    // Helpers
    // --------------------------

    /**
     * Helper class to verify that a Set is not modifiable.
     */
    private static final class SetVerifier {

        private final Set<Integer> originalSet;
        private final ThrowingBehavior throwingBehavior;

        SetVerifier(Set<Integer> originalSet, ThrowingBehavior throwingBehavior) {
            this.originalSet = originalSet;
            this.throwingBehavior = throwingBehavior;
        }

        SetVerifier test(Consumer<Set<Integer>> action) {
            return test(action, null);
        }

        SetVerifier test(Consumer<Set<Integer>> action, Class<? extends Exception> expectedExceptionType) {
            Class<? extends Exception> expectedException = expectedExceptionType;
            if (expectedException == null) {
                expectedException = getExpectedExceptionType(action);
            }

            if (expectedException != null) {
                assertThrows(expectedException, () -> action.accept(originalSet));
            } else {
                action.accept(originalSet);
            }
            return this;
        }

        private Class<? extends Exception> getExpectedExceptionType(Consumer<Set<Integer>> action) {
            return switch (this.throwingBehavior) {
                case ALWAYS_THROWS -> UnsupportedOperationException.class;

                case THROW_INDEX_OUT_OF_BOUNDS_OR_IF_CHANGE ->
                    throw new UnsupportedOperationException("Unsupported throwing behavior for sets: "
                        + ThrowingBehavior.THROW_INDEX_OUT_OF_BOUNDS_OR_IF_CHANGE);

                case THROW_ONLY_IF_CHANGE -> {
                    Set<Integer> copy = new HashSet<>(originalSet);
                    action.accept(copy);
                    yield copy.equals(originalSet) ? null : UnsupportedOperationException.class;
                }
            };
        }
    }
}
