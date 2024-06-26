package ch.jalu.collectionbehavior;

import ch.jalu.collectionbehavior.model.MapCreator;
import ch.jalu.collectionbehavior.model.MapMethod;
import ch.jalu.collectionbehavior.model.MapWithBackingDataModifier;
import ch.jalu.collectionbehavior.model.MethodCallEffect;
import ch.jalu.collectionbehavior.model.ModificationBehavior;
import ch.jalu.collectionbehavior.model.NullSupport;
import ch.jalu.collectionbehavior.model.SequencedMapType;
import ch.jalu.collectionbehavior.model.SetMethod;
import ch.jalu.collectionbehavior.model.SetOrder;
import ch.jalu.collectionbehavior.verification.MapModificationVerifier;
import ch.jalu.collectionbehavior.verification.MapMutabilityVerifier;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.jalu.collectionbehavior.verification.MapModificationVerifier.testMethodsForEntrySet;
import static ch.jalu.collectionbehavior.verification.MapModificationVerifier.testMethodsForKeySet;
import static ch.jalu.collectionbehavior.verification.MapModificationVerifier.testMethodsForValues;
import static ch.jalu.collectionbehavior.verification.MapMutabilityVerifier.immutable_changeToOriginalStructureIsNotReflectedInSet;
import static ch.jalu.collectionbehavior.verification.MapMutabilityVerifier.unmodifiable_changeToOriginalStructureIsReflectedInSet;
import static ch.jalu.collectionbehavior.verification.MapNullBehaviorVerifier.verifyRejectsNullArgInMethods;
import static ch.jalu.collectionbehavior.verification.MapNullBehaviorVerifier.verifySupportsNullArgInMethods;
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

class MapTest {

    /**
     * HashMap: standard implementation of Map. Does not retain iteration order. Supports null values and null as key.
     * Removal of entries from the key set, values set or entry set is reflected to the actual Map.
     */
    @TestFactory
    List<DynamicTest> jdk_HashMap() {
        return forMapType(MapCreator.forMutableType(HashMap::new))
            .expect(NullSupport.FULL, SetOrder.UNORDERED, SequencedMapType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.mutable())
            .createTests();
    }

