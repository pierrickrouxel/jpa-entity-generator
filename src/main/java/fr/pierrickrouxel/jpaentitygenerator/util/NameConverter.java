package fr.pierrickrouxel.jpaentitygenerator.util;

import fr.pierrickrouxel.jpaentitygenerator.rule.ClassNameRule;

import java.util.List;

import org.apache.commons.text.CaseUtils;

/**
 * Utility about name conversions between table/column names and class/field
 * names.
 */
public class NameConverter {

    private NameConverter() {
    }

    public static String toClassName(String tableName, List<ClassNameRule> rules) {
        for (ClassNameRule rule : rules) {
            if (rule.getTableName().equals(tableName)) {
                return rule.getClassName();
            }
        }
        return CaseUtils.toCamelCase(tableName, true, '_');
    }

    public static String toFieldName(String tableName) {
        return CaseUtils.toCamelCase(tableName, false, '_');
    }
}
