package ch.jalu.collectionbehavior.documentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListIteratorDocumentation implements CollectionDocumentation {

    private final String description;

    private String className;
    private boolean supportsNullElements;
    private Boolean supportsNullArguments;

    private Map<Range, String> classesByRange;

    private final List<ModificationBehavior> modificationBehaviors = new ArrayList<>();
    private final List<MethodBehavior> behaviors = new ArrayList<>();

    public ListIteratorDocumentation(String description) {
        this.description = description;
    }

    public void addModificationBehavior(ModificationBehavior behavior) {
        modificationBehaviors.add(behavior);
    }

    public void addBehavior(MethodBehavior behavior) {
        behaviors.add(behavior);
    }

    public void setClassesByRange(Map<Range, String> classesByRange) {
        this.classesByRange = classesByRange;
    }

    @Override
    public String toString() {
        return description + ": " + behaviors;
    }
}
