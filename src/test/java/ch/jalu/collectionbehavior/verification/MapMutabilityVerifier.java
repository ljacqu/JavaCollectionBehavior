package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.MapWithBackingDataModifier;

import java.util.Map;

import static ch.jalu.collectionbehavior.model.MapCreator.A_VALUE;
import static ch.jalu.collectionbehavior.model.MapCreator.B_VALUE;
import static ch.jalu.collectionbehavior.model.MapCreator.C_VALUE;
import static ch.jalu.collectionbehavior.model.MapCreator.D_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;

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

    // TODO keySet, values, entrySet

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
