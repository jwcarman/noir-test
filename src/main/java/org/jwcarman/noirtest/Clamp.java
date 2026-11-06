package org.jwcarman.noirtest;

/** Range limiting. */
public final class Clamp {

  private Clamp() {}

  /**
   * Returns {@code min} if {@code value < min}, {@code max} if {@code value > max}, and {@code
   * value} otherwise.
   *
   * @throws IllegalArgumentException if {@code min > max}
   */
  public static int clamp(int value, int min, int max) {
    if (min > max) {
      throw new IllegalArgumentException(
          "min (" + min + ") must not be greater than max (" + max + ")");
    }
    return Math.max(min, Math.min(value, max));
  }
}
