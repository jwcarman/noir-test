package org.jwcarman.noirtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Greedy word wrapping on single spaces. */
public final class WordWrap {

  private WordWrap() {}

  /**
   * Splits {@code text} into words on runs of spaces (leading and trailing spaces ignored) and packs
   * them greedily into lines: each line holds as many words as fit, joined by one space, within
   * {@code width} characters. A word longer than {@code width} is split into chunks of exactly
   * {@code width} characters (the last chunk may be shorter), each on its own line, and the next
   * word starts a new line. Text with no words gives an empty list.
   *
   * @throws NullPointerException if {@code text} is {@code null}
   * @throws IllegalArgumentException if {@code width} is less than 1
   */
  public static List<String> wrap(String text, int width) {
    Objects.requireNonNull(text, "text");
    if (width < 1) {
      throw new IllegalArgumentException("width must be at least 1: " + width);
    }
    List<String> lines = new ArrayList<>();
    StringBuilder line = new StringBuilder();
    for (String word : text.split(" ")) {
      if (!word.isEmpty()) {
        addWord(lines, line, word, width);
      }
    }
    flush(lines, line);
    return lines;
  }

  private static void addWord(List<String> lines, StringBuilder line, String word, int width) {
    if (word.length() > width) {
      flush(lines, line);
      for (int i = 0; i < word.length(); i += width) {
        lines.add(word.substring(i, Math.min(word.length(), i + width)));
      }
    } else if (line.isEmpty()) {
      line.append(word);
    } else if (line.length() + 1 + word.length() <= width) {
      line.append(' ').append(word);
    } else {
      flush(lines, line);
      line.append(word);
    }
  }

  private static void flush(List<String> lines, StringBuilder line) {
    if (!line.isEmpty()) {
      lines.add(line.toString());
      line.setLength(0);
    }
  }
}
