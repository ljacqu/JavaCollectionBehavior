package ch.jalu.collectionbehavior;

import ch.jalu.collectionbehavior.model.ListCreator;
import ch.jalu.collectionbehavior.model.ListCreator.ListBasedListCreator;
import ch.jalu.collectionbehavior.model.ListModificationBehavior;
import ch.jalu.collectionbehavior.model.ListMethod;
import ch.jalu.collectionbehavior.model.NullSupport;
import ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;

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

    private static class TestsGenerator {

        private String testName;
        private ListCreator listCreator;

        private boolean isRandomAccess;
        private NullSupport nullSupport;
        private boolean skipsWrappingForOwnClass;

        private ListModificationBehavior modificationBehavior;
        private ListModificationBehavior modificationBehaviorSubList;
        private ListModificationBehavior modificationBehaviorReversed;

        static TestsGenerator forType(TestInfo testInfo, ListCreator listCreator) {
            TestsGenerator generator = new TestsGenerator();
            generator.testName = testInfo.getTestMethod().get().getName();
            generator.listCreator = listCreator;
            return generator;
        }

        TestsGenerator expect(NullSupport nullSupport, boolean isRandomAccess) {
            this.nullSupport = nullSupport;
            this.isRandomAccess = isRandomAccess;
            return this;
        }

        TestsGenerator mutability(ListModificationBehavior expectedMutabilityBehavior) {
            this.modificationBehavior = expectedMutabilityBehavior;
            this.modificationBehaviorSubList = expectedMutabilityBehavior;
            this.modificationBehaviorReversed = expectedMutabilityBehavior;
            return this;
        }

        TestsGenerator mutabilitySubList(ListModificationBehavior unmodifiableBehaviorSubList) {
            this.modificationBehaviorSubList = unmodifiableBehaviorSubList;
            return this;
        }

        TestsGenerator mutabilityReversed(ListModificationBehavior unmodifiableBehaviorReversed) {
            this.modificationBehaviorReversed = unmodifiableBehaviorReversed;
            return this;
        }

        TestsGenerator skipsWrappingForOwnClass() {
            this.skipsWrappingForOwnClass = true;
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
                ListBasedListCreator listCopyCreator = (ListBasedListCreator) listCreator;
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
            if (modificationBehavior.isMutable()) {
                return CollectionMutabilityVerifier.createTestsForMutableAssertions(listCreator);
            }
            return CollectionMutabilityVerifier.createTestsForUnmodifiableAssertions(listCreator,
                modificationBehavior, modificationBehaviorSubList, modificationBehaviorReversed);
        }

        private List<DynamicTest> createTestsForNullSupport() {
            List<DynamicTest> tests = switch (nullSupport) {
                case FULL -> List.of(
                    dynamicTest("supportsNullElements",
                        () -> listCreator.createListWithNull()),
                    dynamicTest("supportsNullMethodArgs",
                        () -> verifySupportsNullArgInMethods(listCreator.createList())));

                case ARGUMENTS -> List.of(
                    dynamicTest("mayNotContainNull",
                        () -> assertThrows(NullPointerException.class, () -> listCreator.createListWithNull())),
                    dynamicTest("supportsNullMethodArgs",
                        () -> verifySupportsNullArgInMethods(listCreator.createList())));

                case REJECT -> List.of(
                    dynamicTest("mayNotContainNull",
                        () -> assertThrows(NullPointerException.class, () -> listCreator.createListWithNull())),
                    dynamicTest("rejectsNullMethodArgs",
                        () -> verifyRejectsNullArgInMethods(listCreator.createList())));
            };

            if (listCreator.getSizeLimit() == 0) {
                return tests.subList(1, tests.size());
            }
            return tests;
        }
    }

    /**
     * ArrayList: standard modifiable List implementation. Fully supports null.
     */
    @TestFactory
    List<DynamicTest> jdk_ArrayList(TestInfo testInfo) {
        return TestsGenerator.forType(testInfo, ListCreator.forMutableType(ArrayList::new))
            .expect(NullSupport.FULL, true)
            .mutability(ListModificationBehavior.mutable())
            .createTests();
    }

    /**
     * LinkedList: mutable, fully supports null.
     */
    @TestFactory
    List<DynamicTest> jdk_LinkedList(TestInfo testInfo) {
        return TestsGenerator.forType(testInfo, ListCreator.forMutableType(LinkedList::new))
            .expect(NullSupport.FULL, false)
            .mutability(ListModificationBehavior.mutable())
            .createTests();
    }

    /**
     * {@link List#of} produces an immutable list that does not support null,
     * even when null is called on {@link List#contains}.
     */
    @TestFactory
    List<DynamicTest> jdk_List_of(TestInfo testInfo) {
        return TestsGenerator.forType(testInfo, ListCreator.forArrayBasedType(List::of))
            .expect(NullSupport.REJECT, true)
            .mutability(ListModificationBehavior.immutable().alwaysThrows())
            .createTests();
    }

    /**
     * {@link List#copyOf} copies the incoming collection and returns an immutable List;
     * recognizes lists of its own class and returns the same instance instead of unnecessarily copying.
     * Does not support null (not even as argument passed into things like {@link List#contains}).
     */
    @TestFactory
    List<DynamicTest> jdk_List_copyOf(TestInfo testInfo) {
        return TestsGenerator.forType(testInfo, ListCreator.forListBasedType(List::copyOf))
            .expect(NullSupport.REJECT, true)
            .mutability(ListModificationBehavior.immutable().alwaysThrows())
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
    List<DynamicTest> jdk_Arrays_asList(TestInfo testInfo) {
        return TestsGenerator.forType(testInfo, ListCreator.forArrayBasedType(Arrays::asList))
            .expect(NullSupport.FULL, true)
            .mutability(ListModificationBehavior.unmodifiable().throwsOnSizeModification())
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
    List<DynamicTest> guava_ImmutableList(TestInfo testInfo) {
        return TestsGenerator.forType(testInfo, ListCreator.forArrayBasedType(ImmutableList::copyOf))
            .expect(NullSupport.ARGUMENTS, true)
            .mutability(ListModificationBehavior.immutable().alwaysThrows())
            .mutabilityReversed(ListModificationBehavior.immutable().throwsIfWouldBeModified(false)
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL)
            )
            .createTests();
    }

    /**
     * {@link ImmutableList#copyOf(Iterable)} copies the incoming collection and produces an immutable
     * List. Recognizes instances of the same class and avoids unnecessary copies. Does not support null as
     * element but accepts null passed as argument into {@link List#contains} etc.
     */
    @TestFactory
    List<DynamicTest> guava_ImmutableList_copyOf(TestInfo testInfo) {
        return TestsGenerator.forType(testInfo, ListCreator.forListBasedType(ImmutableList::copyOf))
            .expect(NullSupport.ARGUMENTS, true)
            .mutability(ListModificationBehavior.immutable().alwaysThrows())
            .mutabilityReversed(ListModificationBehavior.immutable().throwsIfWouldBeModified(false)
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL)
            )
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Collections#emptyList()} returns an immutable empty list. Always the same instance.
     * Nice readable name for when an empty list is desired to be returned.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_emptyList(TestInfo testInfo) {
        return TestsGenerator.forType(testInfo, ListCreator.forEmptyList(Collections::emptyList))
            .expect(NullSupport.ARGUMENTS, true)
            .mutability(ListModificationBehavior.immutable().throwsIfWouldBeModified(false))
            .mutabilitySubList(ListModificationBehavior.immutable().throwsIfWouldBeModified(true))
            .mutabilityReversed(ListModificationBehavior.immutable().throwsIfWouldBeModified(true))
            .createTests();
    }

    @TestFactory
    List<DynamicTest> jdk_Collections_unmodifiableList(TestInfo testInfo) {
        // Collections#unmodifiableList returns the same instance in JDK 17 if the list to wrap was created by this
        // method, whereas in JDK 11 it always created a new instance
        return TestsGenerator.forType(testInfo, ListCreator.forListBasedType(Collections::unmodifiableList))
            .expect(NullSupport.FULL, true)
            .mutability(ListModificationBehavior.unmodifiable().alwaysThrows())
            .mutabilityReversed(ListModificationBehavior.unmodifiable().throwsIfWouldBeModified(true)
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
    List<DynamicTest> jdk_Collections_singletonList(TestInfo testInfo) {
        return TestsGenerator.forType(testInfo, ListCreator.forSingleElement(Collections::singletonList))
            .expect(NullSupport.FULL, true)
            .mutability(ListModificationBehavior.immutable().throwsIfWouldBeModified(false)
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL)
            )
            .mutabilitySubList(ListModificationBehavior.immutable().throwsIfWouldBeModified(true)
                .alwaysThrowsFor(ListMethod.REPLACE_ALL, ListMethod.SORT)
            )
            .mutabilityReversed(ListModificationBehavior.immutable().throwsIfWouldBeModified(true)
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL)
            )
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
    List<DynamicTest> jdk_Collectors_toUnmodifiableList(TestInfo testInfo) {
        // Implements RandomAccess (but Javadoc makes no guarantees)
        return TestsGenerator.forType(testInfo, ListCreator.fromStream(str -> str.collect(Collectors.toUnmodifiableList())))
            .expect(NullSupport.REJECT, true)
            .mutability(ListModificationBehavior.immutable().alwaysThrows())
            .createTests();
    }

    /**
     * {@link Stream#toList} produces an unmodifiable list that supports nulls.
     */
    @TestFactory
    List<DynamicTest> jdk_Stream_toList(TestInfo testInfo) {
        return TestsGenerator.forType(testInfo, ListCreator.fromStream(Stream::toList))
            .expect(NullSupport.FULL, true)
            .mutability(ListModificationBehavior.immutable().alwaysThrows())
            .createTests();
    }
}
