package ch.jalu.collectionbehavior.documentation.export;

import ch.jalu.collectionbehavior.documentation.ListDocumentation;
import ch.jalu.collectionbehavior.documentation.MethodBehavior;
import ch.jalu.collectionbehavior.method.CallEffect;

import java.util.List;

public class ListMethodsDocumentationExporter {

    public StringBuilder exportMethodsTable(List<ListDocumentation> listDocs) {
        StringBuilder sb = new StringBuilder();

        // Table header
        sb.append("| Method | ");
        for (ListDocumentation listDoc : listDocs) {
            sb.append(listDoc.getDescription()).append(" | ");
        }
        sb.append("\n| ---- ")
            .append(" | ----- ".repeat(listDocs.size()))
            .append(" |");

        // All list documentations have the same method behaviors in the same order
        final int totalMethods = listDocs.get(0).getMethodBehaviors().size();
        for (int i = 0; i < totalMethods; ++i) {
            MethodBehavior method = listDocs.get(0).getMethodBehaviors().get(i);
            sb.append("\n| ").append(method.getMethodInvocation().methodName()).append("(")
                .append(method.getMethodInvocation().arguments()).append(") | ");

            for (ListDocumentation listDoc : listDocs) {
                MethodBehavior methodBehavior = listDoc.getMethodBehaviors().get(i);
                String value = methodBehavior.getException() == null
                    ? "✅ " + formatEffectForNonException(methodBehavior.getEffect())
                    : methodBehavior.getException();
                sb.append(value).append(" | ");
            }
        }

        return sb;
    }

    private static String formatEffectForNonException(CallEffect effect) {
        return switch (effect) {
            case MODIFYING -> "modifies";
            case SIZE_ALTERING -> "modifies (size)";
            case NON_MODIFYING -> "non-mod.";
            case ILLEGAL_STATE, INDEX_OUT_OF_BOUNDS, NO_SUCH_ELEMENT ->
                throw new IllegalStateException("Unexpected effect: " + effect);
        };
    }
}
