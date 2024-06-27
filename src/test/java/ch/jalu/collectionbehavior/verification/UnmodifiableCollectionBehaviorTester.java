package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.ModificationBehavior;

import java.util.Collection;
import java.util.function.Function;

class UnmodifiableCollectionBehaviorTester<C extends Collection<?>> extends UnmodifiableContainerBehaviorTester<C> {

    UnmodifiableCollectionBehaviorTester(C collection, Function<C, C> copyFunction,
                                         ModificationBehavior expectedBehavior) {
        this(collection, copyFunction, expectedBehavior, false);
    }

    UnmodifiableCollectionBehaviorTester(C collection, Function<C, C> copyFunction,
                                         ModificationBehavior expectedBehavior,
                                         boolean copyOriginalForEqualsCheck) {
        super(collection, copyFunction, expectedBehavior, copyOriginalForEqualsCheck);
    }

    @Override
    protected int getSize(C collection) {
        return collection.size();
    }
}
