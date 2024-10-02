package fr.pierrickrouxel.jpaentitygenerator.util;

import java.sql.Types;

/**
 * Utility to convert SQL types to Java types.
 */
public class TypeConverter {

  private TypeConverter() {
  }

  /**
   * Get java type from SQL type code.
   *
   * @param typeCode The SQL type code
   * @return The java type
   */
  public static String toJavaType(int typeCode) {
    return switch (typeCode) {
      case Types.ARRAY -> "Array";
      case Types.BIGINT -> "Long";
      case Types.BIT -> "boolean";
      case Types.BLOB -> "Blob";
      case Types.BOOLEAN -> "Boolean";
      case Types.CHAR -> "String";
      case Types.CLOB -> "Clob";
      case Types.DATE -> "Date";
      case Types.DECIMAL -> "java.math.BigDecimal";
      case Types.DOUBLE -> "Double";
      case Types.FLOAT -> "Float";
      case Types.INTEGER -> "Integer";
      case Types.LONGVARCHAR -> "String";
      case Types.NUMERIC -> "java.math.BigDecimal";
      case Types.REAL -> "Float";
      case Types.REF -> "java.sql.Ref";
      case Types.SMALLINT -> "Short";
      case Types.STRUCT -> "Struct";
      case Types.TIME -> "Time";
      case Types.TIME_WITH_TIMEZONE -> "Time";
      case Types.TIMESTAMP -> "java.sql.Timestamp";
      case Types.TIMESTAMP_WITH_TIMEZONE -> "java.sql.Timestamp";
      case Types.TINYINT -> "Byte";
      case Types.VARCHAR -> "String";
      default -> "String";
    };
    // case Types.BINARY:
    // case Types.DATALINK:
    // case Types.DISTINCT:
    // case Types.JAVA_OBJECT:
    // case Types.LONGNVARCHAR:
    // case Types.LONGVARBINARY:
    // case Types.NCHAR:
    // case Types.NCLOB:
    // case Types.NULL:
    // case Types.NVARCHAR:
    // case Types.OTHER:
    // case Types.REF_CURSOR:
    // case Types.ROWID:
    // case Types.SQLXML:
    // case Types.VARBINARY:
  }
}
