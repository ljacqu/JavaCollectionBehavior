package ch.jalu.collectionbehavior.documentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ListDocumentation implements CollectionDocumentation {

    private final String description;

    // Class
    private Range supportedSize;
    private Map<Range, String> classesByRange;
    private RandomAccessType randomAccessType;
    private Optional<Boolean> supportsNullElements;
    private Boolean supportsNullArguments;

    private List<ModificationBehavior> modificationBehaviors = new ArrayList<>();
    private Optional<Boolean> doesNotRewrapItself;
    private Set<SpliteratorCharacteristic> spliteratorCharacteristics;

    // Methods
    private List<MethodBehavior> methodBehaviors = new ArrayList<>();


    public ListDocumentation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Range getSupportedSize() {
        return supportedSize;
    }

    public void setSupportedSize(Range supportedSize) {
        this.supportedSize = supportedSize;
    }

    public Map<Range, String> getClassesByRange() {
        return classesByRange;
    }

    public void setClassesByRange(Map<Range, String> classesByRange) {
        this.classesByRange = classesByRange;
    }

    public RandomAccessType getRandomAccessType() {
        return randomAccessType;
    }

    public void setRandomAccessType(RandomAccessType randomAccessType) {
        this.randomAccessType = randomAccessType;
    }

    public Optional<Boolean> getSupportsNullElements() {
        return supportsNullElements;
    }

    public void setSupportsNullElements(Optional<Boolean> supportsNullElements) {
        this.supportsNullElements = supportsNullElements;
    }

    public Boolean getSupportsNullArguments() {
        return supportsNullArguments;
    }

    public void setSupportsNullArguments(Boolean supportsNullArguments) {
        this.supportsNullArguments = supportsNullArguments;
    }

    public List<ModificationBehavior> getModificationBehaviors() {
        return modificationBehaviors;
    }

    public void setModificationBehaviors(List<ModificationBehavior> modificationBehaviors) {
        this.modificationBehaviors = modificationBehaviors;
    }

    public Optional<Boolean> getDoesNotRewrapItself() {
        return doesNotRewrapItself;
    }

    public void setDoesNotRewrapItself(Optional<Boolean> doesNotRewrapItself) {
        this.doesNotRewrapItself = doesNotRewrapItself;
    }

    public Set<SpliteratorCharacteristic> getSpliteratorCharacteristics() {
        return spliteratorCharacteristics;
    }

    public void setSpliteratorCharacteristics(Set<SpliteratorCharacteristic> spliteratorCharacteristics) {
        this.spliteratorCharacteristics = spliteratorCharacteristics;
    }

    public List<MethodBehavior> getMethodBehaviors() {
        return methodBehaviors;
    }

    public void setMethodBehaviors(List<MethodBehavior> methodBehaviors) {
        this.methodBehaviors = methodBehaviors;
    }

    @Override
    public String toString() {
        return description + ": " + methodBehaviors;
    }
}
