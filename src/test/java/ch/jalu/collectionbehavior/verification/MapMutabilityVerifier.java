package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.MapWithBackingDataModifier;

import java.util.Collection;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;
import java.util.SortedMap;

import static ch.jalu.collectionbehavior.model.MapCreator.A_VALUE;
import static ch.jalu.collectionbehavior.model.MapCreator.B_VALUE;
import static ch.jalu.collectionbehavior.model.MapCreator.C_VALUE;
import static ch.jalu.collectionbehavior.model.MapCreator.D_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MapMutabilityVerifier {

    private MapMutabilityVerifier() {
    }

    public static void verifyMapIsMutable(Map<String, Integer> map) {
        assertThat(map, anEmptyMap()); // Validate method contract

        // #put, #putAll, #putIfAbsent
        map.put("A", 1);
        map.putAll(Map.of("B", 2, "C", 3));
        map.putIfAbsent("C", 25); // no change
        map.putIfAbsent("D", 4);
        assertThat(map, equalTo(Map.of("A", 1, "B", 2, "C", 3, "D", 4)));

        // #replace, #replaceAll
        map.replace("A", 3, 65); // no change
        map.replace("B", 2, 4);
        map.replace("C", 9);
        map.replace("Z", 99); // no change
        assertThat(map, equalTo(Map.of("A", 1, "B", 4, "C", 9, "D", 4)));
        map.replaceAll((k, v) -> v * 2);

        // #remove
        map.remove("C");

        // #merge
        map.merge("B", 1, Integer::sum);
        assertThat(map, equalTo(Map.of("A", 2, "B", 9, "D", 8)));

        // #compute, #computeIfPresent, #computeIfAbsent
        map.compute("B", (k, v) -> null);
        map.computeIfPresent("A", (k, v) -> v + 1);
        map.computeIfPresent("Z", (k, v) -> v + 1); // no change
        map.computeIfAbsent("D", k -> (int) k.charAt(0)); // no change
        map.computeIfAbsent("E", k -> (int) k.charAt(0));
        assertThat(map, equalTo(Map.of("A", 3, "D", 8, "E", 69)));

        // #clear
        map.clear();
        assertThat(map, anEmptyMap());
    }

    public static void verifyMapKeySetIsMutable(Map<String, Integer> map) {
        assertThat(map, anEmptyMap()); // Validate method contract
        map.putAll(Map.of("a", 1, "b", 2, "c", 3, "d", 4));

        Set<String> keySet = map.keySet();

        keySet.remove("c");
        keySet.removeIf(k -> k.equals("b"));
        keySet.retainAll(Set.of("d", "z"));
        assertThat(map, equalTo(Map.of("d", 4)));

        keySet.clear();
        assertThat(map, anEmptyMap());
        assertThrows(UnsupportedOperationException.class, () -> keySet.add("f"));
    }

    public static void verifyMapValuesIsMutable(Map<String, Integer> map) {
        assertThat(map, anEmptyMap()); // Validate method contract
        map.putAll(Map.of("a", 1, "b", 2, "c", 3, "d", 4));

        Collection<Integer> values = map.values();

        values.remove(3);
        values.removeIf(k -> k.equals(2));
        values.retainAll(Set.of(4, 8));
        assertThat(map, equalTo(Map.of("d", 4)));

        values.clear();
        assertThat(map, anEmptyMap());
        assertThrows(UnsupportedOperationException.class, () -> values.add(7));
    }

    public static void verifyMapEntrySetIsMutable(Map<String, Integer> map) {
        assertThat(map, anEmptyMap()); // Validate method contract
        map.putAll(Map.of("a", 1, "b", 2, "c", 3, "d", 4));

        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        entrySet.remove(Map.entry("b", 2));
        entrySet.remove(Map.entry("d", 2)); // no change
        entrySet.removeIf(k -> k.equals(Map.entry("b", 2)));
        entrySet.retainAll(Set.of(Map.entry("d", 4), Map.entry("e", 5)));
        assertThat(map, equalTo(Map.of("d", 4)));

        Map.Entry<String, Integer> entry = entrySet.iterator().next();
        entry.setValue(7);
        assertThat(map, equalTo(Map.of("d", 7)));

        entrySet.clear();
        assertThat(map, anEmptyMap());
        assertThrows(UnsupportedOperationException.class, () -> entrySet.add(Map.entry("z", 7)));
    }

    public static void verifyMapIsMutableBySortedMapValues(SortedMap<String, Integer> map) {
        assertThat(map, anEmptyMap()); // Validate method contract

        // SequencedMap's #putFirst and #putLast throw an exception because a SortedMap has implicit ordering
        assertThrows(UnsupportedOperationException.class, () -> map.putFirst("a", 1));
        assertThrows(UnsupportedOperationException.class, () -> map.putLast("a", 1));

        map.putAll(Map.of("a", 1, "b", 2, "c", 3, "d", 4));
        SortedMap<String, Integer> subMap = map.subMap("b", "d");
        subMap.put("b2", 5);
        assertThat(map.keySet(), contains("a", "b", "b2", "c", "d"));

        SortedMap<String, Integer> tailMap = map.tailMap("b2");
        tailMap.remove("c");
        tailMap.put("d2", 8);
        assertThat(map.keySet(), contains("a", "b", "b2", "d", "d2"));

        SortedMap<String, Integer> headMap = map.headMap("c");
        headMap.keySet().removeIf(k -> !k.endsWith("2")); // remove a, b
        assertThat(map.keySet(), contains("b2", "d", "d2"));

        SortedMap<String, Integer> reversed = map.reversed();
        reversed.put("c1", 17);
        assertThat(map.keySet(), contains("b2", "c1", "d", "d2"));

        reversed.clear();
        assertThat(map, anEmptyMap());
    }

    public static void verifyMapIsMutableBySequencedMapValues(SequencedMap<String, Integer> map) {
        assertThat(map, anEmptyMap()); // Validate method contract

        map.put("b", 2);
        map.putFirst("a", 1);
        map.putLast("c", 3);
        assertThat(map.keySet(), contains("a", "b", "c"));

        map.putLast("b", 2);
        assertThat(map.keySet(), contains("a", "c", "b"));

        SequencedMap<String, Integer> reversed = map.reversed();
        assertThat(reversed.keySet(), contains("b", "c", "a"));
        reversed.putFirst("z", 20);
        reversed.putLast("u", 10);
        assertThat(map.keySet(), contains("u", "a", "b", "c", "z"));
    }

    public static void unmodifiable_changeToOriginalStructureIsReflectedInSet(MapWithBackingDataModifier setWithBackingDataModifier) {
        Map<String, Integer> map = setWithBackingDataModifier.map();
        assertThat(map, equalTo(Map.of("a", A_VALUE, "b", B_VALUE, "c", C_VALUE, "d", D_VALUE)));
        setWithBackingDataModifier.runBackingDataModifier();
        assertThat(map, equalTo(Map.of("a", A_VALUE, "b", B_VALUE, "c", C_VALUE)));
    }

    public static void immutable_changeToOriginalStructureIsNotReflectedInSet(MapWithBackingDataModifier setWithBackingDataModifier) {
        Map<String, Integer> map = setWithBackingDataModifier.map();
        assertThat(map, equalTo(Map.of("a", A_VALUE, "b", B_VALUE, "c", C_VALUE, "d", D_VALUE)));
        setWithBackingDataModifier.runBackingDataModifier();
        assertThat(map, equalTo(Map.of("a", A_VALUE, "b", B_VALUE, "c", C_VALUE, "d", D_VALUE))); // unchanged
    }
}
