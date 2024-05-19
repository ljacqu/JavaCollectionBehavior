package ch.jalu.collectionbehavior;

import ch.jalu.collectionbehavior.model.ModificationBehavior;
import ch.jalu.collectionbehavior.model.NullSupport;
import ch.jalu.collectionbehavior.model.SequencedSetType;
import ch.jalu.collectionbehavior.model.SetCreator;
import ch.jalu.collectionbehavior.model.SetCreator.FromCollectionSetCreator;
import ch.jalu.collectionbehavior.model.SetMethod;
import ch.jalu.collectionbehavior.model.SetOrder;
import ch.jalu.collectionbehavior.model.SetWithBackingDataModifier;
import ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.immutable_changeToOriginalStructureIsNotReflectedInSet;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.unmodifiable_changeToOriginalStructureIsReflectedInSet;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.verifyCannotBeModifiedByIterator;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.verifyIsMutableByIterator;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.verifyIsMutableBySequencedSetMethods;
import static ch.jalu.collectionbehavior.verification.CollectionMutabilityVerifier.verifySetExceptionBehavior;
import static ch.jalu.collectionbehavior.verification.CollectionNullBehaviorVerifier.verifyRejectsNullArgInMethods;
import static ch.jalu.collectionbehavior.verification.CollectionNullBehaviorVerifier.verifySupportsNullArgInMethods;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class SetTest {

    /**
     * {@link HashSet}: standard modifiable Set. Fully supports null. Does not keep insertion order.
     * Elements already added to it are silently ignored.
     */
    @TestFactory
    List<DynamicTest> jdk_HashSet() {
        return forSetType(SetCreator.forMutableType(HashSet::new))
            .expect(NullSupport.FULL, SetOrder.UNORDERED, SequencedSetType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.mutable())
            .createTests();
    }

    /**
     * {@link LinkedHashSet}: modifiable Set that keeps insertion order. Fully supports null.
     * Elements already added to it are silently ignored.
     */
    @TestFactory
    List<DynamicTest> jdk_LinkedHashSet() {
        return forSetType(SetCreator.forMutableType(LinkedHashSet::new))
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, SequencedSetType.IMPLEMENTS)
            .mutability(ModificationBehavior.mutable())
            .createTests();
    }

    /**
     * {@link Set#of(Object[])} produces an immutable Set. Does not support null (not even for {@link Set#contains}
     * etc.). Throws an exception if any element is passed in twice. Random iteration order.
     */
    @TestFactory
    List<DynamicTest> jdk_Set_of() {
        return forSetType(SetCreator.forArrayBasedType(Set::of))
            .expect(NullSupport.REJECT, SetOrder.UNORDERED, SequencedSetType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .rejectsDuplicatesOnCreation()
            .createTests();
    }

    /**
     * {@link Set#copyOf(Collection)} produces an immutable Set. Does not support null (not even for
     * {@link Set#contains} etc.). Duplicate elements in the original collection are ignored. Random iteration order.
     * Recognizes instances of the same class and avoids unnecessary copying.
     */
    @TestFactory
    List<DynamicTest> jdk_Set_copyOf() {
        return forSetType(SetCreator.fromCollection(Set::copyOf))
            .expect(NullSupport.REJECT, SetOrder.UNORDERED, SequencedSetType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link ImmutableSet#copyOf(Object[])} produces an immutable Set. Does not support null as elements.
     * Insertion order is kept. Can be instantiated with duplicates (also when using the builder,
     * {@link ImmutableSet#builder()}).
     */
    @TestFactory
    List<DynamicTest> guava_ImmutableSet() {
        // Keeps insertion order, but is not SequencedCollection because Guava supports older JDK versions
        // https://github.com/google/guava/issues/6903

        return forSetType(SetCreator.forArrayBasedType(ImmutableSet::copyOf))
            .expect(NullSupport.ARGUMENTS, SetOrder.INSERTION_ORDER, SequencedSetType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .createTests();
    }

    /**
     * {@link ImmutableSet#copyOf(Collection)} produces an immutable Set. Null as element is not supported but can be
     * used in {@link Set#contains} etc. Elements are copied. Retains iteration order of the original collection.
     * Recognizes its own instances and avoids unnecessary copies.
     */
    @TestFactory
    List<DynamicTest> guava_ImmutableSet_copyOf() {
        // Keeps insertion order, but is not SequencedCollection because Guava supports older JDK versions
        // https://github.com/google/guava/issues/6903

        return forSetType(SetCreator.fromCollection(ImmutableSet::copyOf))
            .expect(NullSupport.ARGUMENTS, SetOrder.INSERTION_ORDER, SequencedSetType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Collections#emptySet()} always returns the same instance: immutable empty Set.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_emptySet() {
        // Not a sequenced collection. Could probably implement it in theory, but there's no point to it?
        return forSetType(SetCreator.forEmptySet(Collections::emptySet))
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, SequencedSetType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().throwsIfWouldBeModified())
            .createTests();
    }

    /**
     * {@link Collections#unmodifiableSet(Set)} wraps a Set into an unmodifiable Set facade. Changes to the backing
     * collection are reflected. Supports null as elements. Iteration order kept from underlying collection.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_unmodifiableSet() {
        // Has same order as backing set, never SequencedCollection (even if backing set is)
        // Same instance returned in JDK 17, whereas in JDK 11 it always returned a new instance
        return forSetType(SetCreator.forSetBasedType(Collections::unmodifiableSet))
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, SequencedSetType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.unmodifiable().alwaysThrows())
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Collections#unmodifiableSequencedSet(SequencedSet)} wraps a sequenced set into an unmodifiable sequenced
     * set facade. Changes to the backing collection are reflected. Supports null as elements.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_unmodifiableSequencedSet() {
        Function<Set<String>, Set<String>> creationFn = input -> {
            if (input instanceof SequencedSet<String> seqSet) {
                return Collections.unmodifiableSequencedSet(seqSet);
            }
            return Collections.unmodifiableSequencedSet(new LinkedHashSet<>(input));
        };

        return forSetType(SetCreator.forSetBasedType(creationFn))
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, SequencedSetType.IMPLEMENTS)
            .mutability(ModificationBehavior.unmodifiable().alwaysThrows())
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Collections#singleton(Object)} produces an immutable Set with a single element. Supports null.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_singleton() {
        return forSetType(SetCreator.forSingleElement(Collections::singleton))
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, SequencedSetType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .alwaysThrowsFor(SetMethod.ADD, SetMethod.REMOVE_IF))
            .createTests();
    }

    /**
     * {@link Collectors#toSet()} makes no guarantees about the returned type or its mutability. For now, it
     * returns a HashSet and therefore also supports null values.
     */
    @Test
    void jdk_Collectors_toSet() {
        Set<String> set = Stream.of("f", "g")
            .collect(Collectors.toSet());
        assertThat(set.getClass(), equalTo(HashSet.class));
    }

    /**
     * {@link Collectors#toUnmodifiableSet()} produces an immutable Set. Null as element is not supported and may not be
     * used as argument in methods like {@link Set#contains}.
     */
    @TestFactory
    List<DynamicTest> jdk_Collectors_toUnmodifiableSet() {
        return forSetType(SetCreator.fromStream(str -> str.collect(Collectors.toUnmodifiableSet())))
            .expect(NullSupport.REJECT, SetOrder.UNORDERED, SequencedSetType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .createTests();
    }

    /**
     * Creates a new test generator instance for the given set creator.
     *
     * @param setCreator set creator providing the type to test
     * @return test generator
     */
    private static TestsGenerator forSetType(SetCreator setCreator) {
        StackWalker instance = StackWalker.getInstance();
        String testName = instance.walk(frames -> frames.skip(1).findFirst()).get().getMethodName();
        return new TestsGenerator(setCreator, testName);
    }

    /**
     * Generates tests based on the expected behavior that is defined.
     */
    private static final class TestsGenerator {

        private final SetCreator setCreator;
        private final String testName;
        private final TestLogic testLogic;

        private NullSupport nullSupport;
        private SetOrder elementOrder;
        private SequencedSetType sequencedSetType;
        private boolean acceptsDuplicatesOnCreation = true;
        private boolean skipsWrappingForOwnClass;

        private ModificationBehavior modificationBehavior;


        private TestsGenerator(SetCreator setCreator, String testName) {
            this.setCreator = setCreator;
            this.testName = testName;
            this.testLogic = new TestLogic(setCreator);
        }

        /**
         * Sets some basic expected properties of the set type to this generator.
         *
         * @param nullSupport expected null support of the list type
         * @param elementOrder expected order of the set's elements
         * @param sequencedSetType whether the set type extends SequencedSet
         * @return this instance, for chaining
         */
        TestsGenerator expect(NullSupport nullSupport, SetOrder elementOrder, SequencedSetType sequencedSetType) {
            this.nullSupport = nullSupport;
            this.elementOrder = elementOrder;
            this.sequencedSetType = sequencedSetType;
            return this;
        }

        /**
         * Registers the expected mutability behavior of the set type that should be tested.
         *
         * @param modificationBehavior definition of how the set is expected to behave wrt mutability
         * @return this instance, for chaining
         */
        TestsGenerator mutability(ModificationBehavior modificationBehavior) {
            this.modificationBehavior = modificationBehavior;
            return this;
        }

        /**
         * When the set is created based on another structure that allows duplicates, defines that duplicates in that
         * input structure will result in an exception.
         *
         * @return this instance, for chaining
         */
        TestsGenerator rejectsDuplicatesOnCreation() {
            this.acceptsDuplicatesOnCreation = false;
            return this;
        }

        /**
         * Only applicable for set-based set creators: it is expected that the method recognizes sets of its return
         * type and that it will not wrap those sets again, i.e. it returns the same set in this case.
         *
         * @return this instance, for chaining
         */
        TestsGenerator skipsWrappingForOwnClass() {
            this.skipsWrappingForOwnClass = true;
            return this;
        }

        List<DynamicTest> createTests() {
            return Stream.of(
                    createTestsForNullSupport(),
                    createTestForElementOrder(),
                    createTestForSequencedSetImpl(),
                    createTestsForMutability(),
                    createTestForSkipsWrappingOwnClassIfApplicable(),
                    createTestForDuplicatesOnCreation()
                )
                .flatMap(Function.identity())
                .toList();
        }

        private Stream<DynamicTest> createTestsForNullSupport() {
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

            if (setCreator.getSizeLimit() == 0) {
                // A null element does not apply to an empty set type, skip the test
                return tests.stream().skip(1);
            }
            return tests.stream();
        }

        private Stream<DynamicTest> createTestForElementOrder() {
            if (setCreator.getSizeLimit() <= 1) {
                // 0 or 1 element are treated as order, throw exception otherwise: misconfiguration
                Preconditions.checkState(elementOrder == SetOrder.INSERTION_ORDER);
                return Stream.empty();
            }

            return switch (elementOrder) {
                case INSERTION_ORDER -> Stream.of(testLogic.keepsElementsByInsertionOrder());
                case SORTED -> Stream.of(testLogic.keepsElementsSorted());
                case UNORDERED -> Stream.of(testLogic.hasRandomElementOrder());
            };
        }

        private Stream<DynamicTest> createTestForSequencedSetImpl() {
            return switch (sequencedSetType) {
                case IMPLEMENTS -> Stream.of(testLogic.isSequencedSet());
                case DOES_NOT_IMPLEMENT -> Stream.of(testLogic.isNotSequencedSet());
            };
        }

        private Stream<DynamicTest> createTestsForMutability() {
            if (modificationBehavior.isMutable()) {
                return createTestsForMutableAssertions().stream();
            }

            List<DynamicTest> testsToRun = new ArrayList<>();
            createTestForImmutabilityBehavior().ifPresent(testsToRun::add);
            testsToRun.add(dynamicTest("unmodifiable",
                () -> verifySetExceptionBehavior(setCreator.createSetWithAbcdOrSubset(), modificationBehavior)));

            if (setCreator.getSizeLimit() > 0) {
                testsToRun.add(dynamicTest("unmodifiable_iterator",
                    () -> verifyCannotBeModifiedByIterator(setCreator.createSetWithAbcdOrSubset())));
            }

            return testsToRun.stream();
        }

        /**
         * Returns tests to run for a list creator whose type should be fully modifiable.
         *
         * @return tests to run
         */
        private List<DynamicTest> createTestsForMutableAssertions() {
            Set<String> emptyList = setCreator.createSet();

            List<DynamicTest> testsToRun = new ArrayList<>();
            testsToRun.add(dynamicTest("mutable",
                () -> CollectionMutabilityVerifier.verifySetIsMutable(emptyList)));
            testsToRun.add(dynamicTest("mutable_iterator",
                () -> verifyIsMutableByIterator(emptyList)));
            if (emptyList instanceof SequencedSet<String> seqSet) {
                testsToRun.add(dynamicTest("mutable_sequencedSet",
                    () -> verifyIsMutableBySequencedSetMethods(seqSet)));
            }
            return testsToRun;
        }

        private Optional<DynamicTest> createTestForImmutabilityBehavior() {
            Optional<SetWithBackingDataModifier> setWithDataModifier =
                setCreator.createSetWithBackingDataModifier("a", "b", "c", "d");

            if (!modificationBehavior.isImmutable) {
                // Must be able to create this in order to "claim" that it's not immutable
                SetWithBackingDataModifier setWithBd = setWithDataModifier.orElseThrow();
                DynamicTest test = dynamicTest("unmodifiable_changeToOriginalStructureReflectedInList",
                    () -> unmodifiable_changeToOriginalStructureIsReflectedInSet(setWithBd));
                return Optional.of(test);
            }

            return setWithDataModifier
                .map(setWithBd -> dynamicTest("immutable_originalStructureDoesNotChangeList",
                    () -> immutable_changeToOriginalStructureIsNotReflectedInSet(setWithBd)));
        }

        private Stream<DynamicTest> createTestForSkipsWrappingOwnClassIfApplicable() {
            if (skipsWrappingForOwnClass) {
                return Stream.of(testLogic.skipsWrappingForOwnClass());
            } else if (setCreator instanceof FromCollectionSetCreator) {
                return Stream.of(testLogic.alwaysWrapsOwnClass());
            }
            return Stream.empty();
        }

        private Stream<DynamicTest> createTestForDuplicatesOnCreation() {
            if (!setCreator.canEncounterDuplicateArguments()) {
                Preconditions.checkState(acceptsDuplicatesOnCreation);
                return Stream.empty();
            }

            return acceptsDuplicatesOnCreation
                ? Stream.of(testLogic.acceptsDuplicatesOnCreation(elementOrder))
                : Stream.of(testLogic.rejectsDuplicatesOnCreation());
        }
    }

    private static final class TestLogic {

        private final SetCreator setCreator;

        private TestLogic(SetCreator setCreator) {
            this.setCreator = setCreator;
        }

        DynamicTest supportsNullElements() {
            return dynamicTest("supportsNullElements",
                () -> assertDoesNotThrow(setCreator::createSetWithNull));
        }

        DynamicTest mayNotContainNull() {
            return dynamicTest("mayNotContainNull",
                () -> assertThrows(NullPointerException.class, setCreator::createSetWithNull));
        }

        DynamicTest supportsNullMethodArgs() {
            return dynamicTest("supportsNullMethodArgs",
                () -> verifySupportsNullArgInMethods(setCreator.createSet()));
        }

        DynamicTest rejectsNullMethodArgs() {
            return dynamicTest("rejectsNullMethodArgs",
                () -> verifyRejectsNullArgInMethods(setCreator.createSet()));
        }

        DynamicTest skipsWrappingForOwnClass() {
            FromCollectionSetCreator setCopyCreator = (FromCollectionSetCreator) setCreator;
            return dynamicTest("skipsWrappingForOwnClass", () -> {
                Set<String> set1 = setCopyCreator.newSet(Set.of("a", "b"));
                Set<String> set2 = setCopyCreator.newSet(set1);
                assertThat(set1, sameInstance(set2));
            });
        }

        DynamicTest alwaysWrapsOwnClass() {
            FromCollectionSetCreator setCopyCreator = (FromCollectionSetCreator) setCreator;
            return dynamicTest("alwaysWrapsOwnClass", () -> {
                Set<String> set1 = setCopyCreator.newSet(Set.of("a", "b"));
                Set<String> set2 = setCopyCreator.newSet(set1);
                assertThat(set1, not(sameInstance(set2)));
            });
        }

        DynamicTest isSequencedSet() {
            return dynamicTest("isSequencedSet",
                () -> assertThat(setCreator.createSet(), instanceOf(SequencedSet.class)));
        }

        DynamicTest isNotSequencedSet() {
            return dynamicTest("isNotSequencedSet", () -> {
                Set<String> set = setCreator.createSet();
                assertThat(set, not(instanceOf(SequencedSet.class)));
                assertThat(set, not(instanceOf(SequencedCollection.class)));
                if (setCreator instanceof FromCollectionSetCreator fcc) {
                    // Explicitly check that a SequencedSet as base does not yield a SequencedSet, as is done for
                    // RandomAccess sometimes with methods that wrap lists
                    SequencedSet<String> sequencedSet = new LinkedHashSet<>(set);
                    assertThat(fcc.newSet(sequencedSet), not(instanceOf(SequencedSet.class)));
                }
            });
        }

        DynamicTest hasRandomElementOrder() {
            return dynamicTest("hasRandomElementOrder", () -> {
                Set<String> set = setCreator.createSetWithAlphanumericalEntries();
                // It's still possible that by coincidence the Set has the same order even though it makes
                // no guarantees. It's improbable to happen because of the number of entries we use.
                assertThat(set, containsInAnyOrder(SetCreator.ALPHANUM_ELEMENTS_RANDOM));
                assertThat(set, not(contains(SetCreator.ALPHANUM_ELEMENTS_RANDOM)));
                assertThat(set, not(contains(SetCreator.ALPHANUM_ELEMENTS_SORTED)));
            });
        }

        DynamicTest keepsElementsSorted() {
            return dynamicTest("keepsElementsSorted", () -> {
                Set<String> set = setCreator.createSetWithAlphanumericalEntries();
                assertThat(set, contains(SetCreator.ALPHANUM_ELEMENTS_SORTED));
            });
        }

        DynamicTest keepsElementsByInsertionOrder() {
            return dynamicTest("keepsElementsByInsertionOrder", () -> {
                Set<String> set = setCreator.createSetWithAlphanumericalEntries();
                assertThat(set, contains(SetCreator.ALPHANUM_ELEMENTS_RANDOM));
            });
        }

        DynamicTest acceptsDuplicatesOnCreation(SetOrder expectedOrder) {
            if (expectedOrder == SetOrder.UNORDERED) {
                return dynamicTest("acceptsDuplicatesOnCreation",
                    () -> assertThat(setCreator.createSetWithDuplicateArgs(), containsInAnyOrder("a", "b", "c")));
            }
            return dynamicTest("acceptsDuplicatesOnCreation",
                () -> assertThat(setCreator.createSetWithDuplicateArgs(), contains("a", "b", "c")));
        }

        DynamicTest rejectsDuplicatesOnCreation() {
            return dynamicTest("rejectsDuplicatesOnCreation",
                () -> assertThrows(IllegalArgumentException.class, setCreator::createSetWithDuplicateArgs));
        }
    }
}
