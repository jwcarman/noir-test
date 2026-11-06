package org.jwcarman.noirtest;

import module java.base;

/**
 * Run-length encoding of strings whose characters are not ASCII digits. A run of {@code n} equal
 * characters {@code c} is written as the decimal {@code n} (no leading zeros) followed by {@code c}.
 */
public final class RunLength {

  private RunLength() {}

  /**
   * Encodes {@code text}; the empty string encodes to the empty string.
   *
   * @throws NullPointerException if {@code text} is {@code null}
   * @throws IllegalArgumentException if {@code text} contains an ASCII digit
   */
  public static String encode(String text) {
    Objects.requireNonNull(text, "text must not be null");
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Character, Integer> run : runs(text)) {
      char c = run.getKey();
      if (isAsciiDigit(c)) {
        throw new IllegalArgumentException("text must not contain ASCII digits: " + text);
      }
      sb.append(run.getValue()).append(c);
    }
    return sb.toString();
  }

  /**
   * Decodes {@code encoded}, the inverse of {@link #encode}.
   *
   * @throws NullPointerException if {@code encoded} is {@code null}
   * @throws IllegalArgumentException if {@code encoded} is not a sequence of a positive count with no
   *     leading zero followed by one non-digit character
   */
  public static String decode(String encoded) {
    Objects.requireNonNull(encoded, "encoded must not be null");
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (i < encoded.length()) {
      int end = skipDigits(encoded, i);
      if (end == i || end == encoded.length() || encoded.charAt(i) == '0') {
        throw malformed(encoded);
      }
      sb.repeat(encoded.charAt(end), parseCount(encoded, i, end));
      i = end + 1;
    }
    return sb.toString();
  }

  /** Returns the runs of {@code text} in order, each as a character and its count. */
  public static List<Map.Entry<Character, Integer>> runs(String text) {
    Objects.requireNonNull(text, "text must not be null");
    List<Map.Entry<Character, Integer>> result = new ArrayList<>();
    int start = 0;
    while (start < text.length()) {
      char c = text.charAt(start);
      int end = start + 1;
      while (end < text.length() && text.charAt(end) == c) {
        end++;
      }
      result.add(Map.entry(c, end - start));
      start = end;
    }
    return List.copyOf(result);
  }

  private static boolean isAsciiDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static int skipDigits(String s, int from) {
    int i = from;
    while (i < s.length() && isAsciiDigit(s.charAt(i))) {
      i++;
    }
    return i;
  }

  private static int parseCount(String encoded, int start, int end) {
    try {
      return Integer.parseInt(encoded, start, end, 10);
    } catch (NumberFormatException e) {
      throw malformed(encoded);
    }
  }

  private static IllegalArgumentException malformed(String encoded) {
    return new IllegalArgumentException("malformed run-length encoding: " + encoded);
  }
}
