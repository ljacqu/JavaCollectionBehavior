package ch.jalu.collectionbehavior.method;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public record ListMethodCall(Consumer<List<String>> call,
                             Set<MethodCallProperty> properties) {

    public ListMethodCall(Consumer<List<String>> call, MethodCallProperty... properties) {
        this(call, Set.of(properties));
    }

    public void invoke(List<String> list) {
        call.accept(list);
    }
}
