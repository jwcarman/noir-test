package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class WordWrapTest {

  @Test
  void packsGreedily() {
    assertEquals(List.of("the quick", "brown fox"), WordWrap.wrap("the quick brown fox", 10));
    assertEquals(List.of("a b", "c d"), WordWrap.wrap("a b c d", 3));
    assertEquals(List.of("hello"), WordWrap.wrap("hello", 5));
  }

  @Test
  void fitsExactlyAtWidthBoundary() {
    assertEquals(List.of("ab cd"), WordWrap.wrap("ab cd", 5));
    assertEquals(List.of("ab", "cd"), WordWrap.wrap("ab cd", 4));
  }

  @Test
  void ignoresExtraSpaces() {
    assertEquals(List.of("the quick"), WordWrap.wrap("  the   quick  ", 20));
    assertEquals(List.of(), WordWrap.wrap("   ", 5));
    assertEquals(List.of(), WordWrap.wrap("", 5));
  }

  @Test
  void splitsLongWords() {
    assertEquals(List.of("abc", "def", "gh", "ij"), WordWrap.wrap("abcdefgh ij", 3));
    assertEquals(List.of("go", "abcd", "ef"), WordWrap.wrap("go abcdef", 4));
    assertEquals(List.of("abc", "def"), WordWrap.wrap("abcdef", 3));
    assertEquals(List.of("a", "b", "c"), WordWrap.wrap("abc", 1));
  }

  @Test
  void nextWordAfterLongWordStartsNewLineEvenIfItFits() {
    assertEquals(List.of("abcd", "e", "f g"), WordWrap.wrap("abcde f g", 4));
  }

  @Test
  void widthOneWorks() {
    assertEquals(List.of("a", "b"), WordWrap.wrap("a b", 1));
  }

  @Test
  void rejectsInvalidArguments() {
    assertThrows(IllegalArgumentException.class, () -> WordWrap.wrap("a", 0));
    assertThrows(IllegalArgumentException.class, () -> WordWrap.wrap("a", -1));
    assertThrows(NullPointerException.class, () -> WordWrap.wrap(null, 5));
  }
}
