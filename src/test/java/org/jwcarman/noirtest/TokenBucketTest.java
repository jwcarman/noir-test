package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.Test;

class TokenBucketTest {

  private final AtomicLong clock = new AtomicLong(0);

  private TokenBucket bucket(long capacity, long refill, Duration period) {
    return new TokenBucket(capacity, refill, period, clock::get);
  }

  private TokenBucket standard() {
    return bucket(3, 1, Duration.ofSeconds(1));
  }

  @Test
  void newBucketIsFull() {
    assertEquals(3, standard().availableTokens());
  }

  @Test
  void tryAcquireTakesOneUntilEmpty() {
    var b = standard();
    assertTrue(b.tryAcquire());
    assertTrue(b.tryAcquire());
    assertTrue(b.tryAcquire());
    assertFalse(b.tryAcquire());
    assertEquals(0, b.availableTokens());
  }

  @Test
  void tryAcquireTakesExactlyOne() {
    var b = standard();
    assertTrue(b.tryAcquire());
    assertEquals(2, b.availableTokens());
  }

  @Test
  void noTokenBeforeWholePeriod() {
    var b = standard();
    assertTrue(b.tryAcquire(3));
    clock.set(999_999_999L);
    assertFalse(b.tryAcquire());
    assertEquals(0, b.availableTokens());
  }

  @Test
  void tokenAfterWholePeriod() {
    var b = standard();
    assertTrue(b.tryAcquire(3));
    clock.set(1_000_000_000L);
    assertTrue(b.tryAcquire());
    assertFalse(b.tryAcquire());
  }

  @Test
  void leftoverTimeCarries() {
    var b = standard();
    assertTrue(b.tryAcquire(3));
    clock.set(1_500_000_000L);
    assertEquals(1, b.availableTokens());
    clock.set(2_000_000_000L);
    assertEquals(2, b.availableTokens());
    clock.set(2_999_999_999L);
    assertEquals(2, b.availableTokens());
  }

  @Test
  void refillCappedAtCapacity() {
    var b = standard();
    assertTrue(b.tryAcquire(3));
    clock.set(100_000_000_000L);
    assertEquals(3, b.availableTokens());
  }

  @Test
  void partialRefillThenCap() {
    var b = standard();
    assertTrue(b.tryAcquire(3));
    clock.set(2_000_000_000L);
    assertEquals(2, b.availableTokens());
    clock.set(3_000_000_000L);
    assertEquals(3, b.availableTokens());
    clock.set(4_000_000_000L);
    assertEquals(3, b.availableTokens());
  }

  @Test
  void periodsWhileFullAreNotBanked() {
    var b = standard();
    clock.set(10_000_000_000L);
    assertTrue(b.tryAcquire(3));
    clock.set(10_500_000_000L);
    assertEquals(0, b.availableTokens());
    clock.set(11_000_000_000L);
    assertEquals(1, b.availableTokens());
  }

  @Test
  void multipleTokensPerPeriod() {
    var b = bucket(5, 2, Duration.ofMillis(500));
    assertTrue(b.tryAcquire(5));
    clock.set(1_200_000_000L);
    assertEquals(4, b.availableTokens());
    clock.set(1_500_000_000L);
    assertEquals(5, b.availableTokens());
  }

  @Test
  void refillAddsToExistingTokens() {
    var b = bucket(10, 2, Duration.ofSeconds(1));
    assertTrue(b.tryAcquire(7));
    clock.set(1_000_000_000L);
    assertEquals(5, b.availableTokens());
    clock.set(4_000_000_000L);
    assertEquals(10, b.availableTokens());
  }

  @Test
  void refillExactlyFillingMissing() {
    var b = bucket(4, 2, Duration.ofSeconds(1));
    assertTrue(b.tryAcquire(4));
    clock.set(1_000_000_000L);
    assertEquals(2, b.availableTokens());
    clock.set(2_000_000_000L);
    assertEquals(4, b.availableTokens());
  }

  @Test
  void allOrNothing() {
    var b = standard();
    assertTrue(b.tryAcquire(2));
    assertFalse(b.tryAcquire(2));
    assertEquals(1, b.availableTokens());
    assertTrue(b.tryAcquire(1));
    assertEquals(0, b.availableTokens());
  }

  @Test
  void moreThanCapacityFails() {
    var b = standard();
    assertFalse(b.tryAcquire(4));
    assertEquals(3, b.availableTokens());
  }

  @Test
  void exactlyCapacitySucceeds() {
    var b = standard();
    assertTrue(b.tryAcquire(3));
    assertEquals(0, b.availableTokens());
  }

