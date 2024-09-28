package fr.pierrickrouxel.jpaentitygenerator.metadata;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.pierrickrouxel.jpaentitygenerator.TestDatabase;

public class TableMetaDataFetcherTest {

    private final TableMetaDataFetcher fetcher = new TableMetaDataFetcher(TestDatabase.jdbcSettings);

    @BeforeAll
    public static void setupDatabase() throws Exception {
        TestDatabase.init();
    }

    @Test
    public void getTableNamesTest() throws SQLException {
        var tableNames = fetcher.getTableNames();
        assertThat(tableNames).hasSameElementsAs(List.of("BLOG", "ARTICLE", "TAG", "ARTICLE_TAG", "ABTEST", "SOMETHING_TMP"));
    }

    @Test
    public void getTableTest() throws SQLException {
        var table = fetcher.getTable("PUBLIC", "ARTICLE");
        assertThat(table).isNotNull();
    }

    @Test
    public void getTableColumnsTest() throws SQLException {
        var table = fetcher.getTable("PUBLIC", "ARTICLE");
        assertThat(table.getColumns().stream().map(o -> o.getName())).containsExactly("ID", "BLOG_ID", "NAME", "TAGS", "CREATED_AT");
    }

    @Test
    public void getTablePrimaryKeyTest() throws SQLException {
        assertThat(getColumn("ARTICLE", "ID").map(Column::isPrimaryKey)).hasValue(true);
        assertThat(getColumn("ARTICLE", "NAME").map(Column::isPrimaryKey)).hasValue(false);
    }

    @Test
    public void getTableAutoIncrementTest() throws SQLException {
        assertThat(getColumn("ARTICLE", "ID").map(Column::isAutoIncrement)).hasValue(true);
        assertThat(getColumn("ARTICLE", "NAME").map(Column::isAutoIncrement)).hasValue(false);
    }

    @Test
    public void getTableIsNullableTest() throws SQLException {
        assertThat(getColumn("ARTICLE", "ID").map(Column::isNullable)).hasValue(false);
        assertThat(getColumn("ARTICLE", "NAME").map(Column::isNullable)).hasValue(true);
    }

    @Test
    public void getTableColumnSizeTest() throws SQLException {
        assertThat(getColumn("ARTICLE", "NAME").map(Column::getColumnSize)).hasValue(30);
    }

    @Test
    public void getTableDecimalDigitsTest() throws SQLException {
        assertThat(getColumn("TAG", "AVERAGE").map(Column::getColumnSize)).hasValue(9);
        assertThat(getColumn("TAG", "AVERAGE").map(Column::getDecimalDigits)).hasValue(2);
    }

    @Test
    public void getTableRemarksTest() throws SQLException {
        assertThat(getColumn("ARTICLE", "BLOG_ID").map(Column::getRemarks)).hasValue("database comment for blog_id");
    }

    @Test
    public void getTableTypeTest() throws SQLException {
        assertThat(getColumn("ARTICLE", "NAME").map(Column::getTypeCode)).hasValue(12);
        assertThat(getColumn("ARTICLE", "NAME").map(Column::getTypeName)).hasValue("CHARACTER VARYING");
    }

    @Test
    public void getTableExportedKeyTest() throws SQLException {
        var table = fetcher.getTable("PUBLIC", "ARTICLE");
        assertThat(table.getExportedKeys()).hasSize(1);
        assertThat(table.getExportedKeys().getFirst().getPrimaryKeyTableName()).isEqualTo("ARTICLE");
        assertThat(table.getExportedKeys().getFirst().getPrimaryKeyColumnName()).isEqualTo("ID");
        assertThat(table.getExportedKeys().getFirst().getForeignKeyTableName()).isEqualTo("ARTICLE_TAG");
        assertThat(table.getExportedKeys().getFirst().getForeignKeyColumnName()).isEqualTo("ARTICLE_ID");
    }

    @Test
    public void getTableImportedKeyTest() throws SQLException {
        var table = fetcher.getTable("PUBLIC", "ARTICLE");
        assertThat(table.getImportedKeys()).hasSize(1);
        assertThat(table.getImportedKeys().getFirst().getPrimaryKeyTableName()).isEqualTo("BLOG");
        assertThat(table.getImportedKeys().getFirst().getPrimaryKeyColumnName()).isEqualTo("ID");
        assertThat(table.getImportedKeys().getFirst().getForeignKeyTableName()).isEqualTo("ARTICLE");
        assertThat(table.getImportedKeys().getFirst().getForeignKeyColumnName()).isEqualTo("BLOG_ID");
    }

    private Optional<Column> getColumn(String tableName, String columnName) throws SQLException {
        var table = fetcher.getTable("PUBLIC", tableName);
        return table.getColumns().stream().filter(o -> o.getName().equals(columnName)).findFirst();
    }
}
