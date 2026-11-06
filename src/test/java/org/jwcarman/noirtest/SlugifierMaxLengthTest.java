package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlugifierMaxLengthTest {

  @ParameterizedTest
  @CsvSource({
    "Hello World Again, 17, hello-world-again",
    "Hello World Again, 100, hello-world-again",
    "Café Crème, 10, cafe-creme",
    "Hello World Again, 11, hello-world",
    "Hello World Again, 12, hello-world",
    "Hello World Again, 16, hello-world",
    "Hello World Again, 10, hello",
    "Hello World Again, 6, hello",
    "Hello World Again, 5, hello",
    "Café Crème, 4, cafe",
    "Café Crème, 9, cafe",
    "Hello World Again, 3, hel",
    "Hello World Again, 4, hell",
    "Supercalifragilistic, 5, super",
    "Hello World Again, 1, h",
    "a b c, 4, a-b",
    "a b c, 3, a-b",
    "a b c, 2, a",
    "a b c, 1, a",
  })
  void shortensAtWordBoundary(String title, int maxLength, String expected) {
    assertEquals(expected, Slugifier.slugify(title, maxLength));
  }

  @Test
  void emptySlugGivesEmptyString() {
    assertEquals("", Slugifier.slugify("!!!", 5));
    assertEquals("", Slugifier.slugify("", 1));
  }

  @Test
  void rejectsNonPositiveMaxLength() {
    assertThrows(IllegalArgumentException.class, () -> Slugifier.slugify("Hello", 0));
    assertThrows(IllegalArgumentException.class, () -> Slugifier.slugify("Hello", -1));
    assertThrows(IllegalArgumentException.class, () -> Slugifier.slugify("", 0));
  }

  @Test
  void rejectsNullTitle() {
    assertThrows(NullPointerException.class, () -> Slugifier.slugify(null, 5));
  }

  @Test
  void singleArgumentSlugifyUnchanged() {
    assertEquals("hello-world-again", Slugifier.slugify("Hello World Again"));
  }
}
