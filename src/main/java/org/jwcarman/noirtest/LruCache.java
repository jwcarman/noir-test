package org.jwcarman.noirtest;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A cache holding at most a fixed number of entries that evicts the least recently used entry
 * when a new key is added to a full cache.
 *
 * <p><b>Recency.</b> A successful {@link #get} of a present key and every successful {@link #put}
 * make that key the most recently used. A {@link #get} of a missing key, {@link #remove}, {@link
 * #containsKey} and {@link #size} never change the recency order of the remaining entries.
 *
 * <p><b>Eviction.</b> When {@link #put} is called with a key that is not present and {@link
 * #size()} equals the capacity, the least recently used entry is removed and the new entry is
 * stored, so {@link #size()} stays equal to the capacity. A {@link #put} of a key that is already
 * present replaces its value and never evicts.
 *
 * <p><b>Eviction listener.</b> If an eviction listener was supplied, it is called synchronously,
 * exactly once per evicted entry, with the evicted key and value, during the {@link #put} that
 * caused the eviction, after the evicted entry has been removed and the new entry has been stored.
 * If the listener throws, the exception propagates from {@link #put} and the cache remains
 * updated: the new entry is present and the evicted entry is gone. The listener is never called
 * for {@link #remove}, for a {@link #put} that replaces an existing value, or for any other
 * operation. The behavior of calls back into this cache from inside the listener is unspecified.
 *
 * <p><b>Nulls.</b> {@code null} keys and values are rejected with {@link NullPointerException}; a
 * call that throws {@link NullPointerException} leaves the cache unchanged, including its recency
 * order.
 *
 * <p>Instances are not thread-safe.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public final class LruCache<K, V> {

  private final int capacity;
  private final BiConsumer<? super K, ? super V> evictionListener;
  private final LinkedHashMap<K, V> entries;

  /**
   * Creates an empty cache that holds at most {@code capacity} entries and has no eviction
   * listener.
   *
   * @param capacity the maximum number of entries, at least 1
   * @throws IllegalArgumentException if {@code capacity} is less than 1
   */
  public LruCache(int capacity) {
    this(capacity, (k, v) -> {});
  }

  /**
   * Creates an empty cache that holds at most {@code capacity} entries and calls {@code
   * evictionListener.accept(key, value)} for each entry it evicts.
   *
   * @param capacity the maximum number of entries, at least 1
   * @param evictionListener the listener told about each evicted entry
   * @throws IllegalArgumentException if {@code capacity} is less than 1
   * @throws NullPointerException if {@code evictionListener} is {@code null}
   */
  public LruCache(int capacity, BiConsumer<? super K, ? super V> evictionListener) {
    if (capacity < 1) {
      throw new IllegalArgumentException("capacity must be at least 1, was " + capacity);
    }
    this.capacity = capacity;
    this.evictionListener =
        Objects.requireNonNull(evictionListener, "evictionListener must not be null");
    this.entries = new LinkedHashMap<>(16, 0.75f, true);
  }

  /**
   * Stores {@code value} under {@code key} and makes {@code key} the most recently used key. If
   * {@code key} is not present and the cache is full, first evicts the least recently used entry.
   *
   * @param key the key
   * @param value the value
   * @return the value previously stored under {@code key}, or {@code null} if {@code key} was not
   *     present
   * @throws NullPointerException if {@code key} or {@code value} is {@code null}
   */
  public V put(K key, V value) {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(value, "value must not be null");
    if (entries.containsKey(key) || entries.size() < capacity) {
      return entries.put(key, value);
    }
    Iterator<Map.Entry<K, V>> it = entries.entrySet().iterator();
    Map.Entry<K, V> eldest = it.next();
    K evictedKey = eldest.getKey();
    V evictedValue = eldest.getValue();
    it.remove();
    entries.put(key, value);
    evictionListener.accept(evictedKey, evictedValue);
    return null;
  }

  /**
   * Returns the value stored under {@code key}, and if it is present makes {@code key} the most
   * recently used key.
   *
   * @param key the key
   * @return the value stored under {@code key}, or {@code null} if {@code key} is not present
   * @throws NullPointerException if {@code key} is {@code null}
   */
  public V get(K key) {
    return entries.get(Objects.requireNonNull(key, "key must not be null"));
  }

  /**
   * Removes the entry for {@code key} if present, without calling the eviction listener and
   * without changing the recency order of the remaining entries.
   *
   * @param key the key
   * @return the value that was stored under {@code key}, or {@code null} if {@code key} was not
   *     present
   * @throws NullPointerException if {@code key} is {@code null}
   */
  public V remove(K key) {
    return entries.remove(Objects.requireNonNull(key, "key must not be null"));
  }

  /**
   * Returns the number of entries in the cache, from 0 to the capacity inclusive.
   *
   * @return the number of entries
   */
  public int size() {
    return entries.size();
  }

  /**
   * Returns whether {@code key} is present, without changing the recency order.
   *
   * @param key the key
   * @return {@code true} if an entry for {@code key} is present
   * @throws NullPointerException if {@code key} is {@code null}
   */
  public boolean containsKey(K key) {
    return entries.containsKey(Objects.requireNonNull(key, "key must not be null"));
  }
}
