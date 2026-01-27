package ch.jalu.collectionbehavior.analysis;

import ch.jalu.collectionbehavior.creator.ListCreator;
import ch.jalu.collectionbehavior.creator.SizeNotSupportedException;
import ch.jalu.collectionbehavior.documentation.Range;
import ch.jalu.collectionbehavior.util.RangeUtils;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

public class ListIteratorAnalyzer {

    private final ListCreator listCreator;

    public ListIteratorAnalyzer(ListCreator listCreator) {
        this.listCreator = listCreator;
    }

    public Map<Range, String> collectClassNamesBySize() {
        List<String> elements = Collections.nCopies(20, "o");
        TreeMap<Integer, String> classNamesBySize = new TreeMap<>();

        for (int i = 0; i <= 20; ++i) {
            try {
                List<String> list = listCreator.createList(elements.subList(0, i).toArray(String[]::new));
                ListIterator<String> iterator = list.listIterator();
                classNamesBySize.put(i, iterator.getClass().getName());
            } catch (SizeNotSupportedException ignore) {
            }
        }

        Map<Range, String> classesByRange = RangeUtils.collectValuesByRange(classNamesBySize);
        return classesByRange;
    }
}
