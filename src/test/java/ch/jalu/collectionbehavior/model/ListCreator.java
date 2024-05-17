package ch.jalu.collectionbehavior.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Callback to create a specific type of List that is tested.
 */
public abstract sealed class ListCreator {

    /**
     * @return New list of the specific List type. The returned list has the minimum number
     *         of entries supported by the type.
     */
    public List<String> createList() {
        return switch (this) {
            case MutableListCreator mlc -> mlc.newList();
            case ArrayBasedListCreator abc -> abc.newList();
            case ListBasedListCreator cbc -> cbc.newList(Collections.emptyList());
            case StreamBasedListCreator sbc -> sbc.newList();
            case EmptyListCreator elc -> elc.newList();
            case SingleElementListCreator sec -> sec.newList("a");
        };
    }

    /**
     * Creates a list that includes a null element. This method throws an exception if the
     * list type does not support null elements. An exception is also thrown if this method
     * is called on a creator for an empty list type ({@link EmptyListCreator}).
     *
     * @return list containing {@code null}
     */
    public List<String> createListWithNull() {
        return switch (this) {
            case MutableListCreator mlc -> mlc.newList((String) null);
            case ArrayBasedListCreator abc -> abc.newList((String) null);
            case ListBasedListCreator cbc -> cbc.newList(Collections.singletonList(null));
            case StreamBasedListCreator sbc -> sbc.newList((String) null);
            case EmptyListCreator elc -> throw new UnsupportedOperationException();
            case SingleElementListCreator sec -> sec.newList(null);
        };
    }

    /**
     * Creates a new list with the string entries {@code "a", "b", "c", "d"}. If the list cannot hold as many
     * arguments, the biggest possible subset is returned (cf. {@link #getSizeLimit()}).
     *
     * @return list with entries "a", "b", "c", "d" or the largest subset supported by the list type
     */
    public List<String> createListWithAbcdOrSubset() {
        String[] args = {"a", "b", "c", "d"};
        return switch (this) {
            case MutableListCreator mlc -> mlc.newList(args);
            case ArrayBasedListCreator abc -> abc.newList(args);
            case ListBasedListCreator cbc -> cbc.newList(Arrays.asList(args));
            case StreamBasedListCreator sbc -> sbc.newList(args);
            case EmptyListCreator elc -> elc.newList();
            case SingleElementListCreator sec -> sec.newList(args[0]);
        };
    }

    /**
     * Creates a new list wrapped in a {@link ListWithBackingDataModifier}. This is used to test for immutability
     * (or the opposite): if a list type wraps another structure (array or collection), the result of this method
     * allows to change the backing structure to see if the change is reflected in the list.
     * <p>
     * An empty Optional is returned if the list type of this creator is not based on another structure
     * (array or collection).
     *
     * @param args the arguments the list should contain
     * @return optional with the new list and its backing structure, if applicable; empty optional otherwise
     */
    public abstract Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String... args);

    /**
     * @return the maximum number of elements the list type created by this creator can contain
     */
    public int getSizeLimit() {
        return Integer.MAX_VALUE;
    }


    // -----------
    // Creation methods
    // -----------

    /**
     * Creates a list creator based on a mutable List type's constructor.
     *
     * @param constructor the constructor of the mutable List type
     * @return list creator
     */
    public static ListCreator forMutableType(Supplier<List<String>> constructor) {
        return new MutableListCreator(constructor);
    }

    /**
     * Creates a list creator based on a method that creates a list from a given array.
     *
     * @param callback method that takes an array and returns a list
     * @return list creator
     */
    public static ListCreator forArrayBasedType(Function<String[], List<String>> callback) {
        return new ArrayBasedListCreator(callback);
    }

    /**
     * Creates a list creator based on a method that copies or wraps another list.
     *
     * @param callback method that takes a list to return a list
     * @return list creator
     */
    public static ListCreator forListBasedType(Function<List<String>, List<String>> callback) {
        return new ListBasedListCreator(callback);
    }

    /**
     * Creates a list creator based on a method that collects a stream to a list.
     *
     * @param callback method that creates a list based on a stream
     * @return list creator
     */
    public static ListCreator fromStream(Function<Stream<String>, List<String>> callback) {
        return new StreamBasedListCreator(callback);
    }

    /**
     * Creates a list creator based on a method that creates a specialized single-element list type.
     *
     * @param callback method with single element as argument
     * @return list creator
     */
    public static ListCreator forSingleElement(Function<String, List<String>> callback) {
        return new SingleElementListCreator(callback);
    }

    /**
     * Creates a list creator based on a method that creates a specialized empty list type.
     *
     * @param callback method returning an empty list object
     * @return list creator
     */
    public static ListCreator forEmptyList(Supplier<List<String>> callback) {
        return new EmptyListCreator(callback);
    }


    // -----------
    // Implementations
    // -----------

    /** Implementation for mutable List types. */
    private static final class MutableListCreator extends ListCreator {

        private final Supplier<List<String>> callback;

        MutableListCreator(Supplier<List<String>> callback) {
            this.callback = callback;
        }

        List<String> newList(String... args) {
            List<String> list = callback.get();
            list.addAll(Arrays.asList(args));
            return list;
        }

        @Override
        public Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String... args) {
            throw new UnsupportedOperationException();
        }
    }

    /** Implementation for array-based List creation, e.g. {@link Arrays#asList}. */
    private static final class ArrayBasedListCreator extends ListCreator {

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

    /** Implementation for list creations based on another list (by wrapping or copying it). */
    public static final class ListBasedListCreator extends ListCreator {

        private final Function<List<String>, List<String>> callback;

        ListBasedListCreator(Function<List<String>, List<String>> callback) {
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

    /** Implementation for list creation based on a stream. */
    private static final class StreamBasedListCreator extends ListCreator {

        private final Function<Stream<String>, List<String>> callback;

        StreamBasedListCreator(Function<Stream<String>, List<String>> callback) {
            this.callback = callback;
        }

        List<String> newList(String... args) {
            return callback.apply(Arrays.stream(args));
        }

        @Override
        public Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }
    }

    /** Implementation for the list creation of a specialized single-element list type. */
    private static final class SingleElementListCreator extends ListCreator {

        private final Function<String, List<String>> callback;

        SingleElementListCreator(Function<String, List<String>> callback) {
            this.callback = callback;
        }

        List<String> newList(String elem) {
            return callback.apply(elem);
        }

        @Override
        public Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }

        @Override
        public int getSizeLimit() {
            return 1;
        }
    }

    /** Implementation for the list creation of a specialized empty list type. */
    private static final class EmptyListCreator extends ListCreator {

        private final Supplier<List<String>> callback;

        EmptyListCreator(Supplier<List<String>> callback) {
            this.callback = callback;
        }

        List<String> newList() {
            return callback.get();
        }

        @Override
        public Optional<ListWithBackingDataModifier> createListWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }

        @Override
        public int getSizeLimit() {
            return 0;
        }
    }
}
