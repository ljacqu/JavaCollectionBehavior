package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.ModificationBehavior;

import java.util.Map;
import java.util.function.Function;

public class UnmodifiableMapBehaviorTester extends UnmodifiableContainerBehaviorTester<Map<String, Integer>> {

    UnmodifiableMapBehaviorTester(Map<String, Integer> originalMap,
                                  Function<Map<String, Integer>, Map<String, Integer>> copyFunction,
                                  ModificationBehavior expectedBehavior) {
        super(originalMap, copyFunction, expectedBehavior);
    }

    @Override
    protected int getSize(Map<String, Integer> map) {
        return map.size();
    }
}
