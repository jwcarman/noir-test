package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClampTest {

  @ParameterizedTest
  @CsvSource({
    "5, 1, 3, 3",
    "2, 1, 3, 2",
    "-4, 1, 3, 1",
    "0, 1, 3, 1",
    "1, 1, 3, 1",
    "3, 1, 3, 3",
    "4, 1, 3, 3",
    "7, 7, 7, 7",
    "6, 7, 7, 7",
    "8, 7, 7, 7",
    "-10, -5, -2, -5",
    "-3, -5, -2, -3",
    "0, -5, -2, -2",
    "-2147483648, -2147483648, 2147483647, -2147483648",
    "2147483647, -2147483648, 2147483647, 2147483647",
    "2147483647, 0, 10, 10",
    "-2147483648, 0, 10, 0"
  })
  void clampsToRange(int value, int min, int max, int expected) {
    assertEquals(expected, Clamp.clamp(value, min, max));
  }

  @Test
  void rejectsInvertedRange() {
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> Clamp.clamp(1, 3, 1));
    assertEquals("min (3) must not be greater than max (1)", e.getMessage());
  }

  @Test
  void rejectsRangeInvertedByOne() {
    assertThrows(IllegalArgumentException.class, () -> Clamp.clamp(0, 1, 0));
  }

  @Test
  void rejectsInvertedRangeEvenWhenValueWouldOtherwiseBeReturned() {
    assertThrows(IllegalArgumentException.class, () -> Clamp.clamp(2, 3, 1));
  }
}
