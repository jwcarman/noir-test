package org.jwcarman.noirtest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A first-in, first-out buffer holding at most {@code capacity} non-null elements. Adding to a
 * full buffer removes the oldest element to make room.
 *
 * <p>Instances are not thread-safe.
 *
 * @param <E> the type of elements
 */
public final class RingBuffer<E> {

  private final Object[] elements;
  private int head;
  private int size;

  /**
   * Creates an empty buffer.
   *
   * @throws IllegalArgumentException if {@code capacity} is less than 1
   */
  public RingBuffer(int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException("capacity must be at least 1: " + capacity);
    }
    this.elements = new Object[capacity];
  }

  /**
   * Adds {@code element} as the newest element.
   *
   * @return the oldest element if it was removed to make room, otherwise empty
   * @throws NullPointerException if {@code element} is {@code null}
   */
  public Optional<E> add(E element) {
    Objects.requireNonNull(element, "element");
    Optional<E> removed = size == elements.length ? poll() : Optional.empty();
    elements[index(size)] = element;
    size++;
    return removed;
  }

  /** Removes and returns the oldest element, or returns empty if the buffer is empty. */
  public Optional<E> poll() {
    Optional<E> oldest = peek();
    if (size > 0) {
      elements[head] = null;
      head = index(1);
      size--;
    }
    return oldest;
  }

  /** Returns the oldest element without removing it, or empty if the buffer is empty. */
  @SuppressWarnings("unchecked")
  public Optional<E> peek() {
    return Optional.ofNullable((E) elements[head]);
  }

  /** Returns the number of elements, from 0 to the capacity. */
  public int size() {
    return size;
  }

  /** Returns the capacity given to the constructor. */
  public int capacity() {
    return elements.length;
  }

  /** Returns the elements from oldest to newest, as an unmodifiable snapshot. */
  @SuppressWarnings("unchecked")
  public List<E> toList() {
    List<E> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add((E) elements[index(i)]);
    }
    return Collections.unmodifiableList(list);
  }

  private int index(int offset) {
    return (head + offset) % elements.length;
  }
}
