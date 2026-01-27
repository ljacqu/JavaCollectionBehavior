package ch.jalu.collectionbehavior.method;

import ch.jalu.collectionbehavior.creator.ListCreator;
import ch.jalu.collectionbehavior.documentation.MethodBehavior;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import static ch.jalu.collectionbehavior.method.CallEffect.MODIFYING;
import static ch.jalu.collectionbehavior.method.CallEffect.SIZE_ALTERING;

public class MethodTester {

    public MethodBehavior test(ListCreator listCreator, ListMethodCall methodCall) {
        List<String> abcdList = listCreator.createAbcdListOrLargestSubset();
        List<String> copyUnmodified = new ArrayList<>(abcdList);
        List<String> copy = new ArrayList<>(abcdList);

        CallEffect effect = null;
        MethodInvocationRecorder<List<String>> observer = new MethodInvocationRecorder<>(copy);
        try {
            observer.invoke(methodCall, List.class);
        } catch (IndexOutOfBoundsException e) {
            effect = CallEffect.INDEX_OUT_OF_BOUNDS;
        } catch (NoSuchElementException e) {
            effect = CallEffect.NO_SUCH_ELEMENT;
        } catch (IllegalStateException e) {
            effect = CallEffect.ILLEGAL_STATE;
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
            throw new IllegalStateException("Expected list to be equal to copy for call " + observer.getLastMethodCall()
                + ", but got " + abcdList + " vs. copy list: " + copy);
        }

        return new MethodBehavior(observer.getLastMethodCall(), effect, exception, methodCall.properties());
    }

    public MethodBehavior test(ListCreator listCreator, ListIteratorMethodCall methodCall) {
        List<String> abcdList = listCreator.createAbcdListOrLargestSubset();
        List<String> copyUnmodified = new ArrayList<>(abcdList);
        List<String> copy = new ArrayList<>(abcdList);

        CallEffect effect = null;
        ListIterator<String> copyIterator = copy.listIterator();
        advanceIteratorIfHasNext(copyIterator);
        MethodInvocationRecorder<ListIterator<String>> observer = new MethodInvocationRecorder<>(copyIterator);
        try {
            observer.invoke(methodCall, ListIterator.class);
        } catch (IllegalStateException e) {
            effect = CallEffect.ILLEGAL_STATE;
        }

        String exception = null;
        ListIterator<String> abcdListIterator = abcdList.listIterator();
        advanceIteratorIfHasNext(abcdListIterator);
        try {
            methodCall.invoke(abcdListIterator);
            effect = determineEffect(copyUnmodified, abcdList);
        } catch (Exception e) {
            exception = e.getClass().getSimpleName();
            effect = effect == null ? determineEffect(copyUnmodified, copy) : effect;
        }

        // Sanity check: modification should be the same as on our copy
        if (exception == null && !abcdList.equals(copy)) {
            throw new IllegalStateException("Expected list to be equal to copy for call " + observer.getLastMethodCall()
                + ", but got " + abcdList + " vs. copy list: " + copy);
        }

        return new MethodBehavior(observer.getLastMethodCall(), effect, exception, methodCall.properties());
    }

    private static void advanceIteratorIfHasNext(Iterator<?> iterator) {
        if (iterator.hasNext()) {
            iterator.next();
        }
    }

    private static CallEffect determineEffect(List<String> copyUnmodified, List<String> copy) {
        if (copy.size() != copyUnmodified.size()) {
            return SIZE_ALTERING;
        }

        return copy.equals(copyUnmodified)
            ? CallEffect.NON_MODIFYING
            : MODIFYING;
    }
}
