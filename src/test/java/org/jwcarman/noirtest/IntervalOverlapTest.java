package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class IntervalOverlapTest {

  private static Interval iv(int s, int e) {
    return new Interval(s, e);
  }

  @Test
  void overlapsPartial() {
    assertTrue(iv(1, 5).overlaps(iv(3, 8)));
    assertTrue(iv(3, 8).overlaps(iv(1, 5)));
  }

  @Test
  void adjacentDoNotOverlap() {
    assertFalse(iv(1, 5).overlaps(iv(5, 8)));
    assertFalse(iv(5, 8).overlaps(iv(1, 5)));
  }

  @Test
  void containedOverlaps() {
    assertTrue(iv(1, 10).overlaps(iv(3, 4)));
    assertTrue(iv(3, 4).overlaps(iv(1, 10)));
  }

  @Test
  void disjointDoNotOverlap() {
    assertFalse(iv(1, 5).overlaps(iv(7, 9)));
    assertFalse(iv(7, 9).overlaps(iv(1, 5)));
  }

  @Test
  void singleValueOverlapAtEdges() {
    assertTrue(iv(1, 5).overlaps(iv(4, 6)));
    assertTrue(iv(4, 6).overlaps(iv(1, 5)));
    assertTrue(iv(1, 5).overlaps(iv(1, 5)));
  }

  @Test
  void overlapsNullThrows() {
    assertThrows(NullPointerException.class, () -> iv(1, 5).overlaps(null));
  }

  @Test
  void intersectionPartial() {
    assertEquals(Optional.of(iv(3, 5)), iv(1, 5).intersection(iv(3, 8)));
    assertEquals(Optional.of(iv(3, 5)), iv(3, 8).intersection(iv(1, 5)));
  }

  @Test
  void intersectionContained() {
    assertEquals(Optional.of(iv(3, 4)), iv(1, 10).intersection(iv(3, 4)));
    assertEquals(Optional.of(iv(3, 4)), iv(3, 4).intersection(iv(1, 10)));
  }

  @Test
  void intersectionAdjacentEmpty() {
    assertEquals(Optional.empty(), iv(1, 5).intersection(iv(5, 8)));
    assertEquals(Optional.empty(), iv(5, 8).intersection(iv(1, 5)));
    assertEquals(Optional.empty(), iv(1, 5).intersection(iv(7, 9)));
  }

  @Test
  void intersectionExtremes() {
    assertEquals(
        Optional.of(iv(-3, 3)),
        iv(Integer.MIN_VALUE, Integer.MAX_VALUE).intersection(iv(-3, 3)));
    assertEquals(
        Optional.of(iv(Integer.MIN_VALUE, Integer.MAX_VALUE)),
        iv(Integer.MIN_VALUE, Integer.MAX_VALUE)
            .intersection(iv(Integer.MIN_VALUE, Integer.MAX_VALUE)));
  }

  @Test
  void intersectionNullThrows() {
    assertThrows(NullPointerException.class, () -> iv(1, 5).intersection(null));
  }
}
