package org.jwcarman.noirtest;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A version number in the Semantic Versioning 2.0.0 format {@code
 * MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]}.
 *
 * <p><b>Syntax.</b> {@code MAJOR}, {@code MINOR} and {@code PATCH} are numeric identifiers: one or
 * more ASCII digits with no leading zero unless the identifier is exactly {@code 0}, and each must
 * fit in an {@code int}. {@code PRERELEASE} and {@code BUILD} are each one or more identifiers
 * separated by {@code '.'}; an identifier is one or more characters from {@code [0-9A-Za-z-]}. A
 * pre-release identifier made only of digits must not have a leading zero unless it is exactly
 * {@code 0}; build identifiers may have leading zeros. Nothing else may appear in the text, not even
 * surrounding whitespace or a {@code v} prefix.
 *
 * <p><b>Precedence.</b> {@link #compareTo} orders versions by SemVer precedence. Major, minor and
 * patch are compared numerically, in that order. When they are equal, a version with a pre-release
 * has lower precedence than one without. Two pre-releases are compared identifier by identifier from
 * left to right: identifiers made only of digits are compared numerically, other identifiers are
 * compared lexically by ASCII code, and an identifier made only of digits has lower precedence than
 * any other identifier. If every identifier compared is equal, the pre-release with fewer
 * identifiers has lower precedence. Build metadata is ignored for precedence.
 *
 * <p><b>Equality.</b> Two versions are {@link #equals equal} exactly when all five parts are equal,
 * build metadata included. So {@code compareTo} is inconsistent with {@code equals}: versions that
 * differ only in build metadata compare as {@code 0} but are not equal.
 */
public final class SemanticVersion implements Comparable<SemanticVersion> {

  private static final String NUMBER = "(0|[1-9][0-9]*)";
  private static final String IDENTIFIERS = "([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)";
  private static final Pattern PATTERN =
      Pattern.compile(
          NUMBER + "\\." + NUMBER + "\\." + NUMBER
              + "(?:-" + IDENTIFIERS + ")?(?:\\+" + IDENTIFIERS + ")?");
  private static final Pattern DIGITS = Pattern.compile("[0-9]+");

  private final int major;
  private final int minor;
  private final int patch;
  private final List<String> preRelease;
  private final List<String> build;
  private final String text;

  private SemanticVersion(
      int major, int minor, int patch, List<String> preRelease, List<String> build, String text) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.preRelease = preRelease;
    this.build = build;
    this.text = text;
  }

  /**
   * Parses {@code text} strictly.
   *
   * @param text the version string
   * @return the version
   * @throws NullPointerException if {@code text} is {@code null}
   * @throws IllegalArgumentException if {@code text} is not a valid version as described on this
   *     class
   */
  public static SemanticVersion parse(String text) {
    Objects.requireNonNull(text, "text");
    Matcher m = PATTERN.matcher(text);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid semantic version: \"" + text + "\"");
    }
    List<String> pre = identifiers(m.group(4));
    for (String id : pre) {
      if (isNumeric(id) && id.length() > 1 && id.charAt(0) == '0') {
        throw new IllegalArgumentException(
            "Leading zero in numeric pre-release identifier of \"" + text + "\"");
      }
    }
    return new SemanticVersion(
        number(m.group(1), text),
        number(m.group(2), text),
        number(m.group(3), text),
        pre,
        identifiers(m.group(5)),
        text);
  }

  private static List<String> identifiers(String group) {
    return group == null ? List.of() : List.copyOf(Arrays.asList(group.split("\\.")));
  }

  private static int number(String digits, String text) {
    try {
      return Integer.parseInt(digits);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Number too large in \"" + text + "\"", e);
    }
  }

  private static boolean isNumeric(String id) {
    return DIGITS.matcher(id).matches();
  }

  /** Returns the major version. */
  public int major() {
    return major;
  }

  /** Returns the minor version. */
  public int minor() {
    return minor;
  }

  /** Returns the patch version. */
  public int patch() {
    return patch;
  }

  /** Returns the pre-release identifiers in order, empty if none; the list is unmodifiable. */
  public List<String> preRelease() {
    return preRelease;
  }

  /** Returns the build metadata identifiers in order, empty if none; the list is unmodifiable. */
  public List<String> build() {
    return build;
  }

  /**
   * Compares by SemVer precedence, as described on this class.
   *
   * @throws NullPointerException if {@code other} is {@code null}
   */
  @Override
  public int compareTo(SemanticVersion other) {
    Objects.requireNonNull(other, "other");
    int c = Integer.compare(major, other.major);
    if (c == 0) {
      c = Integer.compare(minor, other.minor);
    }
    if (c == 0) {
      c = Integer.compare(patch, other.patch);
    }
    return c != 0 ? c : comparePreRelease(preRelease, other.preRelease);
  }

  private static int comparePreRelease(List<String> a, List<String> b) {
    if (a.isEmpty() || b.isEmpty()) {
      return Boolean.compare(a.isEmpty(), b.isEmpty());
    }
    int n = Math.min(a.size(), b.size());
    for (int i = 0; i < n; i++) {
      int c = compareIdentifier(a.get(i), b.get(i));
      if (c != 0) {
        return c;
      }
    }
    return Integer.compare(a.size(), b.size());
  }

  private static int compareIdentifier(String a, String b) {
    boolean aNum = isNumeric(a);
    boolean bNum = isNumeric(b);
    if (aNum != bNum) {
      return aNum ? -1 : 1;
    }
    if (aNum && a.length() != b.length()) {
      return Integer.compare(a.length(), b.length());
    }
    return Integer.signum(a.compareTo(b));
  }

  /**
   * Returns the next version of the given kind. {@code MAJOR} gives {@code (major + 1).0.0},
   * {@code MINOR} gives {@code major.(minor + 1).0} and {@code PATCH} gives {@code
   * major.minor.(patch + 1)}. The result has no pre-release identifiers and no build metadata, and
   * its {@link #toString()} is {@code "MAJOR.MINOR.PATCH"}. This version is unchanged.
   *
   * @param kind which part to increase
   * @return the next version
   * @throws NullPointerException if {@code kind} is {@code null}
   * @throws IllegalStateException if the part to increase is already {@link Integer#MAX_VALUE}
   */
  public SemanticVersion bump(VersionBump kind) {
    Objects.requireNonNull(kind, "kind");
    return switch (kind) {
      case MAJOR -> of(increment(major, "major"), 0, 0);
      case MINOR -> of(major, increment(minor, "minor"), 0);
      case PATCH -> of(major, minor, increment(patch, "patch"));
    };
  }

  private static int increment(int value, String part) {
    if (value == Integer.MAX_VALUE) {
      throw new IllegalStateException("Cannot increase " + part + " version beyond " + value);
    }
    return value + 1;
  }

  private static SemanticVersion of(int major, int minor, int patch) {
    return new SemanticVersion(
        major, minor, patch, List.of(), List.of(), major + "." + minor + "." + patch);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SemanticVersion v)) {
      return false;
    }
    return major == v.major
        && minor == v.minor
        && patch == v.patch
        && preRelease.equals(v.preRelease)
        && build.equals(v.build);
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, patch, preRelease, build);
  }

  /** Returns the version as text, equal to the text it was parsed from. */
  @Override
  public String toString() {
    return text;
  }
}
