package fr.pierrickrouxel.jpaentitygenerator.rule;

import java.util.List;

/**
 * Provides #matches method to check if this rule matches a given table name.
 */
public interface TableMatcher {

    /**
     * Returns a single partial-matching rule.
     */
    String getTableName();

    /**
     * Returns multiple partial-matching rules.
     */
    List<String> getTableNames();

    /**
     * Predicates if the rule this class holds matches a given table name.
     * @param tableName table name
     * @return true if the rule matches.
     */
    default boolean matches(String tableName) {
        if ((getTableName() == null || getTableName().isEmpty()) && (getTableNames() == null || getTableNames().isEmpty())) {
            // global settings
            return true;
        }

        var singleTarget = getTableName();
        if (singleTarget != null) {
            var matched = singleTarget.equals(tableName) || tableName.matches(singleTarget);
            if (matched) {
                return true;
            }
        }

        var targets = getTableNames();
        if (targets != null && targets.isEmpty() == false) {
            var matched = targets.contains(tableName);
            if (matched) {
                return true;
            } else {
                for (var target : targets) {
                    if (tableName.matches(target)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
