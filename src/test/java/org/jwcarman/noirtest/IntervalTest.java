package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IntervalTest {

  @Test
  void lengthAndAccessors() {
    Interval i = new Interval(1, 5);
    assertEquals(4L, i.length());
    assertEquals(1, i.start());
    assertEquals(5, i.end());
  }

  @Test
  void containsIsHalfOpen() {
    Interval i = new Interval(1, 5);
    assertTrue(i.contains(1));
    assertTrue(i.contains(4));
    assertFalse(i.contains(0));
    assertFalse(i.contains(5));
  }

  @Test
  void rejectsEmptyAndReversed() {
    assertThrows(IllegalArgumentException.class, () -> new Interval(5, 5));
    assertThrows(IllegalArgumentException.class, () -> new Interval(5, 1));
  }

  @Test
  void singleValueInterval() {
    Interval i = new Interval(5, 6);
    assertEquals(1L, i.length());
    assertTrue(i.contains(5));
    assertFalse(i.contains(6));
    assertFalse(i.contains(4));
  }

  @Test
  void fullRangeLengthNoOverflow() {
    assertEquals(4294967295L, new Interval(Integer.MIN_VALUE, Integer.MAX_VALUE).length());
  }

  @Test
  void equality() {
    assertEquals(new Interval(1, 5), new Interval(1, 5));
    assertEquals(new Interval(1, 5).hashCode(), new Interval(1, 5).hashCode());
    assertNotEquals(new Interval(1, 5), new Interval(1, 6));
    assertNotEquals(new Interval(1, 5), new Interval(0, 5));
  }
}
