package ch.jalu.collectionbehavior.method;

import java.util.ListIterator;
import java.util.Set;
import java.util.function.Consumer;

public record ListIteratorMethodCall(Consumer<ListIterator<String>> call,
                                     Set<MethodCallProperty> properties) implements MethodCall<ListIterator<String>> {

    public ListIteratorMethodCall(Consumer<ListIterator<String>> call, MethodCallProperty... properties) {
        this(call, Set.of(properties));
    }

    @Override
    public void invoke(ListIterator<String> list) {
        call.accept(list);
    }
}
