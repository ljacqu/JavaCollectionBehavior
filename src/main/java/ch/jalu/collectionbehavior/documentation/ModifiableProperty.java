package ch.jalu.collectionbehavior.documentation;

/**
 * Modifiability properties of a collection (can change size, existing entries can be altered...).
 * An empty list of this enum type implies that the structure described by it is unmodifiable.
 */
public enum ModifiableProperty {

    /**
     * An existing entry can be modified (e.g. {@code list.set(2, "changed")}).
     */
    CAN_MODIFY_ENTRIES,

    /**
     * The size of the list can be modified, i.e. entries can be added or removed.
     */
    CAN_CHANGE_SIZE,

}
