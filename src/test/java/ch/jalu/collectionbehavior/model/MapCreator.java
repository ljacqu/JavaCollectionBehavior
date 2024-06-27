package ch.jalu.collectionbehavior.model;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Callback to create a specific type of Map that is tested.
 */
public abstract sealed class MapCreator {

    public static final String[] ALPHANUM_ELEMENTS_RANDOM = {
        "rA", "Xo", "J1", "lW", "B2", "iK", "c8", "yG", "Kq", "Mf", "dZ", "qv", "eP",
        "Zv", "vs", "Px", "sI", "xU", "Ia", "Ua", "Fu", "Gt", "tj", "ph", "Ns", "Sc",
        "om", "Tu", "Lp", "Ra", "wo", "jp", "am", "Qy", "nO", "Wr", "bH", "Yz", "kr",
        "fT", "hz", "Vr", "Ar", "mt", "Cd", "Dn", "Hr", "zE", "ur", "Eg", "gp", "OK"
    };

    public static final String[] ALPHANUM_ELEMENTS_SORTED = {
        "Ar", "B2", "Cd", "Dn", "Eg", "Fu", "Gt", "Hr", "Ia", "J1", "Kq", "Lp", "Mf",
        "Ns", "OK", "Px", "Qy", "Ra", "Sc", "Tu", "Ua", "Vr", "Wr", "Xo", "Yz", "Zv",
        "am", "bH", "c8", "dZ", "eP", "fT", "gp", "hz", "iK", "jp", "kr", "lW", "mt",
        "nO", "om", "ph", "qv", "rA", "sI", "tj", "ur", "vs", "wo", "xU", "yG", "zE"
    };

    public static final int A_VALUE = createValue("a"); // 97
    public static final int B_VALUE = createValue("b"); // 98
    public static final int C_VALUE = createValue("c"); // 99
    public static final int D_VALUE = createValue("d"); // 100

