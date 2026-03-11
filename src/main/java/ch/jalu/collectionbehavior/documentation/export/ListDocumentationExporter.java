package ch.jalu.collectionbehavior.documentation.export;

import ch.jalu.collectionbehavior.documentation.ListDocumentation;
import ch.jalu.collectionbehavior.documentation.MethodBehavior;
import ch.jalu.collectionbehavior.documentation.MethodInvocation;
import ch.jalu.collectionbehavior.documentation.ModificationBehavior;
import ch.jalu.collectionbehavior.documentation.RandomAccessType;
import ch.jalu.collectionbehavior.documentation.Range;
import ch.jalu.collectionbehavior.documentation.SpliteratorCharacteristic;
import ch.jalu.collectionbehavior.documentation.Support;
import ch.jalu.collectionbehavior.method.CallEffect;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static ch.jalu.collectionbehavior.analysis.ListAnalyzer.MAX_SIZE_TO_INSTANTIATE;

public class ListDocumentationExporter {

    public String toMarkdown(ListDocumentation doc) {
        StringBuilder sb = new StringBuilder();

        sb.append("# ").append(doc.getDescription());
        sb.append("\n## General properties");
        addMutabilityBullets(sb, doc.getDoesNotRewrapItself(), doc.getModificationBehaviors());
        addSizeRestrictionBullet(sb, doc.getSupportedSize());
        addNullElementBullet(sb, doc.getNullElementSupport());
        addNullParametersBullet(sb, doc.getSupportsNullArguments());
        addRandomAccessBullet(sb, doc.getRandomAccessType());
        addNoSelfWrapBullet(sb, doc.getDoesNotRewrapItself());

        sb.append("\n");
        sb.append("\n## Classes");
        addClassNames(sb, doc); // todo: when RandomAccess is only _preserved_, there should be a second set of class sets?

        sb.append("\n");
        sb.append("\n## Spliterator characteristics");
        addSpliteratorCharacteristics(sb, doc.getSpliteratorCharacteristics());

        sb.append("\n");
        sb.append("\n## Method behavior");
        addMethodBehaviors(sb, doc.getMethodBehaviors());

        return sb.toString();
    }

    // -------
    // General
    // -------

    private void addMutabilityBullets(StringBuilder sb, Support doesNotWrapItself,
                                      List<ModificationBehavior> modificationBehaviors) {
        sb.append("\n- ");
        if (modificationBehaviors.contains(ModificationBehavior.CAN_CHANGE_SIZE)
            && modificationBehaviors.contains(ModificationBehavior.CAN_MODIFY_ENTRIES)) {
            sb.append("✍\uD83C\uDFFB Modifiable list");
        } else if (modificationBehaviors.contains(ModificationBehavior.CAN_CHANGE_SIZE)) {
            // Currently not for any list. If this happens, we should extend modification behavior to specify whether
            // entries can be added and/or removed.
            throw new UnsupportedOperationException("can change size but not entries");
        } else if (modificationBehaviors.contains(ModificationBehavior.CAN_MODIFY_ENTRIES)) {
            sb.append("⚠\uFE0F Entries can be modified, but entries cannot be removed or added");
        } else { // not contains CAN_CHANGE_SIZE and not contains CAN_MODIFY_ENTRIES
            if (modificationBehaviors.contains(ModificationBehavior.STRUCTURE_INFLUENCES_COLLECTION)) {
                Preconditions.checkState(doesNotWrapItself != Support.NOT_APPLICABLE);
                sb.append("\uD83D\uDD12 Unmodifiable list (original structure does modify the list, see below)");
            } else {
                sb.append("\uD83D\uDD12 Immutable list");
            }
        }

        if (doesNotWrapItself != Support.NOT_APPLICABLE) {
            sb.append("\n- Original structure: ");
            if (modificationBehaviors.contains(ModificationBehavior.COLLECTION_INFLUENCES_STRUCTURE)) {
                sb.append("Changes to the list change the original structure");
            } else if (modificationBehaviors.contains(ModificationBehavior.STRUCTURE_INFLUENCES_COLLECTION)) {
                sb.append("Changes to the original structure are reflected in the list");
            } else {
                sb.append("Changes to the structure or the list do not influence the other");
            }
        }
    }

    private void addSizeRestrictionBullet(StringBuilder sb, Range supportedSize) {
        if (supportedSize.min() == 0 && supportedSize.max() == null) {
            return; // Standard - nothing to write about
        }

        sb.append("\n- Size restrictions: ");
        if (supportedSize.min() == supportedSize.max()) {
            if (supportedSize.min() == 1) {
                sb.append("Can only have 1 element");
            } else if (supportedSize.min() == 0) {
                sb.append("Always empty");
            } else {
                sb.append("Can only have ").append(supportedSize.min()).append(" elements");
            }
        } else {
            throw new UnsupportedOperationException(); // Currently never happens, so not implemented :)
        }
    }

