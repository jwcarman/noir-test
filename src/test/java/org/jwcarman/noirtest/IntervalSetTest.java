package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class IntervalSetTest {

  private static IntervalSet of(int... bounds) {
    IntervalSet set = new IntervalSet();
    for (int i = 0; i < bounds.length; i += 2) {
      set.add(bounds[i], bounds[i + 1]);
    }
    return set;
  }

  private static Interval iv(int s, int e) {
    return new Interval(s, e);
  }

  @Test
  void newSetIsEmpty() {
    IntervalSet set = new IntervalSet();
    assertTrue(set.isEmpty());
    assertEquals(0L, set.totalLength());
    assertEquals(List.of(), set.intervals());
    assertFalse(set.contains(0));
  }

  @Test
  void addKeepsSeparateRangesSorted() {
    IntervalSet set = of(10, 20, 1, 5);
    assertEquals(List.of(iv(1, 5), iv(10, 20)), set.intervals());
    assertEquals(14L, set.totalLength());
    assertFalse(set.isEmpty());
  }

  @Test
  void addWithGapOfOneKeepsApart() {
    assertEquals(List.of(iv(1, 5), iv(6, 8)), of(1, 5, 6, 8).intervals());
    assertEquals(List.of(iv(1, 5), iv(6, 8)), of(6, 8, 1, 5).intervals());
  }

  @Test
  void addFillsGap() {
    assertEquals(List.of(iv(1, 20)), of(1, 5, 10, 20, 5, 10).intervals());
  }

  @Test
  void addOverlapsBoth() {
    assertEquals(List.of(iv(1, 20)), of(1, 5, 10, 20, 3, 12).intervals());
  }

  @Test
  void addAdjacentAfter() {
    assertEquals(List.of(iv(1, 8)), of(1, 5, 5, 8).intervals());
  }

  @Test
  void addAdjacentBefore() {
    assertEquals(List.of(iv(1, 8)), of(5, 8, 1, 5).intervals());
  }

  @Test
  void addSwallowsMany() {
    IntervalSet set = of(1, 5, 10, 20, 30, 40, 0, 100);
    assertEquals(List.of(iv(0, 100)), set.intervals());
    assertEquals(100L, set.totalLength());
  }

  @Test
  void addInsideExisting() {
    assertEquals(List.of(iv(0, 100)), of(0, 100, 10, 20).intervals());
  }

  @Test
  void addExtendsRightOfLastOverlapped() {
    assertEquals(List.of(iv(1, 5), iv(10, 25)), of(1, 5, 10, 20, 15, 25).intervals());
    assertEquals(List.of(iv(1, 25)), of(1, 5, 10, 20, 4, 25).intervals());
    assertEquals(List.of(iv(1, 5), iv(8, 20)), of(1, 5, 10, 20, 8, 12).intervals());
  }

  @Test
  void addSameRangeTwice() {
    assertEquals(List.of(iv(1, 5)), of(1, 5, 1, 5).intervals());
  }

  @Test
  void addLeavesNonTouchingIntervals() {
    assertEquals(
        List.of(iv(0, 2), iv(5, 15), iv(20, 30)), of(0, 2, 5, 10, 20, 30, 8, 15).intervals());
  }

  @Test
  void removeSplits() {
    IntervalSet set = of(1, 20);
    set.remove(3, 12);
    assertEquals(List.of(iv(1, 3), iv(12, 20)), set.intervals());
    assertEquals(10L, set.totalLength());
  }

  @Test
  void removeExact() {
    IntervalSet set = of(1, 20);
    set.remove(1, 20);
    assertTrue(set.isEmpty());
    assertEquals(List.of(), set.intervals());
    assertEquals(0L, set.totalLength());
  }

  @Test
  void removeSuperset() {
    IntervalSet set = of(1, 20);
    set.remove(0, 100);
    assertTrue(set.isEmpty());
  }

  @Test
  void removeGapChangesNothing() {
    IntervalSet set = of(1, 5, 10, 20);
    set.remove(5, 10);
    assertEquals(List.of(iv(1, 5), iv(10, 20)), set.intervals());
  }

  @Test
  void removeAcrossSeveral() {
    IntervalSet set = of(0, 10, 20, 30, 40, 50);
    set.remove(5, 45);
    assertEquals(List.of(iv(0, 5), iv(45, 50)), set.intervals());
  }

  @Test
  void removePrefix() {
    IntervalSet set = of(1, 20);
    set.remove(1, 5);
    assertEquals(List.of(iv(5, 20)), set.intervals());
  }

  @Test
  void removeSuffix() {
    IntervalSet set = of(1, 20);
    set.remove(15, 20);
    assertEquals(List.of(iv(1, 15)), set.intervals());
  }

  @Test
  void removeTrimsTailOfEarlierInterval() {
    IntervalSet set = of(1, 10, 20, 30);
    set.remove(5, 15);
    assertEquals(List.of(iv(1, 5), iv(20, 30)), set.intervals());
  }

  @Test
  void removeOverlapsLastInterval() {
    IntervalSet set = of(1, 10, 20, 30);
    set.remove(15, 25);
    assertEquals(List.of(iv(1, 10), iv(25, 30)), set.intervals());
  }

  @Test
  void removeFromEmpty() {
    IntervalSet set = new IntervalSet();
    set.remove(0, 10);
    assertTrue(set.isEmpty());
  }

  @Test
  void containsHalfOpen() {
    IntervalSet set = of(1, 5, 10, 20);
    assertTrue(set.contains(1));
    assertTrue(set.contains(4));
    assertTrue(set.contains(10));
    assertTrue(set.contains(19));
    assertFalse(set.contains(0));
    assertFalse(set.contains(5));
    assertFalse(set.contains(9));
    assertFalse(set.contains(20));
  }

  @Test
  void emptyRangesChangeNothing() {
    IntervalSet set = of(1, 5);
    set.add(3, 3);
    set.remove(3, 3);
    set.add(10, 10);
    assertEquals(List.of(iv(1, 5)), set.intervals());
    IntervalSet empty = new IntervalSet();
    empty.add(7, 7);
    assertTrue(empty.isEmpty());
  }

  @Test
  void reversedRangesRejected() {
    IntervalSet set = of(1, 5);
    assertThrows(IllegalArgumentException.class, () -> set.add(5, 1));
    assertEquals(List.of(iv(1, 5)), set.intervals());
    assertThrows(IllegalArgumentException.class, () -> set.remove(5, 1));
    assertEquals(List.of(iv(1, 5)), set.intervals());
  }

  @Test
  void fullIntRange() {
    IntervalSet set = of(Integer.MIN_VALUE, Integer.MAX_VALUE);
    assertEquals(4294967295L, set.totalLength());
    assertTrue(set.contains(Integer.MIN_VALUE));
    assertFalse(set.contains(Integer.MAX_VALUE));
    set.remove(0, 1);
    assertEquals(
        List.of(iv(Integer.MIN_VALUE, 0), iv(1, Integer.MAX_VALUE)), set.intervals());
    assertEquals(4294967294L, set.totalLength());
  }

  @Test
  void totalLengthSumsAboveIntMax() {
    IntervalSet set = of(Integer.MIN_VALUE, -1, 1, Integer.MAX_VALUE);
    assertEquals(4294967293L, set.totalLength());
  }

  @Test
  void snapshotIsIndependentAndUnmodifiable() {
    IntervalSet set = of(1, 5);
    List<Interval> snapshot = set.intervals();
    set.add(10, 20);
    assertEquals(List.of(iv(1, 5)), snapshot);
    assertThrows(UnsupportedOperationException.class, () -> snapshot.add(iv(1, 2)));
    List<Interval> emptySnapshot = new IntervalSet().intervals();
    assertThrows(UnsupportedOperationException.class, () -> emptySnapshot.add(iv(1, 2)));
  }
}
