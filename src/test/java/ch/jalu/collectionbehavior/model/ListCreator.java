package ch.jalu.collectionbehavior.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;

public abstract sealed class ListCreator {

    public List<String> createList() {
        return switch (this) {
            case MutableListCreator mlc -> mlc.newList();
            case ArrayBasedListCreator abc -> abc.newList();
            case CopyBasedListCreator cbc -> cbc.newList(Collections.emptyList());
            case StreamBasedListCreator sbc -> sbc.newList();
            case EmptyListCreator elc -> elc.newList();
            case SingleElementListCreator sec -> sec.newList("a");
        };
    }

    public List<String> listWithNull() {
        return switch (this) {
            case MutableListCreator mlc -> mlc.newList((String) null);
            case ArrayBasedListCreator abc -> abc.newList((String) null);
            case CopyBasedListCreator cbc -> cbc.newList(Collections.singletonList(null));
            case StreamBasedListCreator sbc -> sbc.newList((String) null);
            case EmptyListCreator elc -> throw new UnsupportedOperationException();
            case SingleElementListCreator sec -> sec.newList(null);
        };
    }

    public List<String> createListWithAbcdOrSubset() {
        String[] args = {"a", "b", "c", "d"};
        return switch (this) {
            case MutableListCreator mlc -> mlc.newList(args);
            case ArrayBasedListCreator abc -> abc.newList(args);
            case CopyBasedListCreator cbc -> cbc.newList(Arrays.asList(args));
            case StreamBasedListCreator sbc -> sbc.newList(args);
            case EmptyListCreator elc -> elc.newList();
            case SingleElementListCreator sec -> sec.newList(args[0]);
        };
    }

    public static ListCreator forMutableType(Supplier<List<String>> constructor) {
        return new MutableListCreator(constructor);
    }

    public static ListCreator forArrayBasedType(Function<String[], List<String>> callback) {
        return new ArrayBasedListCreator(callback);
    }

    public static ListCreator forListBasedType(Function<List<String>, List<String>> callback) {
        return new CopyBasedListCreator(callback);
    }

    public static ListCreator fromStream(Function<Stream<String>, List<String>> callback) {
        return new StreamBasedListCreator(callback);
    }

    public static ListCreator forSingleElement(Function<String, List<String>> callback) {
        return new SingleElementListCreator(callback);
    }

    public static ListCreator forEmptyList(Supplier<List<String>> callback) {
        return new EmptyListCreator(callback);
    }

    public abstract Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String... args);

    static final class MutableListCreator extends ListCreator {

        private final Supplier<List<String>> callback;

        MutableListCreator(Supplier<List<String>> callback) {
            this.callback = callback;
        }

        public List<String> newList(String... args) {
            List<String> list = callback.get();
            list.addAll(Arrays.asList(args));
            return list;
        }

        @Override
        public Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String... args) {
            throw new UnsupportedOperationException();
        }
    }

    static final class ArrayBasedListCreator extends ListCreator {

        private final Function<String[], List<String>> callback;

        ArrayBasedListCreator(Function<String[], List<String>> callback) {
            this.callback = callback;
        }

        List<String> newList(String... args) {
            return callback.apply(args);
        }

        @Override
        public Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String[] args) {
            List<String> list = newList(args);
            return Optional.of(new ListWithBackingDataModifier(list, () -> args[1] = "changed"));
        }
    }

    public static final class CopyBasedListCreator extends ListCreator {

        private final Function<List<String>, List<String>> callback;

        CopyBasedListCreator(Function<List<String>, List<String>> callback) {
            this.callback = callback;
        }

        public List<String> newList(List<String> args) {
            return callback.apply(args);
        }

        @Override
        public Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String[] args) {
            List<String> backingList = newArrayList(args);
            List<String> list = newList(backingList);
            return Optional.of(new ListWithBackingDataModifier(list, () -> backingList.set(1, "changed")));
        }
    }

    static final class StreamBasedListCreator extends ListCreator {

        private final Function<Stream<String>, List<String>> callback;

        StreamBasedListCreator(Function<Stream<String>, List<String>> callback) {
            this.callback = callback;
        }

        public List<String> newList(String... args) {
            return callback.apply(Arrays.stream(args));
        }

        @Override
        public Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }
    }

    static final class SingleElementListCreator extends ListCreator {

        private final Function<String, List<String>> callback;

        SingleElementListCreator(Function<String, List<String>> callback) {
            this.callback = callback;
        }

        public List<String> newList(String elem) {
            return callback.apply(elem);
        }

        @Override
        public Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }
    }

    static final class EmptyListCreator extends ListCreator {

        private final Supplier<List<String>> callback;

        EmptyListCreator(Supplier<List<String>> callback) {
            this.callback = callback;
        }

        public List<String> newList() {
            return callback.get();
        }

        @Override
        public Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }
    }
}
