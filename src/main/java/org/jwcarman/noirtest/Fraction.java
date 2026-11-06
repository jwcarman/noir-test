package org.jwcarman.noirtest;

import java.math.BigInteger;

/**
 * An immutable fraction in lowest terms with a positive denominator: {@code new Fraction(2, -4)}
 * equals {@code new Fraction(-1, 2)}. Two fractions are equal exactly when their reduced
 * numerators and denominators are equal. Arithmetic that overflows {@code long} throws {@link
 * ArithmeticException}.
 *
 * @param numerator the reduced numerator
 * @param denominator the reduced denominator, positive
 */
public record Fraction(long numerator, long denominator) implements Comparable<Fraction> {

  /**
   * Creates the fraction {@code numerator / denominator}, reduced, with the sign on the numerator.
   *
   * @throws ArithmeticException if {@code denominator} is zero, or the reduced value cannot be
   *     represented (for example {@code Long.MIN_VALUE / -1})
   */
  public Fraction {
    if (denominator == 0) {
      throw new ArithmeticException("Denominator must not be zero");
    }
    BigInteger n = BigInteger.valueOf(numerator);
    BigInteger d = BigInteger.valueOf(denominator);
    BigInteger g = n.gcd(d);
    if (denominator < 0) {
      g = g.negate();
    }
    numerator = n.divide(g).longValueExact();
    denominator = d.divide(g).longValueExact();
  }

  /** Returns {@code this + other}. */
  public Fraction plus(Fraction other) {
    BigInteger n =
        big(numerator)
            .multiply(big(other.denominator))
            .add(big(other.numerator).multiply(big(denominator)));
    return of(n, big(denominator).multiply(big(other.denominator)));
  }

  /** Returns {@code this * other}. */
  public Fraction times(Fraction other) {
    return of(
        big(numerator).multiply(big(other.numerator)),
        big(denominator).multiply(big(other.denominator)));
  }

  /** Compares by value. */
  @Override
  public int compareTo(Fraction other) {
    return big(numerator)
        .multiply(big(other.denominator))
        .compareTo(big(other.numerator).multiply(big(denominator)));
  }

  /** Returns {@code "n/d"}, or {@code "n"} when the denominator is 1. */
  @Override
  public String toString() {
    return denominator == 1 ? Long.toString(numerator) : numerator + "/" + denominator;
  }

  private static BigInteger big(long value) {
    return BigInteger.valueOf(value);
  }

  /** Reduces a fraction whose denominator is positive and builds it if it fits in {@code long}. */
  private static Fraction of(BigInteger n, BigInteger d) {
    BigInteger g = n.gcd(d);
    return new Fraction(n.divide(g).longValueExact(), d.divide(g).longValueExact());
  }
}
