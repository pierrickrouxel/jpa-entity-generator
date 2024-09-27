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

    public Table getTable(String schemaName, String tableName, boolean generateRelationships) throws SQLException {

        var table = new Table();

        table.setName(tableName);
        try (var connection = getConnection()) {
            var description = getDescription(connection, schemaName, tableName);
            table.setDescription(description);

            var primaryKeyNames = getPrimaryKeyNames(connection, schemaName, tableName);

            if (generateRelationships) {
                try (var importedKeys = connection.getMetaData().getImportedKeys(null, jdbcSettings.getSchemaPattern(), tableName)) {
                    while (importedKeys.next()) {
                        var name = importedKeys.getString("FK_NAME");
                        var fk = new ForeignKey();
                        fk.setColumnName(importedKeys.getString("FKCOLUMN_NAME"));
                        fk.setPkTable(importedKeys.getString("PKTABLE_NAME"));
                        fk.setPkColumnName(importedKeys.getString("PKCOLUMN_NAME"));
                        if (table.getForeignKeyMap().containsKey(name)) {
                            var fkList = new ArrayList<ForeignKey>();
                            fkList.add(table.getForeignKeyMap().get(name));
                            fkList.add(fk);
                            table.getForeignCompositeKeyMap().put(name, fkList);
                            table.getForeignKeyMap().remove(name);
                        } else if (table.getForeignCompositeKeyMap().containsKey(name)) {
                            table.getForeignCompositeKeyMap().get(name).add(fk);
                        } else {
                            table.getForeignKeyMap().put(name, fk);
                        }
                    }
                }
            }

            var columns = getColumns(connection, schemaName, tableName, primaryKeyNames);
            table.setColumns(columns);

            return table;

        }
    }

    private String getDescription(Connection connection, String schemaName, String tableName) throws SQLException {
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
                        .description(rs.getString("REMARKS"))
                        .autoIncrement(getBooleanResult(rs, tableName, "IS_AUTOINCREMENT"))
                        .nullable(getBooleanResult(rs, tableName, "IS_NULLABLE"))
                        .primaryKey(primaryKeyNames.stream().anyMatch(pk -> pk.equals(columnName)))
                        .build();

                columns.add(column);
            }
        }
        return columns;
    }

    private boolean getBooleanResult(ResultSet rs, String tableName, String columnName) {
        try {
            var isNullableMetadata = rs.getString("IS_NULLABLE");
            return isNullableMetadata == null || isNullableMetadata.equals("YES");
        } catch (SQLException e) {
            log.debug("Failed to fetch nullable flag for {}.{}", tableName, columnName, e);
        }
        return false;
    }

}
