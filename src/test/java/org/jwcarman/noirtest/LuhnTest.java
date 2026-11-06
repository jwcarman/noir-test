package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class LuhnTest {

  @ParameterizedTest
  @ValueSource(strings = {"79927398713", "4539578763621486", "0", "18", "00", "59", "91", "26"})
  void isValidAcceptsPassingStrings(String s) {
    assertTrue(Luhn.isValid(s));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"79927398710", "4539578763621487", "79927398731", "1", "9", "10", "19", "81"})
  void isValidRejectsFailingStrings(String s) {
    assertFalse(Luhn.isValid(s));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"", "7992 7398 713", "-18", "+18", "1a8", "18 ", "/", ":", "١٨", "0x"})
  void isValidRejectsNonDigitStrings(String s) {
    assertFalse(Luhn.isValid(s));
  }

  @Test
  void isValidRejectsNull() {
    assertThrows(NullPointerException.class, () -> Luhn.isValid(null));
  }

  @ParameterizedTest
  @CsvSource({
    "7992739871, 3",
    "453957876362148, 6",
    "0, 0",
    "1, 8",
    "5, 9",
    "9, 1",
    "18, 2",
    "00, 0"
  })
  void checkDigitComputesDigit(String payload, int expected) {
    assertEquals(expected, Luhn.checkDigit(payload));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "12a", " 1", "-1", "/", ":"})
  void checkDigitRejectsNonDigitStrings(String s) {
    assertThrows(IllegalArgumentException.class, () -> Luhn.checkDigit(s));
  }

  @Test
  void checkDigitRejectsNull() {
    assertThrows(NullPointerException.class, () -> Luhn.checkDigit(null));
  }

  @Test
  void checkDigitAlwaysProducesValidNumber() {
    String[] payloads = {"0", "1", "12345", "999999999", "7992739871", "100000000000000000001"};
    for (String p : payloads) {
      int d = Luhn.checkDigit(p);
      assertTrue(Luhn.isValid(p + d), p);
      for (int other = 0; other < 10; other++) {
        if (other != d) {
          assertFalse(Luhn.isValid(p + other), p + other);
        }
      }
    }
  }

  @Test
  void handlesStringsLongerThanALong() {
    String payload = "1234567890".repeat(50);
    int d = Luhn.checkDigit(payload);
    assertTrue(Luhn.isValid(payload + d));
    assertFalse(Luhn.isValid(payload + ((d + 1) % 10)));
  }
}
