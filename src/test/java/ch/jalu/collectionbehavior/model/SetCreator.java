package ch.jalu.collectionbehavior.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Callback to create a specific type of Set that is tested.
 */
public abstract sealed class SetCreator {

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

    /**
     * @return New set of the specific Set type. The returned set has the minimum number
     *         of entries supported by the type.
     */
    public Set<String> createSet() {
        return switch (this) {
            case MutableSetCreator msc -> msc.newSet();
            case ArrayBasedSetCreator asc -> asc.newSet();
            case CollectionBasedSetCreator cbc -> cbc.newSet(Collections.emptySet());
            case SetBasedSetCreator sbc -> sbc.newSet(Collections.emptySet());
            case StreamBasedSetCreator ssc -> ssc.newSet();
            case EmptySetCreator esc -> esc.newSet();
            case SingleElementSetCreator sec -> sec.newSet("a");
        };
    }

    /**
     * Creates a set that includes a null element. This method throws an exception if the
     * set type does not support null elements. An exception is also thrown if this method
     * is called on a creator for an empty set type ({@link EmptySetCreator}).
     *
     * @return set containing {@code null}
     */
    public Set<String> createSetWithNull() {
        return switch (this) {
            case MutableSetCreator msc -> msc.newSet((String) null);
            case ArrayBasedSetCreator asc -> asc.newSet((String) null);
            case CollectionBasedSetCreator cbc -> cbc.newSet(Collections.singleton(null));
            case SetBasedSetCreator lsc -> lsc.newSet(Collections.singleton(null));
            case StreamBasedSetCreator ssc -> ssc.newSet((String) null);
            case EmptySetCreator esc -> throw new UnsupportedOperationException();
            case SingleElementSetCreator sec -> sec.newSet(null);
        };
    }

    /**
     * Creates a new set with the string entries {@code "a", "b", "c", "d"}. If the set cannot hold as many
     * arguments, the biggest possible subset is returned (cf. {@link #getSizeLimit()}).
     *
     * @return set with entries "a", "b", "c", "d" or the largest subset supported by the set type
     */
    public Set<String> createSetWithAbcdOrSubset() {
        String[] args = {"a", "b", "c", "d"};
        return switch (this) {
            case MutableSetCreator msc -> msc.newSet(args);
            case ArrayBasedSetCreator asc -> asc.newSet(args);
            case CollectionBasedSetCreator cbc -> cbc.newSet(Arrays.asList(args));
            case SetBasedSetCreator sbc -> sbc.newSet(new LinkedHashSet<>(Arrays.asList(args)));
            case StreamBasedSetCreator sbc -> sbc.newSet(args);
            case EmptySetCreator esc -> esc.newSet();
            case SingleElementSetCreator sec -> sec.newSet(args[0]);
        };
    }

    /**
     * Creates a set with alphanumerical pairs as entries: {@link #ALPHANUM_ELEMENTS_RANDOM}.
     * Used to determine the order of the set (needs many entries to reduce the chance that a hash set has the
     * elements in order by coincidence). Set creators that cannot hold as many entries throw an exception
     * (refer to {@link #getSizeLimit()}).
     *
     * @return set with alphanumerical pairs
     */
    public Set<String> createSetWithAlphanumericalEntries() {
        String[] args = ALPHANUM_ELEMENTS_RANDOM;
        return switch (this) {
            case MutableSetCreator msc -> msc.newSet(args);
            case ArrayBasedSetCreator asc -> asc.newSet(args);
            case CollectionBasedSetCreator cbc -> cbc.newSet(Arrays.asList(args));
            case SetBasedSetCreator sbc -> sbc.newSet(new LinkedHashSet<>(Arrays.asList(args)));
            case StreamBasedSetCreator sbc -> sbc.newSet(args);
            case EmptySetCreator esc -> throw new UnsupportedOperationException();
            case SingleElementSetCreator sec -> throw new UnsupportedOperationException();
        };
    }

