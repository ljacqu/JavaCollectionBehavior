package ch.jalu.collectionbehavior.verification;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Util for verifying a map's behavior with null elements and null method arguments.
 */
public final class MapNullBehaviorVerifier {

    private MapNullBehaviorVerifier() {
    }

    /**
     * Verifies that null can be supplied as argument to all methods that do not modify the map.
     *
     * @param map the map to test (may not contain null as key or value)
     */
    public static void verifySupportsNullArgInMethodsForKeys(Map<String, Integer> map) {
        assertThat(map.containsKey(null), equalTo(false));
    }

    /**
     * Verifies that null can be supplied as argument to all methods that do not modify the map.
     *
     * @param map the map to test (may not contain null as key or value)
     */
    public static void verifySupportsNullArgInMethodsForValues(Map<String, Integer> map) {
        assertThat(map.containsKey(null), equalTo(false));
    }

    /**
     * Verifies that a NullPointerException is thrown by all methods that don't modify the map
     * if null is supplied as argument.
     *
     * @param map the map to test
     */
    public static void verifyRejectsNullArgInMethodsForKeys(Map<String, Integer> map) {
        assertThrows(NullPointerException.class, () -> map.containsKey(null));
    }

    /**
     * Verifies that a NullPointerException is thrown by all methods that don't modify the map
     * if null is supplied as argument.
     *
     * @param map the map to test
     */
    public static void verifyRejectsNullArgInMethodsForValues(Map<String, Integer> map) {
        assertThrows(NullPointerException.class, () -> map.containsValue(null));
    }
}
