package fr.pierrickrouxel.jpaentitygenerator.rule;

import java.util.List;

/**
 * Provides #matches method to check if this rule matches a given class name.
 */
public interface ClassMatcher {

  /**
   * Returns a single partial-matching rule.
   */
  String getClassName();

  /**
   * Returns multiple partial-matching rules.
   */
  List<String> getClassNames();

  /**
   * Predicates if the rule this class holds matches a given class name.
   *
   * @param className class name
   * @return true if the rule matches.
   */
  default boolean matches(String className) {
    if ((getClassName() == null || getClassName().isEmpty())
        && (getClassNames() == null || getClassNames().isEmpty())) {
      // global settings
      return true;
    }

    var singleTarget = getClassName();
    if (singleTarget != null) {
      var matched = singleTarget.equals(className) || className.matches(singleTarget);
      if (matched) {
        return true;
      }
    }

    var targets = getClassNames();
    if (targets != null && targets.isEmpty() == false) {
      var matched = targets.contains(className);
      if (matched) {
        return true;
      } else {
        for (String target : targets) {
          if (className.matches(target)) {
            return true;
          }
        }
      }
    }

    return false;
  }

}
