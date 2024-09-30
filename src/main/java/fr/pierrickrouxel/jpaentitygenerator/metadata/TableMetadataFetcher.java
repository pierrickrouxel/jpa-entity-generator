package fr.pierrickrouxel.jpaentitygenerator.metadata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.pierrickrouxel.jpaentitygenerator.config.JdbcSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetches metadata for all tables in a given database.
 */
@Slf4j
@RequiredArgsConstructor
public class TableMetaDataFetcher {

    private static final String[] TABLE_TYPES = new String[]{"TABLE", "VIEW"};

    private final JdbcSettings jdbcSettings;

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcSettings.getUrl(), jdbcSettings.getUsername(), jdbcSettings.getPassword());
    }

    public List<String> getTableNames() throws SQLException {
        var tableNames = new ArrayList<String>();
        try (var connection = getConnection()) {
            try (var rs = connection.getMetaData().getTables(null, jdbcSettings.getSchemaPattern(), "%", TABLE_TYPES)) {
                while (rs.next()) {
                    tableNames.add(rs.getString("TABLE_NAME"));
                }
            }
        }
        return tableNames;
    }

    public Table getTable(String tableName) throws SQLException {

        try (var connection = getConnection()) {
            var remarks = getRemarks(connection, null, tableName);

            var primaryKeyNames = getPrimaryKeyNames(connection, null, tableName);

            var columns = getColumns(connection, null, tableName, primaryKeyNames);

            var importedKeys = getImportedKeys(connection, null, tableName);
            var exportedKeys = getExportedKeys(connection, null, tableName);

            return Table.builder()
                    .name(tableName)
                    .remarks(remarks)
                    .importedKeys(importedKeys)
                    .exportedKeys(exportedKeys)
                    .columns(columns)
                    .build();
        }
    }

    private String getRemarks(Connection connection, String schemaName, String tableName) throws SQLException {
        try (var rs = connection.getMetaData().getTables(null, schemaName, tableName, TABLE_TYPES)) {
            if (rs.next()) {
                return rs.getString("REMARKS");
            }
        }
        return null;
    }

    private List<String> getPrimaryKeyNames(Connection connection, String schemaName, String tableName) throws SQLException {
        var primaryKeyNames = new ArrayList<String>();
        try (var rs = connection.getMetaData().getPrimaryKeys(null, schemaName, tableName)) {
            while (rs.next()) {
                primaryKeyNames.add(rs.getString("COLUMN_NAME"));
            }
        }
        return primaryKeyNames;
    }

    private List<Column> getColumns(Connection connection, String schemaName, String tableName, List<String> primaryKeyNames) throws SQLException {
        var columns = new ArrayList<Column>();
        try (var rs = connection.getMetaData().getColumns(null, schemaName, tableName, "%")) {
            while (rs.next()) {
                var columnName = rs.getString("COLUMN_NAME");

                var column = Column.builder()
                        .name(columnName)
                        .typeCode(rs.getInt("DATA_TYPE"))
                        .typeName(rs.getString("TYPE_NAME"))
                        .columnSize(rs.getInt("COLUMN_SIZE"))
                        .decimalDigits(rs.getInt("DECIMAL_DIGITS"))
                        .remarks(rs.getString("REMARKS"))
                        .autoIncrement(getBooleanResult(rs, tableName, "IS_AUTOINCREMENT"))
                        .nullable(getBooleanResult(rs, tableName, "IS_NULLABLE"))
                        .primaryKey(primaryKeyNames.stream().anyMatch(pk -> pk.equals(columnName)))
                        .build();

                columns.add(column);
            }
        }
        return columns;
    }

    private List<Key> getImportedKeys(Connection connection, String schemaName, String tableName) throws SQLException {
        var keys = new ArrayList<Key>();
        try (var rs = connection.getMetaData().getImportedKeys(null, schemaName, tableName)) {
            while (rs.next()) {
                var key = getKey(rs);
                keys.add(key);
            }
        }
        return keys;
    }

    private List<Key> getExportedKeys(Connection connection, String schemaName, String tableName) throws SQLException {
        var keys = new ArrayList<Key>();
        try (var rs = connection.getMetaData().getExportedKeys(null, schemaName, tableName)) {
            while (rs.next()) {
                var key = getKey(rs);
                keys.add(key);
            }
        }
        return keys;
    }

    private Key getKey(ResultSet rs) throws SQLException {
        return Key.builder()
                .primaryKeyName(rs.getString("PK_NAME"))
                .primaryKeyTableName(rs.getString("PKTABLE_NAME"))
                .primaryKeyColumnName(rs.getString("PKCOLUMN_NAME"))
                .foreignKeyName(rs.getString("FK_NAME"))
                .foreignKeyTableName(rs.getString("FKTABLE_NAME"))
                .foreignKeyColumnName(rs.getString("FKCOLUMN_NAME"))
                .build();
    }

    private boolean getBooleanResult(ResultSet rs, String tableName, String columnName) {
        try {
            var isNullableMetadata = rs.getString(columnName);
            return isNullableMetadata == null || isNullableMetadata.equals("YES");
        } catch (SQLException e) {
            log.debug("Failed to fetch nullable flag for {}.{}", tableName, columnName, e);
        }
        return false;
    }
}
