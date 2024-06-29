package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.MapMethod;
import ch.jalu.collectionbehavior.model.ModificationBehavior;
import ch.jalu.collectionbehavior.model.SetMethod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;

import static ch.jalu.collectionbehavior.model.MapCreator.A_VALUE;
import static ch.jalu.collectionbehavior.model.MapCreator.B_VALUE;
import static ch.jalu.collectionbehavior.model.MapCreator.D_VALUE;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MapModificationVerifier {

    private MapModificationVerifier() {
    }

    public static void testMethods(Map<String, Integer> originalMap, ModificationBehavior expectedBehavior) {
        new UnmodifiableMapBehaviorTester<>(originalMap, LinkedHashMap::new, expectedBehavior)
            .test(MapMethod.PUT, map -> map.put("e", 5))
            .test(MapMethod.PUT, map -> map.put("a", 1))
            .test(MapMethod.PUT, map -> map.put("a", A_VALUE))
            .test(MapMethod.PUT_ALL, map -> map.putAll(Map.of("e", 5, "f", 6)))
            .test(MapMethod.PUT_ALL, map -> map.putAll(Map.of("a", 2)))
            .test(MapMethod.PUT_ALL, map -> map.putAll(Map.of()))
            .test(MapMethod.PUT_IF_ABSENT, map -> map.putIfAbsent("e", 5))
            .test(MapMethod.PUT_IF_ABSENT, map -> map.putIfAbsent("a", 1)) 
            .test(MapMethod.REMOVE, map -> map.remove("a")) 
            .test(MapMethod.REMOVE, map -> map.remove("e")) 
            .test(MapMethod.REMOVE_KEY_VALUE, map -> map.remove("a", A_VALUE))
            .test(MapMethod.REMOVE_KEY_VALUE, map -> map.remove("a", 5)) 
            .test(MapMethod.REPLACE, map -> map.replace("a", 5)) 
            .test(MapMethod.REPLACE, map -> map.replace("a", A_VALUE))
            .test(MapMethod.REPLACE, map -> map.replace("e", 5))
            .test(MapMethod.REPLACE_WITH_OLD_VALUE, map -> map.replace("a", A_VALUE, 5))
            .test(MapMethod.REPLACE_WITH_OLD_VALUE, map -> map.replace("e", 5, 6))
            .test(MapMethod.REPLACE_ALL, map -> map.replaceAll((k, v) -> v + 1)) 
            .test(MapMethod.REPLACE_ALL, map -> map.replaceAll((k, v) -> v)) 
            .test(MapMethod.CLEAR, map -> map.clear()) 
            .test(MapMethod.MERGE, map -> map.merge("a", 5, Integer::sum)) 
            .test(MapMethod.MERGE, map -> map.merge("e", 5, (a, b) -> a)) 
            .test(MapMethod.MERGE, map -> map.merge("a", 1, (a, b) -> A_VALUE))
            .test(MapMethod.COMPUTE, map -> map.compute("a", (k, v) -> v == null ? null : v + 1))
            .test(MapMethod.COMPUTE, map -> map.compute("e", (k, v) -> v == null ? null : v + 1))
            .test(MapMethod.COMPUTE_IF_ABSENT, map -> map.computeIfAbsent("e", k -> 5)) 
            .test(MapMethod.COMPUTE_IF_ABSENT, map -> map.computeIfAbsent("a", k -> 5)) 
            .test(MapMethod.COMPUTE_IF_PRESENT, map -> map.computeIfPresent("a", (k, v) -> v + 1)) 
            .test(MapMethod.COMPUTE_IF_PRESENT, map -> map.computeIfPresent("e", (k, v) -> v + 1));
    }

    public static void testMethodsForEntrySet(Map<String, Integer> originalMap, ModificationBehavior expectedBehavior) {
        new UnmodifiableCollectionBehaviorTester<>(originalMap.entrySet(), LinkedHashSet::new, expectedBehavior)
            .test(SetMethod.ADD, set -> set.add(Map.entry("g", 1)))
            .test(SetMethod.ADD, set -> set.add(Map.entry("a", A_VALUE)))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of(Map.entry("g", 44))))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of(Map.entry("a", A_VALUE))))
            // TODO .test(SetMethod.ADD_ALL, set -> set.addAll(List.of()))
            .test(SetMethod.REMOVE, set -> set.remove(Map.entry("a", A_VALUE)))
            .test(SetMethod.REMOVE, set -> set.remove(Map.entry("g", 66)))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(entry -> entry.getKey().equals("b")))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(entry -> false))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of(Map.entry("a", A_VALUE), Map.entry("b", B_VALUE))))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of(Map.entry("g", 33))))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(List.of()))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(set))
            .test(SetMethod.CLEAR, set -> set.clear());

        originalMap.entrySet().stream().findFirst().ifPresent(entry -> {
            assertThrows(UnsupportedOperationException.class, () -> entry.setValue(1));
            assertThrows(UnsupportedOperationException.class, () -> entry.setValue(entry.getValue()));
        });
    }

    public static void testMethodsForKeySet(Map<String, Integer> originalMap, ModificationBehavior expectedBehavior) {
        new UnmodifiableCollectionBehaviorTester<>(originalMap.keySet(), LinkedHashSet::new, expectedBehavior)
            .test(SetMethod.ADD, set -> set.add("f"))
            .test(SetMethod.ADD, set -> set.add("b"))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of("g")))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of("a")))
            // TODO .test(SetMethod.ADD_ALL, set -> set.addAll(List.of()))
            .test(SetMethod.REMOVE, set -> set.remove("b"))
            .test(SetMethod.REMOVE, set -> set.remove("g"))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(key -> true))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(key -> false))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of("a", "d")))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of("y", "z")))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(List.of("c")))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(set))
            .test(SetMethod.CLEAR, set -> set.clear());
    }

    public static void testMethodsForValues(Map<String, Integer> originalMap, ModificationBehavior expectedBehavior) {
        // Note: Map#values returns a Collection, not a Set, but we reference SetMethod
        // because it has the least "additions" to the Collection interface
        new UnmodifiableCollectionBehaviorTester<>(originalMap.values(), ArrayList::new, expectedBehavior, true)
            .test(SetMethod.ADD, set -> set.add(1))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of(1)))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of()))
            .test(SetMethod.REMOVE, set -> set.remove(A_VALUE))
            .test(SetMethod.REMOVE, set -> set.remove(1))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(value -> true))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(value -> false))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of(A_VALUE, D_VALUE)))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of(1, 2)))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(List.of(2)))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(set))
            .test(SetMethod.CLEAR, set -> set.clear());
    }

    public static void testMethodsForSequencedMap(SequencedMap<String, Integer> originalMap,
                                                  ModificationBehavior expectedBehavior) {
        new UnmodifiableMapBehaviorTester<>(originalMap, LinkedHashMap::new, expectedBehavior)
            .test(MapMethod.PUT_FIRST, map -> map.putFirst("a", A_VALUE))
            .test(MapMethod.PUT_FIRST, map -> map.putFirst("0", 0))
            .test(MapMethod.PUT_LAST, map -> map.putFirst("d", D_VALUE))
            .test(MapMethod.PUT_LAST, map -> map.putFirst("z", 0))
        ;
    }
}
