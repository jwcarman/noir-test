package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RomanNumeralsUpTo4999Test {

  @ParameterizedTest
  @CsvSource({
    "1, I", "14, XIV", "1994, MCMXCIV", "3999, MMMCMXCIX", "4000, MMMM", "4001, MMMMI",
    "4444, MMMMCDXLIV", "4888, MMMMDCCCLXXXVIII", "4999, MMMMCMXCIX"
  })
  void toRomanConvertsValuesUpTo4999(int value, String expected) {
    assertEquals(expected, RomanNumerals.toRoman(value));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, 5000, 5001, Integer.MIN_VALUE, Integer.MAX_VALUE})
  void toRomanRejectsValuesOutside1To4999(int value) {
    var e = assertThrows(IllegalArgumentException.class, () -> RomanNumerals.toRoman(value));
    assertEquals("Value out of range 1..4999: " + value, e.getMessage());
  }

  @Test
  void fromRomanStillRejectsValuesAbove3999() {
    assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman("MMMM"));
    assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman("MMMMCMXCIX"));
    assertEquals(3999, RomanNumerals.fromRoman("MMMCMXCIX"));
  }
}
