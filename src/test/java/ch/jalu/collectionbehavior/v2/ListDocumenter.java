package ch.jalu.collectionbehavior.v2;

import ch.jalu.collectionbehavior.v2.creator.ListCreator;
import ch.jalu.collectionbehavior.v2.documentation.ListDocumentation;
import ch.jalu.collectionbehavior.v2.method.ListMethod;
import ch.jalu.collectionbehavior.v2.method.ListMethodCall;

import java.util.ArrayList;
import java.util.List;

public class ListDocumenter {

    private final List<ListDocumentation> documentations = new ArrayList<>();

    static void main() {
        ListDocumenter documenter = new ListDocumenter();

        documenter.createDocumentation(ListCreator.ArrayList(), "JDK ArrayList");
        documenter.createDocumentation(ListCreator.LinkedList(), "JDK LinkedList");
        documenter.createDocumentation(ListCreator.List_of(), "JDK List#of");
        documenter.createDocumentation(ListCreator.List_copyOf(), "JDK List#copyOf");
        documenter.createDocumentation(ListCreator.Arrays_asList(), "JDK Arrays#asList");
        documenter.createDocumentation(ListCreator.Guava_ImmutableList_of(), "JDK ImmutableList#of");
        documenter.createDocumentation(ListCreator.Guava_ImmutableList_copyOf(), "Guava ImmutableList#copyOf");

        documenter.documentations.forEach(System.out::println);
    }

    private void createDocumentation(ListCreator listCreator, String description) {
        List<ListMethodCall> methods = ListMethod.createAll();

        ListUnderTest testedList = new ListUnderTest(listCreator, description);
        testedList.collectClassNamesBySize();
        testedList.documentNullElementSupport();
        testedList.checkImplementsRandomAccess();

        for (ListMethodCall method : methods) {
            testedList.test(method);
        }

        documentations.add(testedList.getDocumentation());
    }

}
