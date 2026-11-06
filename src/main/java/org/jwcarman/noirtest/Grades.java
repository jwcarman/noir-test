package org.jwcarman.noirtest;

import java.util.Objects;

/** Letter grades. */
public final class Grades {

  /** The letter for each band of ten points: index {@code score / 10}. */
  private static final String LETTERS = "FFFFFFDCBAA";

  private static final int MAX_SCORE = 100;

  private Grades() {}

  /**
   * Returns {@code 'A'} for 90 to 100, {@code 'B'} for 80 to 89, {@code 'C'} for 70 to 79, {@code
   * 'D'} for 60 to 69 and {@code 'F'} for 0 to 59.
   *
   * @throws IllegalArgumentException if {@code score} is less than 0 or greater than 100
   */
  public static char letter(int score) {
    try {
      return LETTERS.charAt(Objects.checkIndex(score, MAX_SCORE + 1) / 10);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("score must be between 0 and 100: " + score, e);
    }
  }
}
