package org.jwcarman.noirtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class GradesTest {

  @ParameterizedTest
  @CsvSource({
    "100, A", "95, A", "90, A",
    "89, B", "85, B", "80, B",
    "79, C", "75, C", "70, C",
    "69, D", "65, D", "60, D",
    "59, F", "50, F", "30, F", "9, F", "1, F", "0, F"
  })
  void mapsScoresToLetters(int score, char expected) {
    assertEquals(expected, Grades.letter(score));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 101, -10, 110, Integer.MIN_VALUE, Integer.MAX_VALUE})
  void rejectsOutOfRangeScores(int score) {
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> Grades.letter(score));
    assertTrue(e.getMessage().contains(String.valueOf(score)));
  }

  @Test
  void isNotInstantiable() throws Exception {
    Constructor<Grades> c = Grades.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(c.getModifiers()));
    c.setAccessible(true);
    assertEquals(Grades.class, c.newInstance().getClass());
  }
}
