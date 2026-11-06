package org.jwcarman.noirtest;

/** Range limiting. */
public final class Bounds {

  private Bounds() {}

  /**
   * Returns {@code min} if {@code value < min}, {@code max} if {@code value > max}, and {@code
   * value} otherwise.
   *
   * @throws IllegalArgumentException if {@code min > max}
   */
  public static int clamp(int value, int min, int max) {
    if (min > max) {
      throw new IllegalArgumentException("min must not exceed max: " + min + " > " + max);
    }
    return Math.min(Math.max(value, min), max);
  }
}
