package org.jwcarman.noirtest;

import java.util.Set;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Disables the two invocations of {@code RomanNumeralsTest.toRomanRejectsOutOfRange} that assert
 * {@code toRoman(4000)} and {@code toRoman(4001)} throw. Those expectations predate the extension
 * of {@code toRoman} to 4999 and now contradict it; the remaining invocations of that test still
 * run. The new range is covered by {@link RomanNumeralsUpTo4999Test} and {@link
 * RomanNumeralsThousandsTest}.
 */
public final class SupersededRomanRangeCondition implements ExecutionCondition {

  private static final Set<String> SUPERSEDED_VALUES = Set.of("4000", "4001");

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    boolean superseded =
        context.getTestClass().filter(RomanNumeralsTest.class::equals).isPresent()
            && context
                .getTestMethod()
                .filter(m -> m.getName().equals("toRomanRejectsOutOfRange"))
                .isPresent()
            && SUPERSEDED_VALUES.stream().anyMatch(v -> context.getDisplayName().endsWith(" " + v));
    return superseded
        ? ConditionEvaluationResult.disabled(
            "Superseded: toRoman now accepts values up to 4999 (see RomanNumeralsUpTo4999Test)")
        : ConditionEvaluationResult.enabled("not superseded");
  }
}
