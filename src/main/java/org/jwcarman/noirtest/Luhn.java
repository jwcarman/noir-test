package org.jwcarman.noirtest;

import java.util.Objects;

/**
 * The Luhn (mod 10) checksum.
 *
 * <p>For a digit string, number the digits from the right starting at 1. Every digit at an even
 * position is doubled, and 9 is subtracted from a doubled value above 9. The string is valid when
 * the sum of all the resulting digits is a multiple of 10. The check digit for a payload is the
 * digit that, appended to the right of the payload, makes the result valid.
 *
 * <p>A digit string is one or more ASCII digits {@code 0}-{@code 9} and nothing else.
 */
public final class Luhn {

  private Luhn() {}

  /**
   * Returns whether {@code digits} is a digit string that passes the Luhn check. Returns {@code
   * false} for a string that is not a digit string, including the empty string and strings with
   * spaces or signs.
   *
   * @throws NullPointerException if {@code digits} is {@code null}
   */
  public static boolean isValid(String digits) {
    Objects.requireNonNull(digits, "digits");
    return isDigitString(digits) && sum(digits, false) % 10 == 0;
  }

  /**
   * Returns the check digit to append to {@code payload}.
   *
   * @param payload a digit string
   * @return the check digit, from 0 to 9
   * @throws NullPointerException if {@code payload} is {@code null}
   * @throws IllegalArgumentException if {@code payload} is not a digit string
   */
  public static int checkDigit(String payload) {
    Objects.requireNonNull(payload, "payload");
    if (!isDigitString(payload)) {
      throw new IllegalArgumentException("not a digit string: \"" + payload + "\"");
    }
    return (10 - sum(payload, true) % 10) % 10;
  }

  private static boolean isDigitString(String s) {
    if (s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return true;
  }

  /**
   * Sums the Luhn-transformed digits of {@code s}; when {@code doubleRightmost} is true, the
   * rightmost digit is doubled (as it will be once a check digit is appended).
   */
  private static int sum(String s, boolean doubleRightmost) {
    int total = 0;
    boolean doubled = doubleRightmost;
    for (int i = s.length() - 1; i >= 0; i--) {
      int d = s.charAt(i) - '0';
      if (doubled) {
        d *= 2;
        if (d > 9) {
          d -= 9;
        }
      }
      total = (total + d) % 10;
      doubled = !doubled;
    }
    return total;
  }
}
