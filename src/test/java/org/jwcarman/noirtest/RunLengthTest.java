package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RunLengthTest {

  @Test
  void encodesRuns() {
    assertEquals("3a1b", RunLength.encode("aaab"));
    assertEquals("1a1b1c", RunLength.encode("abc"));
    assertEquals("", RunLength.encode(""));
    assertEquals("12x", RunLength.encode("x".repeat(12)));
    assertEquals("1a2b1a", RunLength.encode("abba"));
    assertEquals("1/1:", RunLength.encode("/:"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"a1", "0", "9", "ab5c"})
  void encodeRejectsDigits(String text) {
    assertThrows(IllegalArgumentException.class, () -> RunLength.encode(text));
  }

  @Test
  void encodeRejectsNull() {
    assertThrows(NullPointerException.class, () -> RunLength.encode(null));
  }

  @Test
  void decodesPairs() {
    assertEquals("aaab", RunLength.decode("3a1b"));
    assertEquals("x".repeat(12), RunLength.decode("12x"));
    assertEquals("", RunLength.decode(""));
    assertEquals("a".repeat(100), RunLength.decode("100a"));
    assertEquals("  ", RunLength.decode("2 "));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "a", "aabbbc", "zz zz", "x/y::"})
  void roundTrips(String text) {
    assertEquals(text, RunLength.decode(RunLength.encode(text)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"a", "3", "03a", "0a", "3a1", "3ab", "1a0b", "99999999999a", "3a03b"})
  void decodeRejectsMalformed(String encoded) {
    assertThrows(IllegalArgumentException.class, () -> RunLength.decode(encoded));
  }

  @Test
  void decodeRejectsNull() {
    assertThrows(NullPointerException.class, () -> RunLength.decode(null));
  }

  @Test
  void listsRuns() {
    assertEquals(List.of(Map.entry('a', 3), Map.entry('b', 1)), RunLength.runs("aaab"));
    assertEquals(List.of(), RunLength.runs(""));
    assertEquals(
        List.of(Map.entry('a', 1), Map.entry('b', 2), Map.entry('a', 1)), RunLength.runs("abba"));
  }

  @Test
  void runsRejectsNull() {
    assertThrows(NullPointerException.class, () -> RunLength.runs(null));
  }
}
