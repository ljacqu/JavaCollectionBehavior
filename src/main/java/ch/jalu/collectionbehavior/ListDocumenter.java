package ch.jalu.collectionbehavior;

import ch.jalu.collectionbehavior.creator.ListCreator;
import ch.jalu.collectionbehavior.creator.SizeNotSupportedException;
import ch.jalu.collectionbehavior.documentation.CollectionDocumentation;
import ch.jalu.collectionbehavior.documentation.ListDocumentation;
import ch.jalu.collectionbehavior.documentation.ListIteratorDocumentation;
import ch.jalu.collectionbehavior.documentation.MethodBehavior;
import ch.jalu.collectionbehavior.documentation.Range;
import ch.jalu.collectionbehavior.method.ListIteratorMethod;
import ch.jalu.collectionbehavior.method.ListIteratorMethodCall;
import ch.jalu.collectionbehavior.method.ListMethod;
import ch.jalu.collectionbehavior.method.ListMethodCall;
import ch.jalu.collectionbehavior.method.MethodTester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import static ch.jalu.collectionbehavior.ListUnderTest.collectClassesByRange;

public class ListDocumenter {

    private final List<CollectionDocumentation> documentations = new ArrayList<>();

    static void main() {
        ListDocumenter documenter = new ListDocumenter();

        documenter.document(ListCreator.ArrayList(), "JDK ArrayList");
        documenter.document(ListCreator.LinkedList(), "JDK LinkedList");
        documenter.document(ListCreator.List_of(), "JDK List#of");
        documenter.document(ListCreator.List_copyOf(), "JDK List#copyOf");
        documenter.document(ListCreator.Arrays_asList(), "JDK Arrays#asList");
        documenter.document(ListCreator.Guava_ImmutableList_of(), "JDK ImmutableList#of");
        documenter.document(ListCreator.Guava_ImmutableList_copyOf(), "Guava ImmutableList#copyOf");
        documenter.document(ListCreator.Collections_emptyList(), "JDK Collections#emptyList");
        documenter.document(ListCreator.Collections_singletonList(), "JDK Collections#singletonList");
        documenter.document(ListCreator.Collections_unmodifiableList(), "JDK Collections#unmodifiableList");
        documenter.document(ListCreator.Collectors_toList(), "JDK Collectors#toList");
        documenter.document(ListCreator.Collectors_toUnmodifiableList(), "JDK Collectors#toUnmodifiableList");
        documenter.document(ListCreator.Stream_toList(), "JDK Stream#toList");

        documenter.documentations.forEach(System.out::println);
    }

    private void document(ListCreator listCreator, String description) {
        ListDocumentation doc = createDocumentation(listCreator, description);

        ListCreator subListCreator = new SubListCreator(listCreator, doc.getSupportedSize());
        createDocumentation(subListCreator, description + " (sublist)");
        createDocumentationForListIterator(listCreator, description + " (listIterator)");
    }

    private ListDocumentation createDocumentation(ListCreator listCreator, String description) {
        List<ListMethodCall> methods = ListMethod.createAll();

        ListUnderTest testedList = new ListUnderTest(listCreator, description);
        testedList.collectClassNamesBySize();
        testedList.documentNullElementSupport();
        testedList.checkImplementsRandomAccess();
        testedList.documentSelfWrapping();
        testedList.documentBehaviorWithBackingStructure();

        for (ListMethodCall method : methods) {
            testedList.test(method);
        }

        testedList.analyzeMethodBehaviors();
        ListDocumentation documentation = testedList.getDocumentation();
        documentations.add(documentation);
        return documentation;
    }

    private void createDocumentationForListIterator(ListCreator listCreator, String description) {
        List<ListIteratorMethodCall> methods = ListIteratorMethod.createAll();

        ListIteratorDocumentation documentation = new ListIteratorDocumentation(description);

        MethodTester methodTester = new MethodTester();

        for (ListIteratorMethodCall method : methods) {
            MethodBehavior behavior = methodTester.test(description, listCreator, method);
            documentation.addBehavior(behavior);
        }

        collectListIteratorClassesBySize(listCreator, documentation);

        documentations.add(documentation);
    }

    void collectListIteratorClassesBySize(ListCreator listCreator, ListIteratorDocumentation iteratorDocumentation) {
        List<String> elements = Collections.nCopies(20, "o");
        TreeMap<Integer, String> classNamesBySize = new TreeMap<>();

        for (int i = 0; i <= 20; ++i) {
            try {
                List<String> list = listCreator.createList(elements.subList(0, i).toArray(String[]::new));
                ListIterator<String> iterator = list.listIterator();
                classNamesBySize.put(i, iterator.getClass().getName());
            } catch (SizeNotSupportedException ignore) {
            }
        }

        Map<Range, String> classesByRange = collectClassesByRange(classNamesBySize);
        iteratorDocumentation.setClassesByRange(classesByRange);
    }

    private static final class SubListCreator extends ListCreator {

        private final ListCreator parent;
        private final boolean usePadding;

        private SubListCreator(ListCreator parent, Range parentRange) {
            this.parent = parent;
            this.usePadding = parentRange.min() == 0 && parentRange.max() == null;
        }

        @Override
        public List<String> createAbcdListOrLargestSubset() {
            List<String> abcdOrSubset = parent.createAbcdListOrLargestSubset();
            boolean padElements = abcdOrSubset.size() == 4;
            return createList(abcdOrSubset.toArray(String[]::new), padElements);
        }

        @Override
        public List<String> createList(String... elements) throws SizeNotSupportedException {
            return createList(elements, usePadding);
        }

        private List<String> createList(String[] elements, boolean usePadding) {
            if (!usePadding) {
                return parent.createList(elements).subList(0, elements.length);
            }

            String[] paddedElements = padElements(elements);
            List<String> list = parent.createList(paddedElements);
            return list.subList(1, paddedElements.length - 1);
        }

        private static String[] padElements(String[] elements) {
            String[] paddedElements = new String[2 + elements.length];

            paddedElements[0] = "0";
            System.arraycopy(elements, 0, paddedElements, 1, elements.length);
            paddedElements[paddedElements.length - 1] = "0";

            return paddedElements;
        }
    }
}
