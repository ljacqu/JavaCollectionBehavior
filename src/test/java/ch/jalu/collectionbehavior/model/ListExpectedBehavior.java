package ch.jalu.collectionbehavior.model;

import java.util.Collections;
import java.util.EnumSet;

public class ListExpectedBehavior {

    public final boolean throwsIndexOutOfBounds;
    public final boolean throwsOnModification;
    public final boolean throwsOnSizeModification;
    public final boolean throwsOnNonModifyingModificationMethods;
    private final EnumSet<ListMethod> methodsAlwaysThrowing = EnumSet.noneOf(ListMethod.class);

    private ListExpectedBehavior(boolean throwsIndexOutOfBounds, boolean throwsOnModification,
                                 boolean throwsOnSizeModification, boolean throwsOnNonModifyingModificationMethods) {
        this.throwsIndexOutOfBounds = throwsIndexOutOfBounds;
        this.throwsOnModification = throwsOnModification;
        this.throwsOnSizeModification = throwsOnSizeModification;
        this.throwsOnNonModifyingModificationMethods = throwsOnNonModifyingModificationMethods;
    }

    public static ListExpectedBehavior alwaysThrows() {
        return new ListExpectedBehavior(false, true, true, true);
    }

    public static ListExpectedBehavior throwsIfWouldBeModified(boolean throwsIndexOutOfBounds) {
        return new ListExpectedBehavior(throwsIndexOutOfBounds, true, true, false);
    }

    public static ListExpectedBehavior throwsOnSizeModification() {
        return new ListExpectedBehavior(true, false, true, false);
    }

    public ListExpectedBehavior alwaysThrowsFor(ListMethod... methods) {
        Collections.addAll(methodsAlwaysThrowing, methods);
        return this;
    }

    public Class<? extends Exception> overrideExpectedException(ListMethod method) {
        if (methodsAlwaysThrowing.contains(method)) {
            return UnsupportedOperationException.class;
        }
        return null;
    }
}
