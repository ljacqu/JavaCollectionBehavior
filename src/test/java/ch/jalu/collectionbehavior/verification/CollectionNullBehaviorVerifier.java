package ch.jalu.collectionbehavior.verification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Util for verifying a collection's behavior with null elements and null method arguments.
 */
public final class CollectionNullBehaviorVerifier {

    private CollectionNullBehaviorVerifier() {
    }

    /**
     * Verifies that null can be supplied as argument to all methods that do not modify the collection.
     *
     * @param list the list to test (may not contain null as entry)
     */
    public static void verifySupportsNullArgInMethods(List<String> list) {
        assertThat(list.contains(null), equalTo(false));
        assertThat(list.indexOf(null), equalTo(-1));
        assertThat(list.lastIndexOf(null), equalTo(-1));

        List<String> listWithNull = Collections.singletonList(null);
        assertThat(list.containsAll(listWithNull), equalTo(false));
    }

    /**
     * Verifies that null can be supplied as argument to all methods that do not modify the collection.
     *
     * @param set the set to test (may not contain null as entry)
     */
    public static void verifySupportsNullArgInMethods(Set<String> set) {
        assertThat(set.contains(null), equalTo(false));

        List<Integer> listWithNull = Collections.singletonList(null);
        assertThat(set.containsAll(listWithNull), equalTo(false));
    }

    /**
     * Verifies that a NullPointerException is thrown by all methods that don't modify the collection
     * if null is supplied as argument.
     *
     * @param list the list to test
     */
    public static void verifyRejectsNullArgInMethods(List<String> list) {
        assertThrows(NullPointerException.class, () -> list.contains(null));
        assertThrows(NullPointerException.class, () -> list.indexOf(null));
        assertThrows(NullPointerException.class, () -> list.lastIndexOf(null));

        List<String> listWithNull = Collections.singletonList(null);
        assertThrows(NullPointerException.class, () -> list.containsAll(listWithNull));

        // Exception: if the collection knows it doesn't contain everything due to an element preceding null,
        // no exception will be thrown
        assertThat(list.containsAll(Arrays.asList("qqqq", null)), equalTo(false));
    }

    /**
     * Verifies that a NullPointerException is thrown by all methods that don't modify the collection
     * if null is supplied as argument.
     *
     * @param set the set to test
     */
    public static void verifyRejectsNullArgInMethods(Set<String> set) {
        assertThrows(NullPointerException.class, () -> set.contains(null));

        List<String> listWithNull = Collections.singletonList(null);
        assertThrows(NullPointerException.class, () -> set.containsAll(listWithNull));

        // Exception: if the collection knows it doesn't contain everything due to an element preceding null,
        // no exception will be thrown
        assertThat(set.containsAll(Arrays.asList("qqqq", null)), equalTo(false));
    }
}
