package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LruCacheResizeTest {

  private final List<String> evicted = new ArrayList<>();

  private LruCache<String, Integer> cache(int capacity) {
    return new LruCache<>(capacity, (k, v) -> evicted.add(k + "=" + v));
  }

  @Test
  void capacityReturnsConstructorValue() {
    assertEquals(4, cache(4).capacity());
    assertEquals(1, new LruCache<String, Integer>(1).capacity());
  }

  @Test
  void shrinkingEvictsLeastRecentlyUsedOldestFirst() {
    LruCache<String, Integer> c = cache(4);
    c.put("a", 1);
    c.put("b", 2);
    c.put("c", 3);
    c.put("d", 4);
    c.get("a");
    c.resize(2);
    assertEquals(List.of("b=2", "c=3"), evicted);
    assertEquals(2, c.size());
    assertTrue(c.containsKey("a"));
    assertTrue(c.containsKey("d"));
    assertFalse(c.containsKey("b"));
    assertFalse(c.containsKey("c"));
    assertEquals(2, c.capacity());

    evicted.clear();
    assertEquals(null, c.put("e", 5));
    // Recency is preserved by resize: order is d, a (a was read last), so d is now eldest.
    assertEquals(List.of("d=4"), evicted);
    assertTrue(c.containsKey("a"));
    assertTrue(c.containsKey("e"));
    assertEquals(2, c.size());
  }

  @Test
  void shrinkingPreservesRecencyOrderOfRemaining() {
    LruCache<String, Integer> c = cache(4);
    c.put("a", 1);
    c.put("b", 2);
    c.put("c", 3);
    c.put("d", 4);
    c.resize(3);
    assertEquals(List.of("a=1"), evicted);
    c.resize(1);
    assertEquals(List.of("a=1", "b=2", "c=3"), evicted);
    assertTrue(c.containsKey("d"));
    assertEquals(1, c.size());
  }

  @Test
  void growingKeepsEntries() {
    LruCache<String, Integer> c = cache(2);
    c.put("a", 1);
    c.put("b", 2);
    c.resize(3);
    c.put("c", 3);
    assertEquals(3, c.size());
    assertEquals(3, c.capacity());
    assertTrue(evicted.isEmpty());
    c.put("d", 4);
    assertEquals(List.of("a=1"), evicted);
    assertEquals(3, c.size());
  }

  @Test
  void shrinkingToSizeEvictsNothing() {
    LruCache<String, Integer> c = cache(3);
    c.put("a", 1);
    c.put("b", 2);
    c.resize(2);
    assertTrue(evicted.isEmpty());
    assertEquals(2, c.capacity());
    assertEquals(2, c.size());
    c.put("c", 3);
    assertEquals(List.of("a=1"), evicted);
  }

  @Test
  void resizeOnEmptyCache() {
    LruCache<String, Integer> c = new LruCache<>(5);
    c.resize(1);
    assertEquals(1, c.capacity());
    assertEquals(0, c.size());
  }

  @Test
  void invalidCapacityLeavesCacheUnchanged() {
    LruCache<String, Integer> c = cache(2);
    c.put("a", 1);
    c.put("b", 2);
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> c.resize(0));
    assertEquals("capacity must be at least 1, was 0", e.getMessage());
    assertThrows(IllegalArgumentException.class, () -> c.resize(-3));
    assertEquals(2, c.size());
    assertEquals(2, c.capacity());
    assertTrue(evicted.isEmpty());
  }

  @Test
  void resizeToOneIsValid() {
    LruCache<String, Integer> c = cache(2);
    c.put("a", 1);
    c.put("b", 2);
    c.resize(1);
    assertEquals(1, c.capacity());
    assertEquals(List.of("a=1"), evicted);
  }

  @Test
  void throwingListenerStopsAfterFirstEviction() {
    List<String> seen = new ArrayList<>();
    LruCache<String, Integer> c =
        new LruCache<>(
            3,
            (k, v) -> {
              seen.add(k);
              throw new IllegalStateException("boom");
            });
    c.put("a", 1);
    c.put("b", 2);
    c.put("c", 3);
    assertThrows(IllegalStateException.class, () -> c.resize(1));
    assertEquals(List.of("a"), seen);
    assertFalse(c.containsKey("a"));
    assertTrue(c.containsKey("b"));
    assertTrue(c.containsKey("c"));
    assertEquals(2, c.size());
    assertEquals(1, c.capacity());
  }

  @Test
  void putEvictionStillReportsEvictedEntryAfterRefactor() {
    LruCache<String, Integer> c = cache(1);
    c.put("a", 1);
    c.put("b", 2);
    assertEquals(List.of("a=1"), evicted);
    assertEquals(Integer.valueOf(2), c.get("b"));
    assertFalse(c.containsKey("a"));
  }
}
