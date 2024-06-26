package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.ModificationBehavior;

import java.util.Collection;
import java.util.function.Function;

class UnmodifiableBehaviorTester<C extends Collection<?>> extends UnmodifiableContainerBehaviorTester<C> {

    UnmodifiableBehaviorTester(C originalList, Function<C, C> copyFunction, ModificationBehavior expectedBehavior) {
        this(originalList, copyFunction, expectedBehavior, false);
    }

    UnmodifiableBehaviorTester(C originalList, Function<C, C> copyFunction, ModificationBehavior expectedBehavior,
                               boolean copyOriginalForEqualsCheck) {
        super(originalList, copyFunction, expectedBehavior, copyOriginalForEqualsCheck);
    }

    @Override
    protected int getSize(C collection) {
        return collection.size();
    }
}
