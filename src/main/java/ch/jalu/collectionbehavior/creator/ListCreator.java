package ch.jalu.collectionbehavior.creator;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Produces a list of a type to be documented.
 */
public abstract class ListCreator {

    /**
     * Creates a list with the given elements.
     *
     * @param elements the elements the list should be populated with
     * @return list with the elements
     * @throws SizeNotSupportedException if the list does not support the number of elements provided
     */
    public abstract List<String> createList(String... elements) throws SizeNotSupportedException;

    /**
     * Creates a list with the elements "a", "b", "c", "d", or the largest list supported by the type.
     *
     * @return list with a, b, c, d or largest possible subset (e.g. just "a")
     */
    public List<String> createAbcdListOrLargestSubset() {
        return createList("a", "b", "c", "d");
    }

    // ----------
    // Implementations
    // ----------

    /**
     * {@link ArrayList}: general-purpose list mutable implementation. Full null support.
     */
    public static ListCreator ArrayList() {
        return new ListCreator() {
            @Override
            public List<String> createList(String... elements) {
                List<String> arrayList = new ArrayList<>();
                for (String element : elements) {
                    arrayList.add(element);
                }
                return arrayList;
            }
        };
    }

    /**
     * {@link LinkedList}: mutable implementation. Full null support.
     */
    public static ListCreator LinkedList() {
        return new ListCreator() {
            @Override
            public List<String> createList(String... elements) {
                List<String> linkedList = new LinkedList<>();
                for (String element : elements) {
                    linkedList.add(element);
                }
                return linkedList;
            }
        };
    }

