package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class DurationTextWeekTest {

  @Test
  void oneWeekIsSevenDays() {
    assertEquals(Duration.ofDays(7), DurationText.parse("1w"));
  }

  @Test
  void twoWeeks() {
    assertEquals(Duration.ofDays(14), DurationText.parse("2w"));
  }

  @Test
  void zeroWeeks() {
    assertEquals(Duration.ZERO, DurationText.parse("0w"));
  }

  @Test
  void leadingZeroWeeks() {
    assertEquals(Duration.ofDays(21), DurationText.parse("003w"));
  }

  @Test
  void weeksAndDays() {
    assertEquals(Duration.ofDays(10), DurationText.parse("1w3d"));
  }

  @Test
  void weeksHoursMinutes() {
    assertEquals(Duration.ofDays(7).plusMinutes(90), DurationText.parse("1w1h30m"));
  }

  @Test
  void weeksWithMoreThanAWeekOfDays() {
    assertEquals(Duration.ofDays(17), DurationText.parse("1w10d"));
  }

  @Test
  void weeksWithAllUnits() {
    assertEquals(
        Duration.ofDays(8).plusHours(2).plusMinutes(3).plusSeconds(4).plusMillis(5),
        DurationText.parse("1w1d2h3m4s5ms"));
  }

  @Test
  void weeksAndMillis() {
    assertEquals(Duration.ofDays(7).plusMillis(1), DurationText.parse("1w1ms"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"3d1w", "1w1w", "1W", "w", "1s1w", "1ms1w", "1ww", "1w ", "-1w"})
  void invalidWeekTextIsRejected(String text) {
    assertThrows(IllegalArgumentException.class, () -> DurationText.parse(text));
  }

  @Test
  void formatNeverWritesWeeks() {
    assertEquals("14d", DurationText.format(DurationText.parse("2w")));
    assertEquals("7d", DurationText.format(Duration.ofDays(7)));
    assertEquals("10d1h", DurationText.format(DurationText.parse("1w3d1h")));
    assertEquals("1ms", DurationText.format(Duration.ofMillis(1)));
    assertEquals("1d1h1m1s1ms", DurationText.format(Duration.parse("PT25H1M1.001S")));
  }

  @Test
  void tooManyWeeksIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> DurationText.parse("99999999999999999w"));
  }

  @Test
  void weeksPlusDaysOverflowIsRejected() {
    long maxWeeks = Long.MAX_VALUE / 604_800L;
    assertEquals(Duration.ofSeconds(maxWeeks * 604_800L), DurationText.parse(maxWeeks + "w"));
    assertThrows(
        IllegalArgumentException.class,
        () -> DurationText.parse((maxWeeks + 1) + "w"));
    assertThrows(
        IllegalArgumentException.class,
        () -> DurationText.parse(maxWeeks + "w" + "106751991167300d"));
  }
}
