package org.jwcarman.noirtest;

import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Evaluates integer arithmetic expressions.
 *
 * <p>Grammar: an expression is terms joined by {@code +} or {@code -}; a term is factors joined by
 * {@code *}, {@code /} or {@code %}; a factor is an optional run of unary {@code -} or {@code +}
 * signs followed by a non-negative decimal integer literal or a parenthesized expression. Binary
 * operators are left-associative; {@code *}, {@code /} and {@code %} bind tighter than {@code +}
 * and {@code -}; unary signs bind tighter than every binary operator. Spaces may appear between any
 * two tokens; no other whitespace is allowed. {@code /} and {@code %} follow Java's {@code long}
 * semantics (truncation toward zero).
 */
public final class Arithmetic {

  private Arithmetic() {}

  /**
   * Evaluates {@code text}.
   *
   * @throws NullPointerException if {@code text} is {@code null}
   * @throws ParseException if {@code text} does not match the grammar; its {@code position} is the
   *     zero-based index of the first character that cannot be accepted, or {@code text.length()}
   *     if the text ends too early
   * @throws ArithmeticException on division or remainder by zero, or if a literal or any
   *     intermediate result does not fit in a {@code long}
   */
  public static long evaluate(String text) {
    Objects.requireNonNull(text, "text");
    return new Parser(text).parse().getAsLong();
  }

  /** A malformed expression. */
  public static final class ParseException extends RuntimeException {

    private final int position;

    /** Creates an exception for a problem at {@code position}. */
    public ParseException(String message, int position) {
      super(message);
      this.position = position;
    }

    /** The zero-based index of the first character that cannot be accepted. */
    public int position() {
      return position;
    }
  }

  /**
   * Recursive-descent parser that builds a tree of deferred computations, so that the whole text is
   * checked against the grammar before any arithmetic is performed.
   */
  private static final class Parser {

    private static final int END = -1;

    private final String text;
    private int pos;

    Parser(String text) {
      this.text = text;
    }

    LongSupplier parse() {
      LongSupplier result = expression();
      if (peek() != END) {
        throw unexpected();
      }
      return result;
    }

    private LongSupplier expression() {
      LongSupplier left = term();
      int op = peek();
      while (op == '+' || op == '-') {
        pos++;
        left = binary(op, left, term());
        op = peek();
      }
      return left;
    }

    private LongSupplier term() {
      LongSupplier left = factor();
      int op = peek();
      while (op == '*' || op == '/' || op == '%') {
        pos++;
        left = binary(op, left, factor());
        op = peek();
      }
      return left;
    }

    private LongSupplier factor() {
      int c = peek();
      if (c == '-') {
        pos++;
        LongSupplier operand = factor();
        return () -> Math.negateExact(operand.getAsLong());
      }
      if (c == '+') {
        pos++;
        return factor();
      }
      if (c == '(') {
        pos++;
        return parenthesized();
      }
      return literal();
    }

    private LongSupplier parenthesized() {
      LongSupplier inner = expression();
      if (peek() != ')') {
        throw unexpected();
      }
      pos++;
      return inner;
    }

    private LongSupplier literal() {
      int start = pos;
      while (pos < text.length() && isDigit(text.charAt(pos))) {
        pos++;
      }
      if (pos == start) {
        throw unexpected();
      }
      String digits = text.substring(start, pos);
      return () -> parseLiteral(digits);
    }

    private static boolean isDigit(char c) {
      return c >= '0' && c <= '9';
    }

    private static long parseLiteral(String digits) {
      long value = 0;
      for (int i = 0; i < digits.length(); i++) {
        value = Math.addExact(Math.multiplyExact(value, 10L), digits.charAt(i) - '0');
      }
      return value;
    }

    private static LongSupplier binary(int op, LongSupplier left, LongSupplier right) {
      return switch (op) {
        case '+' -> () -> Math.addExact(left.getAsLong(), right.getAsLong());
        case '-' -> () -> Math.subtractExact(left.getAsLong(), right.getAsLong());
        case '*' -> () -> Math.multiplyExact(left.getAsLong(), right.getAsLong());
        case '/' -> () -> Math.divideExact(left.getAsLong(), right.getAsLong());
        default -> () -> left.getAsLong() % right.getAsLong();
      };
    }

    /** Skips spaces and returns the next character, or {@link #END} at the end of the text. */
    private int peek() {
      while (pos < text.length() && text.charAt(pos) == ' ') {
        pos++;
      }
      return pos < text.length() ? text.charAt(pos) : END;
    }

    private ParseException unexpected() {
      if (pos >= text.length()) {
        return new ParseException("unexpected end of expression", pos);
      }
      return new ParseException("unexpected character '" + text.charAt(pos) + "'", pos);
    }
  }
}
