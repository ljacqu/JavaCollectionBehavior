package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.MapMethod;
import ch.jalu.collectionbehavior.model.ModificationBehavior;

import java.util.LinkedHashMap;
import java.util.Map;

import static ch.jalu.collectionbehavior.model.MapCreator.A_VALUE;

public final class MapModificationVerifier {

    private MapModificationVerifier() {
    }

    public static void testMethods(Map<String, Integer> originalMap, ModificationBehavior expectedBehavior) {
        new UnmodifiableMapBehaviorTester(originalMap, LinkedHashMap::new, expectedBehavior)
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
            .test(MapMethod.REPLACE, map -> map.replace("e", 5)) 
            .test(MapMethod.REPLACE_WITH_OLD_VALUE, map -> map.replace("a", A_VALUE, 5))
            .test(MapMethod.REPLACE_WITH_OLD_VALUE, map -> map.replace("e", 5, 6))
            .test(MapMethod.REPLACE_ALL, map -> map.replaceAll((k, v) -> v + 1)) 
            .test(MapMethod.REPLACE_ALL, map -> map.replaceAll((k, v) -> v)) 
            .test(MapMethod.CLEAR, map -> map.clear()) 
            .test(MapMethod.MERGE, map -> map.merge("a", 5, Integer::sum)) 
            .test(MapMethod.MERGE, map -> map.merge("e", 5, (a, b) -> a)) 
            .test(MapMethod.MERGE, map -> map.merge("a", 1, (a, b) -> A_VALUE))
            .test(MapMethod.COMPUTE, map -> map.compute("a", (k, v) -> v + 1))
            .test(MapMethod.COMPUTE, map -> map.compute("e", (k, v) -> v == null ? null : v + 1))
            .test(MapMethod.COMPUTE_IF_ABSENT, map -> map.computeIfAbsent("e", k -> 5)) 
            .test(MapMethod.COMPUTE_IF_ABSENT, map -> map.computeIfAbsent("a", k -> 5)) 
            .test(MapMethod.COMPUTE_IF_PRESENT, map -> map.computeIfPresent("a", (k, v) -> v + 1)) 
            .test(MapMethod.COMPUTE_IF_PRESENT, map -> map.computeIfPresent("e", (k, v) -> v + 1));
    }
}
