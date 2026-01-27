package ch.jalu.collectionbehavior.analysis;

import ch.jalu.collectionbehavior.creator.ListCreator;
import ch.jalu.collectionbehavior.documentation.MethodBehavior;
import ch.jalu.collectionbehavior.documentation.ModificationBehavior;
import ch.jalu.collectionbehavior.method.CallEffect;
import ch.jalu.collectionbehavior.method.ListIteratorMethod;
import ch.jalu.collectionbehavior.method.ListIteratorMethodCall;
import ch.jalu.collectionbehavior.method.MethodCallProperty;
import ch.jalu.collectionbehavior.method.MethodTester;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class ListIteratorMethodAnalyzer {

    private final ListCreator listCreator;
    private final List<MethodBehavior> methodBehaviors = new ArrayList<>();
    private List<ModificationBehavior> modificationBehaviors;
    private Boolean supportsNullElements;

    public ListIteratorMethodAnalyzer(ListCreator listCreator) {
        this.listCreator = listCreator;
    }

    public static ListIteratorMethodAnalyzer analyzeMethods(ListCreator listCreator) {
        ListIteratorMethodAnalyzer methodAnalyzer = new ListIteratorMethodAnalyzer(listCreator);
        methodAnalyzer.testMethods();
        methodAnalyzer.analyzeMethodBehaviors();
        return methodAnalyzer;
    }

    private void testMethods() {
        List<ListIteratorMethodCall> methods = ListIteratorMethod.createAll();
        MethodTester methodTester = new MethodTester();

        for (ListIteratorMethodCall method : methods) {
            MethodBehavior behavior = methodTester.test(listCreator, method);
            methodBehaviors.add(behavior);
        }
    }

    private void analyzeMethodBehaviors() {
        boolean canChangeSize = false;
        boolean canBeModified = false;

        int nullCallsOk = 0;
        int nullCallsBad = 0;

        for (MethodBehavior methodBehavior : methodBehaviors) {
            if (methodBehavior.getEffect() == CallEffect.MODIFYING && methodBehavior.getException() == null) {
                canBeModified = true;
            } else if (methodBehavior.getEffect() == CallEffect.SIZE_ALTERING && methodBehavior.getEffect() == null) {
                canChangeSize = true;
            }

            if (methodBehavior.getProperties().contains(MethodCallProperty.NULL_ARGUMENT)) {
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
        if (nullCallsOk == 2) {
            supportsNullElements = true;
        } else if (nullCallsBad == 2) {
            supportsNullElements = false;
        } else if (nullCallsOk == 1 && canBeModified && !canChangeSize) {
            supportsNullElements = true;
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

    public Boolean getSupportsNullElements() {
        return supportsNullElements;
    }
}
