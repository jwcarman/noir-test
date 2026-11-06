package org.jwcarman.noirtest;

import java.time.Duration;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * A token-bucket rate limiter with whole-period refills, driven by a supplied clock.
 *
 * <p><b>Clock.</b> {@code nanoTime} is read like {@link System#nanoTime()}: only differences
 * between readings are meaningful, and a difference is computed as {@code later - earlier} with
 * ordinary {@code long} subtraction, so it stays correct when the readings wrap past {@link
 * Long#MAX_VALUE}. The clock is read once at construction and once at the start of every call to
 * {@link #tryAcquire(long)}, {@link #tryAcquire()} and {@link #availableTokens()}.
 *
 * <p><b>State.</b> The bucket holds between 0 and {@code capacity} tokens and remembers a reference
 * time. It starts with {@code capacity} tokens, and its reference time is the construction reading.
 *
 * <p><b>Refill.</b> Each call first refills. Let {@code elapsed} be the current reading minus the
 * reference time. If {@code elapsed} is not positive, nothing changes. Otherwise let {@code periods}
 * be {@code elapsed / periodNanos}, rounded down. If {@code periods} is positive, the bucket gains
 * {@code periods * refillTokens} tokens, capped at {@code capacity} (the product may exceed {@code
 * Long.MAX_VALUE}, and the cap still applies), and the reference time moves forward by {@code
 * periods * periodNanos}. The time left over carries into the next refill. Whole periods that pass
 * while the bucket is full are not banked.
 *
 * <p>Instances are not thread-safe.
 */
public final class TokenBucket {

  private final long capacity;
  private final long refillTokens;
  private final long periodNanos;
  private final LongSupplier nanoTime;
  private long tokens;
  private long reference;

  /**
   * Creates a full bucket.
   *
   * @param capacity the most tokens the bucket holds, at least 1
   * @param refillTokens the tokens added per whole period, at least 1
   * @param refillPeriod the period, positive and at most {@link Long#MAX_VALUE} nanoseconds
   * @param nanoTime the clock
   * @throws IllegalArgumentException if {@code capacity} or {@code refillTokens} is less than 1, or
   *     {@code refillPeriod} is zero, negative or longer than {@link Long#MAX_VALUE} nanoseconds
   * @throws NullPointerException if {@code refillPeriod} or {@code nanoTime} is {@code null}
   */
  public TokenBucket(
      long capacity, long refillTokens, Duration refillPeriod, LongSupplier nanoTime) {
    Objects.requireNonNull(refillPeriod, "refillPeriod");
    Objects.requireNonNull(nanoTime, "nanoTime");
    requirePositive(capacity, "capacity");
    requirePositive(refillTokens, "refillTokens");
    this.capacity = capacity;
    this.refillTokens = refillTokens;
    this.periodNanos = toPeriodNanos(refillPeriod);
    this.nanoTime = nanoTime;
    this.tokens = capacity;
    this.reference = nanoTime.getAsLong();
  }

  /**
   * Refills, then takes {@code permits} tokens if the bucket holds at least that many. Takes
   * nothing otherwise.
   *
   * @param permits the tokens to take, at least 1; more than the capacity never succeeds
   * @return {@code true} if the tokens were taken
   * @throws IllegalArgumentException if {@code permits} is less than 1
   */
  public boolean tryAcquire(long permits) {
    requirePositive(permits, "permits");
    refill();
    if (permits > tokens) {
      return false;
    }
    tokens -= permits;
    return true;
  }

  /**
   * Same as {@code tryAcquire(1)}.
   *
   * @return {@code true} if a token was taken
   */
  public boolean tryAcquire() {
    return tryAcquire(1);
  }

  /**
   * Refills, then returns the number of tokens the bucket holds, without taking any.
   *
   * @return the tokens available, from 0 to the capacity
   */
  public long availableTokens() {
    refill();
    return tokens;
  }

  private void refill() {
    long elapsed = nanoTime.getAsLong() - reference;
    if (elapsed <= 0) {
      return;
    }
    long periods = elapsed / periodNanos;
    reference += periods * periodNanos;
    long missing = capacity - tokens;
    long added = periods > Long.MAX_VALUE / refillTokens ? Long.MAX_VALUE : periods * refillTokens;
    tokens = added >= missing ? capacity : tokens + added;
  }

  private static long toPeriodNanos(Duration period) {
    if (period.isNegative() || period.isZero()) {
      throw new IllegalArgumentException("refillPeriod must be positive: " + period);
    }
    try {
      return period.toNanos();
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException("refillPeriod is too long: " + period, e);
    }
  }

  private static void requirePositive(long value, String name) {
    if (value < 1) {
      throw new IllegalArgumentException(name + " must be at least 1: " + value);
    }
  }
}
