package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.ListMethod;
import ch.jalu.collectionbehavior.model.ModificationBehavior;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class ListModificationVerifier {

    private ListModificationVerifier() {
    }

    public static void testMethods(List<String> originalList, ModificationBehavior expectedBehavior) {
        new UnmodifiableCollectionBehaviorTester<>(originalList, ArrayList::new, expectedBehavior)
            .test(ListMethod.ADD, list -> list.add("foo"))
            .test(ListMethod.ADD_WITH_INDEX, list -> list.add(1, "foo"))
            .test(ListMethod.ADD_WITH_INDEX, list -> list.add(5, "foo"))
            .test(ListMethod.ADD_ALL, list -> list.addAll(List.of("foo", "bar")))
            .test(ListMethod.ADD_FIRST, list -> list.addFirst("foo"))
            .test(ListMethod.ADD_LAST, list -> list.addLast("foo"))
            .test(ListMethod.REMOVE, list -> list.remove("zzz"))
            .test(ListMethod.REMOVE, list -> list.remove("a"))
            .test(ListMethod.REMOVE_INDEX, list -> list.remove(0))
            .test(ListMethod.REMOVE_INDEX, list -> list.remove(5))
            .test(ListMethod.REMOVE_IF, list -> list.removeIf(str -> str.equals("zzz")))
            .test(ListMethod.REMOVE_IF, list -> list.removeIf(str -> str.equals("a")))
            .test(ListMethod.REMOVE_ALL, list -> list.removeAll(Set.of("fff", "xxx")))
            .test(ListMethod.REMOVE_ALL, list -> list.removeAll(Set.of("fff", "a")))
            .test(ListMethod.REMOVE_FIRST, list -> list.removeFirst())
            .test(ListMethod.REMOVE_LAST, list -> list.removeLast())
            .test(ListMethod.SET, list -> list.set(0, "d"))
            .test(ListMethod.SET, list -> list.set(0, "a"))
            .test(ListMethod.SET, list -> list.set(5, "foo"))
            .test(ListMethod.RETAIN_ALL, list -> list.retainAll(Set.of("a")))
            .test(ListMethod.RETAIN_ALL, list -> list.retainAll(originalList))
            .test(ListMethod.REPLACE_ALL, list -> list.replaceAll(s -> s))
            .test(ListMethod.REPLACE_ALL, list -> list.replaceAll(String::toUpperCase))
            .test(ListMethod.SORT, list -> list.sort(Comparator.naturalOrder()))
            .test(ListMethod.SORT, list -> list.sort(Comparator.<String>naturalOrder().reversed()))
            .test(ListMethod.CLEAR, list -> list.clear());
    }
}
