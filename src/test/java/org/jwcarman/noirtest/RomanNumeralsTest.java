package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RomanNumeralsTest {

  @ParameterizedTest
  @CsvSource({
    "1, I", "2, II", "3, III", "4, IV", "5, V", "6, VI", "7, VII", "8, VIII", "9, IX",
    "10, X", "14, XIV", "20, XX", "30, XXX", "40, XL", "50, L", "60, LX", "70, LXX",
    "80, LXXX", "90, XC", "100, C", "200, CC", "300, CCC", "400, CD", "500, D", "600, DC",
    "700, DCC", "800, DCCC", "900, CM", "1000, M", "2000, MM", "3000, MMM", "1994, MCMXCIV",
    "2024, MMXXIV", "3999, MMMCMXCIX", "3888, MMMDCCCLXXXVIII", "1001, MI", "1010, MX",
    "1100, MC", "49, XLIX", "99, XCIX", "444, CDXLIV"
  })
  void toRomanProducesCanonicalNumerals(int value, String expected) {
    assertEquals(expected, RomanNumerals.toRoman(value));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, 4000, 4001, Integer.MIN_VALUE, Integer.MAX_VALUE})
  void toRomanRejectsOutOfRange(int value) {
    assertThrows(IllegalArgumentException.class, () -> RomanNumerals.toRoman(value));
  }

  @ParameterizedTest
  @CsvSource({
    "I, 1", "XIV, 14", "MCMXCIV, 1994", "MMMCMXCIX, 3999", "IV, 4", "IX, 9", "XL, 40",
    "XC, 90", "CD, 400", "CM, 900", "MMXXIV, 2024", "MMMDCCCLXXXVIII, 3888"
  })
  void fromRomanParsesCanonicalNumerals(String numeral, int expected) {
    assertEquals(expected, RomanNumerals.fromRoman(numeral));
  }

  @Test
  void roundTripsEveryValueAndAllNumeralsAreDistinct() {
    Set<String> seen = new HashSet<>();
    for (int n = 1; n <= 3999; n++) {
      String roman = RomanNumerals.toRoman(n);
      assertEquals(n, RomanNumerals.fromRoman(roman));
      assertTrue(seen.add(roman), roman);
    }
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "", " ", " XIV", "XIV ", "X IV", "\tI", "I\n", "xiv", "Xiv", "i", "m", "ABC", "12", "X1",
        "IIII", "VV", "IC", "IL", "XM", "VX", "MMMM", "IIV", "CMM", "DD", "LL", "XXXX", "CCCC",
        "IXI", "XCX", "CMC", "IVI", "VIV", "MMMMCMXCIX", "IM", "ID", "XD", "LC", "DM", "IIX"
      })
  void fromRomanRejectsInvalidStrings(String numeral) {
    assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman(numeral));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "MMMM", "IIII", "xiv"})
  void fromRomanRejectionMessageNamesTheNumeral(String numeral) {
    var e = assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman(numeral));
    assertEquals("Not a canonical Roman numeral: \"" + numeral + "\"", e.getMessage());
  }

  @Test
  void fromRomanRejectsNull() {
    assertThrows(NullPointerException.class, () -> RomanNumerals.fromRoman(null));
  }
}
