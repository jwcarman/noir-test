package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SemanticVersionBumpTest {

  @Test
  void majorBumpResetsMinorAndPatch() {
    SemanticVersion v = SemanticVersion.parse("1.2.3").bump(VersionBump.MAJOR);
    assertEquals("2.0.0", v.toString());
    assertEquals(2, v.major());
    assertEquals(0, v.minor());
    assertEquals(0, v.patch());
  }

  @Test
  void minorBumpResetsPatch() {
    SemanticVersion v = SemanticVersion.parse("1.2.3").bump(VersionBump.MINOR);
    assertEquals("1.3.0", v.toString());
    assertEquals(1, v.major());
    assertEquals(3, v.minor());
    assertEquals(0, v.patch());
  }

  @Test
  void patchBumpIncreasesPatch() {
    SemanticVersion v = SemanticVersion.parse("1.2.3").bump(VersionBump.PATCH);
    assertEquals("1.2.4", v.toString());
    assertEquals(1, v.major());
    assertEquals(2, v.minor());
    assertEquals(4, v.patch());
  }

  @Test
  void patchBumpFromZero() {
    assertEquals(
        SemanticVersion.parse("0.0.1"), SemanticVersion.parse("0.0.0").bump(VersionBump.PATCH));
  }

  @Test
  void dropsPreReleaseAndBuild() {
    SemanticVersion v = SemanticVersion.parse("1.2.3-beta.1+sha.abc").bump(VersionBump.MINOR);
    assertEquals("1.3.0", v.toString());
    assertTrue(v.preRelease().isEmpty());
    assertTrue(v.build().isEmpty());
    assertEquals(SemanticVersion.parse("1.3.0"), v);
  }

  @Test
  void dropsBuildOnPatch() {
    assertEquals(
        SemanticVersion.parse("1.2.4"),
        SemanticVersion.parse("1.2.3+build.7").bump(VersionBump.PATCH));
  }

  @Test
  void dropsPreReleaseOnMajor() {
    SemanticVersion v = SemanticVersion.parse("1.2.3-rc.1").bump(VersionBump.MAJOR);
    assertEquals(SemanticVersion.parse("2.0.0"), v);
    assertEquals(SemanticVersion.parse("2.0.0").hashCode(), v.hashCode());
  }

  @Test
  void originalUnchanged() {
    SemanticVersion v = SemanticVersion.parse("1.2.3-rc.1");
    SemanticVersion next = v.bump(VersionBump.MAJOR);
    assertEquals("1.2.3-rc.1", v.toString());
    assertEquals(1, v.major());
    assertEquals(2, v.minor());
    assertEquals(3, v.patch());
    assertEquals(java.util.List.of("rc", "1"), v.preRelease());
    assertEquals(2, next.major());
  }

  @Test
  void majorOverflow() {
    SemanticVersion v = SemanticVersion.parse("2147483647.0.0");
    assertThrows(IllegalStateException.class, () -> v.bump(VersionBump.MAJOR));
  }

  @Test
  void minorOverflow() {
    SemanticVersion v = SemanticVersion.parse("1.2147483647.5");
    assertThrows(IllegalStateException.class, () -> v.bump(VersionBump.MINOR));
  }

  @Test
  void patchOverflow() {
    SemanticVersion v = SemanticVersion.parse("1.2.2147483647");
    assertThrows(IllegalStateException.class, () -> v.bump(VersionBump.PATCH));
  }

  @Test
  void majorBumpIgnoresMaxMinor() {
    assertEquals(
        "2.0.0", SemanticVersion.parse("1.2147483647.5").bump(VersionBump.MAJOR).toString());
  }

  @Test
  void otherPartsAtMaxDoNotBlockBump() {
    assertEquals(
        "2147483647.2147483647.1",
        SemanticVersion.parse("2147483647.2147483647.0").bump(VersionBump.PATCH).toString());
    assertEquals(
        "2147483647.1.0",
        SemanticVersion.parse("2147483647.0.2147483647").bump(VersionBump.MINOR).toString());
  }

  @Test
  void bumpToMaxValueAllowed() {
    assertEquals(
        "1.2.2147483647",
        SemanticVersion.parse("1.2.2147483646").bump(VersionBump.PATCH).toString());
  }

  @Test
  void nullKindRejected() {
    SemanticVersion v = SemanticVersion.parse("1.2.3");
    assertThrows(NullPointerException.class, () -> v.bump(null));
  }

  @Test
  void enumValues() {
    assertEquals(3, VersionBump.values().length);
    assertEquals(VersionBump.MINOR, VersionBump.valueOf("MINOR"));
  }
}
