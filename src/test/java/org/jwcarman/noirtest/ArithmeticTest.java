package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jwcarman.noirtest.Arithmetic.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ArithmeticTest {

  @ParameterizedTest
  @CsvSource(
      delimiter = '=',
      value = {
        "1 + 2 * 3 = 7",
        "(1 + 2) * 3 = 9",
        "10 - 4 - 3 = 3",
        "100 / 10 / 5 = 2",
        "4 * (1024 + 512) = 6144",
        "17 % 5 * 2 = 4",
        "2 * 3 + 4 = 10",
        "20 % 7 % 4 = 2",
        "1 - 2 + 3 = 2",
        "8 / 2 * 3 = 12",
        "-3 * -2 = 6",
        "--5 = 5",
        "-(2 + 3) = -5",
        "2 - -3 = 5",
        "-7 / 2 = -3",
        "-7 % 3 = -1",
        "+5 = 5",
        "+-+5 = -5",
        "- - 5 = 5",
        "-2 * 3 = -6",
        "((7)) = 7",
        "0 = 0",
        "9 = 9",
        "0123 = 123",
        "1234567890 = 1234567890",
        "9223372036854775807 = 9223372036854775807",
        "-9223372036854775807 - 1 = -9223372036854775808",
        "(-9223372036854775807 - 1) % -1 = 0",
        "7 % -3 = 1",
        "( 1+2 )*( 3 ) = 9"
      })
  void evaluatesExpressions(String text, long expected) {
    assertEquals(expected, Arithmetic.evaluate(text));
  }

  @Test
  void allowsSurroundingSpaces() {
    assertEquals(42, Arithmetic.evaluate(" 42 "));
    assertEquals(3, Arithmetic.evaluate("   1   +   2   "));
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "1 +|3",
        "(1 + 2|6",
        "1 + 2)|5",
        "1 $ 2|2",
        "1 2|2",
        "''|0",
        "'   '|3",
        "'1 + '|4",
        "1 + *|4",
        "(|1",
        "()|1",
        "-|1",
        "'1 * '|4",
        "1 * )|4",
        "1 / 0 $|6",
        "99999999999999999999 x|21",
        "a|0",
        "1 :|2",
        "1 /|3",
        "/|0",
        ":|0",
        "(1 2)|3",
        "1 ** 2|3"
      })
  void reportsParsePosition(String text, int position) {
    ParseException e = assertThrows(ParseException.class, () -> Arithmetic.evaluate(text));
    assertEquals(position, e.position());
  }

  @Test
  void rejectsOtherWhitespace() {
    assertEquals(1, positionOf("1\t+ 2"));
    assertEquals(1, positionOf("1\n"));
    assertEquals(0, positionOf("\u00002"));
    assertEquals(1, positionOf("1\u0000"));
  }

  @Test
  void messagesDescribeProblem() {
    ParseException end = assertThrows(ParseException.class, () -> Arithmetic.evaluate("1 +"));
    assertEquals("unexpected end of expression", end.getMessage());
    ParseException bad = assertThrows(ParseException.class, () -> Arithmetic.evaluate("1 $ 2"));
    assertEquals("unexpected character '$'", bad.getMessage());
  }

  @Test
  void parseExceptionCarriesMessageAndPosition() {
    ParseException e = new ParseException("boom", 7);
    assertEquals("boom", e.getMessage());
    assertEquals(7, e.position());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "1 / (2 - 2)",
        "5 % 0",
        "9223372036854775807 + 1",
        "9223372036854775808",
        "3037000500 * 3037000500",
        "-9223372036854775808",
        "-9223372036854775807 - 2",
        "-(-9223372036854775807 - 1)",
        "(-9223372036854775807 - 1) / -1",
        "99999999999999999999",
        "92233720368547758070"
      })
  void throwsArithmeticException(String text) {
    assertThrows(ArithmeticException.class, () -> Arithmetic.evaluate(text));
  }

  @Test
  void rejectsNull() {
    assertThrows(NullPointerException.class, () -> Arithmetic.evaluate(null));
  }

  private static int positionOf(String text) {
    return assertThrows(ParseException.class, () -> Arithmetic.evaluate(text)).position();
  }
}
