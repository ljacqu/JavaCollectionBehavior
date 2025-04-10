package ch.jalu.collectionbehavior;

import ch.jalu.collectionbehavior.model.MapCreator;
import ch.jalu.collectionbehavior.model.MapInterfaceType;
import ch.jalu.collectionbehavior.model.MapMethod;
import ch.jalu.collectionbehavior.model.MapWithBackingDataModifier;
import ch.jalu.collectionbehavior.model.MethodCallEffect;
import ch.jalu.collectionbehavior.model.ModificationBehavior;
import ch.jalu.collectionbehavior.model.NullSupport;
import ch.jalu.collectionbehavior.model.SetMethod;
import ch.jalu.collectionbehavior.model.SetOrder;
import ch.jalu.collectionbehavior.verification.MapModificationVerifier;
import ch.jalu.collectionbehavior.verification.MapMutabilityVerifier;
import ch.jalu.collectionbehavior.verification.MapNullBehaviorVerifier;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.jalu.collectionbehavior.verification.MapModificationVerifier.testMethods;
import static ch.jalu.collectionbehavior.verification.MapModificationVerifier.testMethodsForEntrySet;
import static ch.jalu.collectionbehavior.verification.MapModificationVerifier.testMethodsForKeySet;
import static ch.jalu.collectionbehavior.verification.MapModificationVerifier.testMethodsForSequencedMap;
import static ch.jalu.collectionbehavior.verification.MapModificationVerifier.testMethodsForValues;
import static ch.jalu.collectionbehavior.verification.MapMutabilityVerifier.immutable_changeToOriginalStructureIsNotReflectedInSet;
import static ch.jalu.collectionbehavior.verification.MapMutabilityVerifier.unmodifiable_changeToOriginalStructureIsReflectedInSet;
import static ch.jalu.collectionbehavior.verification.MapNullBehaviorVerifier.verifyRejectsNullArgInMethodsForKeys;
import static ch.jalu.collectionbehavior.verification.MapNullBehaviorVerifier.verifyRejectsNullArgInMethodsForValues;
import static ch.jalu.collectionbehavior.verification.MapNullBehaviorVerifier.verifySupportsNullArgInMethodsForKeys;
import static ch.jalu.collectionbehavior.verification.MapNullBehaviorVerifier.verifySupportsNullArgInMethodsForValues;
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
     * {@link HashMap}: standard implementation of Map. Does not retain iteration order. Supports null values and null
     * as key. Removal of entries from the key set, values set or entry set is reflected to the actual Map.
     */
    @TestFactory
    List<DynamicTest> jdk_HashMap() {
        return forMapType(MapCreator.forMutableType(HashMap::new))
            .expect(NullSupport.FULL, SetOrder.UNORDERED, MapInterfaceType.NONE)
            .mutability(ModificationBehavior.mutable())
            .createTests();
    }

    /**
     * {@link LinkedHashMap}: hash map with iteration by insertion order. Supports null values and null as key.
     * Removal of entries from the key set, values set or entry set is reflected to the actual Map.
     */
    @TestFactory
    List<DynamicTest> jdk_LinkedHashMap() {
        return forMapType(MapCreator.forMutableType(LinkedHashMap::new))
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, MapInterfaceType.SEQUENCED_MAP)
            .mutability(ModificationBehavior.mutable())
            .createTests();
    }

    /**
     * {@link TreeMap}: sorted mutable map that supports null values.
     */
    @TestFactory
    List<DynamicTest> jdk_TreeMap() {
        Comparator<String> strComparator = Comparator.nullsFirst(Comparator.naturalOrder());
        return forMapType(MapCreator.forMutableType(() -> new TreeMap<>(strComparator)))
            .expect(NullSupport.FULL, SetOrder.SORTED, MapInterfaceType.NAVIGABLE_MAP)
            .mutability(ModificationBehavior.mutable())
            .createTests();
    }

    /**
     * {@link Map#of} returns an immutable Map. Does not support null as value or key; even querying with null to
     * {@link Map#containsKey} results in an exception. Also throws an exception if there are duplicate keys when
     * the Map is being created. Random iteration order.
     */
    @Nested
    class Jdk_Map_of {

        private static final Map<String, Integer> MAP_0 = Map.of();
        private static final Map<String, Integer> MAP_1 = Map.of("rA", 240);
        private static final Map<String, Integer> MAP_2 = Map.of("rA", 240, "Xo", 134);
        private static final Map<String, Integer> MAP_3 = Map.of("rA", 240, "Xo", 134, "J1", 40);
        private static final Map<String, Integer> MAP_4 = Map.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194);
        private static final Map<String, Integer> MAP_5 = Map.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38);
        private static final Map<String, Integer> MAP_6 = Map.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38, "iK", 255);
        private static final Map<String, Integer> MAP_7 = Map.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38, "iK", 255, "c8", 19);
        private static final Map<String, Integer> MAP_8 = Map.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38, "iK", 255, "c8", 19, "yG", 247);
        private static final Map<String, Integer> MAP_9 = Map.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38, "iK", 255, "c8", 19, "yG", 247, "Kq", 169);
        private static final Map<String, Integer> MAP_10 = Map.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38, "iK", 255, "c8", 19, "yG", 247, "Kq", 169, "Mf", 129);

        @Test
        void hasDifferentClassForMapOfOneElement() {
            assertThat(MAP_0.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_1.getClass(), not(equalTo(MAP_2.getClass())));
            assertThat(MAP_3.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_4.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_5.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_6.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_7.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_8.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_9.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_10.getClass(), equalTo(MAP_2.getClass()));
        }

        @Test
        void isNotSequencedMap() {
            assertThat(MAP_0, not(instanceOf(SequencedMap.class)));
            assertThat(MAP_1, not(instanceOf(SequencedMap.class)));
        }

        @Test
        void hasRandomEntryOrder() {
            String[] keysMap10 = {"rA", "Xo", "J1", "lW", "B2", "iK", "c8", "yG", "Kq", "Mf"};
            assertThat(MAP_10.keySet(), containsInAnyOrder(keysMap10));
            assertThat(MAP_10.keySet(), not(contains(keysMap10)));
        }

        @Test
        void rejectsNullMethodArgs() {
            MapNullBehaviorVerifier.verifyRejectsNullArgInMethodsForKeys(MAP_10);
            MapNullBehaviorVerifier.verifyRejectsNullArgInMethodsForValues(MAP_10);
        }

        @Test
        void mayNotContainNull() {
            assertThrows(NullPointerException.class, () -> Map.of("a", 12, "b", null));
            assertThrows(NullPointerException.class, () -> Map.of("a", 12, null, 13));
            assertThrows(NullPointerException.class, () -> Map.of("a", null));
            assertThrows(NullPointerException.class, () -> Map.of(null, 12));
        }

        @Test
        void rejectsDuplicateKeysOnCreation() {
            assertThrows(IllegalArgumentException.class, () -> Map.of("a", 10, "b", 12, "a", 10));
        }

        @Test
        void unmodifiable() {
            ModificationBehavior modificationBehavior = ModificationBehavior.immutable().alwaysThrows();
            MapModificationVerifier.testMethods(MAP_10, modificationBehavior);
        }

        @Test
        void unmodifiable_keySet() {
            ModificationBehavior modificationBehaviorViews = ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD, SetMethod.ADD_ALL);
            MapModificationVerifier.testMethodsForKeySet(MAP_10, modificationBehaviorViews);
        }

        @Test
        void unmodifiable_entrySet() {
            ModificationBehavior modificationBehaviorViews = ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD, SetMethod.ADD_ALL);
            MapModificationVerifier.testMethodsForEntrySet(MAP_10, modificationBehaviorViews);
        }

        @Test
        void unmodifiable_values() {
            ModificationBehavior modificationBehaviorViews = ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD);
            MapModificationVerifier.testMethodsForValues(MAP_10, modificationBehaviorViews);
        }
    }

    /**
     * {@link Map#copyOf} returns an immutable Map copied from another map. Iteration order is not preserved from
     * the original Map. Null is not supported as key or as value. Throws also for null in {@link Map#containsKey} and
     * similar. Recognizes maps of its own class and avoids unnecessary copies.
     */
    @TestFactory
    List<DynamicTest> jdk_Map_copyOf() {
        return forMapType(MapCreator.forMapBasedType(Map::copyOf))
            .expect(NullSupport.REJECT, SetOrder.UNORDERED, MapInterfaceType.NONE)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .mutabilityEntrySet(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD, SetMethod.ADD_ALL)
            )
            .mutabilityKeySet(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD, SetMethod.ADD_ALL)
            )
            .mutabilityValues(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD)
            )
            .skipsWrappingForOwnClass()
            .createTests();
    }

    /**
     * {@link ImmutableMap#of} creates an immutable Map. Null is not supported as key or as values but can be
     * supplied to methods like {@link Map#containsKey} without problems. Iteration order is by order of encounter.
     * Throws if the same key is provided twice on creation.
     */
    @Nested
    class Guava_ImmutableMap_of {

        private static final Map<String, Integer> MAP_0 = ImmutableMap.of();
        private static final Map<String, Integer> MAP_1 = ImmutableMap.of("rA", 240);
        private static final Map<String, Integer> MAP_2 = ImmutableMap.of("rA", 240, "Xo", 134);
        private static final Map<String, Integer> MAP_3 = ImmutableMap.of("rA", 240, "Xo", 134, "J1", 40);
        private static final Map<String, Integer> MAP_4 = ImmutableMap.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194);
        private static final Map<String, Integer> MAP_5 = ImmutableMap.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38);
        private static final Map<String, Integer> MAP_6 = ImmutableMap.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38, "iK", 255);
        private static final Map<String, Integer> MAP_7 = ImmutableMap.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38, "iK", 255, "c8", 19);
        private static final Map<String, Integer> MAP_8 = ImmutableMap.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38, "iK", 255, "c8", 19, "yG", 247);
        private static final Map<String, Integer> MAP_9 = ImmutableMap.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38, "iK", 255, "c8", 19, "yG", 247, "Kq", 169);
        private static final Map<String, Integer> MAP_10 = ImmutableMap.of("rA", 240, "Xo", 134, "J1", 40, "lW", 194, "B2", 38, "iK", 255, "c8", 19, "yG", 247, "Kq", 169, "Mf", 129);

        @Test
        void hasDifferentClassForMapOfOneElement() {
            assertThat(MAP_0.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_1.getClass(), not(equalTo(MAP_2.getClass())));
            assertThat(MAP_3.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_4.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_5.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_6.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_7.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_8.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_9.getClass(), equalTo(MAP_2.getClass()));
            assertThat(MAP_10.getClass(), equalTo(MAP_2.getClass()));
        }

        @Test
        void isNotSequencedMap() {
            assertThat(MAP_0, not(instanceOf(SequencedMap.class)));
            assertThat(MAP_1, not(instanceOf(SequencedMap.class)));
        }

        @Test
        void keepsEntriesByInsertionOrder() {
            String[] keysMap10 = {"rA", "Xo", "J1", "lW", "B2", "iK", "c8", "yG", "Kq", "Mf"};
            assertThat(MAP_10.keySet(), contains(keysMap10));
        }

        @Test
        void supportsNullMethodArgs() {
            MapNullBehaviorVerifier.verifySupportsNullArgInMethodsForKeys(MAP_10);
            MapNullBehaviorVerifier.verifySupportsNullArgInMethodsForValues(MAP_10);
        }

        @Test
        void mayNotContainNull() {
            assertThrows(NullPointerException.class, () -> ImmutableMap.of("a", 12, "b", null));
            assertThrows(NullPointerException.class, () -> ImmutableMap.of("a", 12, null, 13));
            assertThrows(NullPointerException.class, () -> ImmutableMap.of("a", null));
            assertThrows(NullPointerException.class, () -> ImmutableMap.of(null, 12));
        }

        @Test
        void rejectsDuplicateKeysOnCreation() {
            assertThrows(IllegalArgumentException.class, () -> ImmutableMap.of("a", 10, "b", 12, "a", 10));
        }

        @Test
        void unmodifiable() {
            ModificationBehavior modificationBehavior = ModificationBehavior.immutable().alwaysThrows();
            MapModificationVerifier.testMethods(MAP_10, modificationBehavior);
        }

        @Test
        void unmodifiable_keySet() {
            ModificationBehavior modificationBehaviorViews = ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.REMOVE, SetMethod.REMOVE_IF, SetMethod.REMOVE_ALL,
                          SetMethod.RETAIN_ALL);
            MapModificationVerifier.testMethodsForKeySet(MAP_10, modificationBehaviorViews);
        }

        @Test
        void unmodifiable_entrySet() {
            ModificationBehavior modificationBehaviorViews = ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD_ALL, SetMethod.REMOVE, SetMethod.REMOVE_IF,
                          SetMethod.REMOVE_ALL, SetMethod.RETAIN_ALL);
            MapModificationVerifier.testMethodsForEntrySet(MAP_10, modificationBehaviorViews);
        }

        @Test
        void unmodifiable_values() {
            ModificationBehavior modificationBehaviorViews = ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD_ALL, SetMethod.REMOVE, SetMethod.REMOVE_IF, SetMethod.REMOVE_ALL,
                          SetMethod.RETAIN_ALL);
            MapModificationVerifier.testMethodsForValues(MAP_10, modificationBehaviorViews);
        }
    }

    /**
     * {@link ImmutableMap#copyOf} copies an immutable Map and keeps the iteration order of the original map.
     * Does not support null as key or values but accepts null in methods such as {@link Map#containsKey}.
     */
    @TestFactory
    List<DynamicTest> guava_ImmutableMap_copyOf() {
        return forMapType(MapCreator.forMapBasedType(ImmutableMap::copyOf))
            // Not SequencedMap to support older JDKs
            .expect(NullSupport.ARGUMENTS, SetOrder.INSERTION_ORDER, MapInterfaceType.NONE)
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
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, MapInterfaceType.NONE)
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
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, MapInterfaceType.SEQUENCED_MAP)
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
            .expect(NullSupport.ARGUMENTS, SetOrder.INSERTION_ORDER, MapInterfaceType.NONE)
            .mutability(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING,
                        MapMethod.REMOVE_KEY_VALUE, MapMethod.REPLACE, MapMethod.REPLACE_WITH_OLD_VALUE,
                        MapMethod.COMPUTE, MapMethod.COMPUTE_IF_PRESENT, MapMethod.COMPUTE_IF_ABSENT)
            )
            .createTests();
    }

    @TestFactory
    List<DynamicTest> jdk_Collections_singletonMap() {
        return forMapType(MapCreator.forSingleEntry(Collections::singletonMap))
            .expect(NullSupport.FULL, SetOrder.INSERTION_ORDER, MapInterfaceType.NONE)
            .mutability(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class).on(MethodCallEffect.NON_MODIFYING,
                    // putAll non-modifying does not throw...
                    MapMethod.PUT, MapMethod.PUT_IF_ABSENT, MapMethod.REMOVE_KEY_VALUE, MapMethod.REPLACE,
                    MapMethod.REMOVE_KEY_VALUE, MapMethod.REPLACE_WITH_OLD_VALUE, MapMethod.REPLACE_ALL,
                    MapMethod.MERGE, MapMethod.COMPUTE, MapMethod.COMPUTE_IF_ABSENT, MapMethod.COMPUTE_IF_PRESENT)
            )
            .mutabilityEntrySet(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class).on(MethodCallEffect.NON_MODIFYING,
                    SetMethod.ADD, SetMethod.ADD_ALL, SetMethod.REMOVE_IF)
            )
            .mutabilityKeySet(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class).on(MethodCallEffect.NON_MODIFYING,
                    SetMethod.ADD, SetMethod.ADD_ALL, SetMethod.REMOVE_IF)
            )
            .mutabilityValues(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class).on(MethodCallEffect.NON_MODIFYING,
                    SetMethod.REMOVE_IF)
            )
            .createTests();
    }

    /**
     * {@link Collectors#toMap(Function, Function)} makes no guarantees on mutability of the Map, though at the moment
     * it creates a HashMap. As such, order is not kept. The map may not have null values, but null as key is supported.
     * Throws for duplicate keys.
     */
    @Test
    void jdk_Collectors_toMap() {
        Map<Character, Integer> map = Stream.of('f', 'g')
            .collect(Collectors.toMap(v -> v, v -> (int) v));
        assertThat(map.getClass(), equalTo(HashMap.class));

        // Throws for duplicate keys (plain HashMap would not)
        assertThrows(IllegalStateException.class, () -> Stream.of('f', 'g', 'f')
            .collect(Collectors.toMap(v -> v, v -> (int) v)));

        // Null key is OK
        Map<Character, String> mapWithNullKey = Stream.of('f', 'g', null)
            .collect(Collectors.toMap(Function.identity(), String::valueOf));
        assertThat(mapWithNullKey.get(null), equalTo("null"));

        // Throws for null values (plain HashMap does not)
        assertThrows(NullPointerException.class, () -> Stream.of('f', 'g', null)
            .collect(Collectors.toMap(v -> v, null)));
    }

    /**
     * {@link Collectors#toUnmodifiableMap(Function, Function)} produces an unmodifiable map that rejects null values:
     * keys and values may not be null; methods like {@link Map#containsKey} may not be called with null as argument.
     * Order of keys is random.
     */
    @TestFactory
    List<DynamicTest> jdk_Collectors_toUnmodifiableMap() {
        return forMapType(MapCreator.fromStream(
                              str -> str.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue))))
            .expect(NullSupport.REJECT, SetOrder.UNORDERED, MapInterfaceType.NONE)
            .mutability(ModificationBehavior.immutable().alwaysThrows())
            .mutabilityKeySet(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD, SetMethod.ADD_ALL)
            )
            .mutabilityEntrySet(ModificationBehavior.immutable().throwsIfWouldBeModified()
                .butThrows(UnsupportedOperationException.class)
                    .on(MethodCallEffect.NON_MODIFYING, SetMethod.ADD, SetMethod.ADD_ALL)
            )
            .mutabilityValues(ModificationBehavior.immutable().throwsIfWouldBeModified())
            .rejectsDuplicateKeysOnCreation()
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

        private NullSupport keysNullSupport;
        private NullSupport valuesNullSupport;
        private SetOrder keyOrder;
        private MapInterfaceType mapInterfaceType;
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
         * @param nullSupport expected null support of the map type (for keys and values)
         * @param keySetOrder expected order of the map's keys
         * @param mapInterfaceType generic interface the map implements
         * @return this instance, for chaining
         */
        TestsGenerator expect(NullSupport nullSupport, SetOrder keySetOrder, MapInterfaceType mapInterfaceType) {
            return expect(nullSupport, nullSupport, keySetOrder, mapInterfaceType);
        }

        /**
         * Sets some basic expected properties of the map type to this generator.
         *
         * @param keysNullSupport expected null support of the map type for keys
         * @param valuesNullSupport expected null support of the map type for values
         * @param keySetOrder expected order of the map's keys
         * @param mapInterfaceType generic interface the map implements
         * @return this instance, for chaining
         */
        TestsGenerator expect(NullSupport keysNullSupport, NullSupport valuesNullSupport,
                              SetOrder keySetOrder, MapInterfaceType mapInterfaceType) {
            this.keysNullSupport = keysNullSupport;
            this.valuesNullSupport = valuesNullSupport;
            this.keyOrder = keySetOrder;
            this.mapInterfaceType = mapInterfaceType;
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
         * When the map is created based on another structure that allows duplicate keys, defines that duplicate keys
         * in that input structure will result in an exception.
         *
         * @return this instance, for chaining
         */
        TestsGenerator rejectsDuplicateKeysOnCreation() {
            this.acceptsDuplicatesOnCreation = false;
            return this;
        }

        /**
         * Only applicable for map-based map creators: it is expected that the method recognizes sets of its return
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
                    createTestForMapInterface(),
                    createTestForMutability(),
                    createTestForSkipsWrappingOwnClassIfApplicable(),
                    createTestForDuplicateKeysOnCreation()
                )
                .flatMap(Function.identity())
                .toList();
        }

        private Stream<DynamicTest> createTestsForNullSupport() {
            List<DynamicTest> valueTests = new ArrayList<>();
            List<DynamicTest> argumentTests = new ArrayList<>();

            switch (keysNullSupport) {
                case FULL -> {
                    valueTests.add(testLogic.supportsNullElementsKeys());
                    argumentTests.add(testLogic.supportsNullMethodKeyArgs());
                }
                case ARGUMENTS -> {
                    valueTests.add(testLogic.mayNotContainNullKeys());
                    argumentTests.add(testLogic.supportsNullMethodKeyArgs());
                }
                case REJECT -> {
                    valueTests.add(testLogic.mayNotContainNullKeys());
                    argumentTests.add(testLogic.rejectsNullMethodKeyArgs());
                }
            }

            switch (valuesNullSupport) {
                case FULL -> {
                    valueTests.add(testLogic.supportsNullElementsValues());
                    argumentTests.add(testLogic.supportsNullMethodValueArgs());
                }
                case ARGUMENTS -> {
                    valueTests.add(testLogic.mayNotContainNullValues());
                    argumentTests.add(testLogic.supportsNullMethodValueArgs());
                }
                case REJECT -> {
                    valueTests.add(testLogic.mayNotContainNullValues());
                    argumentTests.add(testLogic.rejectsNullMethodValueArgs());
                }
            }

            if (mapCreator.getSizeLimit() == 0) {
                // Null values do not apply to an empty map type, so use just the argument tests
                return argumentTests.stream();
            }
            return Stream.concat(valueTests.stream(), argumentTests.stream());
        }

        private Stream<DynamicTest> createTestForMutability() {
            if (modificationBehavior.isMutable()) {
                return createTestsForMutableAssertions().stream();
            }

            List<DynamicTest> testsToRun = new ArrayList<>();
            Map<String, Integer> map = mapCreator.createMapWithAbcdOrSubset();
            createTestForImmutabilityBehavior().ifPresent(testsToRun::add);
            testsToRun.add(dynamicTest("unmodifiable",
                () -> testMethods(map, modificationBehavior)));
            testsToRun.add(dynamicTest("unmodifiable_entrySet",
                () -> testMethodsForEntrySet(map, modificationBehaviorEntrySet)));
            testsToRun.add(dynamicTest("unmodifiable_keySet",
                () -> testMethodsForKeySet(map, modificationBehaviorKeySet)));
            testsToRun.add(dynamicTest("unmodifiable_values",
                () -> testMethodsForValues(map, modificationBehaviorValues)));

            if (map instanceof SequencedMap<String, Integer> seqMap) {
                testsToRun.add(dynamicTest("unmodifiable_SequencedMap",
                    () -> testMethodsForSequencedMap(seqMap, modificationBehavior)));
            }

            return testsToRun.stream();
        }

        private List<DynamicTest> createTestsForMutableAssertions() {
            Map<String, Integer> map = mapCreator.createMap();
            List<DynamicTest> tests = new ArrayList<>();
            tests.addAll(List.of(
                dynamicTest("mutable",
                    () -> MapMutabilityVerifier.verifyMapIsMutable(map)),
                dynamicTest("mutable_keySet",
                    () -> MapMutabilityVerifier.verifyMapKeySetIsMutable(map)),
                dynamicTest("mutable_values",
                    () -> MapMutabilityVerifier.verifyMapValuesIsMutable(map)),
                dynamicTest("mutable_entrySet",
                    () -> MapMutabilityVerifier.verifyMapEntrySetIsMutable(map))));

            if (map instanceof NavigableMap<String, Integer> navigableMap) {
                tests.add(dynamicTest("mutable_NavigableMap",
                        () -> MapMutabilityVerifier.verifyMapIsMutableByNavigableMapValues(navigableMap)));
            } else if (map instanceof SortedMap<String, Integer> sortedMap) {
                tests.add(dynamicTest("mutable_SortedMap",
                    () -> MapMutabilityVerifier.verifyMapIsMutableBySortedMapValues(sortedMap)));
            }  else if (map instanceof SequencedMap<String, Integer> seqMap) {
                tests.add(dynamicTest("mutable_SequencedMap",
                    () -> MapMutabilityVerifier.verifyMapIsMutableBySequencedMapValues(seqMap)));
            }

            return tests;
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
                Preconditions.checkState(keyOrder == SetOrder.INSERTION_ORDER);
                return Stream.empty();
            }

            return switch (keyOrder) {
                case INSERTION_ORDER -> Stream.of(testLogic.keepsElementsByInsertionOrder());
                case SORTED -> Stream.of(testLogic.keepsElementsSorted());
                case UNORDERED -> Stream.of(testLogic.hasRandomElementOrder());
            };
        }

        private Stream<DynamicTest> createTestForMapInterface() {
            return switch (mapInterfaceType) {
                case SEQUENCED_MAP -> Stream.of(testLogic.isSequencedMap());
                case SORTED_MAP -> Stream.of(testLogic.isSortedMap());
                case NAVIGABLE_MAP -> Stream.of(testLogic.isNavigableMap());
                case NONE -> Stream.of(testLogic.isNotSequencedMap());
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

        private Stream<DynamicTest> createTestForDuplicateKeysOnCreation() {
            if (!mapCreator.canEncounterDuplicateKeys()) {
                Preconditions.checkState(acceptsDuplicatesOnCreation);
                return Stream.empty();
            }

            return acceptsDuplicatesOnCreation
                ? Stream.of(testLogic.acceptsDuplicateKeysOnCreation(keyOrder))
                : Stream.of(testLogic.rejectsDuplicateKeysOnCreation());
        }
    }

    private static final class TestLogic {

        private final MapCreator mapCreator;

        private TestLogic(MapCreator mapCreator) {
            this.mapCreator = mapCreator;
        }

        DynamicTest supportsNullElementsKeys() {
            return dynamicTest("supportsNullElements_keys",
                () -> assertDoesNotThrow(mapCreator::createMapWithNullKey));
        }

        DynamicTest supportsNullElementsValues() {
            return dynamicTest("supportsNullElements_values",
                () -> assertDoesNotThrow(mapCreator::createMapWithNullValue));
        }

        DynamicTest mayNotContainNullKeys() {
            return dynamicTest("mayNotContainNull_keys",
                () -> assertThrows(NullPointerException.class, mapCreator::createMapWithNullKey));
        }

        DynamicTest mayNotContainNullValues() {
            return dynamicTest("mayNotContainNull_values",
                () -> assertThrows(NullPointerException.class, mapCreator::createMapWithNullValue));
        }

        DynamicTest supportsNullMethodKeyArgs() {
            return dynamicTest("supportsNullMethodArgs_keys",
                () -> verifySupportsNullArgInMethodsForKeys(mapCreator.createMap()));
        }

        DynamicTest supportsNullMethodValueArgs() {
            return dynamicTest("supportsNullMethodArgs_values",
                () -> verifySupportsNullArgInMethodsForValues(mapCreator.createMap()));
        }

        DynamicTest rejectsNullMethodKeyArgs() {
            return dynamicTest("rejectsNullMethodArgs_keys",
                () -> verifyRejectsNullArgInMethodsForKeys(mapCreator.createMap()));
        }

        DynamicTest rejectsNullMethodValueArgs() {
            return dynamicTest("rejectsNullMethodArgs_values",
                () -> verifyRejectsNullArgInMethodsForValues(mapCreator.createMap()));
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
            return dynamicTest("isSequencedMap", () -> {
                assertThat(mapCreator.createMap(), instanceOf(SequencedMap.class));
                assertThat(mapCreator.createMap(), not(instanceOf(SortedMap.class)));
                assertThat(mapCreator.createMap(), not(instanceOf(NavigableMap.class)));
            });
        }

        DynamicTest isSortedMap() {
            return dynamicTest("isSortedMap", () -> {
                assertThat(mapCreator.createMap(), instanceOf(SequencedMap.class));
                assertThat(mapCreator.createMap(), instanceOf(SortedMap.class));
                assertThat(mapCreator.createMap(), not(instanceOf(NavigableMap.class)));
            });
        }

        DynamicTest isNavigableMap() {
            return dynamicTest("isNavigableMap", () -> {
                assertThat(mapCreator.createMap(), instanceOf(SequencedMap.class));
                assertThat(mapCreator.createMap(), instanceOf(SortedMap.class));
                assertThat(mapCreator.createMap(), instanceOf(NavigableMap.class));
            });
        }

        DynamicTest isNotSequencedMap() {
            return dynamicTest("isNotSequencedMap", () -> {
                Map<String, Integer> map = mapCreator.createMap();
                assertThat(map, not(instanceOf(SequencedMap.class)));

                if (mapCreator instanceof MapCreator.MapBasedMapCreator<?> mmc) {
                    // Explicitly check that a SequencedMap as base does not yield a SequencedMap, as is done for
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

        DynamicTest acceptsDuplicateKeysOnCreation(SetOrder expectedOrder) {
            if (expectedOrder == SetOrder.UNORDERED) {
                return dynamicTest("acceptsDuplicateKeysOnCreation",
                    () -> assertThat(mapCreator.createMapWithDuplicateKeys().keySet(), containsInAnyOrder("a", "b", "c")));
            }
            return dynamicTest("acceptsDuplicateKeysOnCreation",
                () -> assertThat(mapCreator.createMapWithDuplicateKeys().keySet(), contains("a", "b", "c")));
        }

        DynamicTest rejectsDuplicateKeysOnCreation() {
            return dynamicTest("rejectsDuplicateKeysOnCreation",
                () -> assertThrows(IllegalStateException.class, mapCreator::createMapWithDuplicateKeys));
        }
    }
}
