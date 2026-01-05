package ch.jalu.collectionbehavior.v2.documentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListDocumentation {

    private final String description;

    // Class
    private Map<Integer, String> classNamesBySize;
    private RandomAccessType randomAccessType;
    private boolean supportsNullElements;

    // Methods
    private final List<ListMethodBehavior> behaviors = new ArrayList<>();


    public ListDocumentation(String description) {
        this.description = description;
    }

    public void addBehavior(ListMethodBehavior behavior) {
        behaviors.add(behavior);
    }

    public void setClassNamesBySize(Map<Integer, String> classNamesBySize) {
        this.classNamesBySize = classNamesBySize;
    }

    public void setSupportsNullElements(boolean supportsNullElements) {
        this.supportsNullElements = supportsNullElements;
    }

    public void setRandomAccessType(RandomAccessType randomAccessType) {
        this.randomAccessType = randomAccessType;
    }

    @Override
    public String toString() {
        return description + ": " + behaviors;
    }
}
