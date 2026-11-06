package org.jwcarman.noirtest;

import static org.jwcarman.noirtest.SemanticVersion.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SemanticVersionTest {

  @Test
  void parsesCore() {
    var v = parse("1.2.3");
    assertEquals(1, v.major());
    assertEquals(2, v.minor());
    assertEquals(3, v.patch());
    assertEquals(List.of(), v.preRelease());
    assertEquals(List.of(), v.build());
  }

  @Test
  void parsesZeros() {
    var v = parse("0.0.0");
    assertEquals(0, v.major());
    assertEquals(0, v.minor());
    assertEquals(0, v.patch());
  }

  @Test
  void parsesLargestMajor() {
    var v = parse("2147483647.10.20");
    assertEquals(2147483647, v.major());
    assertEquals(10, v.minor());
    assertEquals(20, v.patch());
  }

  @Test
  void parsesPreReleaseAndBuild() {
    var v = parse("1.0.0-alpha.1+build.5");
    assertEquals(List.of("alpha", "1"), v.preRelease());
    assertEquals(List.of("build", "5"), v.build());
    assertEquals(List.of("0", "3", "7"), parse("1.0.0-0.3.7").preRelease());
    assertEquals(List.of("x-y", "z--"), parse("1.0.0-x-y.z--").preRelease());
    var b = parse("1.0.0+001.sha-5114f85");
    assertEquals(List.of(), b.preRelease());
    assertEquals(List.of("001", "sha-5114f85"), b.build());
    var rc = parse("1.0.0-rc.1+exp.sha.5114f85");
    assertEquals(List.of("rc", "1"), rc.preRelease());
    assertEquals(List.of("exp", "sha", "5114f85"), rc.build());
  }

  @Test
  void toStringReturnsText() {
    assertEquals("1.0.0-alpha.1+build.5", parse("1.0.0-alpha.1+build.5").toString());
    assertEquals("10.20.30", parse("10.20.30").toString());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "1.2", "1", "1.2.3.4", "01.2.3", "1.02.3", "1.2.03", "v1.2.3", " 1.2.3", "1.2.3 ",
        "-1.2.3", "1..3", "2147483648.0.0", "1.2147483648.0", "1.0.2147483648",
        "99999999999999999999.0.0", "", "1.2.3-", "1.2.3+", "1.2.3-alpha..1", "1.2.3-alpha.",
        "1.2.3-01", "1.2.3-alpha.01", "1.2.3-al$pha", "1.2.3-+b", "1.2.3+a+b", "1.2.3+a..b",
        "1.2.3+a.", "1.2.3\n", "1.2.3-١", "١.2.3", "1.2.3-a_b", "a.b.c", "1.2.3-.a"
      })
  void rejectsInvalid(String text) {
    var e = assertThrows(IllegalArgumentException.class, () -> parse(text));
    assertTrue(e.getMessage().contains(text), e.getMessage());
  }

  @Test
  void rejectsNull() {
    assertThrows(NullPointerException.class, () -> parse(null));
  }

  @Test
  void allowsLeadingZeroInAlphanumericPreRelease() {
    assertEquals(List.of("0a"), parse("1.2.3-0a").preRelease());
    assertEquals(List.of("00-x"), parse("1.2.3-00-x").preRelease());
    assertEquals(List.of("0", "a"), parse("1.2.3-0.a").preRelease());
  }

  @ParameterizedTest
  @CsvSource({
    "1.0.0, 2.0.0, -1",
    "2.1.1, 2.1.0, 1",
    "1.10.0, 1.9.0, 1",
    "2.0.0, 1.99.99, 1",
    "1.2.3, 1.3.0, -1",
    "1.2.3, 1.2.4, -1",
    "1.2.3, 1.2.3, 0",
    "1.0.0-2, 1.0.0-10, -1",
    "1.0.0-10, 1.0.0-a, -1",
    "1.0.0-a, 1.0.0-10, 1",
    "1.0.0-2, 1.0.0-1a, -1",
    "1.0.0-Beta, 1.0.0-alpha, -1",
    "1.0.0-alpha.10, 1.0.0-alpha.9, 1",
    "1.0.0-alpha.2147483648, 1.0.0-alpha.2147483647, 1",
    "1.0.0-1.99999999999999999999, 1.0.0-1.100000000000000000000, -1",
    "1.0.0-1.100000000000000000000, 1.0.0-1.99999999999999999999, 1",
    "1.0.0-alpha, 1.0.0-alpha.1, -1",
    "1.0.0-alpha.1, 1.0.0-alpha, 1",
    "1.0.0-a, 1.0.0-a, 0",
    "1.0.0-12, 1.0.0-13, -1",
    "1.0.0-ab, 1.0.0-b, -1",
    "1.0.0+a, 1.0.0+b, 0",
    "1.0.0-alpha+001, 1.0.0-alpha, 0",
    "1.0.0, 1.0.0-0, 1",
    "1.0.0-0, 1.0.0, -1",
    "2.0.0-0, 1.0.0, 1"
  })
  void comparesByPrecedence(String a, String b, int expected) {
    assertEquals(expected, Integer.signum(parse(a).compareTo(parse(b))));
  }

  @Test
  void compareToReturnsSignum() {
    assertEquals(-1, parse("1.0.0-a").compareTo(parse("1.0.0-z")));
    assertEquals(1, parse("1.0.0-z").compareTo(parse("1.0.0-a")));
  }

  @Test
  void sortsInSpecOrder() {
    List<SemanticVersion> list =
        new ArrayList<>(
            List.of(
                parse("1.0.0"),
                parse("1.0.0-rc.1"),
                parse("1.0.0-beta.11"),
                parse("1.0.0-beta.2"),
                parse("1.0.0-beta"),
                parse("1.0.0-alpha.beta"),
                parse("1.0.0-alpha.1"),
                parse("1.0.0-alpha")));
    Collections.sort(list);
    assertEquals(
        List.of(
            "1.0.0-alpha",
            "1.0.0-alpha.1",
            "1.0.0-alpha.beta",
            "1.0.0-beta",
            "1.0.0-beta.2",
            "1.0.0-beta.11",
            "1.0.0-rc.1",
            "1.0.0"),
        list.stream().map(SemanticVersion::toString).toList());
  }

  @Test
  void equalityIncludesAllParts() {
    var a = parse("1.0.0-rc.1+b.2");
    var b = parse("1.0.0-rc.1+b.2");
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(a, a);
    assertNotEquals(parse("1.0.0+a"), parse("1.0.0+b"));
    assertNotEquals(parse("2.0.0"), parse("1.0.0"));
    assertNotEquals(parse("1.1.0"), parse("1.0.0"));
    assertNotEquals(parse("1.0.1"), parse("1.0.0"));
    assertNotEquals(parse("1.0.0-a"), parse("1.0.0-b"));
    assertFalse(a.equals(null));
    assertFalse(a.equals("1.0.0-rc.1+b.2"));
    assertNotEquals(parse("1.0.0+a").hashCode(), parse("1.0.0+b").hashCode());
  }

  @Test
  void listsAreUnmodifiable() {
    var v = parse("1.0.0-a+b");
    assertThrows(UnsupportedOperationException.class, () -> v.preRelease().add("x"));
    assertThrows(UnsupportedOperationException.class, () -> v.build().add("x"));
    var empty = parse("1.0.0");
    assertThrows(UnsupportedOperationException.class, () -> empty.preRelease().add("x"));
    assertThrows(UnsupportedOperationException.class, () -> empty.build().add("x"));
  }

  @Test
  void compareToNullThrows() {
    assertThrows(NullPointerException.class, () -> parse("1.0.0").compareTo(null));
  }
}
