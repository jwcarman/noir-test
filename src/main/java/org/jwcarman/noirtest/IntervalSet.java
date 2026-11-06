package org.jwcarman.noirtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A mutable set of {@code int} values, stored as intervals.
 *
 * <p>The set is always held as sorted, disjoint, non-adjacent {@link Interval}s: adding a range
 * merges it with every interval it overlaps or touches, so {@code [1, 5)} and {@code [5, 8)} become
 * {@code [1, 8)}, and removing a range trims or splits the intervals it overlaps. Ranges passed to
 * {@link #add} and {@link #remove} are half-open, {@code [start, end)}; an empty range ({@code start
 * == end}) changes nothing.
 *
 * <p>Instances are not thread-safe.
 */
public final class IntervalSet {

  /** Maps each interval's start to its end. */
  private final TreeMap<Integer, Integer> ranges;

  /** Creates an empty set. */
  public IntervalSet() {
    ranges = new TreeMap<>();
  }

  /**
   * Adds every value in {@code [start, end)}.
   *
   * @throws IllegalArgumentException if {@code start > end}
   */
  public void add(int start, int end) {
    checkRange(start, end);
    if (start == end) {
      return;
    }
    int newStart = start;
    int newEnd = end;
    Map.Entry<Integer, Integer> floor = ranges.floorEntry(start);
    if (floor != null && floor.getValue() >= start) {
      newStart = floor.getKey();
    }
    NavigableMap<Integer, Integer> merged = ranges.subMap(newStart, true, end, true);
    for (int otherEnd : merged.values()) {
      newEnd = Math.max(newEnd, otherEnd);
    }
    merged.clear();
    ranges.put(newStart, newEnd);
  }

  /**
   * Removes every value in {@code [start, end)}.
   *
   * @throws IllegalArgumentException if {@code start > end}
   */
  public void remove(int start, int end) {
    checkRange(start, end);
    if (start == end) {
      return;
    }
    Map.Entry<Integer, Integer> lower = ranges.lowerEntry(start);
    if (lower != null && lower.getValue() > start) {
      ranges.put(lower.getKey(), start);
      keepTail(end, lower.getValue());
    }
    NavigableMap<Integer, Integer> covered = ranges.subMap(start, true, end, false);
    if (!covered.isEmpty()) {
      int lastEnd = covered.lastEntry().getValue();
      covered.clear();
      keepTail(end, lastEnd);
    }
  }

  /** Returns whether {@code value} is in the set. */
  public boolean contains(int value) {
    Map.Entry<Integer, Integer> floor = ranges.floorEntry(value);
    return floor != null && value < floor.getValue();
  }

  /**
   * Returns the intervals of this set, sorted by start, disjoint and non-adjacent. The list is an
   * unmodifiable snapshot: later changes to the set do not change it.
   */
  public List<Interval> intervals() {
    List<Interval> result = new ArrayList<>(ranges.size());
    ranges.forEach((s, e) -> result.add(new Interval(s, e)));
    return List.copyOf(result);
  }

  /** Returns how many values are in the set, computed without {@code int} overflow. */
  public long totalLength() {
    long total = 0;
    for (Map.Entry<Integer, Integer> entry : ranges.entrySet()) {
      total += (long) entry.getValue() - entry.getKey();
    }
    return total;
  }

  /** Returns whether the set has no values. */
  public boolean isEmpty() {
    return ranges.isEmpty();
  }

  private void keepTail(int end, int originalEnd) {
    if (originalEnd > end) {
      ranges.put(end, originalEnd);
    }
  }

  private static void checkRange(int start, int end) {
    if (start > end) {
      throw new IllegalArgumentException(
          "start must not be greater than end: [" + start + ", " + end + ")");
    }
  }
}
