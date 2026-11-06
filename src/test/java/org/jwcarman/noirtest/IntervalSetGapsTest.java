package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class IntervalSetGapsTest {

  private static IntervalSet set(int... bounds) {
    IntervalSet set = new IntervalSet();
    for (int i = 0; i < bounds.length; i += 2) {
      set.add(bounds[i], bounds[i + 1]);
    }
    return set;
  }

  @Test
  void gapsAroundAndBetween() {
    assertEquals(
        List.of(new Interval(0, 10), new Interval(20, 30), new Interval(40, 50)),
        set(10, 20, 30, 40).gaps(0, 50));
  }

  @Test
  void windowStartingAndEndingInsideIntervals() {
    assertEquals(List.of(new Interval(20, 30)), set(10, 20, 30, 40).gaps(15, 35));
  }

  @Test
  void windowFullyCovered() {
    assertEquals(List.of(), set(10, 20).gaps(12, 18));
    assertEquals(List.of(), set(10, 20).gaps(10, 20));
  }

  @Test
  void windowStartingAtIntervalEnd() {
    assertEquals(List.of(new Interval(20, 25)), set(10, 20).gaps(20, 25));
  }

  @Test
  void windowEndingAtIntervalStart() {
    assertEquals(List.of(new Interval(5, 10)), set(10, 20).gaps(5, 10));
  }

  @Test
  void windowStartingAtIntervalStart() {
    assertEquals(List.of(new Interval(20, 25)), set(10, 20).gaps(10, 25));
  }

  @Test
  void windowBeforeAllIntervals() {
    assertEquals(List.of(new Interval(1, 4)), set(10, 20).gaps(1, 4));
  }

  @Test
  void windowAfterAllIntervals() {
    assertEquals(List.of(new Interval(25, 30)), set(10, 20).gaps(25, 30));
  }

  @Test
  void windowEntirelyBetweenIntervals() {
    assertEquals(List.of(new Interval(22, 28)), set(10, 20, 30, 40).gaps(22, 28));
  }

  @Test
  void windowCoveringManyIntervals() {
    assertEquals(
        List.of(new Interval(3, 5), new Interval(7, 9)),
        set(0, 3, 5, 7, 9, 12).gaps(1, 11));
  }

  @Test
  void emptySetHasWholeWindow() {
    assertEquals(List.of(new Interval(0, 5)), new IntervalSet().gaps(0, 5));
    assertEquals(
        List.of(new Interval(Integer.MIN_VALUE, Integer.MAX_VALUE)),
        new IntervalSet().gaps(Integer.MIN_VALUE, Integer.MAX_VALUE));
  }

  @Test
  void extremeBoundsWithIntervals() {
    assertEquals(
        List.of(new Interval(Integer.MIN_VALUE, 0), new Interval(10, Integer.MAX_VALUE)),
        set(0, 10).gaps(Integer.MIN_VALUE, Integer.MAX_VALUE));
    assertEquals(List.of(), set(Integer.MIN_VALUE, Integer.MAX_VALUE).gaps(-5, 5));
  }

  @Test
  void emptyWindow() {
    assertEquals(List.of(), set(10, 20).gaps(3, 3));
    assertEquals(List.of(), set(10, 20).gaps(15, 15));
    assertEquals(List.of(), new IntervalSet().gaps(0, 0));
  }

  @Test
  void reversedWindowThrows() {
    IntervalSet set = set(10, 20);
    assertThrows(IllegalArgumentException.class, () -> set.gaps(5, 1));
    assertEquals(List.of(new Interval(10, 20)), set.intervals());
  }

  @Test
  void resultIsUnmodifiableAndSetUnchanged() {
    IntervalSet set = set(10, 20);
    var g = set.gaps(0, 30);
    assertEquals(List.of(new Interval(0, 10), new Interval(20, 30)), g);
    assertThrows(UnsupportedOperationException.class, () -> g.add(new Interval(1, 2)));
    assertEquals(List.of(new Interval(10, 20)), set.intervals());
  }

  @Test
  void emptyResultIsUnmodifiable() {
    var g = set(10, 20).gaps(12, 18);
    assertThrows(UnsupportedOperationException.class, () -> g.add(new Interval(1, 2)));
  }
}
