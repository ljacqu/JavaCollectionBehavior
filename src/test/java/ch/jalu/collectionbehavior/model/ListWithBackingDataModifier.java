package ch.jalu.collectionbehavior.model;

import java.util.List;

/**
 * Contains a list and a callback to modify the backing structure (collection or array) to verify whether the
 * list changes.
 *
 * @param list the list
 * @param backingDataModifier changes the backing data based on which the list was created
 */
public record ListWithBackingDataModifier(List<String> list, Runnable backingDataModifier) {

    public void runBackingDataModifier() {
        backingDataModifier.run();
    }
}
