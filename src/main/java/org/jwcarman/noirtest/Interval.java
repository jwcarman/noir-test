package org.jwcarman.noirtest;

import java.util.Objects;
import java.util.Optional;

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

  /**
   * Returns whether this interval and {@code other} share at least one value. Adjacent intervals,
   * such as {@code [1, 5)} and {@code [5, 8)}, share none.
   *
   * @throws NullPointerException if {@code other} is {@code null}
   */
  public boolean overlaps(Interval other) {
    Objects.requireNonNull(other, "other");
    return start < other.end && other.start < end;
  }

  /**
   * Returns the values this interval and {@code other} share, as an interval, or empty if they
   * share none.
   *
   * @throws NullPointerException if {@code other} is {@code null}
   */
  public Optional<Interval> intersection(Interval other) {
    if (!overlaps(other)) {
      return Optional.empty();
    }
    return Optional.of(new Interval(Math.max(start, other.start), Math.min(end, other.end)));
  }
}
