package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.ModificationBehavior;

import java.util.Map;
import java.util.function.Function;

class UnmodifiableMapBehaviorTester<M extends Map<String, Integer>> extends UnmodifiableContainerBehaviorTester<M> {

    UnmodifiableMapBehaviorTester(M originalMap,
                                  Function<M, M> copyFunction,
                                  ModificationBehavior expectedBehavior) {
        super(originalMap, copyFunction, expectedBehavior);
    }

    @Override
    protected int getSize(M map) {
        return map.size();
    }
}
