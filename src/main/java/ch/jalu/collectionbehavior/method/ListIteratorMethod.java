package ch.jalu.collectionbehavior.method;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static ch.jalu.collectionbehavior.method.MethodCallProperty.NULL_ARGUMENT;

public class ListIteratorMethod {

    public static List<ListIteratorMethodCall> createAll() {
        return Stream.of(
                set(),
                remove(),
                add()
            )
            .flatMap(Function.identity())
            .toList();
    }

    private static Stream<ListIteratorMethodCall> set() {
        return Stream.of(
            new ListIteratorMethodCall(li -> li.set("a")),
            new ListIteratorMethodCall(li -> li.set("T")),
            new ListIteratorMethodCall(li -> li.set(null), NULL_ARGUMENT)
        );
    }

    private static Stream<ListIteratorMethodCall> remove() {
        return Stream.of(
            new ListIteratorMethodCall(li -> li.remove())
        );
    }

    private static Stream<ListIteratorMethodCall> add() {
        return Stream.of(
            new ListIteratorMethodCall(li -> li.add("o")),
            new ListIteratorMethodCall(li -> li.add(null), NULL_ARGUMENT)
        );
    }
}
