package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FractionTest {

  private static void assertParts(long n, long d, Fraction f) {
    assertEquals(n, f.numerator());
    assertEquals(d, f.denominator());
  }

  @Test
  void reducesToLowestTerms() {
    assertParts(1, 2, new Fraction(2, 4));
  }

  @Test
  void movesSignToNumerator() {
    assertEquals(new Fraction(-1, 2), new Fraction(2, -4));
    assertParts(-1, 2, new Fraction(2, -4));
    assertParts(1, 2, new Fraction(-2, -4));
    assertParts(-1, 3, new Fraction(-1, 3));
  }

  @Test
  void zeroNormalizes() {
    assertParts(0, 1, new Fraction(0, -7));
    assertParts(0, 1, new Fraction(0, 5));
    assertEquals(new Fraction(0, 1), new Fraction(0, -7));
  }

  @Test
  void alreadyReducedIsKept() {
    assertParts(3, 7, new Fraction(3, 7));
    assertParts(5, 1, new Fraction(5, 1));
  }

  @Test
  void zeroDenominatorThrows() {
    assertThrows(ArithmeticException.class, () -> new Fraction(1, 0));
    assertThrows(ArithmeticException.class, () -> new Fraction(0, 0));
  }

  @Test
  void unrepresentableThrows() {
    assertThrows(ArithmeticException.class, () -> new Fraction(Long.MIN_VALUE, -1));
    assertThrows(ArithmeticException.class, () -> new Fraction(1, Long.MIN_VALUE));
  }

  @Test
  void extremeValuesThatFit() {
    assertParts(Long.MIN_VALUE, 1, new Fraction(Long.MIN_VALUE, 1));
    assertParts(1, 1, new Fraction(Long.MIN_VALUE, Long.MIN_VALUE));
    assertParts(1, Long.MAX_VALUE, new Fraction(-1, -Long.MAX_VALUE));
    assertParts(Long.MIN_VALUE / -2, 1, new Fraction(Long.MIN_VALUE, -2));
  }

  @Test
  void equality() {
    assertEquals(new Fraction(3, 6), new Fraction(1, 2));
    assertEquals(new Fraction(3, 6).hashCode(), new Fraction(1, 2).hashCode());
    assertNotEquals(new Fraction(1, 2), new Fraction(1, 3));
  }

  @Test
  void plus() {
    assertEquals(new Fraction(5, 6), new Fraction(1, 2).plus(new Fraction(1, 3)));
    assertEquals(new Fraction(0, 1), new Fraction(1, 2).plus(new Fraction(-1, 2)));
    assertParts(0, 1, new Fraction(1, 2).plus(new Fraction(-1, 2)));
    assertParts(1, 1, new Fraction(1, 2).plus(new Fraction(1, 2)));
    assertParts(-1, 12, new Fraction(1, 4).plus(new Fraction(-1, 3)));
  }

  @Test
  void plusOverflowThrows() {
    assertThrows(
        ArithmeticException.class,
        () -> new Fraction(Long.MAX_VALUE, 1).plus(new Fraction(1, 1)));
    assertThrows(
        ArithmeticException.class,
        () -> new Fraction(1, Long.MAX_VALUE).plus(new Fraction(1, Long.MAX_VALUE - 1)));
  }

  @Test
  void plusWithLargeIntermediatesThatReduce() {
    Fraction a = new Fraction(1, Long.MAX_VALUE);
    assertParts(2, Long.MAX_VALUE, a.plus(a));
    Fraction big = new Fraction(Long.MAX_VALUE - 1, Long.MAX_VALUE);
    assertParts(1, 1, big.plus(a));
  }

  @Test
  void times() {
    assertEquals(new Fraction(1, 2), new Fraction(2, 3).times(new Fraction(3, 4)));
    assertParts(1, 2, new Fraction(2, 3).times(new Fraction(3, 4)));
    assertParts(-3, 8, new Fraction(-1, 2).times(new Fraction(3, 4)));
    assertParts(0, 1, new Fraction(0, 1).times(new Fraction(3, 4)));
  }

  @Test
  void timesWithLargeIntermediatesThatReduce() {
    Fraction a = new Fraction(Long.MAX_VALUE, 2);
    Fraction b = new Fraction(2, Long.MAX_VALUE);
    assertParts(1, 1, a.times(b));
  }

  @Test
  void timesOverflowThrows() {
    assertThrows(
        ArithmeticException.class,
        () -> new Fraction(Long.MAX_VALUE, 1).times(new Fraction(2, 1)));
    assertThrows(
        ArithmeticException.class,
        () -> new Fraction(1, Long.MAX_VALUE).times(new Fraction(1, 2)));
  }

  @Test
  void compareTo() {
    assertTrue(new Fraction(1, 3).compareTo(new Fraction(1, 2)) < 0);
    assertTrue(new Fraction(1, 2).compareTo(new Fraction(1, 3)) > 0);
    assertEquals(0, new Fraction(-1, 2).compareTo(new Fraction(-2, 4)));
    assertTrue(new Fraction(-1, 2).compareTo(new Fraction(1, 3)) < 0);
    assertTrue(new Fraction(-1, 2).compareTo(new Fraction(-1, 3)) < 0);
  }

  @Test
  void compareToDoesNotOverflow() {
    Fraction a = new Fraction(Long.MAX_VALUE - 1, Long.MAX_VALUE);
    Fraction b = new Fraction(Long.MAX_VALUE - 2, Long.MAX_VALUE - 1);
    assertTrue(a.compareTo(b) > 0);
    assertTrue(b.compareTo(a) < 0);
    assertTrue(new Fraction(Long.MIN_VALUE, 1).compareTo(new Fraction(Long.MAX_VALUE, 1)) < 0);
  }

  @Test
  void toStringFormats() {
    assertEquals("3", new Fraction(3, 1).toString());
    assertEquals("-1/2", new Fraction(-3, 6).toString());
    assertEquals("0", new Fraction(0, -7).toString());
    assertEquals("2/3", new Fraction(4, 6).toString());
    assertEquals("-5", new Fraction(10, -2).toString());
  }
}
