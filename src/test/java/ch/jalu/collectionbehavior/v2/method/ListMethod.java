package ch.jalu.collectionbehavior.v2.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static ch.jalu.collectionbehavior.v2.method.MethodCallProperty.EMPTY_COLLECTION_ARGUMENT;
import static ch.jalu.collectionbehavior.v2.method.MethodCallProperty.NULL_ARGUMENT;

public class ListMethod {

    public static List<ListMethodCall> createAll() {
        return Stream.of(
                add(),
                addWithIndex(),
                addFirst(),
                addLast(),
                addAll(),
                addAllWithIndex(),
                clear(),
                contains(),
                containsAll(),
                indexOf(),
                lastIndexOf(),
                remove(),
                removeWithIndex(),
                removeFirst(),
                removeLast(),
                removeAll(),
                removeIf(),
                retainAll(),
                replaceAll(),
                set(),
                sort()
            )
            .flatMap(Function.identity())
            .toList();
    }

    private static Stream<ListMethodCall> add() {
        return Stream.of(
            new ListMethodCall(list -> list.add("f")),
            new ListMethodCall(list -> list.add(null), NULL_ARGUMENT)
        );
    }

    private static Stream<ListMethodCall> addWithIndex() {
        return Stream.of(
            new ListMethodCall(list -> list.add(1, "G")),
            new ListMethodCall(list -> list.add(2, null), NULL_ARGUMENT),
            new ListMethodCall(list -> list.add(17, "G")),
            new ListMethodCall(list -> list.add(17, null), NULL_ARGUMENT)
        );
    }

    private static Stream<ListMethodCall> addFirst() {
        return Stream.of(
            new ListMethodCall(list -> list.addFirst("o")),
            new ListMethodCall(list -> list.addFirst(null), NULL_ARGUMENT)
        );
    }

    private static Stream<ListMethodCall> addLast() {
        return Stream.of(
            new ListMethodCall(list -> list.addLast("o")),
            new ListMethodCall(list -> list.addLast(null), NULL_ARGUMENT)
        );
    }

    private static Stream<ListMethodCall> addAll() {
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add(null);

        return Stream.of(
            new ListMethodCall(list -> list.addAll(List.of("f", "g"))),
            new ListMethodCall(list -> list.addAll(Collections.emptyList()), EMPTY_COLLECTION_ARGUMENT),
            new ListMethodCall(list -> list.addAll(listWithNull), NULL_ARGUMENT)
        );
    }

    private static Stream<ListMethodCall> addAllWithIndex() {
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add(null);

        return Stream.of(
            new ListMethodCall(list -> list.addAll(2, List.of("f", "g"))),
            new ListMethodCall(list -> list.addAll(2, Collections.emptyList()), EMPTY_COLLECTION_ARGUMENT),
            new ListMethodCall(list -> list.addAll(2, listWithNull), NULL_ARGUMENT),
            new ListMethodCall(list -> list.addAll(17, List.of("f", "g"))),
            new ListMethodCall(list -> list.addAll(17, listWithNull), NULL_ARGUMENT)
        );
    }

    private static Stream<ListMethodCall> clear() {
        return Stream.of(
            new ListMethodCall(list -> list.clear())
        );
    }

    private static Stream<ListMethodCall> contains() {
        return Stream.of(
            new ListMethodCall(list -> list.contains(null), NULL_ARGUMENT)
        );
    }

    private static Stream<ListMethodCall> containsAll() {
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add("a");
        listWithNull.add(null);

        List<String> listWithNull2 = new ArrayList<>();
        listWithNull2.add("Q");
        listWithNull2.add(null);

        return Stream.of(
            new ListMethodCall(list -> list.containsAll(listWithNull), NULL_ARGUMENT),
            new ListMethodCall(list -> list.containsAll(listWithNull2), NULL_ARGUMENT)
        );
    }

    private static Stream<ListMethodCall> indexOf() {
        return Stream.of(
            new ListMethodCall(list -> list.indexOf(null))
        );
    }

    private static Stream<ListMethodCall> lastIndexOf() {
        return Stream.of(
            new ListMethodCall(list -> list.lastIndexOf(null))
        );
    }

    private static Stream<ListMethodCall> remove() {
        return Stream.of(
            new ListMethodCall(list -> list.remove("a")),
            new ListMethodCall(list -> list.remove("zzz")),
            new ListMethodCall(list -> list.remove(null))
        );
    }

    private static Stream<ListMethodCall> removeWithIndex() {
        return Stream.of(
            new ListMethodCall(list -> list.remove(0)),
            new ListMethodCall(list -> list.remove(17))
        );
    }

    private static Stream<ListMethodCall> removeFirst() {
        return Stream.of(
            new ListMethodCall(list -> list.removeFirst())
        );
    }

    private static Stream<ListMethodCall> removeLast() {
        return Stream.of(
            new ListMethodCall(list -> list.removeLast())
        );
    }

    private static Stream<ListMethodCall> removeAll() {
        return Stream.of(
            new ListMethodCall(list -> list.removeAll(List.of("a", "b"))),
            new ListMethodCall(list -> list.removeAll(List.of("zzz"))),
            new ListMethodCall(list -> list.removeAll(Arrays.asList("a", null)), NULL_ARGUMENT),
            new ListMethodCall(list -> list.removeAll(Arrays.asList((String) null)), NULL_ARGUMENT)
        );
    }

    private static Stream<ListMethodCall> removeIf() {
        return Stream.of(
            new ListMethodCall(list -> list.removeIf(new PrettyPredicate<>(e -> true, "e -> true"))),
            new ListMethodCall(list -> list.removeIf(new PrettyPredicate<>(e -> false, "e -> false")))
        );
    }

    private static Stream<ListMethodCall> retainAll() {
        return Stream.of(
            new ListMethodCall(list -> list.retainAll(List.of("b", "d"))),
            new ListMethodCall(list -> list.retainAll(List.of("a", "b", "c", "d"))),
            new ListMethodCall(list -> list.retainAll(Arrays.asList("a", null)), NULL_ARGUMENT),
            new ListMethodCall(list -> list.retainAll(Arrays.asList((String) null)), NULL_ARGUMENT)
        );
    }

    private static Stream<ListMethodCall> replaceAll() {
        return Stream.of(
            new ListMethodCall(list -> list.replaceAll(new PrettyUnaryOperator<>(e -> e, "e -> e"))),
            new ListMethodCall(list -> list.replaceAll(new PrettyUnaryOperator<>(String::toUpperCase, "String::toUpperCase"))),
            new ListMethodCall(list -> list.replaceAll(new PrettyUnaryOperator<>(e -> null, "e -> null")), NULL_ARGUMENT)
        );
    }

    private static Stream<ListMethodCall> set() {
        return Stream.of(
            new ListMethodCall(list -> list.set(0, "f")),
            new ListMethodCall(list -> list.set(0, "a")),
            new ListMethodCall(list -> list.set(0, null), NULL_ARGUMENT),
            new ListMethodCall(list -> list.set(17, "G")),
            new ListMethodCall(list -> list.set(17, null))
        );
    }

    private static Stream<ListMethodCall> sort() {
        return Stream.of(
            new ListMethodCall(list -> list.sort(new PrettyComparator<String>(Comparator.naturalOrder(), "Comparator.naturalOrder()"))),
            new ListMethodCall(list -> list.sort(new PrettyComparator<>(Comparator.<String>naturalOrder().reversed(), "Comparator.reversed")))
        );
    }
}
