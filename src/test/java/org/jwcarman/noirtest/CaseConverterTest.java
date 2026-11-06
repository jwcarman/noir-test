package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CaseConverterTest {

  @ParameterizedTest
  @CsvSource({
    "firstName, first_name",
    "FirstName, first_name",
    "name, name",
    "aB, a_b",
    "parseHTTPResponse, parse_http_response",
    "URL, url",
    "userID, user_id",
    "XMLParser, xml_parser",
    "address2, address2",
    "sha256Hash, sha256_hash",
    "v2API, v2_api",
    "a, a",
    "Z, z",
    "AB, ab",
    "ABc, a_bc",
    "aBC, a_bc",
    "aBc, a_bc",
    "a1b, a1b",
    "A1B, a1_b",
    "z9Z, z9_z",
    "ZzAzz, zz_azz",
    "abcXYZ, abc_xyz",
    "HTTPServer2Go, http_server2_go",
    "AaZz09, aa_zz09"
  })
  void convertsToSnakeCase(String input, String expected) {
    assertEquals(expected, CaseConverter.toSnakeCase(input));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"", "2fast", "first_name", "naïve", "_a", "a-b", "a b", "a@", "a[", "a`", "a{",
        "a/", "a:", "@a", "[a", "`a", "{a", "Ä", "aÄ"})
  void rejectsInvalidIdentifiers(String input) {
    assertThrows(IllegalArgumentException.class, () -> CaseConverter.toSnakeCase(input));
  }

  @Test
  void rejectsNull() {
    assertThrows(NullPointerException.class, () -> CaseConverter.toSnakeCase(null));
  }
}
