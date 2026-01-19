package ch.jalu.collectionbehavior.documentation;

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
    private Boolean supportsNullArguments;

    private List<ModificationBehavior> modificationBehaviors = new ArrayList<>();
    private Boolean doesNotRewrapItself;

    // Methods
    private final List<MethodBehavior> behaviors = new ArrayList<>();


    public ListDocumentation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void addBehavior(MethodBehavior behavior) {
        behaviors.add(behavior);
    }

    public List<MethodBehavior> getMethodBehaviors() {
        return behaviors;
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

    public void setSupportsNullArguments(boolean supportsNullArguments) {
        this.supportsNullArguments = supportsNullArguments;
    }

    public void setRandomAccessType(RandomAccessType randomAccessType) {
        this.randomAccessType = randomAccessType;
    }

    public void addModificationBehavior(ModificationBehavior behavior) {
        modificationBehaviors.add(behavior);
    }

    public void setDoesNotRewrapItself(boolean doesNotRewrapItself) {
        this.doesNotRewrapItself = doesNotRewrapItself;
    }

    @Override
    public String toString() {
        return description + ": " + behaviors;
    }
}
