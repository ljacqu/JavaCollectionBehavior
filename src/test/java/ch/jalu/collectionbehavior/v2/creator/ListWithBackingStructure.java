package ch.jalu.collectionbehavior.v2.creator;

import java.util.List;

/**
 * Contains a list and a callback to modify the backing structure (collection or array) to verify whether the
 * list changes.
 *
 * @param list the list
 * @param backingStructureModifier changes the backing structure based on which the list was created
 */
public record ListWithBackingStructure(List<String> list, Runnable backingStructureModifier) {
}
