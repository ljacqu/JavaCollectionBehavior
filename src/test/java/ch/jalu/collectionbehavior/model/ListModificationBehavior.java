package ch.jalu.collectionbehavior.model;

import java.util.Collections;
import java.util.EnumSet;

public class ListModificationBehavior {

    public final boolean isImmutable;
    public final boolean throwsIndexOutOfBounds;
    public final boolean throwsOnModification;
    public final boolean throwsOnSizeModification;
    public final boolean throwsOnNonModifyingModificationMethods;
    private final EnumSet<ListMethod> methodsAlwaysThrowing = EnumSet.noneOf(ListMethod.class);

    private ListModificationBehavior(boolean isImmutable, boolean throwsIndexOutOfBounds, boolean throwsOnModification,
                                     boolean throwsOnSizeModification, boolean throwsOnNonModifyingModificationMethods) {
        this.isImmutable = isImmutable;
        this.throwsIndexOutOfBounds = throwsIndexOutOfBounds;
        this.throwsOnModification = throwsOnModification;
        this.throwsOnSizeModification = throwsOnSizeModification;
        this.throwsOnNonModifyingModificationMethods = throwsOnNonModifyingModificationMethods;
    }

    public static ListModificationBehavior mutable() {
        return new ListModificationBehavior(false, true, false, false, false);
    }

    public static Builder immutable() {
        return new Builder(true);
    }

    public static Builder unmodifiable() {
        return new Builder(false);
    }

    public ListModificationBehavior alwaysThrowsFor(ListMethod... methods) {
        Collections.addAll(methodsAlwaysThrowing, methods);
        return this;
    }

    public Class<? extends Exception> overrideExpectedException(ListMethod method) {
        if (methodsAlwaysThrowing.contains(method)) {
            return UnsupportedOperationException.class;
        }
        return null;
    }

    public boolean isMutable() {
        return !throwsOnModification && !throwsOnSizeModification;
    }

    public static final class Builder {

        private final boolean isImmutable;

        Builder(boolean isImmutable) {
            this.isImmutable = isImmutable;
        }

        public ListModificationBehavior alwaysThrows() {
            return new ListModificationBehavior(isImmutable, false, true, true, true);
        }

        public ListModificationBehavior throwsIfWouldBeModified(boolean throwsIndexOutOfBounds) {
            return new ListModificationBehavior(isImmutable, throwsIndexOutOfBounds, true, true, false);
        }

        public ListModificationBehavior throwsOnSizeModification() {
            return new ListModificationBehavior(isImmutable, true, false, true, false);
        }
    }
}
