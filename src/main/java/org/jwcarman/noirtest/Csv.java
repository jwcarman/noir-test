package org.jwcarman.noirtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reads and writes comma-separated values.
 *
 * <p><b>Records and line breaks.</b> CSV text is a sequence of records separated by line breaks. A
 * line break is {@code "\r\n"}, {@code "\n"} or a {@code '\r'} that is not followed by {@code
 * '\n'}; so {@code "\r\n"} is a single line break. A line break at the very end of the text ends the
 * last record and does not start another one. An empty text has no records. A line with no
 * characters between two line breaks, or before the first one, is a record with a single empty
 * field.
 *
 * <p><b>Fields.</b> A record is one or more fields separated by commas. Whitespace is part of a
 * field and is never trimmed. A field is either unquoted or quoted:
 *
 * <ul>
 *   <li>An unquoted field is a run of characters other than {@code ','}, {@code '"'}, {@code '\r'}
 *       and {@code '\n'}. It may be empty.
 *   <li>A quoted field starts with {@code '"'} as its first character and ends at the next {@code
 *       '"'} that is not part of a doubled {@code "\"\""}. Inside it, {@code "\"\""} stands for one
 *       {@code '"'} and every other character, including {@code ','}, {@code '\r'} and {@code
 *       '\n'}, is taken literally. The closing quote must be followed by a comma, a line break or
 *       the end of the text.
 * </ul>
 *
 * <p><b>Errors.</b> {@link #parse} throws {@link IllegalArgumentException} for a {@code '"'} inside
 * an unquoted field, a quoted field with no closing quote, any character other than a comma or a
 * line break after a closing quote.
 *
 * <p><b>Writing.</b> {@link #format} writes each record as its fields joined by commas, followed by
 * {@code "\r\n"}. A field is written quoted, with each {@code '"'} doubled, exactly when it
 * contains {@code ','}, {@code '"'}, {@code '\r'} or {@code '\n'}; every other field is written as
 * is.
 */
public final class Csv {

  private Csv() {}

  /**
   * Parses {@code text} into records, each a list of its fields in order.
   *
   * <p>The rules are those described on this class, with one change: a line break is {@code
   * "\r\n"}, {@code "\n"} or a {@code '\r'} that is not followed by {@code '\n'}. So {@code "\r\n"}
   * is still one line break, and a lone {@code '\r'} outside a quoted field is a line break rather
   * than an error. Inside a quoted field, {@code '\r'} is still taken literally. The class
   * documentation is updated to match.
   *
   * @param text the CSV text
   * @return the records; the outer and inner lists are unmodifiable
   * @throws NullPointerException if {@code text} is {@code null}
   * @throws IllegalArgumentException if {@code text} is not valid CSV
   */
  public static List<List<String>> parse(String text) {
    Objects.requireNonNull(text, "text");
    return new Parser(text).records();
  }

  /**
   * Formats {@code records} as CSV text.
   *
   * @param records the records to write, each a list of one or more fields
   * @return the CSV text; the empty string when {@code records} is empty
   * @throws NullPointerException if {@code records}, any record or any field is {@code null}
   * @throws IllegalArgumentException if any record has no fields
   */
  public static String format(List<List<String>> records) {
    Objects.requireNonNull(records, "records");
    StringBuilder out = new StringBuilder();
    for (List<String> record : records) {
      appendRecord(out, Objects.requireNonNull(record, "record"));
    }
    return out.toString();
  }

  private static void appendRecord(StringBuilder out, List<String> record) {
    if (record.isEmpty()) {
      throw new IllegalArgumentException("A record must have at least one field");
    }
    String separator = "";
    for (String field : record) {
      out.append(separator);
      appendField(out, Objects.requireNonNull(field, "field"));
      separator = ",";
    }
    out.append("\r\n");
  }

  private static void appendField(StringBuilder out, String field) {
    if (needsQuotes(field)) {
      out.append('"').append(field.replace("\"", "\"\"")).append('"');
    } else {
      out.append(field);
    }
  }

  private static boolean needsQuotes(String field) {
    for (int i = 0; i < field.length(); i++) {
      if (isSpecial(field.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  private static boolean isSpecial(char c) {
    return c == ',' || c == '"' || c == '\r' || c == '\n';
  }

  /** Single-pass, linear-time parser over one CSV text. */
  private static final class Parser {
    private final String text;
    private final int length;
    private int pos;

    Parser(String text) {
      this.text = text;
      this.length = text.length();
    }

    List<List<String>> records() {
      List<List<String>> records = new ArrayList<>();
      while (pos < length) {
        records.add(record());
      }
      return List.copyOf(records);
    }

    /** Parses one record and consumes its terminating line break, if any. */
    private List<String> record() {
      List<String> fields = new ArrayList<>();
      while (true) {
        fields.add(field());
        if (!consumeComma()) {
          consumeLineBreakOrEnd();
          return List.copyOf(fields);
        }
      }
    }

    private String field() {
      if (pos < length && text.charAt(pos) == '"') {
        return quotedField();
      }
      return unquotedField();
    }

    private String unquotedField() {
      int start = pos;
      while (pos < length && !isSpecial(text.charAt(pos))) {
        pos++;
      }
      if (pos < length && text.charAt(pos) == '"') {
        throw error("Quote inside an unquoted field");
      }
      return text.substring(start, pos);
    }

    private String quotedField() {
      StringBuilder value = new StringBuilder();
      pos++;
      while (true) {
        int quote = text.indexOf('"', pos);
        if (quote < 0) {
          throw error("Quoted field has no closing quote");
        }
        value.append(text, pos, quote);
        pos = quote + 1;
        if (pos >= length || text.charAt(pos) != '"') {
          return value.toString();
        }
        value.append('"');
        pos++;
      }
    }

    private boolean consumeComma() {
      if (pos < length && text.charAt(pos) == ',') {
        pos++;
        return true;
      }
      return false;
    }

    private void consumeLineBreakOrEnd() {
      if (pos >= length) {
        return;
      }
      char c = text.charAt(pos);
      if (c == '\n') {
        pos++;
      } else if (c == '\r') {
        pos++;
        if (pos < length && text.charAt(pos) == '\n') {
          pos++;
        }
      } else {
        throw error("Unexpected character '" + c + "'");
      }
    }

    private IllegalArgumentException error(String message) {
      return new IllegalArgumentException(message + " at index " + pos);
    }
  }
}
