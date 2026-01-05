package ch.jalu.collectionbehavior.v2.creator;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ListCreator {

    public static ListCreator ArrayList() {
        return new ListCreator() {

            @Override
            public List<String> createList(String... elements) {
                return new ArrayList<>(Arrays.asList(elements));
            }
        };
    }

    public static ListCreator LinkedList() {
        return new ListCreator() {

            @Override
            public List<String> createList(String... elements) {
                return new LinkedList<>(Arrays.asList(elements));
            }
        };
    }

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

    public static ListCreator List_copyOf() {
        return new ListBasedListCreator() {
            @Override
            public List<String> fromList(List<String> original) {
                return List.copyOf(original);
            }
        };
    }

    public static ListCreator Arrays_asList() {
        return new ArrayBasedListCreator() {

            @Override
            public List<String> createList(String... elements) {
                return Arrays.asList(elements);
            }
        };
    }

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

    public static ListCreator Guava_ImmutableList_copyOf() {
        return new ListBasedListCreator() {

            @Override
            public List<String> fromList(List<String> original) {
                return ImmutableList.copyOf(original);
            }
        };
    }

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

    public static ListCreator Collections_unmodifiableList() {
        return new ListBasedListCreator() {
            @Override
            public List<String> fromList(List<String> original) {
                return Collections.unmodifiableList(original);
            }
        };
    }

    public static ListCreator Collectors_toList() {
        return new ListCreator() {
            @Override
            public List<String> createList(String... elements) throws SizeNotSupportedException {
                return Arrays.stream(elements)
                    .collect(Collectors.toList());
            }
        };
    }

    public static ListCreator Collectors_toUnmodifiableList() {
        return new ListCreator() {
            @Override
            public List<String> createList(String... elements) throws SizeNotSupportedException {
                return Arrays.stream(elements)
                    .collect(Collectors.toUnmodifiableList());
            }
        };
    }

    public static ListCreator Stream_toList() {
        return new ListCreator() {
            @Override
            public List<String> createList(String... elements) throws SizeNotSupportedException {
                return Arrays.stream(elements).toList();
            }
        };
    }

    public abstract List<String> createList(String... elements) throws SizeNotSupportedException;

    public List<String> createAbcdListOrLargestSubset() {
        return createList("a", "b", "c", "d");
    }

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
            ArrayList<String> arrayList = new ArrayList<>(List.of("a", "b", "c", "d"));
            return new ListWithBackingStructure(
                fromList(arrayList),
                () -> arrayList.set(2, "changed"),
                () -> arrayList);
        }

        public abstract List<String> fromList(List<String> original);

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
    }
}
