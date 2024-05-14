# Java collection behavior

Showcases the behavior of various `List`, `Set` and `Map` implementations and their
differences.

Some highlights are given in this readme. Refer to the unit tests for all the details.

## Difference between Collectors#toList, Collectors#toUnmodifiableList and Stream#toList
Consider the following blocks:
```java
List<Integer> list1 = Stream.of(1, 2, 3)
  .collect(Collectors.toList()); // Since Java 8
List<Integer> list2 = Stream.of(1, 2, 3)
  .collect(Collectors.toUnmodifiableList()); // Since Java 10
List<Integer> list3 = Stream.of(1, 2, 3)
  .toList(); // Since Java 16
```

Is there any difference?
- **Collectors#toList** currently returns an ArrayList, but the Javadoc actually does not guarantee what implementation is
  returned—not even if the list can be modified further! Prefer the more verbose 
  `Collectors.toCollection(ArrayList::new)` if you need to make more changes to a List created by a Stream.
- **Collectors#toUnmodifiableList** does not support nulls. Also in read-only methods like `list2.contains(null)`, an
  exception will be thrown by the list.
- **Stream#toList** supports nulls, and calling something like `list3.contains(null)` is fine.

## Difference between Collections#unmodifiableSet, Set#copyOf and Guava's ImmutableSet#copyOf
```java
Set<Integer> origin = new LinkedHashSet<>(Arrays.asList(1, 2, 3));

Set<Integer> set1 = Collections.unmodifiableSet(origin);
Set<Integer> set2 = Set.copyOf(origin);
Set<Integer> set3 = ImmutableSet.copyOf(origin);
```

- Set#copyOf and Guava's ImmutableSet#copyOf make a copy of the original collection, so they produce _immutable_
  collections: if `origin` is changed afterwards, `set2` and `set3` will not change. Collections#unmodifiableSet
  produces a set that delegates to the origin; `set1` cannot be modified directly but any changes to `origin` are
  still reflected. It is _unmodifiable_  but not _immutable_.
- Set#copyOf and ImmutableSet#copyOf throw an exception if any entry is null.
- The methods on Set#copyOf throw an exception if null is supplied to them (also, for instance, for
  `set2.contains(null)`).
- Collections#unmodifiableSet and ImmutableSet#copyOf keep the order of the original collection; Set#copyOf does not.