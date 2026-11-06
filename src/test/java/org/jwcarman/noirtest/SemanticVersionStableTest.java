package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SemanticVersionStableTest {

  @ParameterizedTest
  @CsvSource({
    "1.0.0, true",
    "2.5.13, true",
    "1.0.0+build.5, true",
    "1.0.0-rc.1, false",
    "3.2.1-alpha+exp.sha.5114f85, false",
    "0.9.9, false",
    "0.0.0, false",
    "0.1.0+build, false",
    "0.1.0-beta, false",
    "10.0.0, true"
  })
  void isStable(String text, boolean expected) {
    assertEquals(expected, SemanticVersion.parse(text).isStable());
  }

  @org.junit.jupiter.api.Test
  void bumpedFromZeroMajorIsStable() {
    assertEquals(
        true, SemanticVersion.parse("0.9.9-rc.1").bump(VersionBump.MAJOR).isStable());
  }
}
