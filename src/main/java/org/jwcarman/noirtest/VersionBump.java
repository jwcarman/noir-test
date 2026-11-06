package org.jwcarman.noirtest;

/** Which part of a {@link SemanticVersion} to increase. */
public enum VersionBump {
  /** Increase the major version; minor and patch become 0. */
  MAJOR,
  /** Increase the minor version; patch becomes 0. */
  MINOR,
  /** Increase the patch version. */
  PATCH
}
