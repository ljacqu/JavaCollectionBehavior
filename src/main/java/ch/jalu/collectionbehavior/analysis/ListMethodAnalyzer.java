package ch.jalu.collectionbehavior.analysis;

import ch.jalu.collectionbehavior.creator.ListCreator;
import ch.jalu.collectionbehavior.documentation.MethodBehavior;
import ch.jalu.collectionbehavior.documentation.ModificationBehavior;
import ch.jalu.collectionbehavior.method.CallEffect;
import ch.jalu.collectionbehavior.method.ListMethod;
import ch.jalu.collectionbehavior.method.ListMethodCall;
import ch.jalu.collectionbehavior.method.MethodCallProperty;
import ch.jalu.collectionbehavior.method.MethodTester;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static ch.jalu.collectionbehavior.method.CallEffect.MODIFYING;
import static ch.jalu.collectionbehavior.method.CallEffect.SIZE_ALTERING;

public class ListMethodAnalyzer {

    private final ListCreator listCreator;
    private final List<MethodBehavior> methodBehaviors = new ArrayList<>();
    private List<ModificationBehavior> modificationBehaviors;
    private Boolean supportsNullArguments;

    public ListMethodAnalyzer(ListCreator listCreator) {
        this.listCreator = listCreator;
    }

    public static ListMethodAnalyzer analyzeMethods(ListCreator listCreator) {
        ListMethodAnalyzer methodAnalyzer = new ListMethodAnalyzer(listCreator);
        methodAnalyzer.testMethods();
        methodAnalyzer.analyzeMethodBehaviors();
        return methodAnalyzer;
    }

    private void testMethods() {
        List<ListMethodCall> methods = ListMethod.createAll();
        MethodTester methodTester = new MethodTester();

        for (ListMethodCall methodCall : methods) {
            MethodBehavior behavior = methodTester.test(listCreator, methodCall);
            methodBehaviors.add(behavior);
        }
    }

    private void analyzeMethodBehaviors() {
        boolean canChangeSize = false;
        boolean canBeModified = false;

        int nullCallsOk = 0;
        int nullCallsBad = 0;

        for (MethodBehavior methodBehavior : methodBehaviors) {
            if (methodBehavior.getEffect() == MODIFYING && methodBehavior.getException() == null) {
                canBeModified = true;
            } else if (methodBehavior.getEffect() == SIZE_ALTERING && methodBehavior.getException() == null) {
                canChangeSize = true;
            }

            if (methodBehavior.getProperties().contains(MethodCallProperty.NULL_ARGUMENT)
                && methodBehavior.getProperties().contains(MethodCallProperty.READ_METHOD)) {
                if (methodBehavior.getException() == null) {
                    ++nullCallsOk;
                } else if (methodBehavior.getEffect() != CallEffect.INDEX_OUT_OF_BOUNDS
                    && !methodBehavior.getException().equals("IndexOutOfBoundsException")) {
                    ++nullCallsBad;
                }
            }
        }

        Preconditions.checkState(!canChangeSize || canBeModified,
            "Inconsistent finding: List can change size but can't be modified");
        List<ModificationBehavior> modificationBehaviors = new ArrayList<>();
        if (canBeModified) {
            modificationBehaviors.add(ModificationBehavior.CAN_MODIFY_ENTRIES);
        }
        if (canChangeSize) {
            modificationBehaviors.add(ModificationBehavior.CAN_CHANGE_SIZE);
        }
        this.modificationBehaviors = modificationBehaviors;

        // Be strict with checks here just to make sure we don't infer something weird. As we add more method calls
        // we'll need to adapt the numbers here; at some point we can be less strict.
        if (nullCallsOk == 5) {
            supportsNullArguments = true;
        } else if (nullCallsBad >= 4) {
            supportsNullArguments = false;
        } else {
            throw new IllegalStateException("Unknown combination. Good null calls="
                + nullCallsOk + ", bad null calls=" + nullCallsBad);
        }
    }

    public List<MethodBehavior> getMethodBehaviors() {
        return methodBehaviors;
    }

    public List<ModificationBehavior> getModificationBehaviors() {
        return modificationBehaviors;
    }

    public Boolean getSupportsNullArguments() {
        return supportsNullArguments;
    }
}
