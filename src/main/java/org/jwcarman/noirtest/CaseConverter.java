package org.jwcarman.noirtest;

import java.util.Objects;

/** Converts identifiers between naming conventions. */
public final class CaseConverter {

  private CaseConverter() {}

  /**
   * Converts a camelCase or PascalCase identifier made of ASCII letters and digits to lower
   * snake_case. An underscore is inserted before an uppercase letter that follows a lowercase
   * letter or a digit, and before an uppercase letter that is followed by a lowercase letter and
   * preceded by another uppercase letter (so an acronym stays one word: {@code "parseHTTPResponse"}
   * becomes {@code "parse_http_response"}). Then every letter is lowercased. Digits never start a
   * new word.
   *
   * @param identifier one or more ASCII letters and digits, starting with a letter
   * @return the snake_case form
   * @throws NullPointerException if {@code identifier} is {@code null}
   * @throws IllegalArgumentException if {@code identifier} is empty, does not start with an ASCII
   *     letter, or contains a character that is not an ASCII letter or digit
   */
  public static String toSnakeCase(String identifier) {
    validate(identifier);
    StringBuilder result = new StringBuilder(identifier.length() + 8);
    for (int i = 0; i < identifier.length(); i++) {
      char c = identifier.charAt(i);
      if (startsWord(identifier, i)) {
        result.append('_');
      }
      result.append(toLower(c));
    }
    return result.toString();
  }

  private static void validate(String identifier) {
    Objects.requireNonNull(identifier, "identifier");
    if (identifier.isEmpty()) {
      throw new IllegalArgumentException("identifier must not be empty");
    }
    if (!isLetter(identifier.charAt(0))) {
      throw new IllegalArgumentException("identifier must start with an ASCII letter: " + identifier);
    }
    for (int i = 1; i < identifier.length(); i++) {
      char c = identifier.charAt(i);
      if (!isLetter(c) && !isDigit(c)) {
        throw new IllegalArgumentException(
            "identifier contains an invalid character at index " + i + ": " + identifier);
      }
    }
  }

  private static boolean startsWord(String s, int i) {
    if (i == 0 || !isUpper(s.charAt(i))) {
      return false;
    }
    char prev = s.charAt(i - 1);
    if (isLower(prev) || isDigit(prev)) {
      return true;
    }
    return i + 1 < s.length() && isLower(s.charAt(i + 1));
  }

  private static char toLower(char c) {
    return isUpper(c) ? (char) (c + ('a' - 'A')) : c;
  }

  private static boolean isLetter(char c) {
    return isUpper(c) || isLower(c);
  }

  private static boolean isUpper(char c) {
    return c >= 'A' && c <= 'Z';
  }

  private static boolean isLower(char c) {
    return c >= 'a' && c <= 'z';
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }
}
