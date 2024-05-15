package ch.jalu.collectionbehavior.model;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ListCreator extends Function<String[], List<String>> {

    @Override
    List<String> apply(String... args);

    static ListCreator fromSupplier(Supplier<List<String>> supplier) {
        return args -> {
            List<String> list = supplier.get();
            list.addAll(Arrays.asList(args));
            return list;
        };
    }

    static ListCreator fromMethod(Function<String[], List<String>> method) {
        return method::apply;
    }

    static ListCreator fromCopyMethod(Function<List<String>, List<String>> method) {
        return args -> method.apply(Arrays.asList(args));
    }
}
