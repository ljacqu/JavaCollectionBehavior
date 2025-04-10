package ch.jalu.collectionbehavior.model;

/**
 * Defines what general interface a map type implements.
 * <p>
 * Hierarchy of types:<ul>
 *    <li>SequencedMap<ul>
 *        <li>SortedMap<ul>
 *            <li>NavigableMap</li>
 *        </ul></li>
 *    </ul></li>
 * </ul>
 */
public enum MapInterfaceType {

    /** The type implements SequencedMap. */
    SEQUENCED_MAP,

    /** The type implements SortedMap (and SequencedMap). */
    SORTED_MAP,

    /** The type implements NavigableMap (and SortedMap, SequencedMap). */
    NAVIGABLE_MAP,

    /** The type does not implement SequencedMap. */
    NONE

}
