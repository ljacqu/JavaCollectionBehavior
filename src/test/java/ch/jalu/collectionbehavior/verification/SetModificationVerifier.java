package ch.jalu.collectionbehavior.verification;

import ch.jalu.collectionbehavior.model.ModificationBehavior;
import ch.jalu.collectionbehavior.model.SetMethod;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;

public final class SetModificationVerifier {

    private SetModificationVerifier() {
    }

    public static void testMethods(Set<String> originalSet, ModificationBehavior expectedBehavior) {
        new UnmodifiableBehaviorTester<>(originalSet, LinkedHashSet::new, expectedBehavior)
            .test(SetMethod.ADD, set -> set.add("a"))
            .test(SetMethod.ADD, set -> set.add("foo"))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of("a", "b")))
            .test(SetMethod.ADD_ALL, set -> set.addAll(List.of("foo", "bar")))
            .test(SetMethod.REMOVE, set -> set.remove("zzz"))
            .test(SetMethod.REMOVE, set -> set.remove("a"))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(str -> str.equals("zzz")))
            .test(SetMethod.REMOVE_IF, set -> set.removeIf(str -> str.equals("a")))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of("fff", "xxx")))
            .test(SetMethod.REMOVE_ALL, set -> set.removeAll(Set.of("fff", "a")))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(Set.of("a")))
            .test(SetMethod.RETAIN_ALL, set -> set.retainAll(Set.of("qqq")))
            .test(SetMethod.CLEAR, set -> set.clear());
    }

    public static void testMethodsForSequencedSet(SequencedSet<String> originalSet, ModificationBehavior expectedBehavior) {
        new UnmodifiableBehaviorTester<>(originalSet, LinkedHashSet::new, expectedBehavior)
            .test(SetMethod.ADD_FIRST, set -> set.addFirst("a"))
            .test(SetMethod.ADD_FIRST, set -> set.addFirst("foo"))
            .test(SetMethod.ADD_LAST, set -> set.addLast("a"))
            .test(SetMethod.ADD_LAST, set -> set.addLast("foo"))
            .test(SetMethod.REMOVE_FIRST, set -> set.removeFirst())
            .test(SetMethod.REMOVE_LAST, set -> set.removeLast());
    }

    public static void testMethodsForReversedSet(SequencedSet<String> reversedSet, ModificationBehavior expectedBehavior) {
        testMethods(reversedSet, expectedBehavior);
        testMethodsForSequencedSet(reversedSet, expectedBehavior);
    }
}
