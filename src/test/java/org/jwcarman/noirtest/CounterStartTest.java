package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CounterStartTest {

  @Test
  void startsAtGivenValue() {
    assertEquals(5, new Counter(5).get());
  }

  @Test
  void startsAtZero() {
    assertEquals(0, new Counter(0).get());
  }

  @Test
  void incrementsFromStart() {
    Counter c = new Counter(5);
    c.increment();
    c.increment();
    assertEquals(7, c.get());
  }

  @Test
  void resetAfterIncrementsReturnsToStart() {
    Counter c = new Counter(5);
    c.increment();
    c.increment();
    c.increment();
    c.reset();
    assertEquals(5, c.get());
  }

  @Test
  void resetAfterDecrementsReturnsToStart() {
    Counter c = new Counter(5);
    c.decrement();
    c.decrement();
    assertEquals(3, c.get());
    c.reset();
    assertEquals(5, c.get());
  }

  @Test
  void resetThenIncrement() {
    Counter c = new Counter(5);
    c.reset();
    c.increment();
    assertEquals(6, c.get());
  }

  @Test
  void decrementStopsAtZeroAndResetReturnsToStart() {
    Counter c = new Counter(1);
    c.decrement();
    assertThrows(IllegalStateException.class, c::decrement);
    assertEquals(0, c.get());
    c.reset();
    assertEquals(1, c.get());
  }

  @Test
  void decrementAtZeroStartThrows() {
    Counter c = new Counter(0);
    assertThrows(IllegalStateException.class, c::decrement);
    assertEquals(0, c.get());
  }

  @Test
  void negativeStartRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Counter(-1));
  }

  @Test
  void minValueStartRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Counter(Integer.MIN_VALUE));
  }

  @Test
  void defaultCounterResetsToZero() {
    Counter c = new Counter();
    c.increment();
    c.increment();
    c.reset();
    assertEquals(0, c.get());
  }
}
