package ch.jalu.collectionbehavior;

import ch.jalu.collectionbehavior.creator.ListCreator;
import ch.jalu.collectionbehavior.creator.ListWithBackingStructure;
import ch.jalu.collectionbehavior.creator.SizeNotSupportedException;
import ch.jalu.collectionbehavior.documentation.ListDocumentation;
import ch.jalu.collectionbehavior.documentation.MethodBehavior;
import ch.jalu.collectionbehavior.documentation.ModificationBehavior;
import ch.jalu.collectionbehavior.documentation.RandomAccessType;
import ch.jalu.collectionbehavior.documentation.Range;
import ch.jalu.collectionbehavior.method.CallEffect;
import ch.jalu.collectionbehavior.method.ListMethodCall;
import ch.jalu.collectionbehavior.method.MethodCallProperty;
import ch.jalu.collectionbehavior.method.MethodInvocationRecorder;
import com.google.common.base.Preconditions;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.TreeMap;

import static ch.jalu.collectionbehavior.method.CallEffect.MODIFYING;
import static ch.jalu.collectionbehavior.method.CallEffect.SIZE_ALTERING;

public class ListUnderTest {

    private static final int MAX_SIZE_TO_INSTANTIATE = 20;

    private final ListCreator listCreator;
    private final ListDocumentation documentation;

    ListUnderTest(ListCreator listCreator, String description) {
        this.listCreator = listCreator;
        this.documentation = new ListDocumentation(description);
    }

    void checkImplementsRandomAccess() {
        if (listCreator instanceof ListCreator.ListBasedListCreator lcl) {
            ArrayList<String> arrayList = new ArrayList<>(List.of("a", "b"));
            LinkedList<String> linkedList = new LinkedList<>(List.of("a", "b"));

            boolean arrayListProducesRandomAccess = lcl.fromList(arrayList) instanceof RandomAccess;
            boolean linkedListProducesRandomAccess = lcl.fromList(linkedList) instanceof RandomAccess;

            if (arrayListProducesRandomAccess && linkedListProducesRandomAccess) {
                documentation.setRandomAccessType(RandomAccessType.IMPLEMENTS);
            } else if (arrayListProducesRandomAccess && !linkedListProducesRandomAccess) {
                documentation.setRandomAccessType(RandomAccessType.PRESERVES);
            } else if (!arrayListProducesRandomAccess && !linkedListProducesRandomAccess) {
                documentation.setRandomAccessType(RandomAccessType.DOES_NOT_IMPLEMENT);
            } else {
                throw new IllegalStateException("RandomAccess check (ArrayList=" + arrayListProducesRandomAccess
                    + ", LinkedList=" + linkedListProducesRandomAccess + ")");
            }
        } else {
            List<String> abcdList = listCreator.createAbcdListOrLargestSubset();
            documentation.setRandomAccessType(abcdList instanceof RandomAccess
                ? RandomAccessType.IMPLEMENTS
                : RandomAccessType.DOES_NOT_IMPLEMENT);
        }
    }

    void collectClassNamesBySize() {
        List<String> elements = Collections.nCopies(MAX_SIZE_TO_INSTANTIATE, "o");
        TreeMap<Integer, String> classNamesBySize = new TreeMap<>();

        for (int i = 0; i <= MAX_SIZE_TO_INSTANTIATE; ++i) {
            try {
                List<String> list = listCreator.createList(elements.subList(0, i).toArray(String[]::new));
                classNamesBySize.put(i, list.getClass().getName());
            } catch (SizeNotSupportedException ignore) {
            }
        }

        int minSize = classNamesBySize.navigableKeySet().first();
        int maxSize = classNamesBySize.navigableKeySet().last();
        documentation.setSupportedSize(maxSize == MAX_SIZE_TO_INSTANTIATE
            ? new Range(minSize, null)
            : new Range(minSize, maxSize));

        Map<Range, String> classesByRange = collectClassesByRange(classNamesBySize);
        classesByRange.putAll(getClassesByRangeAddition(classesByRange));

        documentation.setClassesByRange(classesByRange);
    }

    // TODO: clean up
    private Map<Range, String> getClassesByRangeAddition(Map<Range, String> classesByRange) {
        if (listCreator instanceof ListCreator.ListBasedListCreator lbc) {
            TreeMap<Integer, String> classNamesBySize = new TreeMap<>();
            for (int i = 0; i <= MAX_SIZE_TO_INSTANTIATE; ++i) {
                List<String> linkedList = new LinkedList<>(Collections.nCopies(i, "o"));
                try {
                    List<String> list = lbc.fromList(linkedList);
                    classNamesBySize.put(i, list.getClass().getName());
                } catch (SizeNotSupportedException ignore) {
                }
            }

            Map<Range, String> newClassesByRange = collectClassesByRange(classNamesBySize);
            if (!newClassesByRange.equals(classesByRange)) { // Something is different
                LinkedHashMap<Range, String> classesByRangeAdditions = new LinkedHashMap<>(classesByRange.size());
                classesByRange.forEach((range, clazz) -> {
                    Range rangeAddition = new Range(100 + range.min(), range.max() == null ? null : 100 + range.max());
                    classesByRangeAdditions.put(rangeAddition, clazz);
                });
                return classesByRangeAdditions;
            }
        }
        return Collections.emptyMap();
    }

