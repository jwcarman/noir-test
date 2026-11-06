package org.jwcarman.noirtest;

import java.time.Duration;
import java.util.Objects;

/**
 * Converts between {@link Duration} and compact text such as {@code "1h30m"}.
 *
 * <p><b>Text format.</b> The text is one or more components with nothing else around or between
 * them: no whitespace, sign or decimal point. A component is one or more ASCII digits followed by a
 * unit. The units, from largest to smallest, are {@code w} (7 days), {@code d} (24 hours), {@code
 * h} (hours), {@code m} (minutes), {@code s} (seconds) and {@code ms} (milliseconds). Units are
 * lowercase, each unit
 * appears at most once, and units appear in strictly decreasing size. A component's number may be
 * larger than one of the next unit up (for example {@code "90m"}) and may have leading zeros (for
 * example {@code "007s"}). The text {@code "1ms"} is one millisecond, never one minute followed by
 * an {@code s}.
 *
 * <p><b>Canonical text.</b> {@link #format} writes the components from the largest unit to the
 * smallest, each with the largest whole count that fits after the larger units are taken out,
 * leaves out every zero component, writes no leading zeros, and writes {@code "0s"} for a zero
 * duration. It never writes weeks: the largest unit it writes is {@code d}.
 */
public final class DurationText {

  /** Unit suffixes, largest first; the index is the unit's rank. */
  private static final String[] UNITS = {"w", "d", "h", "m", "s", "ms"};

  /** Seconds per unit for w, d, h, m, s (milliseconds handled separately). */
  private static final long[] SECONDS = {604_800L, 86_400L, 3_600L, 60L, 1L};

  private static final int MILLIS = 5;

  /** Rank of the largest unit {@link #format} writes (days; weeks are never written). */
  private static final int FORMAT_FIRST = 1;

  private DurationText() {}

  /**
   * Parses {@code text} in the text format described on this class, including the week unit
   * {@code w} (7 days).
   *
   * @param text the text to parse
   * @return the duration the text denotes, the sum of its components
   * @throws NullPointerException if {@code text} is {@code null}
   * @throws IllegalArgumentException if {@code text} is not in the text format, or if a component
   *     or the total is too large to be represented as a {@link Duration}
   */
  public static Duration parse(String text) {
    Objects.requireNonNull(text, "text");
    if (text.isEmpty()) {
      throw invalid(text, "empty text");
    }
    Duration total = Duration.ZERO;
    int pos = 0;
    int lastRank = -1;
    while (pos < text.length()) {
      int digitsEnd = skipDigits(text, pos);
      if (digitsEnd == pos) {
        throw invalid(text, "expected digits at index " + pos);
      }
      int rank = unitAt(text, digitsEnd);
      if (rank <= lastRank) {
        throw invalid(text, "unit '" + UNITS[rank] + "' is repeated or out of order");
      }
      long amount = parseAmount(text, pos, digitsEnd);
      total = add(text, total, amount, rank);
      lastRank = rank;
      pos = digitsEnd + UNITS[rank].length();
    }
    return total;
  }

  /**
   * Returns the canonical text for {@code duration}, as described on this class.
   *
   * @param duration the duration to format
   * @return the canonical text, never empty
   * @throws NullPointerException if {@code duration} is {@code null}
   * @throws IllegalArgumentException if {@code duration} is negative or is not a whole number of
   *     milliseconds
   */
  public static String format(Duration duration) {
    Objects.requireNonNull(duration, "duration");
    if (duration.isNegative()) {
      throw new IllegalArgumentException("Negative duration: " + duration);
    }
    if (duration.getNano() % 1_000_000 != 0) {
      throw new IllegalArgumentException("Not a whole number of milliseconds: " + duration);
    }
    if (duration.isZero()) {
      return "0s";
    }
    StringBuilder sb = new StringBuilder();
    long remaining = duration.getSeconds();
    for (int rank = FORMAT_FIRST; rank < SECONDS.length; rank++) {
      append(sb, remaining / SECONDS[rank], rank);
      remaining %= SECONDS[rank];
    }
    append(sb, duration.getNano() / 1_000_000, MILLIS);
    return sb.toString();
  }

  private static void append(StringBuilder sb, long count, int rank) {
    if (count != 0) {
      sb.append(count).append(UNITS[rank]);
    }
  }

  private static int skipDigits(String text, int from) {
    int pos = from;
    while (pos < text.length() && isDigit(text.charAt(pos))) {
      pos++;
    }
    return pos;
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static int unitAt(String text, int pos) {
    if (pos >= text.length()) {
      throw invalid(text, "missing unit at end");
    }
    char c = text.charAt(pos);
    if (c == 'm' && pos + 1 < text.length() && text.charAt(pos + 1) == 's') {
      return MILLIS;
    }
    int rank = "wdhms".indexOf(c);
    if (rank < 0) {
      throw invalid(text, "unknown unit at index " + pos);
    }
    return rank;
  }

  private static long parseAmount(String text, int from, int to) {
    try {
      return Long.parseLong(text, from, to, 10);
    } catch (NumberFormatException e) {
      throw invalid(text, "number too large");
    }
  }

  private static Duration add(String text, Duration total, long amount, int rank) {
    try {
      Duration component =
          rank == MILLIS
              ? Duration.ofMillis(amount)
              : Duration.ofSeconds(Math.multiplyExact(amount, SECONDS[rank]));
      return total.plus(component);
    } catch (ArithmeticException e) {
      throw invalid(text, "duration too large");
    }
  }

  private static IllegalArgumentException invalid(String text, String reason) {
    return new IllegalArgumentException("Invalid duration text '" + text + "': " + reason);
  }
}
