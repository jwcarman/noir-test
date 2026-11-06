package org.jwcarman.noirtest;

import java.util.Locale;
import java.util.Objects;

/**
 * Converts between integers in the range 1 to 3999 inclusive and canonical uppercase Roman
 * numerals.
 *
 * <p>Canonical form writes the thousands, hundreds, tens and ones digits in that order. Ones digits
 * 1-9 are I, II, III, IV, V, VI, VII, VIII, IX; tens use X, L and C in the same pattern; hundreds
 * use C, D and M; thousands are M, MM and MMM; a zero digit contributes nothing. The only
 * subtractive pairs are IV, IX, XL, XC, CD and CM.
 */
public final class RomanNumerals {

  private static final int MIN = 1;
  private static final int MAX = 3999;

  private static final String[] THOUSANDS = {"", "M", "MM", "MMM"};
  private static final String[] HUNDREDS = {
    "", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"
  };
  private static final String[] TENS = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
  private static final String[] ONES = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

  private RomanNumerals() {}

  /**
   * Returns the canonical uppercase Roman numeral for {@code value}.
   *
   * <p>For example, {@code toRoman(14)} returns {@code "XIV"} and {@code toRoman(1994)} returns
   * {@code "MCMXCIV"}.
   *
   * @param value the integer to convert, from 1 to 3999 inclusive
   * @return the canonical uppercase Roman numeral for {@code value}
   * @throws IllegalArgumentException if {@code value} is less than 1 or greater than 3999
   */
  public static String toRoman(int value) {
    if (value < MIN || value > MAX) {
      throw new IllegalArgumentException("Value out of range 1..3999: " + value);
    }
    return THOUSANDS[value / 1000]
        + HUNDREDS[value / 100 % 10]
        + TENS[value / 10 % 10]
        + ONES[value % 10];
  }

  /**
   * Returns the value of {@code numeral}, ignoring letter case: the letters {@code i}, {@code v},
   * {@code x}, {@code l}, {@code c}, {@code d} and {@code m} are read as their uppercase forms, in
   * any mix of cases. The numeral, uppercased, must be exactly the canonical numeral of a value from
   * 1 to 3999. Any other character, including whitespace, still makes it invalid.
   *
   * @param numeral the numeral to read
   * @return its value, from 1 to 3999
   * @throws NullPointerException if {@code numeral} is {@code null}
   * @throws IllegalArgumentException if {@code numeral}, uppercased, is not exactly the canonical
   *     numeral of a value from 1 to 3999; the message is {@code Not a canonical Roman numeral:
   *     "<numeral>"} with the numeral as given
   */
  public static int fromRoman(String numeral) {
    Objects.requireNonNull(numeral, "numeral");
    int value = rawValue(numeral);
    if (value < MIN || value > MAX || !toRoman(value).equals(numeral.toUpperCase(Locale.ROOT))) {
      throw new IllegalArgumentException("Not a canonical Roman numeral: \"" + numeral + "\"");
    }
    return value;
  }

  private static int rawValue(String numeral) {
    int total = 0;
    int length = numeral.length();
    for (int i = 0; i < length; i++) {
      int current = letterValue(numeral, numeral.charAt(i));
      int next = i + 1 < length ? letterValue(numeral, numeral.charAt(i + 1)) : 0;
      total += current < next ? -current : current;
    }
    return total;
  }

  private static int letterValue(String numeral, char c) {
    return switch (c) {
      case 'I', 'i' -> 1;
      case 'V', 'v' -> 5;
      case 'X', 'x' -> 10;
      case 'L', 'l' -> 50;
      case 'C', 'c' -> 100;
      case 'D', 'd' -> 500;
      case 'M', 'm' -> 1000;
      default ->
          throw new IllegalArgumentException(
              "Not a canonical Roman numeral: \"" + numeral + "\"");
    };
  }
}
