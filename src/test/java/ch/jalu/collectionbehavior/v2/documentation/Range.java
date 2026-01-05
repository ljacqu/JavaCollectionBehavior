package ch.jalu.collectionbehavior.v2.documentation;

public record Range(int min, Integer max) {

    Range withMax(int max) {
        return new Range(min, max);
    }
}
