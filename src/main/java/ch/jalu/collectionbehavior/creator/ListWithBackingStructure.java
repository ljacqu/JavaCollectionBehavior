package ch.jalu.collectionbehavior.creator;

import java.util.List;
import java.util.function.Supplier;

/**
 * Contains a list and a callback to modify the backing structure (collection or array) to verify whether the
 * list changes.
 */
public final class ListWithBackingStructure {

    private final List<String> list;
    private final Runnable backingStructureModifier;
    private final Supplier<List<String>> backingStructureAsListProvider;

    /**
     * @param list the list
     * @param backingStructureModifier callback to change the backing structure based on which the list was created
     * @param backingStructureAsListProvider returns the backing structure as list (converting it if needed)
     */
    public ListWithBackingStructure(List<String> list,
                                    Runnable backingStructureModifier,
                                    Supplier<List<String>> backingStructureAsListProvider) {
        this.list = list;
        this.backingStructureModifier = backingStructureModifier;
        this.backingStructureAsListProvider = backingStructureAsListProvider;
    }

    public List<String> getList() {
        return list;
    }

    public void modifyBackingStructure() {
        backingStructureModifier.run();
    }

    public List<String> getBackingStructureAsList() {
        return backingStructureAsListProvider.get();
    }
}