    /**
     * Initializes a set with "a", "b", "c", including some of these entries multiple times to test the
     * set's behavior with duplicate elements. This method throws an exception if the instantiation type
     * cannot encounter sets (see {@link #canEncounterDuplicateArguments()}).
     *
     * @return set initialized with "a", "b", "a", "c", "b"
     */
    public Set<String> createSetWithDuplicateArgs() {
        String[] args = {"a", "b", "a", "c", "b"};
        return switch (this) {
            case MutableSetCreator msc -> msc.newSet(args);
            case ArrayBasedSetCreator asc -> asc.newSet(args);
            case CollectionBasedSetCreator cbc -> cbc.newSet(Arrays.asList(args));
            case SetBasedSetCreator sbc -> throw new UnsupportedOperationException();
            case StreamBasedSetCreator sbc -> sbc.newSet(args);
            case EmptySetCreator esc -> throw new UnsupportedOperationException();
            case SingleElementSetCreator sec -> throw new UnsupportedOperationException();
        };
    }

    /**
     * Returns whether this set creator instantiates a set in a way that elements can be provided to it
     * multiple times. For example, duplicate elements cannot be encountered in a set creation method that
     * copies another set. This method is relevant for {@link #createSetWithDuplicateArgs()}.
     *
     * @return true if it can technically encounter duplicate elements in its instantiation, false otherwise
     */
    public boolean canEncounterDuplicateArguments() {
        return !(this instanceof SetBasedSetCreator) && getSizeLimit() >= 2;
    }

    /**
     * Creates a new set wrapped in a {@link SetWithBackingDataModifier}. This is used to test for immutability
     * (or the opposite): if a set type wraps another structure (array or collection), the result of this method
     * allows to change the backing structure to see if the change is reflected in the set.
     * <p>
     * An empty Optional is returned if the set type of this creator is not based on another structure
     * (array or collection).
     *
     * @param args the arguments the set should contain
     * @return optional with the new set and its backing structure, if applicable; empty optional otherwise
     */
    public abstract Optional<SetWithBackingDataModifier> createSetWithBackingDataModifier(String... args);

    /**
     * @return the maximum number of elements the set type created by this creator can contain
     */
    public int getSizeLimit() {
        return Integer.MAX_VALUE;
    }


    // -----------
    // Creation methods
    // -----------

    /**
     * Creates a set creator based on a mutable Set type's constructor.
     *
     * @param constructor the constructor of the mutable Set type
     * @return set creator
     */
    public static SetCreator forMutableType(Supplier<Set<String>> constructor) {
        return new MutableSetCreator(constructor);
    }

    /**
     * Creates a set creator based on a method that creates a set from a given array.
     *
     * @param callback method that takes an array and returns a set
     * @return set creator
     */
    public static SetCreator forArrayBasedType(Function<String[], Set<String>> callback) {
        return new ArrayBasedSetCreator(callback);
    }

    /**
     * Creates a set created based on a method that creates a set from a collection.
     *
     * @param callback method that takes a collection and returns a set
     * @return set creator
     */
    public static SetCreator fromCollection(Function<Collection<String>, Set<String>> callback) {
        return new CollectionBasedSetCreator(callback);
    }

    /**
     * Creates a set creator based on a method that copies or wraps another set.
     * Use {@link #fromCollection} if the method can generally take collections! This ensures that the creation
     * method is tested on its behavior when duplicate elements exist in the argument of the method.
     *
     * @param callback method that takes a set to return a set
     * @return set creator
     */
    public static SetCreator forSetBasedType(Function<Set<String>, Set<String>> callback) {
        return new SetBasedSetCreator(callback);
    }

    /**
     * Creates a set creator based on a method that collects a stream to a set.
     *
     * @param callback method that creates a set based on a stream
     * @return set creator
     */
    public static SetCreator fromStream(Function<Stream<String>, Set<String>> callback) {
        return new StreamBasedSetCreator(callback);
    }

    /**
     * Creates a set creator based on a method that creates a specialized single-element set type.
     *
     * @param callback method with single element as argument
     * @return set creator
     */
    public static SetCreator forSingleElement(Function<String, Set<String>> callback) {
        return new SingleElementSetCreator(callback);
    }

    /**
     * Creates a set creator based on a method that creates a specialized empty set type.
     *
     * @param callback method returning an empty set object
     * @return set creator
     */
    public static SetCreator forEmptySet(Supplier<Set<String>> callback) {
        return new EmptySetCreator(callback);
    }


    // -----------
    // Implementations
    // -----------

    /** Implementation for mutable Set types. */
    private static final class MutableSetCreator extends SetCreator {

        private final Supplier<Set<String>> callback;

        MutableSetCreator(Supplier<Set<String>> callback) {
            this.callback = callback;
        }

