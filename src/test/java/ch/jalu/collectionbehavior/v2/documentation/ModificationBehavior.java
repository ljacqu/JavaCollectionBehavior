package ch.jalu.collectionbehavior.v2.documentation;

/**
 * Behaviors that can be observed from a list that is created from a backing structure (list or array).
 */
public enum ModificationBehavior {

    /**
     * An existing entry can be modified (e.g. {@code list.set(2, "changed")}).
     */
    CAN_MODIFY_ENTRIES,

    /**
     * The size of the list can be modified, i.e. entries can be added or removed.
     */
    CAN_CHANGE_SIZE,

    /**
     * E.g. Collections.unmodifiableList(originalList), when {@code originalList.clear()}
     * is called, the collection reflects the change.
     */
    STRUCTURE_INFLUENCES_COLLECTION,

    /**
     * E.g. list = Arrays.asList(array), when {@code list.set(2, "changed")}
     * is called, the array reflects the change.
     */
    COLLECTION_INFLUENCES_STRUCTURE,

}