    /**
     * {@link List#of}: immutable list that rejects nulls.
     */
    public static ListCreator List_of() {
        return new ArrayBasedListCreator() {
            @Override
            public List<String> createList(String... elements) throws SizeNotSupportedException {
                return switch (elements.length) {
                    case 0 ->  List.of();
                    case 1 ->  List.of(elements[0]);
                    case 2 ->  List.of(elements[0], elements[1]);
                    case 3 ->  List.of(elements[0], elements[1], elements[2]);
                    case 4 ->  List.of(elements[0], elements[1], elements[2], elements[3]);
                    case 5 ->  List.of(elements[0], elements[1], elements[2], elements[3], elements[4]);
                    case 6 ->  List.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
                    case 7 ->  List.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6]);
                    case 8 ->  List.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6], elements[7]);
                    case 9 ->  List.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6], elements[7], elements[8]);
                    case 10 -> List.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6], elements[7], elements[8], elements[9]);
                    default -> List.of(elements);
                };
            }
        };
    }

    /**
     * {@link List#copyOf}: creates an immutable copy of a list; rejects nulls.
     */
    public static ListCreator List_copyOf() {
        return ListBasedListCreator.of(List::copyOf);
    }

    /**
     * {@link Arrays#asList}: creates a list-view based on the array. The list can be modified but cannot change size.
     */
    public static ListCreator Arrays_asList() {
        return ArrayBasedListCreator.of(Arrays::asList);
    }

    /**
     * {@link ImmutableList#of}: immutable list that may not contain nulls. Method calls with nulls are OK.
     */
    public static ListCreator Guava_ImmutableList_of() {
        return new ArrayBasedListCreator() {
            @Override
            public List<String> createList(String... elements) throws SizeNotSupportedException {
                return switch (elements.length) {
                    case 0 ->  ImmutableList.of();
                    case 1 ->  ImmutableList.of(elements[0]);
                    case 2 ->  ImmutableList.of(elements[0], elements[1]);
                    case 3 ->  ImmutableList.of(elements[0], elements[1], elements[2]);
                    case 4 ->  ImmutableList.of(elements[0], elements[1], elements[2], elements[3]);
                    case 5 ->  ImmutableList.of(elements[0], elements[1], elements[2], elements[3], elements[4]);
                    case 6 ->  ImmutableList.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
                    case 7 ->  ImmutableList.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6]);
                    case 8 ->  ImmutableList.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6], elements[7]);
                    case 9 ->  ImmutableList.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6], elements[7], elements[8]);
                    case 10 -> ImmutableList.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6], elements[7], elements[8], elements[9]);
                    case 11 -> ImmutableList.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6], elements[7], elements[8], elements[9], elements[10]);
                    case 12 -> ImmutableList.of(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6], elements[7], elements[8], elements[9], elements[10], elements[11]);
                    default -> {
                        String[] rest = new String[elements.length - 12];
                        System.arraycopy(elements, 12, rest, 0, elements.length - 12);
                        yield ImmutableList.<String>of(elements[0], elements[1], elements[2], elements[3], elements[4],
                            elements[5], elements[6], elements[7], elements[8], elements[9], elements[10], elements[11], rest);
                    }
                };
            }
        };
    }

    /**
     * {@link ImmutableList#copyOf}: copies a list to an immutable list which may not contain nulls.
     */
    public static ListCreator Guava_ImmutableList_copyOf() {
        return ListBasedListCreator.of(ImmutableList::copyOf);
    }

    /**
     * {@link Collections#emptyList}: empty list, therefore immutable.
     */
    public static ListCreator Collections_emptyList() {
        return new ListCreator() {
            @Override
            public List<String> createList(String... elements) throws SizeNotSupportedException {
                if (elements.length == 0) {
                    return Collections.emptyList();
                }
                throw new SizeNotSupportedException();
            }

            @Override
            public List<String> createAbcdListOrLargestSubset() {
                return Collections.emptyList();
            }
        };
    }

    /**
     * {@link Collections#singletonList}: single-element, immutable list. May contain null.
     */
    public static ListCreator Collections_singletonList() {
        return new ListCreator() {
            @Override
            public List<String> createList(String... elements) throws SizeNotSupportedException {
                if (elements.length == 1) {
                    return Collections.singletonList(elements[0]);
                }
                throw new SizeNotSupportedException();
            }

            @Override
            public List<String> createAbcdListOrLargestSubset() {
                return Collections.singletonList("a");
            }
        };
    }

    /**
     * {@link Collections#unmodifiableList}: wraps a list in an unmodifiable view.
     */
    public static ListCreator Collections_unmodifiableList() {
        return ListBasedListCreator.of(Collections::unmodifiableList);
    }

    /**
     * {@link Collectors#toList}: actually produces an ArrayList, though the documentation does not make any guarantees.
     */
    public static ListCreator Collectors_toList() {
        return new ListCreator() {
            @Override
            public List<String> createList(String... elements) throws SizeNotSupportedException {
                return Arrays.stream(elements)
                    .collect(Collectors.toList());
            }
        };
    }

    /**
     * {@link Collectors#toUnmodifiableList}: produces an unmodifiable list that rejects nulls.
     */
    public static ListCreator Collectors_toUnmodifiableList() {
        return new ListCreator() {
            @Override
            public List<String> createList(String... elements) throws SizeNotSupportedException {
                return Arrays.stream(elements)
                    .collect(Collectors.toUnmodifiableList());
            }
        };
    }

    /**
     * {@link java.util.stream.Stream#toList}: produces an unmodifiable list that supports nulls.
     */
    public static ListCreator Stream_toList() {
        return new ListCreator() {
            @Override
            public List<String> createList(String... elements) throws SizeNotSupportedException {
                return Arrays.stream(elements).toList();
            }
        };
    }


    // -----------
    // Specializations
    // -----------

    /**
     * List creator that was created from another structure (list or array).
     */
    public abstract static class BackingStructurBasedListCreator extends ListCreator {

        public abstract ListWithBackingStructure createListWithBackingStructure();

    }

    public abstract static class ListBasedListCreator extends BackingStructurBasedListCreator {

        @Override
        public List<String> createList(String... elements) throws SizeNotSupportedException {
            return fromList(Arrays.asList(elements));
        }

        @Override
        public ListWithBackingStructure createListWithBackingStructure() {
            List<String> arrayList = new ArrayList<>(List.of("a", "b", "c", "d"));
            return new ListWithBackingStructure(
                fromList(arrayList),
                () -> arrayList.set(2, "changed"),
                () -> arrayList);
        }

        public abstract List<String> fromList(List<String> original);

        static ListBasedListCreator of(Function<List<String>, List<String>> listCreator) {
            return new ListBasedListCreator() {
                @Override
                public List<String> fromList(List<String> original) {
                    return listCreator.apply(original);
                }
            };
        }

    }

    public abstract static class ArrayBasedListCreator extends BackingStructurBasedListCreator {

        @Override
        public ListWithBackingStructure createListWithBackingStructure() {
            String[] array = new String[]{ "a", "b", "c", "d" };
            return new ListWithBackingStructure(
                createList(array),
                () -> array[2] = "changed",
                () -> List.of(array));
        }

        static ArrayBasedListCreator of(Function<String[], List<String>> listCreator) {
            return new ArrayBasedListCreator() {
                @Override
                public List<String> createList(String... elements) throws SizeNotSupportedException {
                    return listCreator.apply(elements);
                }
            };
        }
    }
}
