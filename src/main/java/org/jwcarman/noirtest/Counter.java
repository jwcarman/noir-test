package org.jwcarman.noirtest;

/** A counter that starts at zero and can be incremented, decremented, and reset. */
public class Counter {

  private int count;

  /** Creates a counter whose count is zero. */
  public Counter() {}

  /** Adds one to the count. */
  public void increment() {
    count++;
  }

  /**
   * Subtracts one from the count.
   *
   * @throws IllegalStateException if the count is already zero
   */
  public void decrement() {
    if (count == 0) {
      throw new IllegalStateException("count is already zero");
    }
    count--;
  }

  /** Returns the current count. */
  public int get() {
    return count;
  }

  /** Sets the count back to zero. */
  public void reset() {
    count = 0;
  }
}
