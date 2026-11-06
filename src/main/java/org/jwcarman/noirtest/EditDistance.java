package org.jwcarman.noirtest;

import java.util.Objects;

/** Edit distances between strings. Characters are compared as {@code char} values. */
public final class EditDistance {

  private EditDistance() {}

  /**
   * Returns the Levenshtein distance between {@code a} and {@code b}: the minimum number of
   * single-{@code char} insertions, deletions and substitutions that turn {@code a} into {@code b}.
   * Comparison is case-sensitive.
   *
   * @throws NullPointerException if {@code a} or {@code b} is {@code null}
   */
  public static int levenshtein(String a, String b) {
    Objects.requireNonNull(a, "a");
    Objects.requireNonNull(b, "b");
    if (a.length() < b.length()) {
      return distance(b, a);
    }
    return distance(a, b);
  }

  /** Computes the distance using a single row sized to the shorter string {@code shorter}. */
  private static int distance(String longer, String shorter) {
    int n = shorter.length();
    int[] row = new int[n + 1];
    for (int j = 0; j <= n; j++) {
      row[j] = j;
    }
    for (int i = 1; i <= longer.length(); i++) {
      int diagonal = row[0];
      row[0] = i;
      char c = longer.charAt(i - 1);
      for (int j = 1; j <= n; j++) {
        int above = row[j];
        int cost = c == shorter.charAt(j - 1) ? 0 : 1;
        row[j] = Math.min(Math.min(row[j - 1] + 1, above + 1), diagonal + cost);
        diagonal = above;
      }
    }
    return row[n];
  }
}
