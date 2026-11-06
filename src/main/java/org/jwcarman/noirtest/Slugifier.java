package org.jwcarman.noirtest;

import java.text.Normalizer;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Converts titles into URL slugs made of lowercase ASCII letters and digits joined by single
 * hyphens.
 *
 * <p>The slug for a title is produced by these steps, in order:
 *
 * <ol>
 *   <li>Decompose the title with {@link java.text.Normalizer.Form#NFD} and remove every combining
 *       mark (Unicode general category M), so {@code "é"} becomes {@code "e"}.
 *   <li>Replace every {@code ß} (U+00DF) and {@code ẞ} (U+1E9E) with {@code ss}.
 *   <li>Delete every apostrophe: U+0027 ({@code '}) and U+2019 ({@code ’}).
 *   <li>Lowercase the ASCII letters {@code A}-{@code Z} to {@code a}-{@code z}, independently of
 *       the default locale.
 *   <li>Replace every maximal run of one or more characters that are not {@code a}-{@code z} or
 *       {@code 0}-{@code 9} (spaces, punctuation, symbols, hyphens and any remaining non-ASCII
 *       characters such as {@code ø}, {@code æ} or {@code 日}) with a single hyphen {@code -}.
 *   <li>Remove any leading and trailing hyphen.
 * </ol>
 *
 * <p>For example, {@code slugify("Café Crème")} returns {@code "cafe-creme"}, {@code
 * slugify("Don't Stop")} returns {@code "dont-stop"} and {@code slugify("Straße")} returns {@code
 * "strasse"}. A title with no ASCII letters or digits left after these steps, such as {@code ""},
 * {@code "!!!"} or {@code "日本語"}, produces the empty string.
 */
public final class Slugifier {

  private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");
  private static final Pattern SHARP_S = Pattern.compile("[\u00DF\u1E9E]");
  private static final Pattern APOSTROPHES = Pattern.compile("['’]+");
  private static final Pattern SEPARATORS = Pattern.compile("[^a-z0-9]+");
  private static final Pattern EDGE_HYPHENS = Pattern.compile("^-|-$");

  private Slugifier() {}

  /**
   * Returns the URL slug for {@code title}, following the steps described on this class, with one
   * addition: right after combining marks are removed (step 1), every {@code ß} (U+00DF) and
   * {@code ẞ} (U+1E9E) is replaced with {@code ss}. The class documentation, including its {@code
   * "Straße"} example, is updated to match.
   *
   * <p>The result contains only the characters {@code a}-{@code z}, {@code 0}-{@code 9} and
   * {@code -}; it never starts or ends with {@code -} and never contains two consecutive {@code
   * -}. It is the empty string when no ASCII letters or digits remain.
   *
   * @param title the title to convert
   * @return the slug for {@code title}, possibly empty, never {@code null}
   * @throws NullPointerException if {@code title} is {@code null}
   */
  public static String slugify(String title) {
    Objects.requireNonNull(title, "title");
    String decomposed = Normalizer.normalize(title, Normalizer.Form.NFD);
    String unmarked = COMBINING_MARKS.matcher(decomposed).replaceAll("");
    String noSharpS = SHARP_S.matcher(unmarked).replaceAll("ss");
    String noApostrophes = APOSTROPHES.matcher(noSharpS).replaceAll("");
    String lower = lowercaseAscii(noApostrophes);
    String hyphenated = SEPARATORS.matcher(lower).replaceAll("-");
    return EDGE_HYPHENS.matcher(hyphenated).replaceAll("");
  }

  /**
   * Returns the slug for {@code title}, at most {@code maxLength} characters long.
   *
   * <p>Let {@code full} be {@code slugify(title)}. If {@code full} has at most {@code maxLength}
   * characters, it is returned unchanged. Otherwise the result is the longest prefix of {@code
   * full} that has at most {@code maxLength} characters and ends where a word of {@code full}
   * ends, that is, just before a hyphen of {@code full}. If the first word of {@code full} alone is
   * longer than {@code maxLength}, the result is the first {@code maxLength} characters of {@code
   * full}. The result never ends with a hyphen.
   *
   * @param title the title to convert
   * @param maxLength the most characters the result may have, at least 1
   * @return the shortened slug, possibly empty, never {@code null}
   * @throws NullPointerException if {@code title} is {@code null}
   * @throws IllegalArgumentException if {@code maxLength} is less than 1
   */
  public static String slugify(String title, int maxLength) {
    Objects.requireNonNull(title, "title");
    if (maxLength < 1) {
      throw new IllegalArgumentException("maxLength must be at least 1: " + maxLength);
    }
    String full = slugify(title);
    if (full.length() <= maxLength) {
      return full;
    }
    int boundary = full.lastIndexOf('-', maxLength);
    return full.substring(0, boundary < 0 ? maxLength : boundary);
  }

  private static String lowercaseAscii(String text) {
    char[] chars = text.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (c >= 'A' && c <= 'Z') {
        chars[i] = (char) (c + ('a' - 'A'));
      }
    }
    return new String(chars);
  }
}
