package org.jwcarman.noirtest;

/**
 * A non-empty half-open range of {@code int} values: every {@code x} with {@code start <= x < end}.
 *
 * <p>Two intervals are equal exactly when their starts and their ends are equal.
 *
 * @param start the first value in the range
 * @param end the value just past the last value in the range
 */
public record Interval(int start, int end) {

  /**
   * Creates the interval {@code [start, end)}.
   *
   * @throws IllegalArgumentException if {@code start >= end}
   */
  public Interval {
    if (start >= end) {
      throw new IllegalArgumentException(
          "start must be less than end: [" + start + ", " + end + ")");
    }
  }

  /**
   * Returns the number of values in this interval, {@code end - start}, computed without {@code
   * int} overflow.
   */
  public long length() {
    return (long) end - start;
  }

  /** Returns whether {@code start <= value < end}. */
  public boolean contains(int value) {
    return start <= value && value < end;
  }
}