  @Test
  void invalidPermitsRejected() {
    var b = standard();
    assertThrows(IllegalArgumentException.class, () -> b.tryAcquire(0));
    assertThrows(IllegalArgumentException.class, () -> b.tryAcquire(-1));
    assertThrows(IllegalArgumentException.class, () -> b.tryAcquire(Long.MIN_VALUE));
    assertEquals(3, b.availableTokens());
  }

  @Test
  void availableTokensDoesNotTake() {
    var b = standard();
    b.availableTokens();
    b.availableTokens();
    b.availableTokens();
    assertTrue(b.tryAcquire(3));
  }

  @Test
  void backwardsClockAddsNothingAndKeepsReference() {
    clock.set(10_000_000_000L);
    var b = standard();
    assertTrue(b.tryAcquire(3));
    clock.set(5_000_000_000L);
    assertEquals(0, b.availableTokens());
    clock.set(11_000_000_000L);
    assertEquals(1, b.availableTokens());
  }

  @Test
  void wrapAroundWholePeriod() {
    clock.set(Long.MAX_VALUE - 499_999_999L);
    var b = bucket(1, 1, Duration.ofSeconds(1));
    assertTrue(b.tryAcquire());
    clock.addAndGet(1_000_000_000L);
    assertTrue(clock.get() < 0);
    assertTrue(b.tryAcquire());
  }

  @Test
  void wrapAroundPartialPeriod() {
    clock.set(Long.MAX_VALUE - 499_999_999L);
    var b = bucket(1, 1, Duration.ofSeconds(1));
    assertTrue(b.tryAcquire());
    clock.addAndGet(999_999_999L);
    assertFalse(b.tryAcquire());
  }

  @Test
  void hugeRefillCapped() {
    var b = bucket(10, Long.MAX_VALUE, Duration.ofNanos(1));
    assertTrue(b.tryAcquire(10));
    clock.set(3);
    assertEquals(10, b.availableTokens());
  }

  @Test
  void productExactlyAtLimitNotOverflowing() {
    long refill = Long.MAX_VALUE / 2;
    var b = bucket(10, refill, Duration.ofNanos(1));
    assertTrue(b.tryAcquire(10));
    clock.set(2);
    assertEquals(10, b.availableTokens());
  }

  @Test
  void maxPeriodAccepted() {
    var b = bucket(1, 1, Duration.ofNanos(Long.MAX_VALUE));
    assertTrue(b.tryAcquire());
    clock.set(Long.MAX_VALUE - 1);
    assertFalse(b.tryAcquire());
    clock.set(Long.MAX_VALUE);
    assertTrue(b.tryAcquire());
  }

  @Test
  void clockReadOnceAtConstructionAndPerCall() {
    var reads = new AtomicLong();
    LongSupplier counting = () -> {
      reads.incrementAndGet();
      return 0L;
    };
    var b = new TokenBucket(3, 1, Duration.ofSeconds(1), counting);
    assertEquals(1, reads.get());
    b.availableTokens();
    assertEquals(2, reads.get());
    b.tryAcquire();
    assertEquals(3, reads.get());
    b.tryAcquire(2);
    assertEquals(4, reads.get());
  }

  @Test
  void constructorRejectsInvalidArguments() {
    Duration s = Duration.ofSeconds(1);
    assertThrows(IllegalArgumentException.class, () -> new TokenBucket(0, 1, s, () -> 0L));
    assertThrows(IllegalArgumentException.class, () -> new TokenBucket(-5, 1, s, () -> 0L));
    assertThrows(IllegalArgumentException.class, () -> new TokenBucket(1, 0, s, () -> 0L));
    assertThrows(IllegalArgumentException.class, () -> new TokenBucket(1, -1, s, () -> 0L));
    assertThrows(
        IllegalArgumentException.class, () -> new TokenBucket(1, 1, Duration.ZERO, () -> 0L));
    assertThrows(
        IllegalArgumentException.class,
        () -> new TokenBucket(1, 1, Duration.ofSeconds(-1), () -> 0L));
    assertThrows(
        IllegalArgumentException.class,
        () -> new TokenBucket(1, 1, Duration.ofSeconds(Long.MAX_VALUE), () -> 0L));
    assertThrows(NullPointerException.class, () -> new TokenBucket(1, 1, null, () -> 0L));
    assertThrows(NullPointerException.class, () -> new TokenBucket(1, 1, s, null));
  }

  @Test
  void minimalValidArgumentsAccepted() {
    var b = new TokenBucket(1, 1, Duration.ofNanos(1), () -> 0L);
    assertEquals(1, b.availableTokens());
  }
}
