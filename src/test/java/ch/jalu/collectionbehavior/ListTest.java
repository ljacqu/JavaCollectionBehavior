package ch.jalu.collectionbehavior;

import ch.jalu.collectionbehavior.model.ListBehaviorType;
import ch.jalu.collectionbehavior.model.ListCreator;
import ch.jalu.collectionbehavior.model.ListCreator.CopyBasedListCreator;
import ch.jalu.collectionbehavior.model.ListExpectedBehavior;
import ch.jalu.collectionbehavior.model.ListMethod;
import ch.jalu.collectionbehavior.model.MutabilityType;
import ch.jalu.collectionbehavior.model.NullSupport;
import ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;
import java.util.SequencedCollection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyIsUnmodifiable;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifyRejectsNullArgInMethods;
import static ch.jalu.collectionbehavior.CollectionBehaviorTestUtil.verifySupportsNullArgInMethods;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ListTest {

    @Test
    void assertListSupertypeIsSequencedCollection() {
        // Since List implements SequencedCollection, every List implementation is a SequencedCollection
        assertThat(SequencedCollection.class.isAssignableFrom(List.class), equalTo(true));
    }

    static class TestsGenerator {

        private MutabilityType mutabilityType;
        private boolean isRandomAccess;
        private NullSupport nullSupport;

        private ListCreator listCreator;
        private boolean skipsWrappingForOwnClass;
        private ListBehaviorType listBehaviorType = ListBehaviorType.DEFAULT; // todo revise this
        private ListExpectedBehavior unmodifiableBehavior;
        private ListExpectedBehavior unmodifiableBehaviorSubList;
        private ListExpectedBehavior unmodifiableBehaviorReversed;

        static TestsGenerator forProperties(MutabilityType mutabilityType,
                                            boolean isRandomAccess,
                                            NullSupport nullSupport) {
            TestsGenerator generator = new TestsGenerator();
            generator.mutabilityType = mutabilityType;
            generator.isRandomAccess = isRandomAccess;
            generator.nullSupport = nullSupport;
            return generator;
        }

        TestsGenerator withListCreator(ListCreator listCreator) {
            this.listCreator = listCreator;
            return this;
        }

        TestsGenerator skipsWrappingForOwnClass() {
            this.skipsWrappingForOwnClass = true;
            return this;
        }

        TestsGenerator setListBehaviorType(ListBehaviorType listBehaviorType) {
            this.listBehaviorType = listBehaviorType;
            return this;
        }

        TestsGenerator unmodifiableBehavior(ListExpectedBehavior unmodifiableBehavior) {
            this.unmodifiableBehavior = unmodifiableBehavior;
            return this;
        }

        TestsGenerator unmodifiableBehaviorSubList(ListExpectedBehavior unmodifiableBehaviorSubList) {
            this.unmodifiableBehaviorSubList = unmodifiableBehaviorSubList;
            return this;
        }

        TestsGenerator unmodifiableBehaviorReversed(ListExpectedBehavior unmodifiableBehaviorReversed) {
            this.unmodifiableBehaviorReversed = unmodifiableBehaviorReversed;
            return this;
        }

        public List<DynamicTest> createTests() {
            List<DynamicTest> tests = new ArrayList<>();
            tests.addAll(createTestsForMutability());
            tests.add(createTestForRandomAccess());
            tests.addAll(createTestForSkipsWrappingOwnClassIfApplicable());
            tests.addAll(createTestsForNullSupport());
            return tests;
        }

        public DynamicTest createTestForRandomAccess() {
            if (isRandomAccess) {
                return dynamicTest("randomAccess",
                    () -> assertThat(listCreator.createList(), instanceOf(RandomAccess.class)));
            } else {
                return dynamicTest("noRandomAccess",
                    () -> assertThat(listCreator.createList(), not(instanceOf(RandomAccess.class))));
            }
        }

        public List<DynamicTest> createTestForSkipsWrappingOwnClassIfApplicable() {
            if (skipsWrappingForOwnClass) {
                CopyBasedListCreator listCopyCreator = (CopyBasedListCreator) listCreator;
                DynamicTest test = dynamicTest("skipsWrappingForOwnClass", () -> {
                    List<String> list1 = listCopyCreator.newList(Arrays.asList("a", "b"));
                    List<String> list2 = listCopyCreator.newList(list1);
                    assertThat(list1, sameInstance(list2));
                });
                return List.of(test);
            }
            return Collections.emptyList();
        }

        private List<DynamicTest> createTestsForMutability() {
            return switch (mutabilityType) {
                case MODIFIABLE -> CollectionMutabilityVerifier.createTestsForMutableAssertions(listCreator);

                case UNMODIFIABLE -> CollectionMutabilityVerifier.createTestsForUnmodifiableAssertions(listCreator,
                    unmodifiableBehavior, unmodifiableBehaviorSubList, unmodifiableBehaviorReversed);

                case IMMUTABLE -> CollectionMutabilityVerifier.createTestsForImmutableAssertions(listCreator,
                    unmodifiableBehavior, unmodifiableBehaviorSubList, unmodifiableBehaviorReversed);
            };
        }

        private List<DynamicTest> createTestsForNullSupport() {
            List<DynamicTest> tests = switch (nullSupport) {
                case FULL -> List.of(
                    dynamicTest("supportsNullElements",
                        () -> listCreator.listWithNull()),
                    dynamicTest("supportsNullMethodArgs",
                        () -> verifySupportsNullArgInMethods(listCreator.createList())));

                case ARGUMENTS -> List.of(
                    dynamicTest("mayNotContainNull",
                        () -> assertThrows(NullPointerException.class, () -> listCreator.listWithNull())),
                    dynamicTest("supportsNullMethodArgs",
                        () -> verifySupportsNullArgInMethods(listCreator.createList())));

                case REJECT -> List.of(
                    dynamicTest("mayNotContainNull",
                        () -> assertThrows(NullPointerException.class, () -> listCreator.listWithNull())),
                    dynamicTest("rejectsNullMethodArgs",
                        () -> verifyRejectsNullArgInMethods(listCreator.createList())));
            };

            if (listBehaviorType == ListBehaviorType.COLLECTIONS_EMPTYLIST) {
                return tests.subList(1, tests.size());
            }
            return tests;
        }
    }

    /**
     * ArrayList: standard modifiable List implementation. Fully supports null.
     */
    @TestFactory
    List<DynamicTest> jdk_ArrayList() {
        return TestsGenerator.forProperties(MutabilityType.MODIFIABLE, true, NullSupport.FULL)
            .withListCreator(ListCreator.forMutableType(ArrayList::new))
            .createTests();
    }

    /**
     * LinkedList: mutable, fully supports null.
     */
    @TestFactory
    List<DynamicTest> jdk_LinkedList() {
        return TestsGenerator.forProperties(MutabilityType.MODIFIABLE, false, NullSupport.FULL)
            .withListCreator(ListCreator.forMutableType(LinkedList::new))
            .createTests();
    }

    /**
     * {@link List#of} produces an immutable list that does not support null,
     * even when null is called on {@link List#contains}.
     */
    @TestFactory
    List<DynamicTest> jdk_List_of() {
        return TestsGenerator.forProperties(MutabilityType.IMMUTABLE, true, NullSupport.REJECT)
            .withListCreator(ListCreator.forArrayBasedType(List::of))
            .unmodifiableBehavior(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorSubList(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorReversed(ListExpectedBehavior.alwaysThrows())
            .createTests();
    }

    /**
     * {@link List#copyOf} copies the incoming collection and returns an immutable List;
     * recognizes lists of its own class and returns the same instance instead of unnecessarily copying.
     * Does not support null (not even as argument passed into things like {@link List#contains}).
     */
    @TestFactory
    List<DynamicTest> jdk_List_copyOf() {
        return TestsGenerator.forProperties(MutabilityType.IMMUTABLE, true, NullSupport.REJECT)
            .withListCreator(ListCreator.forListBasedType(List::copyOf))
            .unmodifiableBehavior(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorSubList(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorReversed(ListExpectedBehavior.alwaysThrows())
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Arrays#asList} wraps an array into the List interface. Allows to change individual elements
     * but elements cannot be added or removed. The array passed into it is not copied! Supports null.
     * Basically wraps an array into a List interface as changes to the array are propagated to the List and
     * vice versa (changing an entry in the List actually changes the backing array).
     */
    @TestFactory
    List<DynamicTest> jdk_Arrays_asList() {
        return TestsGenerator.forProperties(MutabilityType.UNMODIFIABLE, true, NullSupport.FULL)
            .withListCreator(ListCreator.forArrayBasedType(Arrays::asList))
            .unmodifiableBehavior(ListExpectedBehavior.throwsOnSizeModification())
            .unmodifiableBehaviorSubList(ListExpectedBehavior.throwsOnSizeModification())
            .unmodifiableBehaviorReversed(ListExpectedBehavior.throwsOnSizeModification())
            .setListBehaviorType(ListBehaviorType.ARRAYS_ASLIST)
            .createTests();
    }

    /**
     * {@link Arrays#asList} wraps an array into the List interface. Allows to change individual elements
     * but elements cannot be added or removed. The array passed into it is not copied! Supports null.
     * Basically wraps an array into a List interface as changes to the array are propagated to the List and
     * vice versa (changing an entry in the List actually changes the backing array).
     */
    @Test
    void jdkArraysAsList() {
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
    }

    /**
     * {@link ImmutableList#copyOf} returns an immutable list. Does not support null as element but
     * null can be passed as argument into methods like {@link List#contains}.
     * Prefer {@link ImmutableList#of} if you are not starting from an array (unlike this test case).
     */
    @TestFactory
    List<DynamicTest> guava_ImmutableList() {
        return TestsGenerator.forProperties(MutabilityType.IMMUTABLE, true, NullSupport.ARGUMENTS)
            .withListCreator(ListCreator.forArrayBasedType(ImmutableList::copyOf))
            .unmodifiableBehavior(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorSubList(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorReversed(ListExpectedBehavior.throwsIfWouldBeModified(false)
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL)
            )
            .setListBehaviorType(ListBehaviorType.GUAVA_IMMUTABLE_LIST)
            .createTests();
    }

    /**
     * {@link ImmutableList#copyOf(Iterable)} copies the incoming collection and produces an immutable
     * List. Recognizes instances of the same class and avoids unnecessary copies. Does not support null as
     * element but accepts null passed as argument into {@link List#contains} etc.
     */
    @TestFactory
    List<DynamicTest> guava_ImmutableList_copyOf() {
        return TestsGenerator.forProperties(MutabilityType.IMMUTABLE, true, NullSupport.ARGUMENTS)
            .withListCreator(ListCreator.forListBasedType(ImmutableList::copyOf))
            .unmodifiableBehavior(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorSubList(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorReversed(ListExpectedBehavior.throwsIfWouldBeModified(false)
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL)
            )
            .setListBehaviorType(ListBehaviorType.GUAVA_IMMUTABLE_LIST)
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Collections#emptyList()} returns an immutable empty list. Always the same instance.
     * Nice readable name for when an empty list is desired to be returned.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_emptyList() {
        return TestsGenerator.forProperties(MutabilityType.IMMUTABLE, true, NullSupport.FULL)
            .withListCreator(ListCreator.forEmptyList(Collections::emptyList))
            .unmodifiableBehavior(ListExpectedBehavior.throwsIfWouldBeModified(false))
            .unmodifiableBehaviorSubList(ListExpectedBehavior.throwsIfWouldBeModified(true))
            .unmodifiableBehaviorReversed(ListExpectedBehavior.throwsIfWouldBeModified(true))
            .setListBehaviorType(ListBehaviorType.COLLECTIONS_EMPTYLIST)
            .createTests();
    }

    @TestFactory
    List<DynamicTest> jdk_Collections_unmodifiableList() {
        // Collections#unmodifiableList returns the same instance in JDK 17 if the list to wrap was created by this
        // method, whereas in JDK 11 it always created a new instance
        return TestsGenerator.forProperties(MutabilityType.UNMODIFIABLE, true, NullSupport.FULL)
            .withListCreator(ListCreator.forListBasedType(Collections::unmodifiableList))
            .unmodifiableBehavior(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorSubList(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorReversed(ListExpectedBehavior.throwsIfWouldBeModified(true)
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL)
            )
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Collections#unmodifiableList} wraps a List into an unmodifiable List, i.e. changes to the
     * original List are propagated but cannot be modified via the returned list itself. Fully supports null
     * as elements or method parameter.
     * Implements RandomAccess only if the underlying List implements it.
     */
    @Test
    void jdkCollectionsUnmodifiableList() {
        // Is unmodifiable
        List<String> elements = newArrayList("a", "b", "c", "d");
        List<String> list = Collections.unmodifiableList(elements);
        verifyIsUnmodifiable(list, () -> elements.set(2, "changed"));

        // Implements RandomAccess if underlying List implements it
        assertThat(list, instanceOf(RandomAccess.class)); // Because wrapped list is RandomAccess, too.
        assertThat(new LinkedList<>(), not(instanceOf(RandomAccess.class))); // validate assumption
        assertThat(Collections.unmodifiableList(new LinkedList<>()), not(instanceOf(RandomAccess.class)));
    }

    /**
     * {@link Collections#singletonList} provides a list with a single given element. Immutable. Supports null.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_singletonList() {
        return TestsGenerator.forProperties(MutabilityType.IMMUTABLE, true, NullSupport.FULL)
            .withListCreator(ListCreator.forSingleElement(Collections::singletonList))
            .unmodifiableBehavior(ListExpectedBehavior.throwsIfWouldBeModified(false)
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL)
            )
            .unmodifiableBehaviorSubList(ListExpectedBehavior.throwsIfWouldBeModified(true)
                .alwaysThrowsFor(ListMethod.REPLACE_ALL, ListMethod.SORT)
            )
            .unmodifiableBehaviorReversed(ListExpectedBehavior.throwsIfWouldBeModified(true)
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL)
            )
            .setListBehaviorType(ListBehaviorType.COLLECTIONS_SINGLETONLIST)
            .createTests();
    }

    /**
     * {@link Collectors#toList} produces an ArrayList, although it makes no guarantees about the returned type,
     * nor about the mutability of the return value.
     * As it creates an ArrayList (for now), null values are supported.
     */
    @Test
    void jdk_Collectors_toList() {
        // Is mutable (but Javadoc makes no guarantees)
        List<String> list = Stream.of("a", "b", "c", "d")
            .filter(str -> false)
            .collect(Collectors.toList());
        assertThat(list.getClass(), equalTo(ArrayList.class));
    }

    /**
     * {@link Collectors#toUnmodifiableList} produces an unmodifiable list. It throws an exception if null is
     * passed to it or to any of its methods.
     */
    @TestFactory
    List<DynamicTest> jdk_Collectors_toUnmodifiableList() {
        // Implements RandomAccess (but Javadoc makes no guarantees)
        return TestsGenerator.forProperties(MutabilityType.IMMUTABLE, true, NullSupport.REJECT)
            .withListCreator(ListCreator.fromStream(str -> str.collect(Collectors.toUnmodifiableList())))
            .unmodifiableBehavior(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorSubList(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorReversed(ListExpectedBehavior.alwaysThrows())
            .createTests();
    }

    /**
     * {@link Stream#toList} produces an unmodifiable list that supports nulls.
     */
    @TestFactory
    List<DynamicTest> jdk_Stream_toList() {
        return TestsGenerator.forProperties(MutabilityType.IMMUTABLE, true, NullSupport.FULL)
            .withListCreator(ListCreator.fromStream(Stream::toList))
            .unmodifiableBehavior(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorSubList(ListExpectedBehavior.alwaysThrows())
            .unmodifiableBehaviorReversed(ListExpectedBehavior.alwaysThrows())
            .createTests();
    }
}
