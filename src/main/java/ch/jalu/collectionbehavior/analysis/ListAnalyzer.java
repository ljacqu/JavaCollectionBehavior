package ch.jalu.collectionbehavior.analysis;

import ch.jalu.collectionbehavior.creator.ListCreator;
import ch.jalu.collectionbehavior.creator.ListWithBackingStructure;
import ch.jalu.collectionbehavior.creator.SizeNotSupportedException;
import ch.jalu.collectionbehavior.documentation.ModificationBehavior;
import ch.jalu.collectionbehavior.documentation.RandomAccessType;
import ch.jalu.collectionbehavior.documentation.Range;
import ch.jalu.collectionbehavior.documentation.SpliteratorCharacteristic;
import ch.jalu.collectionbehavior.documentation.Support;
import ch.jalu.collectionbehavior.util.RangeUtils;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

public class ListAnalyzer {

    private static final int MAX_SIZE_TO_INSTANTIATE = 20;

    private final ListCreator listCreator;

    public ListAnalyzer(ListCreator listCreator) {
        this.listCreator = listCreator;
    }

    public RandomAccessType determineRandomAccessType() {
        if (listCreator instanceof ListCreator.ListBasedListCreator lcl) {
            ArrayList<String> arrayList = new ArrayList<>(List.of("a", "b"));
            LinkedList<String> linkedList = new LinkedList<>(List.of("a", "b"));

            boolean arrayListProducesRandomAccess = lcl.fromList(arrayList) instanceof RandomAccess;
            boolean linkedListProducesRandomAccess = lcl.fromList(linkedList) instanceof RandomAccess;

            if (arrayListProducesRandomAccess && linkedListProducesRandomAccess) {
                return RandomAccessType.IMPLEMENTS;
            } else if (arrayListProducesRandomAccess && !linkedListProducesRandomAccess) {
                return RandomAccessType.PRESERVES;
            } else if (!arrayListProducesRandomAccess && !linkedListProducesRandomAccess) {
                return RandomAccessType.DOES_NOT_IMPLEMENT;
            } else {
                throw new IllegalStateException("RandomAccess check (ArrayList=" + arrayListProducesRandomAccess
                    + ", LinkedList=" + linkedListProducesRandomAccess + ")");
            }
        } else {
            List<String> abcdList = listCreator.createAbcdListOrLargestSubset();
            return abcdList instanceof RandomAccess
                ? RandomAccessType.IMPLEMENTS
                : RandomAccessType.DOES_NOT_IMPLEMENT;
        }
    }

    public Range determineSupportedSize() {
        List<Integer> supportedSizes = new ArrayList<>(
            getClassNamesBySize(el -> listCreator.createList(el.toArray(String[]::new))).navigableKeySet());

        int min = supportedSizes.getFirst();
        int current = min;
        for (int i = 1; i < supportedSizes.size(); ++i) {
            if (supportedSizes.get(i) != current + 1) {
                throw new IllegalStateException(
                    "Gap in supported size: " + current + " went to " + supportedSizes.get(i));
            }
            current = supportedSizes.get(i);
        }

        return current == MAX_SIZE_TO_INSTANTIATE
            ? new Range(min, null)
            : new Range(min, current);
    }

    public Map<Range, String> collectClassNamesBySize() {
        TreeMap<Integer, String> classNamesBySize =
            getClassNamesBySize(elements -> listCreator.createList(elements.toArray(String[]::new)));

        Map<Range, String> classesByRange = RangeUtils.collectValuesByRange(classNamesBySize);
        classesByRange.putAll(getClassesByRangeAddition(classesByRange));
        return classesByRange;
    }

    public Support determineNullElementSupport() {
        try {
            listCreator.createList((String)null);
            return Support.YES;
        } catch (NullPointerException e) {
            return Support.NO;
        } catch (SizeNotSupportedException ignore) {
            return Support.NOT_APPLICABLE;
        }
    }

    public Support determineSkipsSelfWrapping() {
        if (listCreator instanceof ListCreator.ListBasedListCreator lbc) {
            List<String> list1 = lbc.fromList(Arrays.asList("o", "g"));
            List<String> list2 = lbc.fromList(list1);

            boolean doesNotWrapItself = list1 == list2; // == is intentional: need to see if it's the same object
            return doesNotWrapItself ? Support.YES : Support.NO;
        }
        return Support.NOT_APPLICABLE;
    }

    public List<ModificationBehavior> determineBackingStructureBehaviors() {
        List<ModificationBehavior> behaviors = new ArrayList<>();
        if (listCreator instanceof ListCreator.BackingStructurBasedListCreator lbc) {
            // Check if changing the backing structure changes the list
            ListWithBackingStructure listWithBackingStructure = lbc.createListWithBackingStructure();
            List<String> list = listWithBackingStructure.getList();
            Preconditions.checkState(list.equals(List.of("a", "b", "c", "d")));
            listWithBackingStructure.modifyBackingStructure();
            if (!list.equals(List.of("a", "b", "c", "d"))) {
                behaviors.add(ModificationBehavior.STRUCTURE_INFLUENCES_COLLECTION);
            }

            // Check if changing the list (if allowed) changes the backing structure
            listWithBackingStructure = lbc.createListWithBackingStructure();
            list = listWithBackingStructure.getList();
            Preconditions.checkState(list.equals(List.of("a", "b", "c", "d")));
            try {
                list.set(2, "changed");
                if (listWithBackingStructure.getBackingStructureAsList().equals(List.of("a", "b", "changed", "d"))) {
                    behaviors.add(ModificationBehavior.COLLECTION_INFLUENCES_STRUCTURE);
                }
            } catch (UnsupportedOperationException ignore) {
            }
        }
        return behaviors;
    }

    public Set<SpliteratorCharacteristic> determineSpliteratorProperties() {
        List<String> list = listCreator.createAbcdListOrLargestSubset();

        int characteristics = list.spliterator().characteristics();
        // todo - do we ever get distinct? Do we need to check with non-distinct elements?
        return SpliteratorCharacteristic.create(characteristics);
    }

    // ---- utils

    private static TreeMap<Integer, String> getClassNamesBySize(UnaryOperator<List<String>> createListFn) {
        List<String> elements = Collections.nCopies(MAX_SIZE_TO_INSTANTIATE, "o");
        TreeMap<Integer, String> classNamesBySize = new TreeMap<>();

        for (int i = 0; i <= MAX_SIZE_TO_INSTANTIATE; ++i) {
            try {
                List<String> list = createListFn.apply(elements.subList(0, i));
                classNamesBySize.put(i, list.getClass().getName());
            } catch (SizeNotSupportedException ignore) {
            }
        }

        return classNamesBySize;
    }

    // TODO: clean up
    private Map<Range, String> getClassesByRangeAddition(Map<Range, String> classesByRange) {
        if (listCreator instanceof ListCreator.ListBasedListCreator lbc) {
            TreeMap<Integer, String> classNamesBySize = getClassNamesBySize(lbc::fromList);
            Map<Range, String> newClassesByRange = RangeUtils.collectValuesByRange(classNamesBySize);

            if (!newClassesByRange.equals(classesByRange)) { // Something is different
                LinkedHashMap<Range, String> classesByRangeAdditions = new LinkedHashMap<>(classesByRange.size());
                classesByRange.forEach((range, clazz) -> {
                    Range rangeAddition = new Range(100 + range.min(), range.max() == null ? null : 100 + range.max());
                    classesByRangeAdditions.put(rangeAddition, clazz);
                });
                return classesByRangeAdditions;
            }
        }
        return Collections.emptyMap();
    }
}
