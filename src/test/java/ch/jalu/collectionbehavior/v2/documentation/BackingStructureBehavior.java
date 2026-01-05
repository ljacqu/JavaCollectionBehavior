package ch.jalu.collectionbehavior.v2.documentation;

public enum BackingStructureBehavior {

    /**
     * E.g. Collections.unmodifiableList(originalList), when {@code originalList.clear()}
     * is called, the collection reflects the change.
     */
    STRUCTURE_INFLUENCES_COLLECTION,

    /**
     * E.g. list = Arrays.asList(array), when {@code list.set(2, "changed")}
     * is called, the array reflects the change.
     */
    COLLECTION_INFLUENCES_STRUCTURE

}
