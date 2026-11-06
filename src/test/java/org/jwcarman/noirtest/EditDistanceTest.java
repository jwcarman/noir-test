package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class EditDistanceTest {

  @ParameterizedTest(name = "\"{0}\" -> \"{1}\" = {2}")
  @CsvSource(
      value = {
        "kitten|sitting|3",
        "flaw|lawn|2",
        "intention|execution|5",
        "''|''|0",
        "''|abc|3",
        "abc|''|3",
        "same|same|0",
        "cat|cut|1",
        "cat|cats|1",
        "cat|at|1",
        "Cat|cat|1",
        "sitting|kitten|3",
        "ab|ba|2",
        "a|b|1",
        "a|a|0",
        "abc|xyz|3",
        "abcdef|azced|3",
        "sunday|saturday|3",
        "saturday|sunday|3",
        "book|back|2",
        "a|abcde|4",
        "abcde|e|4",
        "xabcx|abc|2"
      },
      delimiter = '|')
  void computesDistance(String a, String b, int expected) {
    assertEquals(expected, EditDistance.levenshtein(a, b));
    assertEquals(expected, EditDistance.levenshtein(b, a));
  }

  @Test
  void nullFirstArgumentIsRejected() {
    assertThrows(NullPointerException.class, () -> EditDistance.levenshtein(null, "a"));
  }

  @Test
  void nullSecondArgumentIsRejected() {
    assertThrows(NullPointerException.class, () -> EditDistance.levenshtein("a", null));
  }

  @Test
  void bothNullIsRejected() {
    assertThrows(NullPointerException.class, () -> EditDistance.levenshtein(null, null));
  }
}
