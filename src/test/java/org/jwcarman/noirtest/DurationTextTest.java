package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DurationTextTest {

  @ParameterizedTest
  @CsvSource({
    "45s, 45000",
    "2d, 172800000",
    "3h, 10800000",
    "90m, 5400000",
    "1500ms, 1500",
    "0s, 0",
    "007s, 7000",
    "0ms, 0",
    "0d0h0m0s0ms, 0",
    "1h30m, 5400000",
    "2d1s, 172801000",
    "1ms, 1",
    "1m1ms, 60001",
    "1m1s, 61000",
    "1d1ms, 86400001",
    "1h1ms, 3600001",
    "1s1ms, 1001",
    "1d1h1m1s, 90061000",
    "10m, 600000",
    "9s, 9000"
  })
  void parsesValidText(String text, long millis) {
    assertEquals(Duration.ofMillis(millis), DurationText.parse(text));
  }

  @Test
  void parsesAllUnits() {
    assertEquals(
        Duration.ofDays(1).plusHours(2).plusMinutes(3).plusSeconds(4).plusMillis(5),
        DurationText.parse("1d2h3m4s5ms"));
  }

  @Test
  void parsesLargestDuration() {
    assertEquals(
        Duration.ofSeconds(Long.MAX_VALUE), DurationText.parse("106751991167300d15h30m7s"));
  }

  @Test
  void parsesLargeMillis() {
    assertEquals(
        Duration.ofMillis(Long.MAX_VALUE), DurationText.parse(Long.MAX_VALUE + "ms"));
  }

  @Test
  void parsesLargestWithMillis() {
    assertEquals(
        Duration.ofSeconds(Long.MAX_VALUE, 999_000_000),
        DurationText.parse("106751991167300d15h30m7s999ms"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "", "h", "10", "1h30", "1x", "1H", "1h 30m", " 1h", "1h ", "-1h", "+1h", "1.5h", "1MS",
        "1M", "1D", "1S", "ms", "1m1", "1hh", "1h1", "s", "1ms5", "1sm", "١s", "1µs",
        "30m1h", "1s1m", "1ms1s", "1h1h", "1m1m", "1ms1ms", "1s1s", "1d1d", "1s1h", "1h1d",
        "1ms1m", "1ms1d", "1m1h",
        "9999999999999999999d", "999999999999999d", "106751991167300d15h30m8s",
        "106751991167301d", "9999999999999999999ms", "9223372036854775807s1ms1",
        "106751991167300d15h30m7s1000ms", "9223372036854775807m"
      })
  void rejectsInvalidText(String text) {
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> DurationText.parse(text));
    assertTrue(e.getMessage().contains(text));
  }

  @Test
  void parseRejectsNull() {
    assertThrows(NullPointerException.class, () -> DurationText.parse(null));
  }

  @ParameterizedTest
  @CsvSource({
    "5400000, 1h30m",
    "1500, 1s500ms",
    "0, 0s",
    "176400000, 2d1h",
    "172801000, 2d1s",
    "1, 1ms",
    "86400000, 1d",
    "123456000, 1d10h17m36s",
    "93784005, 1d2h3m4s5ms",
    "3600000, 1h",
    "60000, 1m",
    "1000, 1s",
    "999, 999ms",
    "59999, 59s999ms",
    "3599999, 59m59s999ms",
    "86399999, 23h59m59s999ms",
    "86400001, 1d1ms"
  })
  void formatsCanonicalText(long millis, String expected) {
    assertEquals(expected, DurationText.format(Duration.ofMillis(millis)));
  }

  @Test
  void formatsLargestDuration() {
    assertEquals(
        "106751991167300d15h30m7s", DurationText.format(Duration.ofSeconds(Long.MAX_VALUE)));
    assertEquals(
        "106751991167300d15h30m7s999ms",
        DurationText.format(Duration.ofSeconds(Long.MAX_VALUE, 999_000_000)));
  }

  @Test
  void formatRejectsNegative() {
    assertThrows(IllegalArgumentException.class, () -> DurationText.format(Duration.ofSeconds(-1)));
    assertThrows(IllegalArgumentException.class, () -> DurationText.format(Duration.ofMillis(-1)));
    assertThrows(IllegalArgumentException.class, () -> DurationText.format(Duration.ofNanos(-1)));
  }

  @Test
  void formatRejectsFractionalMillis() {
    assertThrows(IllegalArgumentException.class, () -> DurationText.format(Duration.ofNanos(1)));
    assertThrows(
        IllegalArgumentException.class,
        () -> DurationText.format(Duration.ofMillis(1).plusNanos(1)));
    assertThrows(
        IllegalArgumentException.class, () -> DurationText.format(Duration.ofNanos(999_999)));
    assertThrows(
        IllegalArgumentException.class, () -> DurationText.format(Duration.ofNanos(1_000_001)));
  }

  @Test
  void formatRejectsNull() {
    assertThrows(NullPointerException.class, () -> DurationText.format(null));
  }

  @Test
  void roundTrips() {
    assertEquals("1d2h3m4s5ms", DurationText.format(DurationText.parse("1d2h3m4s5ms")));
    assertEquals("1h30m", DurationText.format(DurationText.parse("90m")));
    assertEquals("0s", DurationText.format(DurationText.parse("000ms")));
    assertEquals(
        Duration.ofMillis(93784005),
        DurationText.parse(DurationText.format(Duration.ofMillis(93784005))));
  }

  @Test
  void roundTripsManyValues() {
    for (long ms = 0; ms < 200_000_000L; ms += 9_876_543L) {
      Duration d = Duration.ofMillis(ms);
      assertEquals(d, DurationText.parse(DurationText.format(d)));
    }
  }
}