        Set<String> newSet(String... args) {
            Set<String> set = callback.get();
            set.addAll(Arrays.asList(args));
            return set;
        }

        @Override
        public Optional<SetWithBackingDataModifier> createSetWithBackingDataModifier(String... args) {
            throw new UnsupportedOperationException();
        }
    }

    /** Implementation for array-based Set creation. */
    private static final class ArrayBasedSetCreator extends SetCreator {

        private final Function<String[], Set<String>> callback;

        ArrayBasedSetCreator(Function<String[], Set<String>> callback) {
            this.callback = callback;
        }

        Set<String> newSet(String... args) {
            return callback.apply(args);
        }

        @Override
        public Optional<SetWithBackingDataModifier> createSetWithBackingDataModifier(String[] args) {
            Set<String> set = newSet(args);
            return Optional.of(new SetWithBackingDataModifier(set, () -> args[1] = "changed"));
        }
    }

    /** Common set creator type that creates a set based on another collection or set. */
    public abstract static sealed class FromCollectionSetCreator extends SetCreator {

        public abstract Set<String> newSet(Set<String> args);

    }

    /** Implementation for set creations based on another collection (by wrapping or copying it). */
    private static final class CollectionBasedSetCreator extends FromCollectionSetCreator {

        private final Function<Collection<String>, Set<String>> callback;

        CollectionBasedSetCreator(Function<Collection<String>, Set<String>> callback) {
            this.callback = callback;
        }

        @Override
        public Set<String> newSet(Set<String> args) {
            return newSet((Collection<String>) args);
        }

        public Set<String> newSet(Collection<String> args) {
            return callback.apply(args);
        }

        @Override
        public Optional<SetWithBackingDataModifier> createSetWithBackingDataModifier(String[] args) {
            LinkedHashSet<String> backingSet = new LinkedHashSet<>(Arrays.asList(args));
            Set<String> set = newSet(backingSet);
            return Optional.of(new SetWithBackingDataModifier(set, backingSet::removeLast));
        }
    }

    /** Implementation for set creations based on another set (by wrapping or copying it). */
    private static final class SetBasedSetCreator extends FromCollectionSetCreator {

        private final Function<Set<String>, Set<String>> callback;

        SetBasedSetCreator(Function<Set<String>, Set<String>> callback) {
            this.callback = callback;
        }

        @Override
        public Set<String> newSet(Set<String> args) {
            return callback.apply(args);
        }

        @Override
        public Optional<SetWithBackingDataModifier> createSetWithBackingDataModifier(String[] args) {
            LinkedHashSet<String> backingSet = new LinkedHashSet<>(Arrays.asList(args));
            Set<String> set = newSet(backingSet);
            return Optional.of(new SetWithBackingDataModifier(set, backingSet::removeLast));
        }
    }

    /** Implementation for set creation based on a stream. */
    private static final class StreamBasedSetCreator extends SetCreator {

        private final Function<Stream<String>, Set<String>> callback;

        StreamBasedSetCreator(Function<Stream<String>, Set<String>> callback) {
            this.callback = callback;
        }

        Set<String> newSet(String... args) {
            return callback.apply(Arrays.stream(args));
        }

        @Override
        public Optional<SetWithBackingDataModifier> createSetWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }
    }

    /** Implementation for the set creation of a specialized single-element set type. */
    private static final class SingleElementSetCreator extends SetCreator {

        private final Function<String, Set<String>> callback;

        SingleElementSetCreator(Function<String, Set<String>> callback) {
            this.callback = callback;
        }

        Set<String> newSet(String elem) {
            return callback.apply(elem);
        }

        @Override
        public Optional<SetWithBackingDataModifier> createSetWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }

        @Override
        public int getSizeLimit() {
            return 1;
        }
    }

    /** Implementation for the set creation of a specialized empty set type. */
    private static final class EmptySetCreator extends SetCreator {

        private final Supplier<Set<String>> callback;

        EmptySetCreator(Supplier<Set<String>> callback) {
            this.callback = callback;
        }

        Set<String> newSet() {
            return callback.get();
        }

        @Override
        public Optional<SetWithBackingDataModifier> createSetWithBackingDataModifier(String[] args) {
            return Optional.empty();
        }

        @Override
        public int getSizeLimit() {
            return 0;
        }
    }
}
