package ch.jalu.collectionbehavior.v2;

import ch.jalu.collectionbehavior.v2.creator.ListCreator;
import ch.jalu.collectionbehavior.v2.creator.SizeNotSupportedException;
import ch.jalu.collectionbehavior.v2.documentation.ListDocumentation;
import ch.jalu.collectionbehavior.v2.documentation.ListMethodBehavior;
import ch.jalu.collectionbehavior.v2.documentation.MethodDefinition;
import ch.jalu.collectionbehavior.v2.documentation.RandomAccessType;
import ch.jalu.collectionbehavior.v2.method.CallEffect;
import ch.jalu.collectionbehavior.v2.method.ListMethodCall;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ListUnderTest {

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
        List<String> elements = new ArrayList<>();
        Map<Integer, String> classNamesBySize = new TreeMap<>();

        for (int i = 0; i <= 20; ++i) {
            try {
                List<String> list = listCreator.createList(elements.toArray(String[]::new));
                classNamesBySize.put(i, list.getClass().getName());
            } catch (SizeNotSupportedException ignore) {
            }
            elements.add("a");
        }

        documentation.setClassNamesBySize(classNamesBySize);
    }

    public void documentNullElementSupport() {
        try {
            listCreator.createList((String)null);
            documentation.setSupportsNullElements(true);
        } catch (NullPointerException e) {
            documentation.setSupportsNullElements(false);
        } catch (SizeNotSupportedException ignore) {
        }
    }

    void test(ListMethodCall methodCall) {
        List<String> abcdList = listCreator.createAbcdListOrLargestSubset();
        List<String> copyUnmodified = new ArrayList<>(abcdList);
        List<String> copy = new ArrayList<>(abcdList);

        CallEffect effect = null;
        MethodObserver observer = new MethodObserver(copy);
        try {
            invokeMethodWithObserver(methodCall, observer);
        } catch (IndexOutOfBoundsException e) {
            effect = CallEffect.INDEX_OUT_OF_BOUNDS;
        } catch (NoSuchElementException e) {
            effect = CallEffect.NO_SUCH_ELEMENT;
        }


        String result = null;
        try {
            methodCall.invoke(abcdList);
            effect = determineEffect(copyUnmodified, abcdList);
        } catch (Exception e) {
            result = e.getClass().getSimpleName();
            effect = effect == null ? determineEffect(copy, copyUnmodified) : effect;
        }

        MethodDefinition methodDefinition = new MethodDefinition(
            observer.lastCalledMethod, observer.lastCalledMethodParameters, observer.lastArguments);
        documentation.addBehavior(new ListMethodBehavior(methodDefinition, effect, result));
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

    private static void invokeMethodWithObserver(ListMethodCall call, MethodObserver observer) {
        List<String> listProxy = (List) Proxy.newProxyInstance(List.class.getClassLoader(),
            new Class[]{ List.class }, observer);
        call.invoke(listProxy);
    }

    private static final class MethodObserver implements InvocationHandler {

        private final List<String> list;
        private String lastCalledMethod;
        private String lastCalledMethodParameters;
        private String lastArguments;

        private MethodObserver(List<String> list) {
            this.list = list;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            lastCalledMethod = method.getDeclaringClass().getSimpleName() + "#" + method.getName();

            if (method.getParameterCount() > 0) {
                lastCalledMethodParameters = Arrays.stream(method.getParameterTypes())
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", "));
            } else {
                lastCalledMethodParameters = "";
            }

            if (args == null) {
                lastArguments = "";
            } else {
                lastArguments = Arrays.stream(args)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            }

            try {
                return method.invoke(list, args);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof IndexOutOfBoundsException iob) {
                    throw iob;
                } else if (e.getCause() instanceof NoSuchElementException nse) {
                    throw nse;
                }
                throw e;
            }
        }
    }
}
