package ch.jalu.collectionbehavior.documentation;

import java.util.List;
import java.util.Map;

public class ListIteratorDocumentation implements CollectionDocumentation {

    private final String description;

    private Map<Range, String> classesByRange;
    private Boolean supportsNullArguments;
    private List<ModifiableProperty> modifiableProperties;
    private List<MethodBehavior> methodBehaviors;

    public ListIteratorDocumentation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Map<Range, String> getClassesByRange() {
        return classesByRange;
    }

    public void setClassesByRange(Map<Range, String> classesByRange) {
        this.classesByRange = classesByRange;
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
