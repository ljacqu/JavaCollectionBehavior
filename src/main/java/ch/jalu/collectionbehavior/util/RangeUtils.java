package ch.jalu.collectionbehavior.util;

import ch.jalu.collectionbehavior.documentation.Range;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public final class RangeUtils {

    private RangeUtils() {
    }

    public static Map<Range, String> collectValuesByRange(TreeMap<Integer, String> classNamesBySize) {
        Map<Range, String> classesByRange = new LinkedHashMap<>();

        Integer start = null;
        Integer last = null;
        String currentClass = null;

        for (Map.Entry<Integer, String> entry : classNamesBySize.entrySet()) {
            if (currentClass != null && !currentClass.equals(entry.getValue())) {
                classesByRange.put(new Range(start, last), currentClass);
                currentClass = null;
            }

            if (currentClass == null) {
                start = entry.getKey();
                last = entry.getKey();
                currentClass = entry.getValue();
            } else { // currentClass.equals(entry.getValue())
                last = entry.getKey();
            }
        }

        if (currentClass != null) {
            classesByRange.put(new Range(start, last), currentClass);
        }
        return classesByRange;
    }
}
