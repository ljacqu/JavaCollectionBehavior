package ch.jalu.collectionbehavior;

import ch.jalu.collectionbehavior.model.ListCreator;
import ch.jalu.collectionbehavior.model.ListCreator.ListBasedListCreator;
import ch.jalu.collectionbehavior.model.ListMethod;
import ch.jalu.collectionbehavior.model.ListWithBackingDataModifier;
import ch.jalu.collectionbehavior.model.ModificationBehavior;
import ch.jalu.collectionbehavior.model.NullSupport;
import ch.jalu.collectionbehavior.model.RandomAccessType;
import ch.jalu.collectionbehavior.verification.ListModificationVerifier;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.SequencedCollection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.immutable_changeToOriginalStructureIsNotReflectedInList;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.unmodifiable_changeToOriginalStructureIsReflectedInList;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.verifyCannotBeModifiedByIterator;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.verifyCannotBeModifiedByListIterator;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.verifyIsMutableByIteratorAndListIterator;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.verifyIsMutableByReversedList;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.verifyIsMutableBySubList;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.verifyListIsMutable;
import static ch.jalu.collectionbehavior.verification.CollectionNullBehaviorVerifier.verifyRejectsNullArgInMethods;
import static ch.jalu.collectionbehavior.verification.CollectionNullBehaviorVerifier.verifySupportsNullArgInMethods;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ListTest {

    @Test
    void assertListSupertypeIsSequencedCollection() {
        // Since List implements SequencedCollection, every List implementation is a SequencedCollection
        assertThat(SequencedCollection.class.isAssignableFrom(List.class), equalTo(true));
    }

    /**
     * {@link ArrayList}: standard modifiable List implementation. Fully supports null.
     */
    @TestFactory
    List<DynamicTest> jdk_ArrayList() {
        return forListType(ListCreator.forMutableType(ArrayList::new))
            .expect(NullSupport.FULL, RandomAccessType.IMPLEMENTS)
            .mutability(ModificationBehavior.mutable())
            .createTests();
    }

    /**
     * {@link LinkedList}: mutable, fully supports null. Does not implement RandomAccess.
     */
    @TestFactory
    List<DynamicTest> jdk_LinkedList() {
        return forListType(ListCreator.forMutableType(LinkedList::new))
            .expect(NullSupport.FULL, RandomAccessType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.mutable())
            .createTests();
    }

    /**
     * {@link List#of(Object[])} produces an immutable list that does not support null,
     * even when null is called on {@link List#contains}.
     */
    @TestFactory
    List<DynamicTest> jdk_List_of() {
        return forListType(ListCreator.forArrayBasedType(List::of))
            .expect(NullSupport.REJECT, RandomAccessType.IMPLEMENTS)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .createTests();
    }

    /**
     * {@link List#copyOf(Collection)} copies the incoming collection and returns an immutable List;
     * recognizes lists of its own class and returns the same instance instead of unnecessarily copying.
     * Does not support null (not even as argument passed into things like {@link List#contains}).
     */
    @TestFactory
    List<DynamicTest> jdk_List_copyOf() {
        return forListType(ListCreator.forListBasedType(List::copyOf))
            .expect(NullSupport.REJECT, RandomAccessType.IMPLEMENTS)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Arrays#asList(Object[])} wraps an array into the List interface. Allows to change individual elements
     * but elements cannot be added or removed. The array passed into it is not copied! Supports null.
     * Basically wraps an array into a List interface as changes to the array are propagated to the List and
     * vice versa (changing an entry in the List actually changes the backing array).
     */
    @TestFactory
    List<DynamicTest> jdk_Arrays_asList() {
        return forListType(ListCreator.forArrayBasedType(Arrays::asList))
            .expect(NullSupport.FULL, RandomAccessType.IMPLEMENTS)
            .mutability(ModificationBehavior.unmodifiable().throwsOnSizeModification())
            .createTests();
    }

    /**
     * Lists returned by {@link Arrays#asList(Object[])} can be modified (just not in size). Any changes are
     * propagated to the original array.
     * See {@link #jdk_Arrays_asList()} for the rest of the behavior. This test shows that the backing array is
     * modified.
     */
    @Test
    void jdk_Arrays_asList_modifiesBackingArray() {
        // The list from Arrays#asList just delegates to the wrapped array, so anything that can be done on
        // the array (changing an existing value, but not adding a new value) is supported
        String[] elements = {"a", "b", "c", "d"};
        List<String> list = Arrays.asList(elements);
        list.set(2, "foo");
        assertThat(list, contains("a", "b", "foo", "d"));
        assertThat(elements, arrayContaining("a", "b", "foo", "d")); // backing array changed via list
    }

    /**
     * {@link ImmutableList#copyOf(Object[])} returns an immutable list. Does not support null as element but
     * null can be passed as argument into methods like {@link List#contains}.
     * Prefer {@link ImmutableList#of} if you are not starting from an array (unlike this test case).
     */
    @TestFactory
    List<DynamicTest> guava_ImmutableList() {
        return forListType(ListCreator.forArrayBasedType(ImmutableList::copyOf))
            .expect(NullSupport.ARGUMENTS, RandomAccessType.IMPLEMENTS)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .mutabilityReversed(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL, ListMethod.SORT)
            )
            .createTests();
    }

    /**
     * {@link ImmutableList#copyOf(Collection)} copies the incoming collection and produces an immutable
     * List. Recognizes instances of the same class and avoids unnecessary copies. Does not support null as
     * element but accepts null passed as argument into {@link List#contains} etc.
     */
    @TestFactory
    List<DynamicTest> guava_ImmutableList_copyOf() {
        return forListType(ListCreator.forListBasedType(ImmutableList::copyOf))
            .expect(NullSupport.ARGUMENTS, RandomAccessType.IMPLEMENTS)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .mutabilityReversed(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL, ListMethod.SORT)
            )
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Collections#emptyList()} returns an immutable empty list. Always the same instance.
     * Nice readable name for when an empty list is desired to be returned.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_emptyList() {
        return forListType(ListCreator.forEmptyList(Collections::emptyList))
            .expect(NullSupport.ARGUMENTS, RandomAccessType.IMPLEMENTS)
            .mutability(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .throwsUnsupportedOpForIndexOutOfBounds())
            .mutabilitySubList(ModificationBehavior.immutable().throwsIfWouldBeModified())
            .mutabilityReversed(ModificationBehavior.immutable().throwsIfWouldBeModified())
            .createTests();
    }

    /**
     * {@link Collections#unmodifiableList(List)} wraps a List into an unmodifiable List, i.e. changes to the
     * original List are propagated but cannot be modified via the returned list itself. Fully supports null
     * as elements or method parameter.
     * Implements RandomAccess only if the underlying List implements it.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_unmodifiableList() {
        // Collections#unmodifiableList returns the same instance in JDK 17 if the list to wrap was created by this
        // method, whereas in JDK 11 it always created a new instance
        return forListType(ListCreator.forListBasedType(Collections::unmodifiableList))
            .expect(NullSupport.FULL, RandomAccessType.PRESERVES)
            .mutability(ModificationBehavior.unmodifiable().alwaysThrows())
            .mutabilityReversed(ModificationBehavior.unmodifiable().throwsIfWouldBeModified()
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL, ListMethod.SORT)
            )
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Collections#singletonList(Object)} provides a list with a single given element. Immutable. Supports null.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_singletonList() {
        return forListType(ListCreator.forSingleElement(Collections::singletonList))
            .expect(NullSupport.FULL, RandomAccessType.IMPLEMENTS)
            .mutability(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .throwsUnsupportedOpForIndexOutOfBounds()
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL)
            )
            .mutabilitySubList(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .alwaysThrowsFor(ListMethod.REPLACE_ALL, ListMethod.SORT)
            )
            .mutabilityReversed(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .alwaysThrowsFor(ListMethod.REMOVE_IF, ListMethod.REPLACE_ALL)
            )
            .createTests();
    }

    /**
     * {@link Collectors#toList()} produces an ArrayList, although it makes no guarantees about the returned type,
     * nor about the mutability of the return value.
     * As it creates an ArrayList (for now), null values are supported.
     */
    @Test
    void jdk_Collectors_toList() {
        // Is mutable (but Javadoc makes no guarantees)
        List<String> list = Stream.of("a", "b", "c", "d").collect(Collectors.toList());
        assertThat(list.getClass(), equalTo(ArrayList.class));
    }

    /**
     * {@link Collectors#toUnmodifiableList()} produces an unmodifiable list. It throws an exception if null is
     * passed to it or to any of its methods.
     */
    @TestFactory
    List<DynamicTest> jdk_Collectors_toUnmodifiableList() {
        // Implements RandomAccess (but Javadoc makes no guarantees)
        return forListType(ListCreator.fromStream(str -> str.collect(Collectors.toUnmodifiableList())))
            .expect(NullSupport.REJECT, RandomAccessType.IMPLEMENTS)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .createTests();
    }

    /**
     * {@link Stream#toList()} produces an unmodifiable list that supports nulls.
     */
    @TestFactory
    List<DynamicTest> jdk_Stream_toList() {
        return forListType(ListCreator.fromStream(Stream::toList))
            .expect(NullSupport.FULL, RandomAccessType.IMPLEMENTS)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .createTests();
    }

    /**
     * Creates a new test generator instance for the given list creator.
     *
     * @param listCreator list creator providing the type to test
     * @return test generator
     */
    private static TestsGenerator forListType(ListCreator listCreator) {
        StackWalker instance = StackWalker.getInstance();
        String testName = instance.walk(frames -> frames.skip(1).findFirst()).get().getMethodName();
        return new TestsGenerator(listCreator, testName);
    }

    /**
     * Generates tests based on the expected behavior that is defined.
     */
    private static final class TestsGenerator {

        private final ListCreator listCreator;
        private final String testName;
        private final TestLogic testLogic;

        private RandomAccessType randomAccessType;
        private NullSupport nullSupport;
        private boolean skipsWrappingForOwnClass;

        private ModificationBehavior modificationBehavior;
        private ModificationBehavior modificationBehaviorSubList;
        private ModificationBehavior modificationBehaviorReversed;

        TestsGenerator(ListCreator listCreator, String testName) {
            this.listCreator = listCreator;
            this.testName = testName;
            this.testLogic = new TestLogic(listCreator);
        }

        /**
         * Sets some basic expected properties of the list type to this generator.
         *
         * @param nullSupport expected null support of the list type
         * @param randomAccessType expresses how RandomAccess is expected to be implemented (or not) by the list type
         * @return this instance, for chaining
         */
        TestsGenerator expect(NullSupport nullSupport, RandomAccessType randomAccessType) {
            this.nullSupport = nullSupport;
            this.randomAccessType = randomAccessType;
            return this;
        }

        /**
         * Sets the expected mutability behavior of this list type. If not overridden, the mutability definition is also
         * taken over for this list type's associated lists: {@link List#subList} and {@link List#reversed()}.
         *
         * @param expectedModificationBehavior definition of how this list type is expected to behave wrt mutability
         * @return this instance, for chaining
         */
        TestsGenerator mutability(ModificationBehavior expectedModificationBehavior) {
            this.modificationBehavior = expectedModificationBehavior;
            this.modificationBehaviorSubList = expectedModificationBehavior;
            this.modificationBehaviorReversed = expectedModificationBehavior;
            return this;
        }

        /**
         * Sets the expected mutability behavior of this list type's subList type ({@link List#subList}).
         *
         * @param expectedModificationBehaviorSubList definition of how this list type's subList is expected to behave
         *                                            wrt mutability
         * @return this instance, for chaining
         */
        TestsGenerator mutabilitySubList(ModificationBehavior expectedModificationBehaviorSubList) {
            this.modificationBehaviorSubList = expectedModificationBehaviorSubList;
            return this;
        }

        /**
         * Sets the expected mutability behavior of this list type's reversed type ({@link List#reversed}).
         *
         * @param expectedModificationBehaviorReversed definition of how this list type's reverse list is expected to
         *                                             behave wrt mutability
         * @return this instance, for chaining
         */
        TestsGenerator mutabilityReversed(ModificationBehavior expectedModificationBehaviorReversed) {
            this.modificationBehaviorReversed = expectedModificationBehaviorReversed;
            return this;
        }

        /**
         * Only applicable for list-based list creators: it is expected that the method recognizes lists of its return
         * type and that it will not wrap those lists again, i.e. it returns the same list in this case.
         *
         * @return this instance, for chaining
         */
        TestsGenerator skipsWrappingForOwnClass() {
            this.skipsWrappingForOwnClass = true;
            return this;
        }

        /**
         * Creates the tests based on the expectations that were set to this generator.
         *
         * @return tests to run to verify the expected behavior
         */
        List<DynamicTest> createTests() {
            List<DynamicTest> tests = new ArrayList<>();
            tests.addAll(createTestsForMutability());
            tests.add(createTestForRandomAccess());
            tests.addAll(createTestForSkipsWrappingOwnClassIfApplicable());
            tests.addAll(createTestsForNullSupport());
            return tests;
        }

        private DynamicTest createTestForRandomAccess() {
            return switch (randomAccessType) {
                case IMPLEMENTS -> testLogic.isRandomAccess();
                case PRESERVES -> testLogic.preservesRandomAccess();
                case DOES_NOT_IMPLEMENT -> testLogic.isNotRandomAccess();
            };
        }

        private List<DynamicTest> createTestForSkipsWrappingOwnClassIfApplicable() {
            if (skipsWrappingForOwnClass) {
                return List.of(testLogic.skipsWrappingForOwnClass());
            }
            return Collections.emptyList();
        }

        private List<DynamicTest> createTestsForMutability() {
            if (modificationBehavior.isMutable()) {
                return createTestsForMutableAssertions();
            }
            return createTestsForUnmodifiableAssertions();
        }

        private List<DynamicTest> createTestsForNullSupport() {
            List<DynamicTest> tests = switch (nullSupport) {
                case FULL -> List.of(
                    testLogic.supportsNullElements(),
                    testLogic.supportsNullMethodArgs());

                case ARGUMENTS -> List.of(
                    testLogic.mayNotContainNull(),
                    testLogic.supportsNullMethodArgs());

                case REJECT -> List.of(
                    testLogic.mayNotContainNull(),
                    testLogic.rejectsNullMethodArgs());
            };

            if (listCreator.getSizeLimit() == 0) {
                // Null elements don't apply to an empty list type :)
                return tests.subList(1, tests.size());
            }
            return tests;
        }

        /**
         * Returns tests to run for a list creator whose type should be fully modifiable.
         *
         * @return tests to run
         */
        private List<DynamicTest> createTestsForMutableAssertions() {
            List<String> emptyList = listCreator.createList();
            return List.of(
                dynamicTest("mutable", () -> verifyListIsMutable(emptyList)),
                dynamicTest("mutable_iterators", () -> verifyIsMutableByIteratorAndListIterator(emptyList)),
                dynamicTest("mutable_subList", () -> verifyIsMutableBySubList(emptyList)),
                dynamicTest("mutable_reversed", () -> verifyIsMutableByReversedList(emptyList)));
        }

        /**
         * Returns tests to run to verify that the list type of the list creator is unmodifiable (and immutable,
         * if specified by the expected {@code mutability}).
         *
         * @return tests to run to verify the list type is unmodifiable (and immutable, if applicable)
         */
        private List<DynamicTest> createTestsForUnmodifiableAssertions() {
            List<String> list = listCreator.createListWithAbcdOrSubset();

            DynamicTest testForImmutabilityType = createTestForImmutabilityTypeIfApplicable();
            DynamicTest testForIteratorModification = listCreator.getSizeLimit() == 0
                ? null
                : dynamicTest("unmodifiable_iterator", () -> verifyCannotBeModifiedByIterator(list));

            return Stream.of(
                    testForImmutabilityType,
                    dynamicTest("unmodifiable",
                        () -> ListModificationVerifier.testMethods(list, modificationBehavior)),
                    dynamicTest("unmodifiable_subList",
                        () -> ListModificationVerifier.testMethods(list.subList(0, list.size()), modificationBehaviorSubList)),
                    dynamicTest("unmodifiable_reversed",
                        () -> ListModificationVerifier.testMethods(list.reversed(), modificationBehaviorReversed)),
                    testForIteratorModification,
                    dynamicTest("unmodifiable_listIterator",
                        () -> verifyCannotBeModifiedByListIterator(modificationBehavior, listCreator, list)))
                .filter(Objects::nonNull)
                .toList();
        }

        private DynamicTest createTestForImmutabilityTypeIfApplicable() {
            Optional<ListWithBackingDataModifier> listWithDataModifier =
                listCreator.createListWithBackingDataModifier("a", "b", "c", "d");

            if (!modificationBehavior.isImmutable) {
                // Must be able to create this in order to "claim" that it's not immutable
                ListWithBackingDataModifier listWithBd = listWithDataModifier.orElseThrow();
                return dynamicTest("unmodifiable_changeToOriginalStructureReflectedInList",
                    () -> unmodifiable_changeToOriginalStructureIsReflectedInList(listWithBd));
            }

            return listWithDataModifier
                .map(listWithBd -> dynamicTest("immutable_originalStructureDoesNotChangeList",
                    () -> immutable_changeToOriginalStructureIsNotReflectedInList(listWithBd)))
                .orElse(null);
        }
    }

    /** Provides individual test methods to verify list implementations. */
    private static final class TestLogic {

        private final ListCreator listCreator;

        private TestLogic(ListCreator listCreator) {
            this.listCreator = listCreator;
        }

        DynamicTest supportsNullElements() {
            return dynamicTest("supportsNullElements",
                () -> assertDoesNotThrow(listCreator::createListWithNull));
        }

        DynamicTest mayNotContainNull() {
            return dynamicTest("mayNotContainNull",
                () -> assertThrows(NullPointerException.class, listCreator::createListWithNull));
        }

        DynamicTest supportsNullMethodArgs() {
            return dynamicTest("supportsNullMethodArgs",
                () -> verifySupportsNullArgInMethods(listCreator.createList()));
        }

        DynamicTest rejectsNullMethodArgs() {
            return dynamicTest("rejectsNullMethodArgs",
                () -> verifyRejectsNullArgInMethods(listCreator.createList()));
        }

        DynamicTest skipsWrappingForOwnClass() {
            ListBasedListCreator listCopyCreator = (ListBasedListCreator) listCreator;
            return dynamicTest("skipsWrappingForOwnClass", () -> {
                List<String> list1 = listCopyCreator.newList(Arrays.asList("a", "b"));
                List<String> list2 = listCopyCreator.newList(list1);
                assertThat(list1, sameInstance(list2));
            });
        }

        DynamicTest isRandomAccess() {
            if (listCreator instanceof ListBasedListCreator lbc) {
                return dynamicTest("isRandomAccess", () -> {
                    assertThat(lbc.newList(new ArrayList<>()), instanceOf(RandomAccess.class));
                    assertThat(lbc.newList(new LinkedList<>()), instanceOf(RandomAccess.class));
                });
            }

            return dynamicTest("isRandomAccess",
                () -> assertThat(listCreator.createList(), instanceOf(RandomAccess.class)));
        }

        DynamicTest preservesRandomAccess() {
            if (listCreator instanceof ListBasedListCreator lbc) {
                return dynamicTest("preservesRandomAccess", () -> {
                    assertThat(lbc.newList(new ArrayList<>()), instanceOf(RandomAccess.class));
                    assertThat(lbc.newList(new LinkedList<>()), not(instanceOf(RandomAccess.class)));
                });
            } else {
                // Test configuration error
                throw new UnsupportedOperationException("Preserves RandomAccess but list creator is not list-based");
            }
        }

        DynamicTest isNotRandomAccess() {
            return dynamicTest("isNotRandomAccess",
                () -> assertThat(listCreator.createList(), not(instanceOf(RandomAccess.class))));
        }
    }
}