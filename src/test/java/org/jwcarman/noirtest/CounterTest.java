package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CounterTest {

  private static Counter counterAt(int n) {
    Counter c = new Counter();
    for (int i = 0; i < n; i++) {
      c.increment();
    }
    return c;
  }

  @Test
  void newCounterIsZero() {
    assertEquals(0, new Counter().get());
  }

  @Test
  void incrementOnceGivesOne() {
    Counter c = new Counter();
    c.increment();
    assertEquals(1, c.get());
  }

  @Test
  void incrementThreeTimesGivesThree() {
    assertEquals(3, counterAt(3).get());
  }

  @Test
  void decrementFromTwoGivesOne() {
    Counter c = counterAt(2);
    c.decrement();
    assertEquals(1, c.get());
  }

  @Test
  void decrementFromOneGivesZero() {
    Counter c = counterAt(1);
    c.decrement();
    assertEquals(0, c.get());
  }

  @Test
  void decrementAtZeroThrowsAndStaysZero() {
    Counter c = new Counter();
    assertThrows(IllegalStateException.class, c::decrement);
    assertEquals(0, c.get());
  }

  @Test
  void decrementToZeroThenAgainThrows() {
    Counter c = counterAt(1);
    c.decrement();
    assertThrows(IllegalStateException.class, c::decrement);
    assertEquals(0, c.get());
    c.increment();
    assertEquals(1, c.get());
  }

  @Test
  void resetFromFiveGivesZero() {
    Counter c = counterAt(5);
    c.reset();
    assertEquals(0, c.get());
  }

  @Test
  void resetThenIncrementStartsFromZero() {
    Counter c = counterAt(5);
    c.reset();
    c.increment();
    assertEquals(1, c.get());
  }
}
