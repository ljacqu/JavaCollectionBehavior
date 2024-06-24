package ch.jalu.collectionbehavior.model;

import java.util.Map;

/**
 * Contains a map and a callback to modify the backing structure to verify whether the map changes.
 *
 * @param map the map
 * @param backingDataModifier changes the backing data based on which the map was created
 */
public record MapWithBackingDataModifier(Map<String, Integer> map, Runnable backingDataModifier) {

    public void runBackingDataModifier() {
        backingDataModifier.run();
    }
}
