package ch.jalu.collectionbehavior.v2;

import ch.jalu.collectionbehavior.v2.creator.ListCreator;
import ch.jalu.collectionbehavior.v2.creator.ListWithBackingStructure;
import ch.jalu.collectionbehavior.v2.creator.SizeNotSupportedException;
import ch.jalu.collectionbehavior.v2.documentation.BackingStructureBehavior;
import ch.jalu.collectionbehavior.v2.documentation.ListDocumentation;
import ch.jalu.collectionbehavior.v2.documentation.MethodBehavior;
import ch.jalu.collectionbehavior.v2.documentation.RandomAccessType;
import ch.jalu.collectionbehavior.v2.documentation.Range;
import ch.jalu.collectionbehavior.v2.method.CallEffect;
import ch.jalu.collectionbehavior.v2.method.ListMethodCall;
import ch.jalu.collectionbehavior.v2.method.MethodInvocationRecorder;
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
import java.util.stream.Collectors;

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
                return newClassesByRange.entrySet().stream()
                    .map(e -> Map.entry(new Range(-e.getKey().min(), -e.getKey().max()), e.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (_, _) -> {
                            throw new IllegalStateException();
                        },
                        LinkedHashMap::new));
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
                documentation.addBackingStructureBehavior(BackingStructureBehavior.STRUCTURE_INFLUENCES_COLLECTION);
            }

            // Check if changing the list (if allowed) changes the backing structure
            listWithBackingStructure = lbc.createListWithBackingStructure();
            list = listWithBackingStructure.getList();
            Preconditions.checkState(list.equals(List.of("a", "b", "c", "d")));
            try {
                list.set(2, "changed");
                if (listWithBackingStructure.getBackingStructureAsList().equals(List.of("a", "b", "changed", "d"))) {
                    documentation.addBackingStructureBehavior(BackingStructureBehavior.COLLECTION_INFLUENCES_STRUCTURE);
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
            effect = effect == null ? determineEffect(copy, copyUnmodified) : effect;
        }

        documentation.addBehavior(new MethodBehavior(observer.getLastMethodCall(), effect, exception));
    }

    public ListDocumentation getDocumentation() {
        return documentation;
    }

    private static CallEffect determineEffect(List<String> copyUnmodified, List<String> copy) {
        if (copy.size() != copyUnmodified.size()) {
            return CallEffect.SIZE_ALTERING;
        }

        return copy.equals(copyUnmodified)
            ? CallEffect.NON_MODIFYING
            : CallEffect.MODIFYING;
    }

    private static void invokeMethodWithObserver(ListMethodCall call, MethodInvocationRecorder observer) {
        List<String> listProxy = (List) Proxy.newProxyInstance(List.class.getClassLoader(),
            new Class[]{ List.class }, observer);
        call.invoke(listProxy);
    }

}
