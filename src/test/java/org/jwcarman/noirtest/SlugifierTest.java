package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class SlugifierTest {

  private static void check(String title, String expected) {
    assertEquals(expected, Slugifier.slugify(title));
  }

  @Test
  void keepsLettersAndDigitsAndLowercases() {
    check("Hello World", "hello-world");
    check("ABC123", "abc123");
    check("already-a-slug", "already-a-slug");
    check("AZ", "az");
    check("A", "a");
    check("Z", "z");
    check("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz");
    check("0123456789", "0123456789");
  }

  @Test
  void asciiNeighboursOfLetterRangesAreSeparators() {
    check("a@b[c`d{e", "a-b-c-d-e");
    check("a/b:c", "a-b-c");
  }

  @Test
  void collapsesSeparatorRuns() {
    check("Hello, World!", "hello-world");
    check("Java 21 -- New Features", "java-21-new-features");
    check("a_b.c/d", "a-b-c-d");
    check("a\t\nb", "a-b");
  }

  @Test
  void trimsHyphens() {
    check("  Java 21 -- New Features  ", "java-21-new-features");
    check("--Hello--", "hello");
    check("!Wow!", "wow");
    check("-a-", "a");
    check("-", "");
    check("--", "");
  }

  @Test
  void removesCombiningMarks() {
    check("Café Crème", "cafe-creme");
    check("Ångström", "angstrom");
    check("Ñandú", "nandu");
    check("ÉCOLE", "ecole");
    check("Cafe\u0301", "cafe");
    check("a\u0301\u0308b", "ab");
  }

  @Test
  void nonAsciiBecomesSeparator() {
    check("Straße", "stra-e");
    check("Øresund", "resund");
    check("I ❤ Java", "i-java");
    check("naïve 日本 café", "naive-cafe");
    check("a\uD83D\uDE00b", "a-b");
  }

  @Test
  void deletesApostrophes() {
    check("Don't Stop", "dont-stop");
    check("It\u2019s 5 o\u2019clock", "its-5-oclock");
    check("rock 'n' roll", "rock-n-roll");
    check("a''b", "ab");
  }

  @Test
  void emptyCases() {
    check("", "");
    check("   ", "");
    check("!!!", "");
    check("日本語", "");
    check("'\u2019", "");
  }

  @Test
  void rejectsNull() {
    assertThrows(NullPointerException.class, () -> Slugifier.slugify(null));
  }

  @Test
  void independentOfDefaultLocale() {
    Locale original = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("tr-TR"));
      check("TITLE", "title");
      check("ISTANBUL", "istanbul");
    } finally {
      Locale.setDefault(original);
    }
  }

  @Test
  void isDeterministic() {
    assertEquals(Slugifier.slugify("Some Title"), Slugifier.slugify("Some Title"));
  }
}
