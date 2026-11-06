package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SlugifierSharpSTest {

  @Test
  void sharpSBecomesSs() {
    assertEquals("strasse", Slugifier.slugify("Straße"));
    assertEquals("fussball-wm", Slugifier.slugify("Fußball WM"));
    assertEquals("gross", Slugifier.slugify("GROẞ"));
    assertEquals("ss", Slugifier.slugify("ß"));
    assertEquals("ss", Slugifier.slugify("ẞ"));
    assertEquals("masse-gewichte", Slugifier.slugify("Maße & Gewichte"));
    assertEquals("ssss", Slugifier.slugify("ßẞ"));
    assertEquals("a-ss-b", Slugifier.slugify("a ß b"));
  }

  @Test
  void sharpSWithCombiningMarkBecomesSs() {
    assertEquals("ss", Slugifier.slugify("ß́"));
  }

  @Test
  void lengthLimitedOverloadSeesSharpS() {
    assertEquals("strass", Slugifier.slugify("Straße", 6));
    assertEquals("grosse", Slugifier.slugify("Große Straße", 10));
    assertEquals("grosse-strasse", Slugifier.slugify("Große Straße", 14));
  }
}
