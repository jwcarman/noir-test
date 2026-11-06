package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IntervalSetOverlapTest {

  private static IntervalSet twoRanges() {
    IntervalSet set = new IntervalSet();
    set.add(1, 5);
    set.add(10, 20);
    return set;
  }

  @Test
  void overlapsAcrossEnd() {
    assertTrue(twoRanges().overlaps(4, 6));
  }

  @Test
  void gapDoesNotOverlap() {
    IntervalSet set = twoRanges();
    assertFalse(set.overlaps(5, 10));
    assertFalse(set.overlaps(6, 9));
    assertFalse(set.overlaps(-5, 1));
    assertFalse(set.overlaps(20, 30));
  }

  @Test
  void coveringRangeOverlaps() {
    assertTrue(twoRanges().overlaps(0, 100));
  }

  @Test
  void overlapsAtLastValue() {
    IntervalSet set = twoRanges();
    assertTrue(set.overlaps(19, 25));
    assertTrue(set.overlaps(9, 11));
    assertTrue(set.overlaps(0, 2));
    assertTrue(set.overlaps(12, 13));
  }

  @Test
  void emptySetOverlapsNothing() {
    assertFalse(new IntervalSet().overlaps(0, 100));
  }

  @Test
  void emptyRangeOverlapsNothing() {
    IntervalSet set = new IntervalSet();
    set.add(1, 5);
    assertFalse(set.overlaps(3, 3));
    assertFalse(set.overlaps(1, 1));
    assertFalse(set.overlaps(5, 5));
  }

  @Test
  void reversedRangeThrows() {
    IntervalSet set = new IntervalSet();
    set.add(1, 5);
    assertThrows(IllegalArgumentException.class, () -> set.overlaps(5, 1));
    assertThrows(IllegalArgumentException.class, () -> new IntervalSet().overlaps(1, 0));
  }

  @Test
  void extremeBounds() {
    IntervalSet set = new IntervalSet();
    set.add(Integer.MIN_VALUE, Integer.MIN_VALUE + 1);
    assertTrue(set.overlaps(Integer.MIN_VALUE, Integer.MAX_VALUE));
    assertFalse(set.overlaps(Integer.MIN_VALUE + 1, Integer.MAX_VALUE));
  }
}