    private static Integer createValue(String key) {
        if ("null".equals(key)) {
            return null;
        }

        int value = 0;
        char[] charArray = key == null ? new char[]{ 'ê' } : key.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            value ^= (c << i);
        }
        return value;
    }

    private static List<Map.Entry<String, Integer>> createEntries(String... keys) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        for (String k : keys) {
            Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>(k, createValue(k));
            list.add(entry);
        }
        return list;
    }

    private static LinkedHashMap<String, Integer> createLinkedHashMap(String... keys) {
        return Arrays.stream(keys)
            .collect(Collectors.toMap(Function.identity(), MapCreator::createValue, (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * @return New map of the specific Map type. The returned map has the minimum number
     *         of entries supported by the type.
     */
    public Map<String, Integer> createMap() {
        return switch (this) {
            case MutableMapCreator mmc -> mmc.newMap();
            case MapBasedMapCreator<?> mmc -> mmc.newMap(Collections.emptyMap());
            case StreamBasedMapCreator smc -> smc.newMap();
            case EmptyMapCreator emc -> emc.newMap();
            case SingleEntryMapCreator sec -> sec.newMap("a");
        };
    }

    /**
     * Creates a map that includes a null key. This method throws an exception if the
     * map type does not support null keys. An exception is also thrown if this method
     * is called on a creator for an empty map type ({@link EmptyMapCreator}).
     *
     * @return map containing {@code null}
     */
    public Map<String, Integer> createMapWithNullKey() {
        return switch (this) {
            case MutableMapCreator mmc -> mmc.newMap((String) null);
            case MapBasedMapCreator<?> mmc -> mmc.newMap(MapCreator.createLinkedHashMap((String) null));
            case StreamBasedMapCreator smc -> smc.newMap((String) null);
            case EmptyMapCreator emc -> throw new UnsupportedOperationException();
            case SingleEntryMapCreator sec -> sec.newMap(null);
        };
    }

    /**
     * Creates a map that includes a key with a null value. This method throws an exception if the
     * map type does not support null values. An exception is also thrown if this method
     * is called on a creator for an empty map type ({@link EmptyMapCreator}).
     *
     * @return map containing {@code null}
     */
    public Map<String, Integer> createMapWithNullValue() {
        return switch (this) {
            case MutableMapCreator mmc -> mmc.newMapWithNullValue("null");
            case MapBasedMapCreator<?> mmc -> {
                Map<String, Integer> map = new HashMap<>();
                map.put("null", null);
                yield mmc.newMap(map);
            }
            case StreamBasedMapCreator smc -> smc.newMapWithNullValue("null");
            case EmptyMapCreator emc -> throw new UnsupportedOperationException();
            case SingleEntryMapCreator sec -> sec.newMapWithNullValue("null");
        };
    }

    /**
     * Creates a new map with the string keys {@code "a", "b", "c", "d"}. If the map cannot hold as many
     * arguments, the biggest possible subset is returned (cf. {@link #getSizeLimit()}).
     *
     * @return map with keys "a", "b", "c", "d" or the largest subset supported by the map type
     */
    public Map<String, Integer> createMapWithAbcdOrSubset() {
        String[] args = {"a", "b", "c", "d"};
        return switch (this) {
            case MutableMapCreator mmc -> mmc.newMap(args);
            case MapBasedMapCreator<?> mmc -> mmc.newMap(MapCreator.createLinkedHashMap(args));
            case StreamBasedMapCreator smc -> smc.newMap(args);
            case EmptyMapCreator emc -> emc.newMap();
            case SingleEntryMapCreator sec -> sec.newMap(args[0]);
        };
    }

    /**
     * Creates a map with alphanumerical pairs as keys: {@link #ALPHANUM_ELEMENTS_RANDOM}.
     * Used to determine the order of the map (needs many entries to reduce the chance that a hash map has the
     * elements in order by coincidence). Map creators that cannot hold as many entries throw an exception
     * (refer to {@link #getSizeLimit()}).
     *
     * @return map with alphanumerical pairs as keys
     */
    public Map<String, Integer> createMapWithAlphanumericalEntries() {
        String[] args = ALPHANUM_ELEMENTS_RANDOM;
        return switch (this) {
            case MutableMapCreator mmc -> mmc.newMap(args);
            case MapBasedMapCreator<?> mmc -> mmc.newMap(MapCreator.createLinkedHashMap(args));
            case StreamBasedMapCreator smc -> smc.newMap(args);
            case EmptyMapCreator emc -> throw new UnsupportedOperationException();
            case SingleEntryMapCreator sec -> throw new UnsupportedOperationException();
        };
    }

    /**
     * Initializes a map with keys "a", "b", "c", including some of these keys multiple times to test the
     * map's behavior with duplicate keys. This method throws an exception if the instantiation type
     * cannot encounter duplicates (see {@link #canEncounterDuplicateKeys()}).
     *
     * @return map initialized with keys "a", "b", "a", "c", "b"
     */
    public Map<String, Integer> createMapWithDuplicateKeys() {
        String[] args = {"a", "b", "a", "c", "b"};
        return switch (this) {
            case MutableMapCreator mmc -> mmc.newMap(args);
            case MapBasedMapCreator<?> mmc -> throw new UnsupportedOperationException();
            case StreamBasedMapCreator smc -> smc.newMap(args);
            case EmptyMapCreator emc -> throw new UnsupportedOperationException();
            case SingleEntryMapCreator sec -> throw new UnsupportedOperationException();
        };
    }

    /**
     * Returns whether this map creator instantiates a map in a way that keys can be provided to it
     * multiple times. For example, duplicate keys cannot be encountered in a map creation method that
     * copies another map. This method is relevant for {@link #createMapWithDuplicateKeys()}.
     *
     * @return true if it can technically encounter duplicate keys in its instantiation, false otherwise
     */
    public boolean canEncounterDuplicateKeys() {
        return !(this instanceof MapBasedMapCreator<?>) && getSizeLimit() >= 2;
    }

    /**
     * Creates a new map wrapped in a {@link MapWithBackingDataModifier}. This is used to test for immutability
     * (or the opposite): if a map type wraps another map, the result of this method allows to change the backing
     * structure to see if the change is reflected in the map.
     * <p>
     * An empty Optional is returned if the map type of this creator is not based on another map.
     *
     * @param args the keys the map should contain
     * @return optional with the new map and its backing structure, if applicable; empty optional otherwise
     */
    public abstract Optional<MapWithBackingDataModifier> createMapWithBackingDataModifier(String... args);

    /**
     * @return the maximum number of elements the map type created by this creator can contain
     */
    public int getSizeLimit() {
        return Integer.MAX_VALUE;
    }


    // -----------
    // Creation methods
    // -----------

    /**
     * Creates a map creator based on a mutable Map type's constructor.
     *
     * @param constructor the constructor of the mutable Map type
     * @return map creator
     */
    public static MapCreator forMutableType(Supplier<Map<String, Integer>> constructor) {
        return new MutableMapCreator(constructor);
    }

    /**
     * Creates a map creator based on a method that copies or wraps another map.
     *
     * @param callback method that takes a map to return a map
     * @return map creator
     */
    public static MapCreator forMapBasedType(Function<Map<String, Integer>, Map<String, Integer>> callback) {
        return forMapBasedType(callback, Function.identity());
    }

    /**
     * Creates a map creator based on a method that copies or wraps another map of a specific type.
     *
     * @param callback method that takes a map to return a map
     * @param inputTransformer callback to convert the map to the required input type. This callback should check the
     *                         incoming map's type and cast it if it can be used, otherwise it should create a new map
     * @param <M> the map type required to create a map
     * @return map creator
     */
    public static <M extends Map<String, Integer>> MapCreator forMapBasedType(
                                                         Function<M, Map<String, Integer>> callback,
                                                         Function<Map<String, Integer>, ? extends M> inputTransformer) {
        return new MapBasedMapCreator<>(callback, inputTransformer);
    }

    /**
     * Creates a map creator based on a method that collects a stream to a map.
     *
     * @param callback method that creates a map based on a stream
     * @return map creator
     */
    public static MapCreator fromStream(Function<Stream<Map.Entry<String, Integer>>, Map<String, Integer>> callback) {
        return new StreamBasedMapCreator(callback);
    }

    /**
     * Creates a map creator based on a method that creates a specialized single-element map type.
     *
     * @param callback method with single element as argument
     * @return map creator
     */
    public static MapCreator forSingleEntry(BiFunction<String, Integer, Map<String, Integer>> callback) {
        return new SingleEntryMapCreator(callback);
    }

    /**
     * Creates a map creator based on a method that creates a specialized empty map type.
     *
     * @param callback method returning an empty map object
     * @return map creator
     */
    public static MapCreator forEmptyMap(Supplier<Map<String, Integer>> callback) {
        return new EmptyMapCreator(callback);
    }


    // -----------
    // Implementations
    // -----------

    /** Implementation for mutable Map types. */
    private static final class MutableMapCreator extends MapCreator {

        private final Supplier<Map<String, Integer>> callback;

        MutableMapCreator(Supplier<Map<String, Integer>> callback) {
            this.callback = callback;
        }

        Map<String, Integer> newMap(String... args) {
            Map<String, Integer> map = callback.get();
            MapCreator.createEntries(args)
                .forEach(e -> map.put(e.getKey(), e.getValue()));
            return map;
        }

        Map<String, Integer> newMapWithNullValue(String key) {
            Map<String, Integer> map = callback.get();
            map.put(key, null);
            return map;
        }

        @Override
        public Optional<MapWithBackingDataModifier> createMapWithBackingDataModifier(String... args) {
            throw new UnsupportedOperationException();
        }
    }

    /** Implementation for map creations based on another map (by wrapping or copying it). */
    public static final class MapBasedMapCreator<M extends Map<String, Integer>> extends MapCreator {

        private final Function<M, Map<String, Integer>> callback;
        private final Function<Map<String, Integer>, ? extends M> inputTransformer;

        MapBasedMapCreator(Function<M, Map<String, Integer>> callback,
                           Function<Map<String, Integer>, ? extends M> inputTransformer) {
            this.callback = callback;
            this.inputTransformer = inputTransformer;
        }

        public Map<String, Integer> newMap(Map<String, Integer> args) {
            M input = inputTransformer.apply(args);
            return callback.apply(input);
        }

        @Override
        public Optional<MapWithBackingDataModifier> createMapWithBackingDataModifier(String[] args) {
            LinkedHashMap<String, Integer> initialMap = MapCreator.createLinkedHashMap(args);

            M backingMap = inputTransformer.apply(initialMap);
            Map<String, Integer> map = callback.apply(backingMap);
            return Optional.of(new MapWithBackingDataModifier(map, () -> backingMap.remove("d")));
        }
    }

    /** Implementation for map creation based on a stream. */
    private static final class StreamBasedMapCreator extends MapCreator {

        private final Function<Stream<Map.Entry<String, Integer>>, Map<String, Integer>> callback;

        StreamBasedMapCreator(Function<Stream<Map.Entry<String, Integer>>, Map<String, Integer>> callback) {
            this.callback = callback;
        }

        Map<String, Integer> newMap(String... args) {
            Stream<Map.Entry<String, Integer>> stream = MapCreator.createEntries(args).stream();
            return callback.apply(stream);
        }

        Map<String, Integer> newMapWithNullValue(String key) {
            Stream<Map.Entry<String, Integer>> stream = Stream.of(
                new AbstractMap.SimpleEntry<>(key, null));
            return callback.apply(stream);
        }

        @Override
        public Optional<MapWithBackingDataModifier> createMapWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }
    }

    /** Implementation for the map creation of a specialized single-entry map type. */
    private static final class SingleEntryMapCreator extends MapCreator {

        private final BiFunction<String, Integer, Map<String, Integer>> callback;

        SingleEntryMapCreator(BiFunction<String, Integer, Map<String, Integer>> callback) {
            this.callback = callback;
        }

        Map<String, Integer> newMap(String elem) {
            return callback.apply(elem, MapCreator.createValue(elem));
        }

        Map<String, Integer> newMapWithNullValue(String key) {
            return callback.apply(key, null);
        }

        @Override
        public Optional<MapWithBackingDataModifier> createMapWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }

        @Override
        public int getSizeLimit() {
            return 1;
        }
    }

    /** Implementation for the map creation of a specialized empty map type. */
    private static final class EmptyMapCreator extends MapCreator {

        private final Supplier<Map<String, Integer>> callback;

        EmptyMapCreator(Supplier<Map<String, Integer>> callback) {
            this.callback = callback;
        }

        Map<String, Integer> newMap() {
            return callback.get();
        }

        @Override
        public Optional<MapWithBackingDataModifier> createMapWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }

        @Override
        public int getSizeLimit() {
            return 0;
        }
    }
}
