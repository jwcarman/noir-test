package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RingBufferTest {

  @Test
  void pollReturnsOldestFirst() {
    var b = new RingBuffer<String>(3);
    assertEquals(Optional.empty(), b.add("a"));
    assertEquals(Optional.empty(), b.add("b"));
    assertEquals(Optional.of("a"), b.poll());
    assertEquals(1, b.size());
    assertEquals(Optional.of("b"), b.poll());
    assertEquals(0, b.size());
    assertEquals(Optional.empty(), b.poll());
    assertEquals(0, b.size());
    assertEquals(List.of(), b.toList());
  }

  @Test
  void addToFullBufferEvictsOldest() {
    var b = new RingBuffer<String>(2);
    assertEquals(Optional.empty(), b.add("a"));
    assertEquals(Optional.empty(), b.add("b"));
    assertEquals(Optional.of("a"), b.add("c"));
    assertEquals(List.of("b", "c"), b.toList());
    assertEquals(2, b.size());
    assertEquals(Optional.of("b"), b.peek());
  }

  @Test
  void addToNonFullBufferReturnsEmpty() {
    var b = new RingBuffer<String>(2);
    assertEquals(Optional.empty(), b.add("a"));
    assertEquals(1, b.size());
  }

  @Test
  void keepsLastElementsAfterManyAdds() {
    var b = new RingBuffer<Integer>(3);
    for (int i = 1; i <= 10; i++) {
      Optional<Integer> evicted = b.add(i);
      assertEquals(i > 3 ? Optional.of(i - 3) : Optional.empty(), evicted);
    }
    assertEquals(List.of(8, 9, 10), b.toList());
    assertEquals(3, b.size());
  }

  @Test
  void peekDoesNotRemove() {
    var b = new RingBuffer<String>(3);
    b.add("a");
    b.add("b");
    assertEquals(Optional.of("a"), b.peek());
    assertEquals(Optional.of("a"), b.peek());
    assertEquals(2, b.size());
    assertEquals(List.of("a", "b"), b.toList());
  }

  @Test
  void emptyBufferReports() {
    var b = new RingBuffer<String>(3);
    assertEquals(Optional.empty(), b.peek());
    assertEquals(0, b.size());
    assertEquals(3, b.capacity());
  }

  @Test
  void capacityOfOne() {
    var b = new RingBuffer<String>(1);
    assertEquals(1, b.capacity());
    assertEquals(Optional.empty(), b.add("a"));
    assertEquals(Optional.of("a"), b.add("b"));
    assertEquals(List.of("b"), b.toList());
    assertEquals(Optional.of("b"), b.poll());
    assertEquals(Optional.empty(), b.peek());
  }

  @Test
  void wrapsAroundManyTimes() {
    var b = new RingBuffer<Integer>(2);
    for (int i = 0; i < 100; i++) {
      b.add(i);
      if (i % 2 == 1) {
        assertEquals(Optional.of(i - 1), b.poll());
      }
    }
    assertEquals(List.of(99), b.toList());
    assertEquals(1, b.size());
  }

  @Test
  void interleavedPollAndAddAfterWrap() {
    var b = new RingBuffer<Integer>(3);
    b.add(1);
    b.add(2);
    b.add(3);
    assertEquals(Optional.of(1), b.poll());
    b.add(4);
    assertEquals(List.of(2, 3, 4), b.toList());
    assertEquals(Optional.of(2), b.add(5));
    assertEquals(List.of(3, 4, 5), b.toList());
    assertEquals(Optional.of(3), b.poll());
    assertEquals(Optional.of(4), b.poll());
    assertEquals(Optional.of(5), b.peek());
    assertEquals(List.of(5), b.toList());
  }

  @Test
  void toListIsUnmodifiableSnapshot() {
    var b = new RingBuffer<String>(2);
    b.add("a");
    var l = b.toList();
    b.add("b");
    assertEquals(List.of("a"), l);
    assertThrows(UnsupportedOperationException.class, () -> l.add("x"));
  }

  @Test
  void rejectsInvalidCapacity() {
    assertThrows(IllegalArgumentException.class, () -> new RingBuffer<String>(0));
    assertThrows(IllegalArgumentException.class, () -> new RingBuffer<String>(-1));
  }

  @Test
  void rejectsNullElement() {
    var b = new RingBuffer<String>(2);
    assertThrows(NullPointerException.class, () -> b.add(null));
    assertEquals(0, b.size());
    b.add("a");
    b.add("b");
    assertThrows(NullPointerException.class, () -> b.add(null));
    assertEquals(List.of("a", "b"), b.toList());
  }
}
