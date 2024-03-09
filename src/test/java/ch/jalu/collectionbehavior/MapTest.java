package ch.jalu.collectionbehavior;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapTest {

    /**
     * HashMap: standard implementation of Map. Does not retain iteration order. Supports null values and null as key.
     * Removal of entries from the key set, values set or entry set is reflected to the actual Map.
     */
    @Test
    void testJdkHashMap() {
        Map<Character, Integer> map = new HashMap<>();
        map.put('0', 48);
        map.put('z', 122);
        map.put('A', 65);

        assertContainsEntriesNotInOrder(map);
        assertThat(map, not(instanceOf(SequencedMap.class)));

        assertThat(map.containsValue(null), equalTo(false));
        assertThat(map.containsKey(null), equalTo(false));
        map.put(null, -1);
        map.put('~', null);
        assertThat(map.containsValue(null), equalTo(true));
        assertThat(map.containsKey(null), equalTo(true));

        map.keySet().remove('A');
        assertThat(map.keySet(), containsInAnyOrder('0', 'z', null, '~'));
        map.values().remove(-1);
        assertThat(map.keySet(), containsInAnyOrder('0', 'z', '~'));
        map.entrySet().removeIf(e -> e.getValue() == null);
        assertThat(map.keySet(), containsInAnyOrder('0', 'z'));
    }

    /**
     * LinkedHashMap: hash map with iteration by insertion order. Supports null values and null as key.
     * Removal of entries from the key set, values set or entry set is reflected to the actual Map.
     */
    @Test
    void testJdkLinkedHashMap() {
        Map<Character, Integer> map = new LinkedHashMap<>();
        map.put('0', 48);
        map.put('z', 122);
        map.put('A', 65);

        assertThat(map.keySet(), contains('0', 'z', 'A'));
        assertThat(map, instanceOf(SequencedMap.class));

        assertThat(map.containsValue(null), equalTo(false));
        assertThat(map.containsKey(null), equalTo(false));
        map.put(null, -1);
        map.put('~', null);
        assertThat(map.containsValue(null), equalTo(true));
        assertThat(map.containsKey(null), equalTo(true));

        map.keySet().remove('A');
        assertThat(map.keySet(), contains('0', 'z', null, '~'));
        map.values().remove(-1);
        assertThat(map.keySet(), contains('0', 'z', '~'));
        map.entrySet().removeIf(e -> e.getValue() == null);
        assertThat(map.keySet(), contains('0', 'z'));
    }

    /**
     * {@link Map#of} returns an immutable Map. Does not support null as value or key; even querying with null to
     * {@link Map#containsKey} results in an exception. Also throws an exception if there are duplicate keys when
     * the Map is being created. Random iteration order.
     */
    @Test
    void testJdkMapOf() {
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

    /**
     * {@link Map#copyOf} returns an immutable Map copied from another map. Iteration order is not preserved from
     * the original Map. Null is not supported as key or as value. Throws also for null in {@link Map#containsKey} and
     * similar. Recognizes maps of its own class and avoids unnecessary copies.
     */
    @Test
    void testJdkMapCopyOf() {
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

        assertThrows(NullPointerException.class, () -> map.containsKey(null));
        assertThrows(NullPointerException.class, () -> map.containsValue(null));
    }

    /**
     * {@link ImmutableMap#of} creates an immutable Map. Null is not supported as key or as values but can be
     * supplied to methods like {@link Map#containsKey} without problems. Iteration order is by order of encounter.
     * Throws if the same key is provided twice on creation.
     */
    @Test
    void testGuavaImmutableMapOf() {
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
    void testGuavaImmutableMapCopyOf() {
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
    void testJdkCollectionsUnmodifiableMap() {
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
    void testJdkCollectionsSequencedMap() {
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
    void testJdkCollectionsEmptyMap() {
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
    void testJdkCollectorsToMap() {
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
    void testJdkCollectorsToUnmodifiableMap() {
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
}
