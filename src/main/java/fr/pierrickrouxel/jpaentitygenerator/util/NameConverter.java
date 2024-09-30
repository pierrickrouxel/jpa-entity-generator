package fr.pierrickrouxel.jpaentitygenerator.util;

import java.util.List;

import org.apache.commons.text.CaseUtils;
import org.atteo.evo.inflector.English;

import fr.pierrickrouxel.jpaentitygenerator.rule.ClassNameRule;

/**
 * Utility about name conversions between table/column names and class/field
 * names.
 */
public class NameConverter {

  private NameConverter() {
  }

  /**
   * Get class name from table name.
   *
   * @param tableName The table name
   * @param rules     The class name rules
   * @return The class name
   */
  public static String toClassName(String tableName, List<ClassNameRule> rules) {
    for (var rule : rules) {
      if (rule.getTableName().equals(tableName)) {
        return rule.getClassName();
      }
    }
    return CaseUtils.toCamelCase(tableName, true, '_');
  }

  /**
   * Get field name from table name.
   *
   * @param tableName The table name
   * @return The field name
   */
  public static String toFieldName(String tableName) {
    return CaseUtils.toCamelCase(tableName, false, '_');
  }

  /**
   * Get list field name from table name.
   *
   * @param tableName The table name
   * @return The list field name
   */
  public static String toListFieldName(String tableName) {
    return toFieldName(English.plural(tableName));
  }
}
