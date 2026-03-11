package ch.jalu.collectionbehavior.documentation.export;

import ch.jalu.collectionbehavior.documentation.ListIteratorDocumentation;
import ch.jalu.collectionbehavior.documentation.ModifiableProperty;

import java.util.List;

public class ListIteratorDocumentationExporter extends AbstrDocumentationExporter {

    public void toMarkdown(StringBuilder sb, ListIteratorDocumentation doc) {
        sb.append("# ").append(doc.getDescription());
        sb.append("\n## General properties");
        addMutabilityBullet(sb, doc.getModificationBehaviors());
        addNullParametersBullet(sb, doc.getSupportsNullArguments());

        sb.append("\n");
        sb.append("\n## Classes");
        addClassNames(sb, doc.getClassesByRange());

        sb.append("\n");
        sb.append("\n## Method behavior");
        addMethodBehaviors(sb, doc.getMethodBehaviors(), doc.getModificationBehaviors());
    }

    private void addMutabilityBullet(StringBuilder sb, List<ModifiableProperty> modifiableProperties) {
        sb.append("\n- ");
        if (modifiableProperties.contains(ModifiableProperty.CAN_CHANGE_SIZE)
            && modifiableProperties.contains(ModifiableProperty.CAN_MODIFY_ENTRIES)) {
            sb.append("✍\uD83C\uDFFB Modifiable list");
        } else if (modifiableProperties.contains(ModifiableProperty.CAN_CHANGE_SIZE)) {
            // Currently not for any list. If this happens, we should extend modification behavior to specify whether
            // entries can be added and/or removed.
            throw new UnsupportedOperationException("can change size but not entries");
        } else if (modifiableProperties.contains(ModifiableProperty.CAN_MODIFY_ENTRIES)) {
            sb.append("⚠\uFE0F Entries can be modified, but entries cannot be removed or added");
        } else { // not contains CAN_CHANGE_SIZE and not contains CAN_MODIFY_ENTRIES
            sb.append("\uD83D\uDD12 Unmodifiable");
        }
    }
}
