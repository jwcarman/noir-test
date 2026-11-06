package org.jwcarman.noirtest;

import java.util.Objects;

/**
 * Base 62 with the alphabet {@code 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz}
 * (digit value = index in the alphabet), most significant digit first, no padding.
 */
public final class Base62 {

  private static final String ALPHABET =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final int BASE = 62;

  private Base62() {}

  /**
   * Encodes {@code value}. Zero is {@code "0"}; no other result starts with {@code '0'}.
   *
   * @throws IllegalArgumentException if {@code value} is negative
   */
  public static String encode(long value) {
    if (value < 0) {
      throw new IllegalArgumentException("value must not be negative: " + value);
    }
    StringBuilder sb = new StringBuilder();
    long remaining = value;
    do {
      sb.append(ALPHABET.charAt((int) (remaining % BASE)));
      remaining /= BASE;
    } while (remaining > 0);
    return sb.reverse().toString();
  }

  /**
   * Decodes {@code text}, the inverse of {@link #encode}.
   *
   * @throws NullPointerException if {@code text} is {@code null}
   * @throws IllegalArgumentException if {@code text} is empty, has a character outside the
   *     alphabet, has a leading {@code '0'} (other than {@code "0"} itself), or exceeds {@link
   *     Long#MAX_VALUE}
   */
  public static long decode(String text) {
    Objects.requireNonNull(text, "text");
    if (text.isEmpty()) {
      throw new IllegalArgumentException("text must not be empty");
    }
    if (text.length() > 1 && text.charAt(0) == '0') {
      throw new IllegalArgumentException("leading zero: " + text);
    }
    long result = 0;
    for (int i = 0; i < text.length(); i++) {
      result = accumulate(result, digit(text.charAt(i)), text);
    }
    return result;
  }

  private static long accumulate(long result, int digit, String text) {
    if (result > (Long.MAX_VALUE - digit) / BASE) {
      throw new IllegalArgumentException("value exceeds Long.MAX_VALUE: " + text);
    }
    return result * BASE + digit;
  }

  private static int digit(char c) {
    int d = ALPHABET.indexOf(c);
    if (d < 0) {
      throw new IllegalArgumentException("invalid Base62 character: " + c);
    }
    return d;
  }
}
