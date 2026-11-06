package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BoundsTest {

  @ParameterizedTest
  @CsvSource({
    "5, 1, 3, 3",
    "2, 1, 3, 2",
    "-4, 1, 3, 1",
    "1, 1, 3, 1",
    "3, 1, 3, 3",
    "0, 1, 3, 1",
    "4, 1, 3, 3",
    "7, 7, 7, 7",
    "-9, 7, 7, 7",
    "9, 7, 7, 7",
    "-2147483648, -5, 5, -5",
    "2147483647, -5, 5, 5",
    "0, -2147483648, 2147483647, 0"
  })
  void limitsValueToRange(int value, int min, int max, int expected) {
    assertEquals(expected, Bounds.clamp(value, min, max));
  }

  @ParameterizedTest
  @CsvSource({"1, 3, 1", "0, 2, 1", "5, 2147483647, -2147483648"})
  void rejectsInvalidRange(int value, int min, int max) {
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> Bounds.clamp(value, min, max));
    assertTrue(e.getMessage().contains(min + " > " + max));
  }

  @Test
  void isNotInstantiable() throws Exception {
    Constructor<Bounds> c = Bounds.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(c.getModifiers()));
    assertTrue(Modifier.isFinal(Bounds.class.getModifiers()));
    c.setAccessible(true);
    c.newInstance();
  }
}