    private static Map<Range, String> collectClassesByRange(TreeMap<Integer, String> classNamesBySize) {
        Map<Range, String> classesByRange = new LinkedHashMap<>();

        Integer start = null;
        Integer last = null;
        String currentClass = null;

        for (Map.Entry<Integer, String> entry : classNamesBySize.entrySet()) {
            if (currentClass != null && !currentClass.equals(entry.getValue())) {
                classesByRange.put(new Range(start, last), currentClass);
                currentClass = null;
            }

            if (currentClass == null) {
                start = entry.getKey();
                last = entry.getKey();
                currentClass = entry.getValue();
            } else { // currentClass.equals(entry.getValue())
                last = entry.getKey();
            }
        }

        if (currentClass != null) {
            classesByRange.put(new Range(start, last), currentClass);
        }
        return classesByRange;
    }

    void documentNullElementSupport() {
        try {
            listCreator.createList((String)null);
            documentation.setSupportsNullElements(true);
        } catch (NullPointerException e) {
            documentation.setSupportsNullElements(false);
        } catch (SizeNotSupportedException ignore) {
        }
    }

    void documentSelfWrapping() {
        if (listCreator instanceof ListCreator.ListBasedListCreator lbc) {
            List<String> list1 = lbc.fromList(Arrays.asList("o", "g"));
            List<String> list2 = lbc.fromList(list1);

            boolean doesNotWrapItself = list1 == list2; // == intentional, need to see if it's the same object
            documentation.setDoesNotRewrapItself(doesNotWrapItself);
        }
    }

    void documentBehaviorWithBackingStructure() {
        if (listCreator instanceof ListCreator.BackingStructurBasedListCreator lbc) {
            // Check if changing the backing structure changes the list
            ListWithBackingStructure listWithBackingStructure = lbc.createListWithBackingStructure();
            List<String> list = listWithBackingStructure.getList();
            Preconditions.checkState(list.equals(List.of("a", "b", "c", "d")));
            listWithBackingStructure.modifyBackingStructure();
            if (!list.equals(List.of("a", "b", "c", "d"))) {
                documentation.addModificationBehavior(ModificationBehavior.STRUCTURE_INFLUENCES_COLLECTION);
            }

            // Check if changing the list (if allowed) changes the backing structure
            listWithBackingStructure = lbc.createListWithBackingStructure();
            list = listWithBackingStructure.getList();
            Preconditions.checkState(list.equals(List.of("a", "b", "c", "d")));
            try {
                list.set(2, "changed");
                if (listWithBackingStructure.getBackingStructureAsList().equals(List.of("a", "b", "changed", "d"))) {
                    documentation.addModificationBehavior(ModificationBehavior.COLLECTION_INFLUENCES_STRUCTURE);
                }
            } catch (UnsupportedOperationException ignore) {
            }
        }
    }

    void test(ListMethodCall methodCall) {
        List<String> abcdList = listCreator.createAbcdListOrLargestSubset();
        List<String> copyUnmodified = new ArrayList<>(abcdList);
        List<String> copy = new ArrayList<>(abcdList);

        CallEffect effect = null;
        MethodInvocationRecorder observer = new MethodInvocationRecorder(copy);
        try {
            invokeMethodWithObserver(methodCall, observer);
        } catch (IndexOutOfBoundsException e) {
            effect = CallEffect.INDEX_OUT_OF_BOUNDS;
        } catch (NoSuchElementException e) {
            effect = CallEffect.NO_SUCH_ELEMENT;
        }


        String exception = null;
        try {
            methodCall.invoke(abcdList);
            effect = determineEffect(copyUnmodified, abcdList);
        } catch (Exception e) {
            exception = e.getClass().getSimpleName();
            effect = effect == null ? determineEffect(copyUnmodified, copy) : effect;
        }

        // Sanity check: modification should be the same as on our copy
        if (exception == null && !abcdList.equals(copy)) {
            throw new IllegalStateException("For " + documentation.getDescription()
                + ": expected list to be equal to copy for call " + observer.getLastMethodCall()
                + ", but got " + abcdList + " vs. copy list: " + copy);
        }

        documentation.addBehavior(
            new MethodBehavior(observer.getLastMethodCall(), effect, exception, methodCall.properties()));
    }

    void analyzeMethodBehaviors() {
        boolean canChangeSize = false;
        boolean canBeModified = false;

        int nullCallsOk = 0;
        int nullCallsBad = 0;

        for (MethodBehavior methodBehavior : documentation.getMethodBehaviors()) {
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
        if (canBeModified) {
            documentation.addModificationBehavior(ModificationBehavior.CAN_MODIFY_ENTRIES);
        }
        if (canChangeSize) {
            documentation.addModificationBehavior(ModificationBehavior.CAN_CHANGE_SIZE);
        }

        // Be strict with checks here just to make sure we don't infer something weird. As we add more method calls
        // we'll need to adapt the numbers here; at some point we can be less strict.
        if (nullCallsOk == 5) {
            documentation.setSupportsNullArguments(true);
        } else if (nullCallsBad >= 4) {
            documentation.setSupportsNullArguments(false);
        } else {
            throw new IllegalStateException("Unknown combination. Good null calls="
                + nullCallsOk + ", bad null calls=" + nullCallsBad);
        }
    }

    public ListDocumentation getDocumentation() {
        return documentation;
    }

    private static CallEffect determineEffect(List<String> copyUnmodified, List<String> copy) {
        if (copy.size() != copyUnmodified.size()) {
            return SIZE_ALTERING;
        }

        return copy.equals(copyUnmodified)
            ? CallEffect.NON_MODIFYING
            : MODIFYING;
    }

    private static void invokeMethodWithObserver(ListMethodCall call, MethodInvocationRecorder observer) {
        List<String> listProxy = (List) Proxy.newProxyInstance(List.class.getClassLoader(),
            new Class[]{ List.class }, observer);
        call.invoke(listProxy);
    }
}
