package ch.jalu.collectionbehavior.documentation;

import java.util.Arrays;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;

/**
 * Represents spliterator characteristics.
 */
public enum SpliteratorCharacteristic {

    ORDERED(Spliterator.ORDERED),
    DISTINCT(Spliterator.DISTINCT),
    SORTED(Spliterator.SORTED),
    SIZED(Spliterator.SIZED),
    NONNULL(Spliterator.NONNULL),
    IMMUTABLE(Spliterator.IMMUTABLE),
    CONCURRENT(Spliterator.CONCURRENT),
    SUBSIZED(Spliterator.SUBSIZED);

    private final int value;

    SpliteratorCharacteristic(int value) {
        this.value = value;
    }

    /**
     * Returns a set of all characteristics as defined by the given value.
     *
     * @param value the spliterator characteristics value
     * @return set of characteristics included in the number
     */
    public static Set<SpliteratorCharacteristic> create(int value) {
        return Arrays.stream(values())
            .filter(ch -> (ch.value & value) == ch.value)
            .collect(Collectors.toUnmodifiableSet());
    }
}