    private void addNullElementBullet(StringBuilder sb, Support supportsNullElements) {
        switch (supportsNullElements) {
            case YES -> sb.append("\n- ✅ Can have null elements");
            case NO -> sb.append("\n- ❌ Does not support null elements");
            case NOT_APPLICABLE -> { /* noop */ }
            default -> throw new IllegalStateException("Unsupported entry: " + supportsNullElements);
        }
    }

    private void addNullParametersBullet(StringBuilder sb, boolean supportsNullParams) {
        if (supportsNullParams) {
            sb.append("\n- ✅ Supports null parameters");
        } else {
            sb.append("\n- ❌ Throws an exception for null params (e.g. `list.contains(null)`)");
        }
    }

    private void addRandomAccessBullet(StringBuilder sb, RandomAccessType randomAccessType) {
        switch (randomAccessType) {
            case IMPLEMENTS -> sb.append("\n- ✅ Implements RandomAccess");
            case PRESERVES -> sb.append("\n- ❔ Implements RandomAccess only when the list it is based on also implements it");
            case DOES_NOT_IMPLEMENT -> sb.append("\n- ❌ Does not implement RandomAccess");
            default -> throw new IllegalStateException("Unsupported random access type: " + randomAccessType);
        };
    }

    private void addNoSelfWrapBullet(StringBuilder sb, Support doesNotWrapItself) {
        switch (doesNotWrapItself) {
            case YES -> sb.append("\n- ✅ Does not re-wrap lists of its own type");
            case NO -> sb.append("\n- ❌ Wraps lists of its own type again");
            case NOT_APPLICABLE -> { /* noop */ }
            default -> throw new IllegalStateException("Unsupported entry: " + doesNotWrapItself);
        }
    }

    // ------------
    // Class names
    // ------------

    private void addClassNames(StringBuilder sb, ListDocumentation listDocumentation) {
        Preconditions.checkArgument(!listDocumentation.getClassesByRange().isEmpty());

        if (listDocumentation.getClassesByRange().size() == 1) {
            Map.Entry<Range, String> firstEntry = Iterables.get(listDocumentation.getClassesByRange().entrySet(), 0);
            sb.append("\nProduces objects of the class ").append(firstEntry.getValue());
            return;
        }

        sb.append("\n| List size | Class |");
        sb.append("\n| --------- | ----- |");
        listDocumentation.getClassesByRange().forEach((range, clz) -> {
            String rangeText = Integer.toString(range.min());
            if (range.max() == MAX_SIZE_TO_INSTANTIATE && range.min() < MAX_SIZE_TO_INSTANTIATE) {
                rangeText = "≥ " + rangeText;
            } else if (range.max() != range.min()) {
                rangeText += ".." + range.max();
            }
            sb.append("\n| ").append(rangeText).append(" | ").append(clz).append("|");
        });
    }

    // ------------
    // Spliterators
    // ------------

    private void addSpliteratorCharacteristics(StringBuilder sb,
                                               Collection<SpliteratorCharacteristic> characteristics) {
        for (SpliteratorCharacteristic characteristic : characteristics) {
            sb.append("\n- ").append(characteristic.name());
        }
    }

    // ----------------
    // Method behaviors
    // ----------------

    private void addMethodBehaviors(StringBuilder sb, Collection<MethodBehavior> behaviors) {
        sb.append("\n| Method call | Effect | Exception |");
        sb.append("\n| ----------- | ------ | --------- |");

        for (MethodBehavior behavior : behaviors) {
            String methodCall = formatMethodCall(behavior.getMethodInvocation());

            sb.append("\n | ").append(methodCall)
                .append(" | ").append(formatMethodCallEffect(behavior.getEffect()))
                .append(" | ").append(formatMethodCallException(behavior.getException()))
                .append(" |");
        }
    }

    private static String formatMethodCall(MethodInvocation invocation) {
        String methodName = invocation.methodName();
        int hashSignIndex = methodName.indexOf('#');

        return methodName.substring(0, hashSignIndex + 1)
            + "**" + methodName.substring(hashSignIndex + 1)
            + "**(" + invocation.methodParameters() + ")";
    }

    private static String formatMethodCallEffect(CallEffect effect) {
        return switch (effect) {
            case MODIFYING -> "Modifying";
            case SIZE_ALTERING -> "Size-altering";
            case NON_MODIFYING -> "Non-modifying";
            case INDEX_OUT_OF_BOUNDS -> "Out of bounds index";
            case NO_SUCH_ELEMENT -> "No element";
            case ILLEGAL_STATE -> throw new UnsupportedOperationException("ILLEGAL_STATE"); // doesn't happen for List
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
            case "NoSuchElementException" -> join("\uD83E\uDEB9", exception);
            default -> throw new IllegalStateException("Unhandled exception: " + exception);
        };
    }

    private static String join(String prefix, String text) {
        return prefix + " " + text;
    }

}
