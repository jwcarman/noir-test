package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Locale;
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
    "1100, MC", "49, XLIX", "99, XCIX", "444, CDXLIV", "4000, MMMM", "4999, MMMMCMXCIX",
    "4001, MMMMI", "4444, MMMMCDXLIV"
  })
  void toRomanProducesCanonicalNumerals(int value, String expected) {
    assertEquals(expected, RomanNumerals.toRoman(value));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, 5000, 5001, Integer.MIN_VALUE, Integer.MAX_VALUE})
  void toRomanRejectsOutOfRange(int value) {
    assertThrows(IllegalArgumentException.class, () -> RomanNumerals.toRoman(value));
  }

  @ParameterizedTest
  @CsvSource({"0, 'Value out of range 1..4999: 0'", "5000, 'Value out of range 1..4999: 5000'"})
  void toRomanRejectionMessageNamesTheRange(int value, String expected) {
    var e = assertThrows(IllegalArgumentException.class, () -> RomanNumerals.toRoman(value));
    assertEquals(expected, e.getMessage());
  }

  @Test
  void toRomanProducesDistinctNumeralsUpTo4999() {
    Set<String> seen = new HashSet<>();
    for (int n = 1; n <= 4999; n++) {
      assertTrue(seen.add(RomanNumerals.toRoman(n)));
    }
    assertEquals("MMMMCMXCIX", RomanNumerals.toRoman(4999));
  }

  @ParameterizedTest
  @ValueSource(ints = {4000, 4001, 4999})
  void fromRomanStillRejectsNumeralsAbove3999(int value) {
    String numeral = RomanNumerals.toRoman(value);
    var e = assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman(numeral));
    assertEquals("Not a canonical Roman numeral: \"" + numeral + "\"", e.getMessage());
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
        "", " ", " XIV", "XIV ", "X IV", "\tI", "I\n", "ABC", "12", "X1",
        "IIII", "VV", "IC", "IL", "XM", "VX", "MMMM", "IIV", "CMM", "DD", "LL", "XXXX", "CCCC",
        "IXI", "XCX", "CMC", "IVI", "VIV", "MMMMCMXCIX", "IM", "ID", "XD", "LC", "DM", "IIX"
      })
  void fromRomanRejectsInvalidStrings(String numeral) {
    assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman(numeral));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "MMMM", "IIII"})
  void fromRomanRejectionMessageNamesTheNumeral(String numeral) {
    var e = assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman(numeral));
    assertEquals("Not a canonical Roman numeral: \"" + numeral + "\"", e.getMessage());
  }

  @ParameterizedTest
  @CsvSource({
    "xiv, 14", "Xiv, 14", "xIV, 14", "mcmxciv, 1994", "i, 1", "m, 1000", "mmmcmxcix, 3999",
    "v, 5", "x, 10", "l, 50", "c, 100", "d, 500", "MmXxIv, 2024", "mmmdccclxxxviii, 3888",
    "cdxliv, 444"
  })
  void fromRomanIgnoresLetterCase(String numeral, int expected) {
    assertEquals(expected, RomanNumerals.fromRoman(numeral));
  }

  @Test
  void fromRomanParsesEveryValueInLowercase() {
    for (int n = 1; n <= 3999; n++) {
      assertEquals(n, RomanNumerals.fromRoman(RomanNumerals.toRoman(n).toLowerCase(Locale.ROOT)));
    }
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "iiii", "mmmm", "ic", "xiv ", " xiv", "x iv", "vv", "iiv", "mmmmcmxcix", "Iiii", "iC",
        "\u0131", "x\u0131v", "\u2170", "abc", "xiv\n"
      })
  void fromRomanRejectsNonCanonicalNumeralsInAnyCase(String numeral) {
    var e = assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman(numeral));
    assertEquals("Not a canonical Roman numeral: \"" + numeral + "\"", e.getMessage());
  }

  @Test
  void fromRomanIgnoresDefaultLocale() {
    Locale saved = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("tr"));
      assertEquals(14, RomanNumerals.fromRoman("xiv"));
      assertEquals(1999, RomanNumerals.fromRoman("mcmxcix"));
    } finally {
      Locale.setDefault(saved);
    }
  }

  @Test
  void fromRomanRejectsNull() {
    assertThrows(NullPointerException.class, () -> RomanNumerals.fromRoman(null));
  }
}
