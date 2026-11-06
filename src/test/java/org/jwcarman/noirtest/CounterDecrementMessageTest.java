package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CounterDecrementMessageTest {

  @Test
  void decrementAtZeroOnNewCounterThrowsWithClearMessage() {
    Counter c = new Counter();
    IllegalStateException e = assertThrows(IllegalStateException.class, c::decrement);
    assertEquals("cannot decrement below zero", e.getMessage());
    assertEquals(0, c.get());
  }

  @Test
  void decrementBelowZeroAfterCountingDownThrowsWithClearMessage() {
    Counter c = new Counter(2);
    c.decrement();
    assertEquals(1, c.get());
    c.decrement();
    assertEquals(0, c.get());
    IllegalStateException e = assertThrows(IllegalStateException.class, c::decrement);
    assertEquals("cannot decrement below zero", e.getMessage());
    assertEquals(0, c.get());
  }
}
