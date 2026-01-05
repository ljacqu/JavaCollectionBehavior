package ch.jalu.collectionbehavior.v2.documentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListDocumentation {

    private final String description;

    // Class
    private Range supportedSize;
    private Map<Range, String> classesByRange;
    private RandomAccessType randomAccessType;
    private boolean supportsNullElements;

    private Boolean doesNotRewrapItself;
    private List<BackingStructureBehavior> backingStructureBehaviors = new ArrayList<>();

    // Methods
    private final List<MethodBehavior> behaviors = new ArrayList<>();


    public ListDocumentation(String description) {
        this.description = description;
    }

    public void addBehavior(MethodBehavior behavior) {
        behaviors.add(behavior);
    }

    public void setSupportedSize(Range supportedSize) {
        this.supportedSize = supportedSize;
    }

    public void setClassesByRange(Map<Range, String> classesByRange) {
        this.classesByRange = classesByRange;
    }

    public void setSupportsNullElements(boolean supportsNullElements) {
        this.supportsNullElements = supportsNullElements;
    }

    public void setRandomAccessType(RandomAccessType randomAccessType) {
        this.randomAccessType = randomAccessType;
    }

    public void setDoesNotRewrapItself(boolean doesNotRewrapItself) {
        this.doesNotRewrapItself = doesNotRewrapItself;
    }

    public void addBackingStructureBehavior(BackingStructureBehavior behavior) {
        backingStructureBehaviors.add(behavior);
    }

    @Override
    public String toString() {
        return description + ": " + behaviors;
    }
}
