package org.jwcarman.noirtest;

/**
 * A counter that starts at a non-negative value (zero by default) and can be incremented,
 * decremented, and reset to its starting value.
 */
public class Counter {

  private final int start;
  private int count;

  /** Creates a counter whose count, and starting value, is zero. */
  public Counter() {
    this(0);
  }

  /**
   * Creates a counter whose count, and starting value, is {@code start}.
   *
   * @param start the starting count, at least 0
   * @throws IllegalArgumentException if {@code start} is negative
   */
  public Counter(int start) {
    if (start < 0) {
      throw new IllegalArgumentException("start must not be negative: " + start);
    }
    this.start = start;
    this.count = start;
  }

  /** Adds one to the count. */
  public void increment() {
    count++;
  }

  /**
   * Subtracts one from the count.
   *
   * @throws IllegalStateException with the message {@code cannot decrement below zero} if the
   *     count is already zero
   */
  public void decrement() {
    if (count == 0) {
      throw new IllegalStateException("cannot decrement below zero");
    }
    count--;
  }

  /** Returns the current count. */
  public int get() {
    return count;
  }

  /** Sets the count back to the starting value: {@code start}, or zero for {@code new Counter()}. */
  public void reset() {
    count = start;
  }
}
