package ch.jalu.collectionbehavior;

import ch.jalu.collectionbehavior.model.MapCreator;
import ch.jalu.collectionbehavior.model.MapWithBackingDataModifier;
import ch.jalu.collectionbehavior.model.ModificationBehavior;
import ch.jalu.collectionbehavior.model.NullSupport;
import ch.jalu.collectionbehavior.model.SequencedMapType;
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
import static org.hamcrest.Matchers.nullValue;
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

        assertContainsEntriesNotInOrder(map);
        assertThat(map, not(instanceOf(SequencedMap.class)));

        assertThrows(UnsupportedOperationException.class, () -> map.put('f', 999));
        assertThrows(UnsupportedOperationException.class, () -> map.remove('0'));
        assertKeyAndValuesAndEntrySetImmutable(map);

        assertThrows(NullPointerException.class, () -> Map.of('A', 65, 'B', null));
        assertThrows(NullPointerException.class, () -> Map.of('A', 65, null, 32));
        assertThrows(NullPointerException.class, () -> map.containsKey(null));
        assertThrows(NullPointerException.class, () -> map.containsValue(null));

        assertThrows(IllegalArgumentException.class, () -> Map.of('A', 65, '0', 48, 'A', 65));
        assertThrows(IllegalArgumentException.class, () -> Map.ofEntries(
            Map.entry('A', 65), Map.entry('0', 48), Map.entry('A', 65)));
    }

    @TestFactory
    List<DynamicTest> jdk_Map_copyOf() {
        return forMapType(MapCreator.forMapBasedType(Map::copyOf))
            .expect(NullSupport.REJECT, SetOrder.UNORDERED, SequencedMapType.DOES_NOT_IMPLEMENT)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link Map#copyOf} returns an immutable Map copied from another map. Iteration order is not preserved from
     * the original Map. Null is not supported as key or as value. Throws also for null in {@link Map#containsKey} and
     * similar. Recognizes maps of its own class and avoids unnecessary copies.
     */
    @Test
    void jdkMapCopyOf() {
        Map<Character, Integer> elements = newLinkedHashMap('0', 48, 'z', 122, 'A', 65);
        Map<Character, Integer> map = Map.copyOf(elements);

        elements.put('f', 999);
        assertContainsEntriesNotInOrder(map);
        assertThat(map, not(instanceOf(SequencedMap.class)));

        assertThat(Map.copyOf(map), sameInstance(map));

        assertThrows(UnsupportedOperationException.class, () -> map.put('f', 999));
        assertThrows(UnsupportedOperationException.class, () -> map.remove('0'));
        assertKeyAndValuesAndEntrySetImmutable(map);

        Map<Character, Integer> mapWithNullValue = newLinkedHashMap('A', 65, 'z', null, '?', 120);
        assertThrows(NullPointerException.class, () -> Map.copyOf(mapWithNullValue));
        Map<Character, Integer> mapWithNullKey = newLinkedHashMap('A', 65, null, -1, 'z', 122);
        assertThrows(NullPointerException.class, () -> Map.copyOf(mapWithNullKey));
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
        assertKeyAndValuesAndEntrySetImmutable(map);

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
    @Test
    void guavaImmutableMapCopyOf() {
        Map<Character, Integer> elements = newLinkedHashMap('0', 48, 'z', 122, 'A', 65);
        Map<Character, Integer> map = ImmutableMap.copyOf(elements);

        elements.put('f', 999);
        assertThat(map.keySet(), contains('0', 'z', 'A'));
        assertThat(map, not(instanceOf(SequencedMap.class))); // Not SequencedMap to support older JDKs

        assertThat(ImmutableMap.copyOf(map), sameInstance(map));

        assertThrows(UnsupportedOperationException.class, () -> map.put('f', 999));
        assertThrows(UnsupportedOperationException.class, () -> map.remove('0'));
        assertKeyAndValuesAndEntrySetImmutable(map);

        Map<Character, Integer> mapWithNullValue = newLinkedHashMap('A', 65, 'z', null, '?', 120);
        assertThrows(NullPointerException.class, () -> ImmutableMap.copyOf(mapWithNullValue));
        Map<Character, Integer> mapWithNullKey = newLinkedHashMap('A', 65, null, -1, 'z', 122);
        assertThrows(NullPointerException.class, () -> ImmutableMap.copyOf(mapWithNullKey));

        assertThat(map.containsKey(null), equalTo(false));
        assertThat(map.containsValue(null), equalTo(false));
    }

    /**
     * {@link Collections#unmodifiableMap} wraps the original Map and provides an unmodifiable Map facade, i.e.
     * changes to the original map are reflected. Supports null as key and as values.
     */
    @Test
    void jdkCollectionsUnmodifiableMap() {
        SequencedMap<Character, Integer> elements = newLinkedHashMap('0', 48, 'z', 122, 'A', 65);
        Map<Character, Integer> map = Collections.unmodifiableMap(elements);

        elements.put('f', 999);
        assertThat(map.keySet(), contains('0', 'z', 'A', 'f'));
        assertThat(map, not(instanceOf(SequencedMap.class))); // Not SequencedMap despite preserving order

        // Same instance returned in JDK 17, whereas in JDK 11 it always returned a new instance
        assertThat(Collections.unmodifiableMap(map), sameInstance(map));

        assertThrows(UnsupportedOperationException.class, () -> map.put('f', 999));
        assertThrows(UnsupportedOperationException.class, () -> map.remove('0'));
        assertKeyAndValuesAndEntrySetImmutable(map);

        Map<Character, Integer> mapWithNullValue = newLinkedHashMap('A', 65, 'z', null, '?', 120);
        assertThat(Collections.unmodifiableMap(mapWithNullValue), equalTo(mapWithNullValue));
        Map<Character, Integer> mapWithNullKey = newLinkedHashMap('A', 65, null, -1, 'z', 122);
        assertThat(Collections.unmodifiableMap(mapWithNullKey), equalTo(mapWithNullKey));
    }

    /**
     * {@link Collections#unmodifiableSequencedMap(SequencedMap)} wraps the original SequencedMap into an unmodifiable
     * SequencedMap facade, i.e. changes to the original map are reflected. Supports null as key and as values.
     */
    @Test
    void jdkCollectionsUnmodifiableSequencedMap() {
        SequencedMap<Character, Integer> elements = newLinkedHashMap('0', 48, 'z', 122, 'A', 65);
        SequencedMap<Character, Integer> map = Collections.unmodifiableSequencedMap(elements);

        elements.put('f', 999);
        assertThat(map.keySet(), contains('0', 'z', 'A', 'f'));
        // is SequencedMap (as seen in type declaration)

        // Same instance returned, but not by Collections#unmodifiable
        assertThat(Collections.unmodifiableSequencedMap(map), sameInstance(map));
        assertThat(Collections.unmodifiableMap(map), not(sameInstance(map)));

        assertThrows(UnsupportedOperationException.class, () -> map.put('f', 999));
        assertThrows(UnsupportedOperationException.class, () -> map.remove('0'));
        assertKeyAndValuesAndEntrySetImmutable(map);

        SequencedMap<Character, Integer> mapWithNullValue = newLinkedHashMap('A', 65, 'z', null, '?', 120);
        assertThat(Collections.unmodifiableSequencedMap(mapWithNullValue), equalTo(mapWithNullValue));
        SequencedMap<Character, Integer> mapWithNullKey = newLinkedHashMap('A', 65, null, -1, 'z', 122);
        assertThat(Collections.unmodifiableSequencedMap(mapWithNullKey), equalTo(mapWithNullKey));
    }

    /**
     * {@link Collections#emptyMap()} provides an immutable empty map. Always the same instance.
     * Curiously, certain methods do not provoke an exception if they don't cause any change to the map
     * (e.g. {@code map.putAll(emptyMap)}).
     */
    @Test
    void jdkCollectionsEmptyMap() {
        Map<Character, Short> map1 = Collections.emptyMap();
        Map<String, Integer> map2 = Collections.emptyMap();

        assertThat(map1, sameInstance(map2));

        // Not SequencedMap because there's no point
        assertThat(map1, not(instanceOf(SequencedMap.class)));

        assertThat(map1.containsKey(null), equalTo(false));
        assertThat(map1.containsValue(null), equalTo(false));

        assertThrows(UnsupportedOperationException.class, () -> map2.put("A", 5));
        assertThrows(UnsupportedOperationException.class, () -> map2.remove("f", 3));

        // Some unexpected behavior
        assertThat(map2.remove("0"), nullValue());
        assertThat(map1.keySet().remove('C'), equalTo(false));
        map1.values().clear();
        map1.putAll(Collections.emptyMap());
    }

    /**
     * {@link Collectors#toMap(Function, Function)} makes no guarantees on mutability of the Map, though at the moment
     * it creates a HashMap. As such, order is not kept. The map may not have null values, but null as key is supported.
     * Throws for duplicate keys.
     */
    @Test
    void jdkCollectorsToMap() {
        Map<Character, Integer> map = Stream.of('0', 'z', 'A')
            .collect(Collectors.toMap(Function.identity(), chr -> (int) chr));
        assertContainsEntriesNotInOrder(map);
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

    /**
     * {@link Collectors#toUnmodifiableMap(Function, Function)} produces an unmodifiable map that rejects null values:
     * keys and values may not be null; methods like {@link Map#containsKey} may not be called with null as argument.
     * Order of keys is random.
     */
    @Test
    void jdkCollectorsToUnmodifiableMap() {
        Map<Character, Integer> map = Stream.of('0', 'z', 'A')
            .collect(Collectors.toUnmodifiableMap(Function.identity(), chr -> (int) chr));
        assertContainsEntriesNotInOrder(map);
        assertThat(map, not(instanceOf(SequencedMap.class)));

        assertThrows(UnsupportedOperationException.class, () -> map.put('f', 999));
        assertThrows(UnsupportedOperationException.class, () -> map.remove('0'));

        assertThrows(NullPointerException.class, () -> map.containsKey(null));
        assertThrows(NullPointerException.class, () -> map.containsValue(null));

        assertThrows(NullPointerException.class, () -> Stream.of(3, null, 5)
            .collect(Collectors.toUnmodifiableMap(String::valueOf, Function.identity())));

        assertThrows(NullPointerException.class, () -> Stream.of(3, null, 5)
            .collect(Collectors.toUnmodifiableMap(Function.identity(), String::valueOf)));

        IllegalStateException duplicateKeyEx = assertThrows(IllegalStateException.class, () -> Stream.of(3, 4, -3)
            .collect(Collectors.toUnmodifiableMap(i -> i * i, i -> Integer.toString(i))));
        assertThat(duplicateKeyEx.getMessage(), equalTo("Duplicate key 9 (attempted merging values 3 and -3)"));
    }

    private static void assertContainsEntriesNotInOrder(Map<Character, Integer> map) {
        assertThat(map.keySet(), containsInAnyOrder('0', 'z', 'A'));
        assertThat(map.keySet(), not(contains('0', 'z', 'A')));

        assertThat(map.get('0'), equalTo(48));
        assertThat(map.get('z'), equalTo(122));
        assertThat(map.get('A'), equalTo(65));
    }

    private static void assertKeyAndValuesAndEntrySetImmutable(Map<?, ?> map) {
        assertThrows(UnsupportedOperationException.class, () -> map.keySet().removeIf(k -> true));
        assertThrows(UnsupportedOperationException.class, () -> map.values().removeIf(v -> true));
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().removeIf(e -> true));
    }

    private static LinkedHashMap<Character, Integer> newLinkedHashMap(Character key1, Integer value1,
                                                                      Character key2, Integer value2,
                                                                      Character key3, Integer value3) {
        LinkedHashMap<Character, Integer> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
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
                    createTestsForMutability(),
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

        private Stream<DynamicTest> createTestsForMutability() {
            if (modificationBehavior.isMutable()) {
                return Stream.of(
                    dynamicTest("mutable",
                        () -> MapMutabilityVerifier.verifyMapIsMutable(mapCreator.createMap())));
            }

            List<DynamicTest> testsToRun = new ArrayList<>();
            Map<String, Integer> map = mapCreator.createMapWithAbcdOrSubset();
            createTestForImmutabilityBehavior().ifPresent(testsToRun::add);
            testsToRun.add(dynamicTest("unmodifiable",
                () -> MapModificationVerifier.testMethods(map, modificationBehavior)));
            return testsToRun.stream();

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
