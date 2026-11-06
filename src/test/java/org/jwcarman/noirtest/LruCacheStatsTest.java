package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LruCacheStatsTest {

  @Test
  void newCacheHasNoHitsOrMisses() {
    var c = new LruCache<String, Integer>(2);
    assertEquals(0, c.hitCount());
    assertEquals(0, c.missCount());
  }

  @Test
  void getCountsHitsAndMisses() {
    var c = new LruCache<String, Integer>(2);
    c.put("a", 1);
    assertEquals(1, c.get("a"));
    assertEquals(1, c.get("a"));
    assertNull(c.get("b"));
    assertEquals(2, c.hitCount());
    assertEquals(1, c.missCount());
  }

  @Test
  void getOfEvictedKeyIsMiss() {
    var c = new LruCache<String, Integer>(1);
    c.put("a", 1);
    c.put("b", 2);
    assertNull(c.get("a"));
    assertEquals(1, c.missCount());
    assertEquals(0, c.hitCount());
  }

  @Test
  void onlyGetChangesCounts() {
    var c = new LruCache<String, Integer>(2);
    c.put("a", 1);
    assertTrue(c.containsKey("a"));
    assertFalse(c.containsKey("b"));
    assertEquals(1, c.size());
    assertEquals(1, c.remove("a"));
    assertEquals(0, c.hitCount());
    assertEquals(0, c.missCount());
  }

  @Test
  void getThatThrowsDoesNotCount() {
    var c = new LruCache<String, Integer>(2);
    assertThrows(NullPointerException.class, () -> c.get(null));
    assertEquals(0, c.hitCount());
    assertEquals(0, c.missCount());
  }

  @Test
  void resetStatsZeroesCountsAndKeepsEntries() {
    var c = new LruCache<String, Integer>(2);
    c.put("a", 1);
    c.get("a");
    c.get("b");
    c.resetStats();
    assertEquals(0, c.hitCount());
    assertEquals(0, c.missCount());
    assertEquals(1, c.get("a"));
    assertEquals(1, c.hitCount());
    assertEquals(0, c.missCount());
  }

  @Test
  void resetStatsZeroesMissesIndependently() {
    var c = new LruCache<String, Integer>(2);
    c.get("x");
    c.resetStats();
    assertEquals(0, c.missCount());
    c.get("y");
    assertEquals(1, c.missCount());
  }

  @Test
  void countingKeepsRecencyOrder() {
    var c = new LruCache<String, Integer>(2);
    c.put("a", 1);
    c.put("b", 2);
    c.get("a");
    c.put("c", 3);
    assertTrue(c.containsKey("a"));
    assertFalse(c.containsKey("b"));
  }
}
