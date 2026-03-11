package ch.jalu.collectionbehavior.documentation.export;

import ch.jalu.collectionbehavior.documentation.MethodBehavior;
import ch.jalu.collectionbehavior.documentation.MethodInvocation;
import ch.jalu.collectionbehavior.documentation.ModifiableProperty;
import ch.jalu.collectionbehavior.documentation.Range;
import ch.jalu.collectionbehavior.method.CallEffect;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static ch.jalu.collectionbehavior.analysis.ListAnalyzer.MAX_SIZE_TO_INSTANTIATE;

/**
 * Common parent of documentation exporters.
 */
abstract class AbstrDocumentationExporter {

    protected static void addNullParametersBullet(StringBuilder sb, boolean supportsNullParams) {
        if (supportsNullParams) {
            sb.append("\n- ✅ Supports null parameters");
        } else {
            sb.append("\n- ❌ Throws an exception for null params (e.g. `list.contains(null)`)");
        }
    }

    protected static void addClassNames(StringBuilder sb, Map<Range, String> classesByRange) {
        Preconditions.checkArgument(!classesByRange.isEmpty());

        if (classesByRange.size() == 1) {
            Map.Entry<Range, String> firstEntry = Iterables.get(classesByRange.entrySet(), 0);
            sb.append("\nProduces objects of the class ").append(firstEntry.getValue());
            return;
        }

        sb.append("\n| List size | Class |");
        sb.append("\n| --------- | ----- |");
        classesByRange.forEach((range, clz) -> {
            String rangeText = Integer.toString(range.min());
            if (range.max() == MAX_SIZE_TO_INSTANTIATE && range.min() < MAX_SIZE_TO_INSTANTIATE) {
                rangeText = "≥ " + rangeText;
            } else if (range.max() != range.min()) {
                rangeText += ".." + range.max();
            }
            sb.append("\n| ").append(rangeText).append(" | ").append(clz).append("|");
        });
    }

    // ----------------
    // Method behaviors
    // ----------------

    protected void addMethodBehaviors(StringBuilder sb, Collection<MethodBehavior> behaviors,
                                      List<ModifiableProperty> modifiableProperties) {
        final boolean modifiableWithFixedSize = modifiableProperties.contains(ModifiableProperty.CAN_MODIFY_ENTRIES)
            && !modifiableProperties.contains(ModifiableProperty.CAN_CHANGE_SIZE);

        sb.append("\n| Method call | Effect | Exception |");
        sb.append("\n| ----------- | ------ | --------- |");

        for (MethodBehavior behavior : behaviors) {
            String methodCall = formatMethodCall(behavior.getMethodInvocation());

            sb.append("\n | ").append(methodCall)
                .append(" | ").append(formatMethodCallEffect(behavior.getEffect(), modifiableWithFixedSize))
                .append(" | ").append(formatMethodCallException(behavior.getException()))
                .append(" |");
        }
    }

    private static String formatMethodCall(MethodInvocation invocation) {
        String methodName = invocation.methodName();
        int hashSignIndex = methodName.indexOf('#');

        return methodName.substring(0, hashSignIndex + 1)
            + "**" + methodName.substring(hashSignIndex + 1)
            + "**(" + invocation.arguments() + ")";
    }

    private static String formatMethodCallEffect(CallEffect effect, boolean modifiableWithFixedSize) {
        if (modifiableWithFixedSize && effect == CallEffect.SIZE_ALTERING) {
            // Size altering and modifying is treated the same, unless it's relevant to the list -> that's when it can
            // modify existing entries but it can't change size
            return "Modifying (size)";
        }

        return switch (effect) {
            case MODIFYING, SIZE_ALTERING -> "Modifying";
            case NON_MODIFYING -> "–";
            case INDEX_OUT_OF_BOUNDS -> "Invalid index";
            case NO_SUCH_ELEMENT -> "No element";
            case ILLEGAL_STATE -> "n/a"; // doesn't happen for List
        };
    }

    private static String formatMethodCallException(String exception) {
        if (exception == null) {
            return "";
        }

        return switch (exception) {
            case "UnsupportedOperationException" -> join("⛔", exception);
            case "IllegalStateException" -> join("\uD83D\uDEAB", exception);
            case "NullPointerException" -> join("❔", exception);
            case "IndexOutOfBoundsException", "ArrayIndexOutOfBoundsException" -> join("\uD83D\uDD0D", exception);
            case "NoSuchElementException" -> join("\uD83D\uDD75\uFE0F", exception);
            default -> throw new IllegalStateException("Unhandled exception: " + exception);
        };
    }

    private static String join(String prefix, String text) {
        return prefix + " " + text;
    }
}
