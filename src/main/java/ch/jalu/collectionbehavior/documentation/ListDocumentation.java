package ch.jalu.collectionbehavior.documentation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListDocumentation implements CollectionDocumentation {

    private final String description;

    // Class
    private Range supportedSize;
    private Map<Range, String> classesByRange;
    private RandomAccessType randomAccessType;
    private Support nullElementSupport;
    private Boolean supportsNullArguments;

    private List<ModifiableProperty> modifiableProperties;
    private List<BackingStructureBehavior> backingStructureBehaviors;
    private Support doesNotRewrapItself;
    private Set<SpliteratorCharacteristic> spliteratorCharacteristics;

    // Methods
    private List<MethodBehavior> methodBehaviors;


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

    public Support getNullElementSupport() {
        return nullElementSupport;
    }

    public void setNullElementSupport(Support nullElementSupport) {
        this.nullElementSupport = nullElementSupport;
    }

    public Boolean getSupportsNullArguments() {
        return supportsNullArguments;
    }

    public void setSupportsNullArguments(Boolean supportsNullArguments) {
        this.supportsNullArguments = supportsNullArguments;
    }

    public List<ModifiableProperty> getModificationBehaviors() {
        return modifiableProperties;
    }

    public void setModificationBehaviors(List<ModifiableProperty> modifiableProperties) {
        this.modifiableProperties = modifiableProperties;
    }

    public List<BackingStructureBehavior> getBackingStructureBehaviors() {
        return backingStructureBehaviors;
    }

    public void setBackingStructureBehaviors(List<BackingStructureBehavior> backingStructureBehaviors) {
        this.backingStructureBehaviors = backingStructureBehaviors;
    }

    public Support getDoesNotRewrapItself() {
        return doesNotRewrapItself;
    }

    public void setDoesNotRewrapItself(Support doesNotRewrapItself) {
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
