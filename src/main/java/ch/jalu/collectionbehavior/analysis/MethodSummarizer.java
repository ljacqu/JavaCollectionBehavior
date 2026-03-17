package ch.jalu.collectionbehavior.analysis;

import ch.jalu.collectionbehavior.documentation.MethodBehavior;
import ch.jalu.collectionbehavior.method.CallEffect;
import ch.jalu.collectionbehavior.method.MethodCallProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MethodSummarizer {

    public Summary summarize(List<MethodBehavior> methods) {
        List<MethodBehavior> methodsCopy = new ArrayList<>(methods);
        String modificationSummary = checkUnsupportedOpExceptions(methodsCopy);
        String oobSummary = checkIndexOutOfBoundsException(methodsCopy);
        String nullSummary = checkNullArgumentExceptions(methodsCopy);


        List<String> summaryPoints = Stream.of(oobSummary, nullSummary, modificationSummary)
            .filter(Objects::nonNull)
            .toList();
        return new Summary(summaryPoints, methodsCopy);
    }

    private String checkIndexOutOfBoundsException(List<MethodBehavior> methods) {
        List<MethodBehavior> callsWithIoobe = new ArrayList<>();
        List<MethodBehavior> callsWithUoe = new ArrayList<>();
        List<MethodBehavior> otherCalls = new ArrayList<>();

        for (MethodBehavior method : methods) {
            if (method.getEffect() == CallEffect.INDEX_OUT_OF_BOUNDS) {
                if ("IndexOutOfBoundsException".equals(method.getException())) {
                    callsWithIoobe.add(method);
                } else if ("UnsupportedOperationException".equals(method.getException())) {
                    callsWithUoe.add(method);
                } else {
                    otherCalls.add(method);
                }
            }
        }

        if (!callsWithIoobe.isEmpty() && callsWithUoe.isEmpty() && otherCalls.isEmpty()) {
            methods.removeAll(callsWithIoobe);
            return "Throws IndexOutOfBoundsException if an index is invalid";
        } else if (!callsWithUoe.isEmpty() && callsWithIoobe.isEmpty() && otherCalls.isEmpty()) {
            methods.removeAll(callsWithUoe);
            return "Throws UnsupportedOperationException if an index is invalid";
        }
        return null;
    }

    private String checkNullArgumentExceptions(List<MethodBehavior> methods) {
        List<MethodBehavior> callsWithNpe = new ArrayList<>();
        List<MethodBehavior> callsWithNoException = new ArrayList<>();
        List<MethodBehavior> callsWithOther = new ArrayList<>();

        for (MethodBehavior method : methods) {
            if (method.getProperties().contains(MethodCallProperty.NULL_ARGUMENT)
                && method.getEffect() == CallEffect.NON_MODIFYING) {
                if ("NullPointerException".equals(method.getException())) {
                    callsWithNpe.add(method);
                } else if (method.getException() == null) {
                    callsWithNoException.add(method);
                } else {
                    callsWithOther.add(method);
                }
            }
        }

        if (!callsWithNpe.isEmpty() && callsWithNoException.isEmpty() && callsWithOther.isEmpty()) {
            methods.removeAll(callsWithNpe);
            return "Throws NullPointerException if an argument is null";
        }
        return null;
    }

    private String checkUnsupportedOpExceptions(List<MethodBehavior> methods) {
        List<MethodBehavior> callsWithUoe = new ArrayList<>();
        List<MethodBehavior> callsWithNoException = new ArrayList<>();
        List<MethodBehavior> callsWithOther = new ArrayList<>();

        for (MethodBehavior method : methods) {
            if (method.getEffect() == CallEffect.MODIFYING || method.getEffect() == CallEffect.SIZE_ALTERING) {
                if ("UnsupportedOperationException".equals(method.getException())) {
                    callsWithUoe.add(method);
                } else if (method.getException() == null) {
                    callsWithNoException.add(method);
                } else {
                    callsWithOther.add(method);
                }
            }
        }

        if (!callsWithUoe.isEmpty() && callsWithNoException.isEmpty() && callsWithOther.isEmpty()) {
            methods.removeAll(callsWithUoe);
            return "Throws UnsupportedOperationException when a method call would modify the list";
        }
        if (!callsWithNoException.isEmpty() && callsWithUoe.isEmpty() && callsWithOther.isEmpty()) {
            // No summary point, but remove all modifying calls, since they just work
            methods.removeAll(callsWithNoException);
            // Additionally, remove all non-modifying calls that don't have an exception
            methods.removeIf(method -> method.getException() == null
                && method.getEffect() == CallEffect.NON_MODIFYING);
        }
        return null;
    }

    public record Summary(List<String> summary, List<MethodBehavior> remainingMethods) {

    }
}
