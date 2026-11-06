package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RomanNumeralsThousandsTest {

  @ParameterizedTest
  @CsvSource({
    "1000, M", "2000, MM", "3000, MMM", "4000, MMMM", "999, CMXCIX", "4009, MMMMIX",
    "4090, MMMMXC", "4900, MMMMCM", "4500, MMMMD", "4050, MMMML", "4005, MMMMV"
  })
  void writesEachThousandAsM(int value, String expected) {
    assertEquals(expected, RomanNumerals.toRoman(value));
  }

  @Test
  void everyValueUpTo4999RoundTripsThroughDigitDecomposition() {
    for (int v = 1; v <= 4999; v++) {
      String roman = RomanNumerals.toRoman(v);
      assertEquals("M".repeat(v / 1000), roman.substring(0, v / 1000));
      if (v <= 3999) {
        assertEquals(v, RomanNumerals.fromRoman(roman));
      } else {
        String r = roman;
        assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman(r));
      }
    }
  }

  @Test
  void boundariesAreExact() {
    assertEquals("I", RomanNumerals.toRoman(1));
    assertEquals("MMMMCMXCIX", RomanNumerals.toRoman(4999));
    assertThrows(IllegalArgumentException.class, () -> RomanNumerals.toRoman(0));
    assertThrows(IllegalArgumentException.class, () -> RomanNumerals.toRoman(5000));
  }
}
