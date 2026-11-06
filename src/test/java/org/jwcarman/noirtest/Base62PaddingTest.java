package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class Base62PaddingTest {

  @ParameterizedTest
  @CsvSource({
    "62, 5, 00010",
    "0, 3, 000",
    "0, 1, 0",
    "1, 1, 1",
    "61, 2, 0z",
    "62, 2, 10",
    "62, 1, 10",
    "3844, 3, 100",
    "3844, 4, 0100",
    "9223372036854775807, 4, AzL8n0Y58m7",
    "9223372036854775807, 11, AzL8n0Y58m7",
    "9223372036854775807, 12, 0AzL8n0Y58m7"
  })
  void encodesWithPadding(long value, int minLength, String text) {
    assertEquals(text, Base62.encode(value, minLength));
    assertEquals(value, Base62.decode(text));
  }

  @Test
  void encodeRejectsNegativeValue() {
    assertThrows(IllegalArgumentException.class, () -> Base62.encode(-1, 3));
    assertThrows(IllegalArgumentException.class, () -> Base62.encode(Long.MIN_VALUE, 3));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
  void encodeRejectsMinLengthBelowOne(int minLength) {
    assertThrows(IllegalArgumentException.class, () -> Base62.encode(5, minLength));
  }

  @ParameterizedTest
  @CsvSource({
    "00010, 62",
    "000, 0",
    "00, 0",
    "01, 1",
    "0z, 61",
    "0AzL8n0Y58m7, 9223372036854775807",
    "000000000000000000000AzL8n0Y58m7, 9223372036854775807"
  })
  void decodesLeadingZeros(String text, long value) {
    assertEquals(value, Base62.decode(text));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0AzL8n0Y58m8", "0100000000000", "00-", "0 "})
  void decodeRejectsInvalidWithLeadingZeros(String text) {
    assertThrows(IllegalArgumentException.class, () -> Base62.decode(text));
  }

  @Test
  void paddedRoundTripPreservesWidth() {
    for (long v : new long[] {0, 1, 61, 62, 123456789L}) {
      String s = Base62.encode(v, 8);
      assertEquals(8, s.length());
      assertEquals(v, Base62.decode(s));
    }
  }
}