    /**
     * LinkedHashMap: hash map with iteration by insertion order. Supports null values and null as key.
     * Removal of entries from the key set, values set or entry set is reflected to the actual Map.
     */
    @TestFactory
    List<DynamicTest> jdk_LinkedHashMap() {
        return forMapType(MapCreator.forMutableType(LinkedHashMap::new))
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, SequencedMapType.IMPLEMENTS)
            .mutability(ModificationBehavior.mutable())
            .createTests();
    }

    /**
     * {@link Map#of} returns an immutable Map. Does not support null as value or key; even querying with null to
     * {@link Map#containsKey} results in an exception. Also throws an exception if there are duplicate keys when
     * the Map is being created. Random iteration order.
     */
    @Test
    void jdkMapOf() {
        Map<Character, Integer> map = Map.of('0', 48, 'z', 122, 'A', 65);

        // assertContainsEntriesNotInOrder(map);
        assertThat(map, not(instanceOf(SequencedMap.class)));

        assertThrows(UnsupportedOperationException.class, () -> map.put('f', 999));
        assertThrows(UnsupportedOperationException.class, () -> map.remove('0'));
        // assertKeyAndValuesAndEntrySetImmutable(map);

        assertThrows(NullPointerException.class, () -> Map.of('A', 65, 'B', null));
        assertThrows(NullPointerException.class, () -> Map.of('A', 65, null, 32));
        assertThrows(NullPointerException.class, () -> map.containsKey(null));
        assertThrows(NullPointerException.class, () -> map.containsValue(null));

        assertThrows(IllegalArgumentException.class, () -> Map.of('A', 65, '0', 48, 'A', 65));
        assertThrows(IllegalArgumentException.class, () -> Map.ofEntries(
            Map.entry('A', 65), Map.entry('0', 48), Map.entry('A', 65)));
    }

    /**
     * {@link Map#copyOf} returns an immutable Map copied from another map. Iteration order is not preserved from
     * the original Map. Null is not supported as key or as value. Throws also for null in {@link Map#containsKey} and
     * similar. Recognizes maps of its own class and avoids unnecessary copies.
     */
    @TestFactory
    List<DynamicTest> jdk_Map_copyOf() {
        return forMapType(MapCreator.forMapBasedType(Map::copyOf))
            .expect(NullSupport.REJECT, SetOrder.UNORDERED, SequencedMapType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .mutabilityEntrySet(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD, SetMethod.ADD_ALL))
            .mutabilityKeySet(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD, SetMethod.ADD_ALL))
            .mutabilityValues(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD, SetMethod.ADD_ALL))
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link ImmutableMap#of} creates an immutable Map. Null is not supported as key or as values but can be
     * supplied to methods like {@link Map#containsKey} without problems. Iteration order is by order of encounter.
     * Throws if the same key is provided twice on creation.
     */
    @Test
    void guavaImmutableMapOf() {
        Map<Character, Integer> map = ImmutableMap.of('0', 48, 'z', 122, 'A', 65);

        assertThat(map.keySet(), contains('0', 'z', 'A'));
        assertThat(map, not(instanceOf(SequencedMap.class))); // Not SequencedMap to support older JDKs

        assertThrows(UnsupportedOperationException.class, () -> map.put('f', 999));
        assertThrows(UnsupportedOperationException.class, () -> map.remove('0'));
        // assertKeyAndValuesAndEntrySetImmutable(map);

        assertThrows(NullPointerException.class, () -> ImmutableMap.of('A', 65, 'B', null));
        assertThrows(NullPointerException.class, () -> ImmutableMap.of('A', 65, null, 32));
        assertThat(map.containsKey(null), equalTo(false));
        assertThat(map.containsValue(null), equalTo(false));

        assertThrows(IllegalArgumentException.class, () -> ImmutableMap.of('A', 65, '0', 48, 'A', 65));

        assertThrows(IllegalArgumentException.class, () -> ImmutableMap.builder()
          .put('A', 65)
          .put('0', 48)
          .put('A', 65).build());
    }

    /**
     * {@link ImmutableMap#copyOf} copies an immutable Map and keeps the iteration order of the original map.
     * Does not support null as key or values but accepts null in methods such as {@link Map#containsKey}.
     */
    @TestFactory
    List<DynamicTest> guava_ImmutableMap_copyOf() {
        return forMapType(MapCreator.forMapBasedType(ImmutableMap::copyOf))
            // Not SequencedMap to support older JDKs
            .expect(NullSupport.ARGUMENTS, SetOrder.INSERTION_ORDER, SequencedMapType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Collections#unmodifiableMap} wraps the original Map and provides an unmodifiable Map facade, i.e.
     * changes to the original map are reflected. Supports null as key and as values.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_unmodifiableMap() {
        return forMapType(MapCreator.forMapBasedType(Collections::unmodifiableMap))
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, SequencedMapType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.unmodifiable().alwaysThrows())
            // Same instance returned in JDK 17, whereas in JDK 11 it always returned a new instance
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Collections#unmodifiableSequencedMap(SequencedMap)} wraps the original SequencedMap into an unmodifiable
     * SequencedMap facade, i.e. changes to the original map are reflected. Supports null as key and as values.
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_unmodifiableSequencedMap() {
        return forMapType(MapCreator.forMapBasedType(Collections::unmodifiableSequencedMap,
                map -> map instanceof SequencedMap<String, Integer> seqMap ? seqMap : new LinkedHashMap<>(map)))
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, SequencedMapType.IMPLEMENTS)
            .mutability(ModificationBehavior.unmodifiable().alwaysThrows())
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Collections#emptyMap()} provides an immutable empty map. Always the same instance.
     * Curiously, certain methods do not provoke an exception if they don't cause any change to the map
     * (e.g. {@code map.putAll(emptyMap)}).
     */
    @TestFactory
    List<DynamicTest> jdk_Collections_emptyMap() {
        return forMapType(MapCreator.forEmptyMap(Collections::emptyMap))
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, SequencedMapType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING,
                        MapMethod.REMOVE_KEY_VALUE, MapMethod.REPLACE, MapMethod.REPLACE_WITH_OLD_VALUE,
                        MapMethod.COMPUTE, MapMethod.COMPUTE_IF_PRESENT, MapMethod.COMPUTE_IF_ABSENT))
            .createTests();
    }

    /**
     * {@link Collectors#toMap(Function, Function)} makes no guarantees on mutability of the Map, though at the moment
     * it creates a HashMap. As such, order is not kept. The map may not have null values, but null as key is supported.
     * Throws for duplicate keys.
     */
    @Test
    void jdk_Collectors_toMap() {
        // TODO: Needs to be extended to showcase duplicate keys and null support that differs from hashmap
        Map<Character, Integer> map = Stream.of('f', 'g')
            .collect(Collectors.toMap(v -> v, v -> (int) v));
        assertThat(map.getClass(), equalTo(HashMap.class));
    }

    @Test
    void jdkCollectorsToMap() {
        Map<Character, Integer> map = Stream.of('0', 'z', 'A')
            .collect(Collectors.toMap(Function.identity(), chr -> (int) chr));
        assertThat(map.getClass(), equalTo(HashMap.class));

        assertThat(map.containsKey(null), equalTo(false));
        assertThat(map.containsValue(null), equalTo(false));

        map.put('f', -3);
        map.remove('z');
        assertThat(map.keySet(), containsInAnyOrder('0', 'A', 'f'));

        assertThrows(NullPointerException.class, () -> Stream.of(3, null, 5)
            .collect(Collectors.toMap(String::valueOf, Function.identity())));

        Map<Integer, String> mapWithNullKey = Stream.of(3, null, 5)
            .collect(Collectors.toMap(Function.identity(), String::valueOf));
        assertThat(mapWithNullKey.keySet(), containsInAnyOrder(3, null, 5));

        IllegalStateException duplicateKeyEx = assertThrows(IllegalStateException.class, () -> Stream.of(3, 4, -3)
            .collect(Collectors.toMap(i -> i * i, i -> Integer.toString(i))));
        assertThat(duplicateKeyEx.getMessage(), equalTo("Duplicate key 9 (attempted merging values 3 and -3)"));
    }

    @TestFactory
    List<DynamicTest> jdk_Collectors_toUnmodifiableMap() {
        return forMapType(MapCreator.fromStream(
                            str -> str.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue))))
            .expect(NullSupport.REJECT, SetOrder.UNORDERED, SequencedMapType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .mutabilityKeySet(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD, SetMethod.ADD_ALL))
            .mutabilityEntrySet(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD, SetMethod.ADD_ALL))
            .mutabilityValues(ModificationBehavior.immutable().throwsIfWouldBeModified())
            .createTests();
    }

    private static TestsGenerator forMapType(MapCreator mapCreator) {
        StackWalker instance = StackWalker.getInstance();
        String testName = instance.walk(frames -> frames.skip(1).findFirst()).get().getMethodName();
        return new TestsGenerator(mapCreator, testName);
    }

    /**
     * Generates tests based on the expected behavior that is defined.
     */
    private static final class TestsGenerator {

        private final MapCreator mapCreator;
        private final String testName;
        private final TestLogic testLogic;

        private NullSupport nullSupport;
        private SetOrder elementOrder; // todo: different type? or just rename?
        private SequencedMapType sequencedMapType;
        private boolean acceptsDuplicatesOnCreation = true;
        private boolean skipsWrappingForOwnClass;

        private ModificationBehavior modificationBehavior;
        private ModificationBehavior modificationBehaviorKeySet;
        private ModificationBehavior modificationBehaviorEntrySet;
        private ModificationBehavior modificationBehaviorValues;


        private TestsGenerator(MapCreator mapCreator, String testName) {
            this.mapCreator = mapCreator;
            this.testName = testName;
            this.testLogic = new TestLogic(mapCreator);
        }

        /**
         * Sets some basic expected properties of the map type to this generator.
         *
         * @param nullSupport expected null support of the map type
         * @param elementOrder expected order of the map's elements
         * @param sequencedMapType whether the map type extends SequencedMap
         * @return this instance, for chaining
         */
        TestsGenerator expect(NullSupport nullSupport, SetOrder elementOrder, SequencedMapType sequencedMapType) {
            this.nullSupport = nullSupport;
            this.elementOrder = elementOrder;
            this.sequencedMapType = sequencedMapType;
            return this;
        }

        /**
         * Registers the expected mutability behavior of the map type that should be tested.
         *
         * @param modificationBehavior definition of how the map is expected to behave wrt mutability
         * @return this instance, for chaining
         */
        TestsGenerator mutability(ModificationBehavior modificationBehavior) {
            this.modificationBehavior = modificationBehavior;
            this.modificationBehaviorKeySet = modificationBehavior;
            this.modificationBehaviorEntrySet = modificationBehavior;
            this.modificationBehaviorValues = modificationBehavior;
            return this;
        }

        TestsGenerator mutabilityKeySet(ModificationBehavior expectedKeySetBehavior) {
            this.modificationBehaviorKeySet = expectedKeySetBehavior;
            return this;
        }

        TestsGenerator mutabilityEntrySet(ModificationBehavior expectedEntrySetBehavior) {
            this.modificationBehaviorEntrySet = expectedEntrySetBehavior;
            return this;
        }

        TestsGenerator mutabilityValues(ModificationBehavior expectedValuesBehavior) {
            this.modificationBehaviorValues = expectedValuesBehavior;
            return this;
        }

        /**
         * When the map is created based on another structure that allows duplicates, defines that duplicates in that
         * input structure will result in an exception.
         * TODO: Revise javadoc
         *
         * @return this instance, for chaining
         */
        TestsGenerator rejectsDuplicatesOnCreation() {
            this.acceptsDuplicatesOnCreation = false;
            return this;
        }

        /**
         * Only applicable for map-based set creators: it is expected that the method recognizes sets of its return
         * type and that it will not wrap those maps again, i.e. it returns the same map in this case.
         *
         * @return this instance, for chaining
         */
        TestsGenerator skipsWrappingForOwnClass() {
            this.skipsWrappingForOwnClass = true;
            return this;
        }

        public List<DynamicTest> createTests() {
            return Stream.of(
                    createTestsForNullSupport(),
                    createTestForElementOrder(),
                    createTestForSequencedMapImpl(),
                    createTestForMutability(),
                    createTestForSkipsWrappingOwnClassIfApplicable()
                )
                .flatMap(Function.identity())
                .toList();
        }

        // TODO: Test null keys and null values
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

            if (mapCreator.getSizeLimit() == 0) {
                // A null element does not apply to an empty map type, skip the test
                return tests.stream().skip(1);
            }
            return tests.stream();
        }

        private Stream<DynamicTest> createTestForMutability() {
            if (modificationBehavior.isMutable()) {
                return createTestsForMutableAssertions().stream();
            }

            List<DynamicTest> testsToRun = new ArrayList<>();
            Map<String, Integer> map = mapCreator.createMapWithAbcdOrSubset();
            createTestForImmutabilityBehavior().ifPresent(testsToRun::add);
            testsToRun.add(dynamicTest("unmodifiable",
                () -> MapModificationVerifier.testMethods(map, modificationBehavior)));
            testsToRun.add(dynamicTest("unmodifiable_entrySet",
                () -> testMethodsForEntrySet(map, modificationBehaviorEntrySet)));
            testsToRun.add(dynamicTest("unmodifiable_keySet",
                () -> testMethodsForKeySet(map, modificationBehaviorKeySet)));
            testsToRun.add(dynamicTest("unmodifiable_values",
                () -> testMethodsForValues(map, modificationBehaviorValues)));
            return testsToRun.stream();
        }

        private List<DynamicTest> createTestsForMutableAssertions() {
            Map<String, Integer> map = mapCreator.createMap();
            return List.of(
                dynamicTest("mutable",
                    () -> MapMutabilityVerifier.verifyMapIsMutable(map)),
                dynamicTest("mutable_keySet",
                    () -> MapMutabilityVerifier.verifyMapKeySetIsMutable(map)),
                dynamicTest("mutable_values",
                    () -> MapMutabilityVerifier.verifyMapValuesIsMutable(map)),
                dynamicTest("mutable_entrySet",
                    () -> MapMutabilityVerifier.verifyMapEntrySetIsMutable(map)));
            // TODO: navigableMap, sortedMap
        }

        private Optional<DynamicTest> createTestForImmutabilityBehavior() {
            Optional<MapWithBackingDataModifier> mapWithDataModifier =
                mapCreator.createMapWithBackingDataModifier("a", "b", "c", "d");

            if (!modificationBehavior.isImmutable) {
                // Must be able to create this in order to "claim" that it's not immutable
                MapWithBackingDataModifier mapWithBd = mapWithDataModifier.orElseThrow();
                DynamicTest test = dynamicTest("unmodifiable_changeToOriginalStructureReflectedInMap",
                    () -> unmodifiable_changeToOriginalStructureIsReflectedInSet(mapWithBd));
                return Optional.of(test);
            }

            return mapWithDataModifier
                .map(mapWithBd -> dynamicTest("immutable_originalStructureDoesNotChangeMap",
                    () -> immutable_changeToOriginalStructureIsNotReflectedInSet(mapWithBd)));
        }

        private Stream<DynamicTest> createTestForElementOrder() {
            if (mapCreator.getSizeLimit() <= 1) {
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

        private Stream<DynamicTest> createTestForSequencedMapImpl() {
            return switch (sequencedMapType) {
                case IMPLEMENTS -> Stream.of(testLogic.isSequencedMap());
                case DOES_NOT_IMPLEMENT -> Stream.of(testLogic.isNotSequencedMap());
            };
        }

        private Stream<DynamicTest> createTestForSkipsWrappingOwnClassIfApplicable() {
            if (skipsWrappingForOwnClass) {
                return Stream.of(testLogic.skipsWrappingForOwnClass());
            } else if (mapCreator instanceof MapCreator.MapBasedMapCreator<?>) {
                return Stream.of(testLogic.alwaysWrapsOwnClass());
            }
            return Stream.empty();
        }
    }

    private static final class TestLogic {

        private final MapCreator mapCreator;

        private TestLogic(MapCreator mapCreator) {
            this.mapCreator = mapCreator;
        }

        DynamicTest supportsNullElements() {
            return dynamicTest("supportsNullElements",
                () -> assertDoesNotThrow(mapCreator::createMapWithNull));
        }

        DynamicTest mayNotContainNull() {
            return dynamicTest("mayNotContainNull",
                () -> assertThrows(NullPointerException.class, mapCreator::createMapWithNull));
        }

        DynamicTest supportsNullMethodArgs() {
            return dynamicTest("supportsNullMethodArgs",
                () -> verifySupportsNullArgInMethods(mapCreator.createMap()));
        }

        DynamicTest rejectsNullMethodArgs() {
            return dynamicTest("rejectsNullMethodArgs",
                () -> verifyRejectsNullArgInMethods(mapCreator.createMap()));
        }

        DynamicTest skipsWrappingForOwnClass() {
            MapCreator.MapBasedMapCreator<?> mapCopyCreator = (MapCreator.MapBasedMapCreator<?>) mapCreator;
            return dynamicTest("skipsWrappingForOwnClass", () -> {
                Map<String, Integer> map1 = mapCopyCreator.newMap(Map.of("a", 1, "b", 2));
                Map<String, Integer> map2 = mapCopyCreator.newMap(map1);
                assertThat(map1, sameInstance(map2));
            });
        }

        DynamicTest alwaysWrapsOwnClass() {
            MapCreator.MapBasedMapCreator<?> mapCopyCreator = (MapCreator.MapBasedMapCreator<?>) mapCreator;
            return dynamicTest("alwaysWrapsOwnClass", () -> {
                Map<String, Integer> map1 = mapCopyCreator.newMap(Map.of("a", 1, "b", 2));
                Map<String, Integer> map2 = mapCopyCreator.newMap(map1);
                assertThat(map1, not(sameInstance(map2)));
            });
        }

        DynamicTest isSequencedMap() {
            return dynamicTest("isSequencedMap",
                () -> assertThat(mapCreator.createMap(), instanceOf(SequencedMap.class)));
        }

        DynamicTest isNotSequencedMap() {
            return dynamicTest("isNotSequencedMap", () -> {
                Map<String, Integer> map = mapCreator.createMap();
                assertThat(map, not(instanceOf(SequencedMap.class)));

                if (mapCreator instanceof MapCreator.MapBasedMapCreator<?> mmc) {
                    // Explicitly check that a SequencedSet as base does not yield a SequencedSet, as is done for
                    // RandomAccess sometimes with methods that wrap lists
                    SequencedMap<String, Integer> sequencedMap = new LinkedHashMap<>(map);
                    assertThat(mmc.newMap(sequencedMap), not(instanceOf(SequencedMap.class)));
                }
            });
        }

        DynamicTest hasRandomElementOrder() {
            return dynamicTest("hasRandomEntryOrder", () -> {
                Map<String, Integer> map = mapCreator.createMapWithAlphanumericalEntries();
                // It's still possible that by coincidence the key set has the same order even though it makes
                // no guarantees. It's improbable to happen because of the number of entries we use.
                Set<String> keySet = map.keySet();
                assertThat(keySet, containsInAnyOrder(MapCreator.ALPHANUM_ELEMENTS_RANDOM));
                assertThat(keySet, not(contains(MapCreator.ALPHANUM_ELEMENTS_RANDOM)));
                assertThat(keySet, not(contains(MapCreator.ALPHANUM_ELEMENTS_SORTED)));
            });
        }

        DynamicTest keepsElementsSorted() {
            return dynamicTest("keepsEntriesSorted", () -> {
                Map<String, Integer> map = mapCreator.createMapWithAlphanumericalEntries();
                assertThat(map.keySet(), contains(MapCreator.ALPHANUM_ELEMENTS_SORTED));
            });
        }

        DynamicTest keepsElementsByInsertionOrder() {
            return dynamicTest("keepsEntriesByInsertionOrder", () -> {
                Map<String, Integer> map = mapCreator.createMapWithAlphanumericalEntries();
                assertThat(map.keySet(), contains(MapCreator.ALPHANUM_ELEMENTS_RANDOM));
            });
        }
    }
}
