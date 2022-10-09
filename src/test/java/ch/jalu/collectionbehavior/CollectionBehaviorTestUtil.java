package ch.jalu.collectionbehavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CollectionBehaviorTestUtil {

    private CollectionBehaviorTestUtil() {
    }

    // --------------------------
    // Mutability
    // --------------------------

    public static void verifyIsMutable(List<String> emptyList) {
        assertThat(emptyList, empty()); // Validate method contract
        List<String> list = emptyList;

        // List#add, List#addAll
        list.add("a");
        list.add("b");
        list.add("f");
        list.add(2, "c"); // a, b, c, f
        list.addAll(List.of("a", "b", "Y", "X"));
        assertThat(list, contains("a", "b", "c", "f", "a", "b", "Y", "X"));

        // List#remove, List#removeAll, List#removeIf
        list.remove("b");                      // a, c, f, a, b, Y, X
        list.remove(1);                        // a, f, a, b, Y, X
        list.removeAll(List.of("a"));          // f, b, Y, X
        list.removeIf(str -> str.equals("Y")); // f, b, X
        assertThat(list, contains("f", "b", "X"));

        // List#set(int, Object)
        list.set(1, "a");
        assertThat(list, contains("f", "a", "X"));

        // List#retainAll, List#replaceAll
        list.retainAll(Set.of("f", "a", "m"));
        list.replaceAll(String::toUpperCase);
        assertThat(list, contains("F", "A"));

        // List#sort
        list.sort(Comparator.comparing(Function.identity()));
        assertThat(list, contains("A", "F"));

        // List#clear
        list.clear();
        assertThat(list, empty());

        verifyIsMutableBySubListAndIterator(list);
    }

    private static void verifyIsMutableBySubListAndIterator(List<String> list) {
        list.add("north");
        list.add("east");
        list.add("south");
        list.add("west");

        List<String> subList = list.subList(1, 3); // east, south
        subList.remove("south"); // east
        subList.add("best"); // east, best
        subList.addAll(List.of("crest")); // east, best, crest
        subList.remove(0); // best, crest
        assertThat(list, contains("north", "best", "crest", "west"));

        subList.sort(Comparator.comparing(String::length).reversed()); // crest, best
        assertThat(list, contains("north", "crest", "best", "west"));

        subList.clear();
        assertThat(list, contains("north", "west"));

        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }

        assertThat(list, empty());
    }

    public static void verifyIsImmutable(List<String> abcdImmutableList, Runnable originModifier) {
        assertThat(abcdImmutableList, contains("a", "b", "c", "d")); // Validate method contract
        originModifier.run();
        assertThat(abcdImmutableList, contains("a", "b", "c", "d"));

        verifyListExceptionBehavior(abcdImmutableList, UnmodifiableListExceptionBehavior.ALWAYS_THROWS, false);
        verifyListExceptionBehavior(abcdImmutableList.subList(0, 3), UnmodifiableListExceptionBehavior.ALWAYS_THROWS, true);
        verifyCannotBeModifiedByIterator(abcdImmutableList);
    }

    public static void verifyIsUnmodifiable(List<String> abcdUnmodifiableList, Runnable originModifier) {
        assertThat(abcdUnmodifiableList, contains("a", "b", "c", "d")); // Validate method contract
        originModifier.run();
        assertThat(abcdUnmodifiableList, contains("a", "b", "changed", "d"));

        verifyListExceptionBehavior(abcdUnmodifiableList, UnmodifiableListExceptionBehavior.ALWAYS_THROWS, false);
        verifyListExceptionBehavior(abcdUnmodifiableList.subList(0, 3), UnmodifiableListExceptionBehavior.ALWAYS_THROWS, true);
        verifyCannotBeModifiedByIterator(abcdUnmodifiableList);
    }

    private static void verifyCannotBeModifiedByIterator(List<String> list) {
        assertThat(list.size(), greaterThanOrEqualTo(1));

        Iterator<String> iterator = list.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    public static void verifyThrowsOnlyIfListWouldBeModified(List<String> list,
                                                             UnmodifiableListExceptionBehavior exceptionBehavior) {
        if (list != Collections.<String>emptyList()) {
            assertThat(list, contains("a")); // Validate method contract
        }
        List<String> copy = new ArrayList<>(list);

        verifyListExceptionBehavior(list, exceptionBehavior, false);
        assertThat(list, equalTo(copy));

        verifyListExceptionBehavior(list.subList(0, list.size()), exceptionBehavior, true);
        assertThat(list, equalTo(copy));
    }

    private static void verifyListExceptionBehavior(List<String> listToVerify,
                                                    UnmodifiableListExceptionBehavior exceptionBehavior,
                                                    boolean isSubList) {
        Class<? extends Exception> removeIfExOverride = null;
        Class<? extends Exception> replaceAllExOverride = null;
        Class<? extends Exception> sortExOverride = null;
        if (!isSubList) {
            removeIfExOverride = exceptionBehavior.getNonModifyingRemoveIfExceptionOverride();
            replaceAllExOverride = exceptionBehavior.getNonModifyingReplaceAllExceptionOverride();
        } else {
            replaceAllExOverride = exceptionBehavior.getNonModifyingReplaceAllSubListExOverride();
            sortExOverride = exceptionBehavior.getSortSubListExOverride();
        }

        ThrowingBehavior throwingBehavior = switch (exceptionBehavior) {
            case ALWAYS_THROWS ->
                ThrowingBehavior.ALWAYS_THROWS;
            case COLLECTIONS_SINGLETONLIST, COLLECTIONS_EMPTYLIST ->
                isSubList
                    ? ThrowingBehavior.THROW_INDEX_OUT_OF_BOUNDS_OR_IF_CHANGE
                    : ThrowingBehavior.THROW_ONLY_IF_CHANGE;
        };

        new ListVerifier(listToVerify, throwingBehavior)
            .test(list -> list.add("foo"))
            .test(list -> list.add(1, "foo"))
            .test(list -> list.add(3, "foo"))
            .test(list -> list.addAll(List.of("foo", "bar")))
            .test(list -> list.remove("zzz"))
            .test(list -> list.remove("a"))
            .test(list -> list.remove(0))
            .test(list -> list.remove(3))
            .test(list -> list.removeIf(str -> str.equals("zzz")), removeIfExOverride)
            .test(list -> list.removeIf(str -> str.equals("a")), removeIfExOverride)
            .test(list -> list.removeAll(Set.of("fff", "xxx")))
            .test(list -> list.removeAll(Set.of("fff", "a")))
            .test(list -> list.set(0, "foo"))
            .test(list -> list.set(3, "foo"))
            .test(list -> list.retainAll(Set.of("a")))
            .test(list -> list.retainAll(Set.of("qqq")))
            .test(list -> list.replaceAll(s -> s), replaceAllExOverride)
            .test(list -> list.replaceAll(String::toUpperCase))
            .test(list -> list.sort(Comparator.comparing(Function.identity())), sortExOverride)
            .test(list -> list.clear());
    }

    // --------------------------
    // Null support
    // --------------------------

    public static void verifySupportsNullArgInMethods(List<String> list) {
        assertThat(list.contains(null), equalTo(false));
        assertThat(list.indexOf(null), equalTo(-1));
        assertThat(list.lastIndexOf(null), equalTo(-1));

        List<String> listWithNull = Arrays.asList((String) null);
        assertThat(list.containsAll(listWithNull), equalTo(false));
    }

    public static void verifyRejectsNullArgInMethods(List<String> list) {
        assertThrows(NullPointerException.class, () -> list.contains(null));
        assertThrows(NullPointerException.class, () -> list.indexOf(null));
        assertThrows(NullPointerException.class, () -> list.lastIndexOf(null));

        List<String> listWithNull = Arrays.asList((String) null);
        assertThrows(NullPointerException.class, () -> list.containsAll(listWithNull));
        // todo: interesting to note that Arrays.asList("test", null); will not produce an NPE because "test" was
        // already evaluated to false -> should document this in the future
    }

    // --------------------------
    // Helpers
    // --------------------------


    private static final class ListVerifier {

        private final List<String> originalList;
        private final ThrowingBehavior throwingBehavior;

        private ListVerifier(List<String> originalList, ThrowingBehavior throwingBehavior) {
            this.originalList = originalList;
            this.throwingBehavior = throwingBehavior;
        }

        ListVerifier test(Consumer<List<String>> action) {
            return test(action, null);
        }

        ListVerifier test(Consumer<List<String>> action, Class<? extends Exception> expectedExceptionType) {
            Class<? extends Exception> expectedException = expectedExceptionType;
            if (expectedException == null) {
                expectedException = getExpectedExceptionType(action);
            }

            if (expectedException != null) {
                assertThrows(expectedException, () -> action.accept(originalList));
            } else {
                action.accept(originalList);
            }
            return this;
        }

        private Class<? extends Exception> getExpectedExceptionType(Consumer<List<String>> action) {
            if (this.throwingBehavior == ThrowingBehavior.ALWAYS_THROWS) {
                return UnsupportedOperationException.class;
            }

            List<String> copy = new ArrayList<>(originalList);
            try {
                action.accept(copy);
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                return throwingBehavior == ThrowingBehavior.THROW_INDEX_OUT_OF_BOUNDS_OR_IF_CHANGE
                    ? IndexOutOfBoundsException.class
                    : UnsupportedOperationException.class;
            }
            return copy.equals(originalList) ? null : UnsupportedOperationException.class;
        }
    }

    private enum ThrowingBehavior {

        ALWAYS_THROWS,

        THROW_ONLY_IF_CHANGE,

        THROW_INDEX_OUT_OF_BOUNDS_OR_IF_CHANGE

    }
}
