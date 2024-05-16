package ch.jalu.collectionbehavior.model;

import java.util.List;

public record ListWithBackingDataModifier(List<String> list, Runnable backingDataModifier) {

    public void runBackingDataModifier() {
        backingDataModifier.run();
    }
}
