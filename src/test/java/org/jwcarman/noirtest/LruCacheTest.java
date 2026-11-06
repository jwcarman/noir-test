package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LruCacheTest {

  private final List<String> events = new ArrayList<>();

  private LruCache<String, Integer> recording(int capacity) {
    return new LruCache<>(capacity, (k, v) -> events.add(k + "=" + v));
  }

  private static LruCache<String, Integer> abFull() {
    LruCache<String, Integer> c = new LruCache<>(2);
    c.put("a", 1);
    c.put("b", 2);
    return c;
  }

  private static LruCache<String, Integer> abcFull() {
    LruCache<String, Integer> c = new LruCache<>(3);
    c.put("a", 1);
    c.put("b", 2);
    c.put("c", 3);
    return c;
  }

  // R1
  @Test
  void rejectsZeroCapacity() {
    assertThrows(IllegalArgumentException.class, () -> new LruCache<String, Integer>(0));
  }

  @Test
  void rejectsNegativeCapacity() {
    assertThrows(IllegalArgumentException.class, () -> new LruCache<String, Integer>(-1));
  }

  @Test
  void rejectsZeroCapacityWithListener() {
    assertThrows(
        IllegalArgumentException.class, () -> new LruCache<String, Integer>(0, (k, v) -> {}));
  }

  @Test
  void rejectsNegativeCapacityWithListener() {
    assertThrows(
        IllegalArgumentException.class, () -> new LruCache<String, Integer>(-5, (k, v) -> {}));
  }

  @Test
  void capacityOneAllowed() {
    assertEquals(0, new LruCache<String, Integer>(1).size());
    assertEquals(0, new LruCache<String, Integer>(1, (k, v) -> {}).size());
  }

  // R2
  @Test
  void rejectsNullListener() {
    assertThrows(NullPointerException.class, () -> new LruCache<String, Integer>(2, null));
  }

  @Test
  void rejectsNullListenerEvenWithBadCapacity() {
    assertThrows(RuntimeException.class, () -> new LruCache<String, Integer>(0, null));
  }

  // R3
  @Test
  void putNewKey() {
    LruCache<String, Integer> c = new LruCache<>(2);
    assertNull(c.put("a", 1));
    assertEquals(1, c.get("a"));
    assertTrue(c.containsKey("a"));
    assertEquals(1, c.size());
  }

  @Test
  void missingKeyIsAbsent() {
    LruCache<String, Integer> c = new LruCache<>(2);
    c.put("a", 1);
    assertNull(c.get("missing"));
    assertFalse(c.containsKey("missing"));
    assertEquals(1, c.size());
  }

  @Test
  void fillsUpToCapacityWithoutEviction() {
    LruCache<String, Integer> c = recording(3);
    c.put("a", 1);
    c.put("b", 2);
    c.put("c", 3);
    assertEquals(3, c.size());
    assertTrue(c.containsKey("a"));
    assertTrue(events.isEmpty());
  }

  // R4
  @Test
  void replaceReturnsOld() {
    LruCache<String, Integer> c = new LruCache<>(2);
    c.put("a", 1);
    assertEquals(1, c.put("a", 2));
    assertEquals(2, c.get("a"));
    assertEquals(1, c.size());
  }

  @Test
  void replaceInFullCacheDoesNotEvict() {
    LruCache<String, Integer> c = abFull();
    assertEquals(2, c.put("b", 20));
    assertEquals(2, c.size());
    assertEquals(1, c.get("a"));
    assertEquals(20, c.get("b"));
  }

  // R5
  @Test
  void removePresent() {
    LruCache<String, Integer> c = new LruCache<>(2);
    c.put("a", 1);
    assertEquals(1, c.remove("a"));
    assertFalse(c.containsKey("a"));
    assertNull(c.get("a"));
    assertEquals(0, c.size());
  }

  @Test
  void removeMissing() {
    LruCache<String, Integer> c = new LruCache<>(2);
    c.put("a", 1);
    assertNull(c.remove("missing"));
    assertEquals(1, c.size());
  }

  // R6
  @Test
  void evictsLru() {
    LruCache<String, Integer> c = abFull();
    assertNull(c.put("c", 3));
    assertFalse(c.containsKey("a"));
    assertTrue(c.containsKey("b"));
    assertTrue(c.containsKey("c"));
    assertEquals(2, c.size());
  }

  @Test
  void capacityOneEvicts() {
    LruCache<String, Integer> c = new LruCache<>(1);
    c.put("a", 1);
    assertNull(c.put("b", 2));
    assertFalse(c.containsKey("a"));
    assertEquals(2, c.get("b"));
    assertEquals(1, c.size());
  }

  @Test
  void multipleEvictions() {
    LruCache<String, Integer> c = abcFull();
    c.put("d", 4);
    c.put("e", 5);
    assertFalse(c.containsKey("a"));
    assertFalse(c.containsKey("b"));
    assertTrue(c.containsKey("c"));
    assertTrue(c.containsKey("d"));
    assertTrue(c.containsKey("e"));
    assertEquals(3, c.size());
  }

  // R7
  @Test
  void getCountsAsUse() {
    LruCache<String, Integer> c = abFull();
    assertEquals(1, c.get("a"));
    c.put("c", 3);
    assertFalse(c.containsKey("b"));
    assertTrue(c.containsKey("a"));
    assertTrue(c.containsKey("c"));
  }

  // R8
  @Test
  void replaceCountsAsUse() {
    LruCache<String, Integer> c = abFull();
    c.put("a", 10);
    c.put("c", 3);
    assertFalse(c.containsKey("b"));
    assertEquals(10, c.get("a"));
    assertTrue(c.containsKey("c"));
  }

  // R9
  @Test
  void containsKeyIsNotUse() {
    LruCache<String, Integer> c = abFull();
    assertTrue(c.containsKey("a"));
    c.put("c", 3);
    assertFalse(c.containsKey("a"));
    assertTrue(c.containsKey("b"));
  }

  @Test
  void getMissingIsNotUse() {
    LruCache<String, Integer> c = abFull();
    c.get("missing");
    c.put("c", 3);
    assertFalse(c.containsKey("a"));
    assertTrue(c.containsKey("b"));
  }

  @Test
  void sizeIsNotUse() {
    LruCache<String, Integer> c = abFull();
    assertEquals(2, c.size());
    c.put("c", 3);
    assertFalse(c.containsKey("a"));
    assertTrue(c.containsKey("b"));
  }

  @Test
  void removeKeepsOrder() {
    LruCache<String, Integer> c = abcFull();
    c.remove("b");
    c.put("d", 4);
    c.put("e", 5);
    assertFalse(c.containsKey("a"));
    assertTrue(c.containsKey("c"));
    assertTrue(c.containsKey("d"));
    assertTrue(c.containsKey("e"));
  }

  @Test
  void removeFreesSlot() {
    LruCache<String, Integer> c = abcFull();
    c.remove("b");
    c.put("d", 4);
    assertEquals(3, c.size());
    assertTrue(c.containsKey("a"));
  }

  // R10
  @Test
  void listenerToldOfEvictedEntry() {
    LruCache<String, Integer> c = recording(2);
    c.put("a", 1);
    c.put("b", 2);
    c.get("a");
    c.put("c", 3);
    assertEquals(List.of("b=2"), events);
  }

  @Test
  void listenerCalledPerEviction() {
    LruCache<String, Integer> c = recording(2);
    c.put("a", 1);
    c.put("b", 2);
    c.put("c", 3);
    c.put("d", 4);
    assertEquals(List.of("a=1", "b=2"), events);
  }

  @Test
  void listenerNotCalledForReplaceOrRemove() {
    LruCache<String, Integer> c = recording(2);
    c.put("a", 1);
    c.put("b", 2);
    c.put("a", 10);
    c.remove("b");
    assertTrue(events.isEmpty());
  }

  @Test
  void listenerNotCalledWhenNotFull() {
    LruCache<String, Integer> c = recording(2);
    c.put("a", 1);
    c.put("b", 2);
    assertTrue(events.isEmpty());
  }

  @Test
  void listenerSeesCacheAlreadyUpdated() {
    List<String> seen = new ArrayList<>();
    LruCache<String, Integer>[] holder = new LruCache[1];
    holder[0] =
        new LruCache<>(
            1,
            (k, v) ->
                seen.add(
                    holder[0].size()
                        + ":"
                        + holder[0].containsKey("a")
                        + ":"
                        + holder[0].containsKey("b")));
    holder[0].put("a", 1);
    holder[0].put("b", 2);
    assertEquals(List.of("1:false:true"), seen);
  }

  // R11
  @Test
  void listenerExceptionPropagatesAfterUpdate() {
    LruCache<String, Integer> c =
        new LruCache<>(
            1,
            (k, v) -> {
              throw new IllegalStateException("boom");
            });
    c.put("a", 1);
    IllegalStateException e = assertThrows(IllegalStateException.class, () -> c.put("b", 2));
    assertEquals("boom", e.getMessage());
    assertFalse(c.containsKey("a"));
    assertEquals(2, c.get("b"));
    assertEquals(1, c.size());
  }

  // R12
  @Test
  void nullKeyPutRejected() {
    LruCache<String, Integer> c = new LruCache<>(2);
    c.put("a", 1);
    assertThrows(NullPointerException.class, () -> c.put(null, 2));
    assertEquals(1, c.size());
    assertEquals(1, c.get("a"));
  }

  @Test
  void nullKeyAndValuePutRejected() {
    LruCache<String, Integer> c = new LruCache<>(2);
    assertThrows(NullPointerException.class, () -> c.put(null, null));
    assertEquals(0, c.size());
  }

  @Test
  void nullValuePutRejected() {
    LruCache<String, Integer> c = new LruCache<>(2);
    c.put("a", 1);
    assertThrows(NullPointerException.class, () -> c.put("a", null));
    assertEquals(1, c.get("a"));
    assertEquals(1, c.size());
  }

  @Test
  void nullNewKeyValueRejected() {
    LruCache<String, Integer> c = new LruCache<>(2);
    assertThrows(NullPointerException.class, () -> c.put("x", null));
    assertFalse(c.containsKey("x"));
    assertEquals(0, c.size());
  }

  @Test
  void nullArgumentsRejected() {
    LruCache<String, Integer> c = new LruCache<>(2);
    assertThrows(NullPointerException.class, () -> c.get(null));
    assertThrows(NullPointerException.class, () -> c.remove(null));
    assertThrows(NullPointerException.class, () -> c.containsKey(null));
  }

  @Test
  void rejectedPutDoesNotChangeRecency() {
    LruCache<String, Integer> c = abFull();
    assertThrows(NullPointerException.class, () -> c.put("a", null));
    c.put("c", 3);
    assertFalse(c.containsKey("a"));
    assertTrue(c.containsKey("b"));
  }

  @Test
  void rejectedPutDoesNotEvict() {
    LruCache<String, Integer> c = recording(1);
    c.put("a", 1);
    assertThrows(NullPointerException.class, () -> c.put("b", null));
    assertTrue(events.isEmpty());
    assertEquals(1, c.get("a"));
    assertEquals(1, c.size());
  }
}
