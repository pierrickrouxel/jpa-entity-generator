package fr.pierrickrouxel.jpaentitygenerator.rule;

import org.apache.commons.lang3.StringUtils;

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
        if (StringUtils.isEmpty(getTableName()) && (getTableNames() == null || getTableNames().size() == 0)) {
            // global settings
            return true;
        }

        String singleTarget = getTableName();
        if (singleTarget != null) {
            boolean matched = singleTarget.equals(tableName) || tableName.matches(singleTarget);
            if (matched) {
                return true;
            }
        }

        List<String> targets = getTableNames();
        if (targets != null && targets.isEmpty() == false) {
            boolean matched = targets.contains(tableName);
            if (matched) {
                return true;
            } else {
                for (String target : targets) {
                    if (tableName.matches(target)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
