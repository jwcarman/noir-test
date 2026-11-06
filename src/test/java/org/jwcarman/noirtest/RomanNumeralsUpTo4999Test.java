package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RomanNumeralsUpTo4999Test {

  @ParameterizedTest
  @CsvSource({
    "4000, MMMM", "4999, MMMMCMXCIX", "1994, MCMXCIV", "1, I", "3999, MMMCMXCIX",
    "4001, MMMMI", "4444, MMMMCDXLIV", "4888, MMMMDCCCLXXXVIII", "4010, MMMMX", "4100, MMMMC"
  })
  void toRomanConvertsValuesUpTo4999(int value, String expected) {
    assertEquals(expected, RomanNumerals.toRoman(value));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, 5000, 5001, Integer.MIN_VALUE, Integer.MAX_VALUE})
  void toRomanRejectsOutOfRange(int value) {
    var e = assertThrows(IllegalArgumentException.class, () -> RomanNumerals.toRoman(value));
    assertEquals("Value out of range 1..4999: " + value, e.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"MMMM", "MMMMCMXCIX", "mmmm"})
  void fromRomanStillRejectsValuesAbove3999(String numeral) {
    var e = assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman(numeral));
    assertEquals("Not a canonical Roman numeral: \"" + numeral + "\"", e.getMessage());
  }
}
