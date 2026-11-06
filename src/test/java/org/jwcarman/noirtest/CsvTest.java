package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CsvTest {

  // R1
  @Test
  void parsesSingleLine() {
    assertEquals(List.of(List.of("a", "b", "c")), Csv.parse("a,b,c"));
  }

  @Test
  void parsesLinesEndingWithLineFeed() {
    assertEquals(List.of(List.of("a", "b"), List.of("c", "d")), Csv.parse("a,b\nc,d\n"));
  }

  @Test
  void parsesLinesSeparatedByCrLf() {
    assertEquals(List.of(List.of("a", "b"), List.of("c", "d")), Csv.parse("a,b\r\nc,d"));
    assertEquals(List.of(List.of("a", "b"), List.of("c", "d")), Csv.parse("a,b\r\nc,d\r\n"));
  }

  @Test
  void emptyTextHasNoRecords() {
    assertEquals(List.of(), Csv.parse(""));
  }

  @Test
  void lineBreakOnlyIsOneEmptyField() {
    assertEquals(List.of(List.of("")), Csv.parse("\r\n"));
    assertEquals(List.of(List.of("")), Csv.parse("\n"));
    assertEquals(List.of(List.of(""), List.of("")), Csv.parse("\n\r\n"));
  }

  // R2
  @Test
  void keepsEmptyFields() {
    assertEquals(List.of(List.of("a", "", "c")), Csv.parse("a,,c"));
    assertEquals(List.of(List.of("", "")), Csv.parse(","));
    assertEquals(List.of(List.of("a", "")), Csv.parse("a,"));
    assertEquals(List.of(List.of("a", "")), Csv.parse("a,\n"));
    assertEquals(List.of(List.of("", "a")), Csv.parse(",a"));
  }

  @Test
  void keepsEmptyLines() {
    assertEquals(List.of(List.of("a"), List.of(""), List.of("b")), Csv.parse("a\n\nb"));
    assertEquals(List.of(List.of("a"), List.of(""), List.of("b")), Csv.parse("a\r\n\r\nb"));
  }

  @Test
  void neverTrimsWhitespace() {
    assertEquals(List.of(List.of(" a ", " b ")), Csv.parse(" a , b "));
    assertEquals(List.of(List.of("\t", " ")), Csv.parse("\t, "));
  }

  // R3
  @Test
  void quotedFieldWithComma() {
    assertEquals(List.of(List.of("x,y", "z")), Csv.parse("\"x,y\",z"));
  }

  @Test
  void quotedFieldWithDoubledQuotes() {
    assertEquals(List.of(List.of("say \"hi\"")), Csv.parse("\"say \"\"hi\"\"\""));
    assertEquals(List.of(List.of("\"")), Csv.parse("\"\"\"\""));
    assertEquals(List.of(List.of("\"\"", "x")), Csv.parse("\"\"\"\"\"\",x"));
  }

  @Test
  void quotedFieldWithLineBreaks() {
    assertEquals(List.of(List.of("line1\nline2", "x")), Csv.parse("\"line1\nline2\",x"));
    assertEquals(List.of(List.of("a\r\nb"), List.of("c")), Csv.parse("\"a\r\nb\"\r\nc"));
    assertEquals(List.of(List.of("a\rb")), Csv.parse("\"a\rb\""));
  }

  @Test
  void emptyQuotedFields() {
    assertEquals(List.of(List.of("")), Csv.parse("\"\""));
    assertEquals(List.of(List.of("a", "", "c")), Csv.parse("a,\"\",c"));
    assertEquals(List.of(List.of("", "")), Csv.parse("\"\",\"\"\n"));
  }

  @Test
  void quotedFieldFollowedByLineBreaks() {
    assertEquals(List.of(List.of("a"), List.of("b")), Csv.parse("\"a\"\nb"));
    assertEquals(List.of(List.of("a"), List.of("b")), Csv.parse("\"a\"\r\n\"b\"\r\n"));
  }

  // R4
  @ParameterizedTest
  @ValueSource(
      strings = {
        "a\"b",
        "a\"",
        "\"abc",
        "\"",
        "\"ab\"\"",
        "a,\"b",
        "\"a\"b",
        "\"a\" ",
        "\"a\"\"\"x",
        " \"a\"",
        "a\rb",
        "a\r",
        "\r",
        "\"a\"\r",
        "\"a\"\rb",
        "a\n\rb",
        "x\nb\"c"
      })
  void rejectsMalformedText(String text) {
    assertThrows(IllegalArgumentException.class, () -> Csv.parse(text));
  }

  @Test
  void rejectsNullText() {
    assertThrows(NullPointerException.class, () -> Csv.parse(null));
  }

  // R5
  @Test
  void resultsAreUnmodifiable() {
    var r = Csv.parse("a,b");
    assertThrows(UnsupportedOperationException.class, () -> r.add(List.of("c")));
    assertThrows(UnsupportedOperationException.class, () -> r.get(0).add("c"));
    var empty = Csv.parse("");
    assertThrows(UnsupportedOperationException.class, () -> empty.add(List.of("c")));
  }

  // R6
  @Test
  void formatsPlainRecords() {
    assertEquals("a,b\r\nc,d\r\n", Csv.format(List.of(List.of("a", "b"), List.of("c", "d"))));
  }

  @Test
  void quotesFieldsThatNeedIt() {
    assertEquals("\"x,y\",z\r\n", Csv.format(List.of(List.of("x,y", "z"))));
    assertEquals("\"say \"\"hi\"\"\"\r\n", Csv.format(List.of(List.of("say \"hi\""))));
    assertEquals("\"a\nb\"\r\n", Csv.format(List.of(List.of("a\nb"))));
    assertEquals("\"a\rb\"\r\n", Csv.format(List.of(List.of("a\rb"))));
    assertEquals("\",\"\r\n", Csv.format(List.of(List.of(","))));
    assertEquals("\"\"\"\"\r\n", Csv.format(List.of(List.of("\""))));
    assertEquals("\"\n\"\r\n", Csv.format(List.of(List.of("\n"))));
    assertEquals("\"\r\"\r\n", Csv.format(List.of(List.of("\r"))));
    assertEquals("abc,\"de,\"\r\n", Csv.format(List.of(List.of("abc", "de,"))));
  }

  @Test
  void writesOtherFieldsAsIs() {
    assertEquals(" a ,b\r\n", Csv.format(List.of(List.of(" a ", "b"))));
    assertEquals("\r\n", Csv.format(List.of(List.of(""))));
    assertEquals(",\r\n", Csv.format(List.of(List.of("", ""))));
    assertEquals("a;b\tc'd\r\n", Csv.format(List.of(List.of("a;b\tc'd"))));
  }

  @Test
  void formatsNoRecordsAsEmptyString() {
    assertEquals("", Csv.format(List.of()));
  }

  // R7
  @Test
  void rejectsRecordWithNoFields() {
    assertThrows(IllegalArgumentException.class, () -> Csv.format(List.of(List.of())));
    assertThrows(
        IllegalArgumentException.class, () -> Csv.format(List.of(List.of("a"), List.of())));
  }

  @Test
  void rejectsNulls() {
    assertThrows(NullPointerException.class, () -> Csv.format(List.of(Arrays.asList("a", null))));
    assertThrows(NullPointerException.class, () -> Csv.format(Arrays.asList(List.of("a"), null)));
    assertThrows(NullPointerException.class, () -> Csv.format(null));
  }

  // R8
  @Test
  void roundTrips() {
    var records1 =
        List.of(
            List.of("a", "x,y", ""), List.of("say \"hi\"", "line1\nline2"), List.of(""));
    assertEquals(records1, Csv.parse(Csv.format(records1)));
    var records2 = List.of(List.of("", ""), List.of(" ", "\r\n"));
    assertEquals(records2, Csv.parse(Csv.format(records2)));
  }

  @Test
  void parsesLongInputQuickly() {
    String text = "\"" + "x\"\"".repeat(200_000) + "\"," + "y".repeat(200_000) + "\n";
    var result = Csv.parse(text);
    assertEquals(1, result.size());
    assertEquals(2, result.get(0).size());
    assertEquals(400_000, result.get(0).get(0).length());
    assertTrue(result.get(0).get(1).chars().allMatch(c -> c == 'y'));
  }
}
