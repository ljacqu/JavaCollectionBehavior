package ch.jalu.collectionbehavior.documentation;

import java.util.List;
import java.util.Map;

public class ListIteratorDocumentation implements CollectionDocumentation {

    private final String description;

    private Map<Range, String> classesByRange;
    private Boolean supportsNullArguments;
    private List<ModificationBehavior> modificationBehaviors;
    private List<MethodBehavior> methodBehaviors;

    public ListIteratorDocumentation(String description) {
        this.description = description;
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

    public List<ModificationBehavior> getModificationBehaviors() {
        return modificationBehaviors;
    }

    public void setModificationBehaviors(List<ModificationBehavior> modificationBehaviors) {
        this.modificationBehaviors = modificationBehaviors;
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
