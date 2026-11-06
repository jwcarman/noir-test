package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class Base62Test {

  @ParameterizedTest
  @CsvSource({
    "0, 0",
    "1, 1",
    "9, 9",
    "10, A",
    "35, Z",
    "36, a",
    "61, z",
    "62, 10",
    "3843, zz",
    "3844, 100",
    "9223372036854775807, AzL8n0Y58m7"
  })
  void encodesAndDecodes(long value, String text) {
    assertEquals(text, Base62.encode(value));
    assertEquals(value, Base62.decode(text));
  }

  @ParameterizedTest
  @ValueSource(longs = {0, 1, 61, 62, 123456789L, Long.MAX_VALUE})
  void roundTrips(long value) {
    assertEquals(value, Base62.decode(Base62.encode(value)));
  }

  @ParameterizedTest
  @ValueSource(longs = {-1, Long.MIN_VALUE})
  void encodeRejectsNegative(long value) {
    assertThrows(IllegalArgumentException.class, () -> Base62.encode(value));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "", "a-b", "-", " ", "AzL8n0Y58m8", "AzL8n0Y58mz", "B00000000000",
        "zzzzzzzzzzz", "100000000000", "é"
      })
  void decodeRejectsInvalid(String text) {
    assertThrows(IllegalArgumentException.class, () -> Base62.decode(text));
  }

  @Test
  void decodeRejectsNull() {
    assertThrows(NullPointerException.class, () -> Base62.decode(null));
  }

  @Test
  void decodesSingleZero() {
    assertEquals(0L, Base62.decode("0"));
  }

  @Test
  void decodesJustBelowMax() {
    assertEquals(Long.MAX_VALUE - 1, Base62.decode("AzL8n0Y58m6"));
  }
}
