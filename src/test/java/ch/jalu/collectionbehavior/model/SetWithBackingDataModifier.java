package ch.jalu.collectionbehavior.model;

import java.util.Set;

/**
 * Contains a set and a callback to modify the backing structure (collection or array) to verify whether the
 * set changes.
 *
 * @param set the set
 * @param backingDataModifier changes the backing data based on which the set was created
 */
public record SetWithBackingDataModifier(Set<String> set, Runnable backingDataModifier) {

    public void runBackingDataModifier() {
        backingDataModifier.run();
    }
}
