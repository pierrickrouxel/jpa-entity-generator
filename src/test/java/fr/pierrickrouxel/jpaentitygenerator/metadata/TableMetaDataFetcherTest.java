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
  public static void setupDatabase() throws SQLException {
    TestDatabase.init();
  }

  @Test
  public void testGetTableNames() throws SQLException {
    var tableNames = fetcher.getTableNames();
    assertThat(tableNames).hasSameElementsAs(List.of("BLOG", "ARTICLE", "TAG", "ARTICLE_TAG", "SOMETHING_TMP", "SOMETHING2_TMP"));
  }

  @Test
  public void testGetTable() throws SQLException {
    var table = fetcher.getTable("ARTICLE");
    assertThat(table).isNotNull();
  }

  @Test
  public void testGetTableColumns() throws SQLException {
    var table = fetcher.getTable("ARTICLE");
    assertThat(table.getColumns().stream().map(o -> o.getName())).containsExactly("ID", "BLOG_ID", "NAME", "TAGS",
        "CREATED_AT");
  }

  @Test
  public void testGetTablePrimaryKey() throws SQLException {
    assertThat(getColumn("ARTICLE", "ID").map(Column::isPrimaryKey)).hasValue(true);
    assertThat(getColumn("ARTICLE", "NAME").map(Column::isPrimaryKey)).hasValue(false);
  }

  @Test
  public void testGetTableAutoIncrement() throws SQLException {
    assertThat(getColumn("ARTICLE", "ID").map(Column::isAutoIncrement)).hasValue(true);
    assertThat(getColumn("ARTICLE", "NAME").map(Column::isAutoIncrement)).hasValue(false);
  }

  @Test
  public void testGetTableIsNullable() throws SQLException {
    assertThat(getColumn("ARTICLE", "ID").map(Column::isNullable)).hasValue(false);
    assertThat(getColumn("ARTICLE", "NAME").map(Column::isNullable)).hasValue(true);
  }

  @Test
  public void testGetTableColumnSize() throws SQLException {
    assertThat(getColumn("ARTICLE", "NAME").map(Column::getColumnSize)).hasValue(30);
  }

  @Test
  public void testGetTableDecimalDigits() throws SQLException {
    assertThat(getColumn("TAG", "AVERAGE").map(Column::getColumnSize)).hasValue(9);
    assertThat(getColumn("TAG", "AVERAGE").map(Column::getDecimalDigits)).hasValue(2);
  }

  @Test
  public void testGetTableRemarks() throws SQLException {
    assertThat(getColumn("ARTICLE", "BLOG_ID").map(Column::getRemarks)).hasValue("database comment for blog_id");
  }

  @Test
  public void testGetTableType() throws SQLException {
    assertThat(getColumn("ARTICLE", "NAME").map(Column::getTypeCode)).hasValue(12);
    assertThat(getColumn("ARTICLE", "NAME").map(Column::getTypeName)).hasValue("CHARACTER VARYING");
  }

  @Test
  public void testGetTableExportedKeys() throws SQLException {
    var table = fetcher.getTable("ARTICLE");
    assertThat(table.getExportedKeys()).hasSize(1);
    assertThat(table.getExportedKeys().getFirst().getPrimaryKeyTableName()).isEqualTo("ARTICLE");
    assertThat(table.getExportedKeys().getFirst().getPrimaryKeyColumnName()).isEqualTo("ID");
    assertThat(table.getExportedKeys().getFirst().getForeignKeyTableName()).isEqualTo("ARTICLE_TAG");
    assertThat(table.getExportedKeys().getFirst().getForeignKeyColumnName()).isEqualTo("ARTICLE_ID");
  }

  @Test
  public void testGetTableImportedKeys() throws SQLException {
    var table = fetcher.getTable("ARTICLE");
    assertThat(table.getImportedKeys()).hasSize(1);
    assertThat(table.getImportedKeys().getFirst().getPrimaryKeyTableName()).isEqualTo("BLOG");
    assertThat(table.getImportedKeys().getFirst().getPrimaryKeyColumnName()).isEqualTo("ID");
    assertThat(table.getImportedKeys().getFirst().getForeignKeyTableName()).isEqualTo("ARTICLE");
    assertThat(table.getImportedKeys().getFirst().getForeignKeyColumnName()).isEqualTo("BLOG_ID");
  }

  @Test
  public void testGetTableExportedKeysComposite() throws SQLException {
    var table = fetcher.getTable("SOMETHING_TMP");
    assertThat(table.getExportedKeys()).hasSize(2);
    assertThat(table.getExportedKeys()).allMatch(o -> o.getPrimaryKeyTableName().equals("SOMETHING_TMP"));
    assertThat(table.getExportedKeys()).anyMatch(o -> o.getPrimaryKeyColumnName().equals("IDENTIFIER"));
    assertThat(table.getExportedKeys()).anyMatch(o -> o.getPrimaryKeyColumnName().equals("EXPIRATION_TIMESTAMP"));
  }

  @Test
  public void testGetTableImportedKeysComposite() throws SQLException {
    var table = fetcher.getTable("SOMETHING2_TMP");
    assertThat(table.getImportedKeys()).hasSize(2);
    assertThat(table.getImportedKeys()).allMatch(o -> o.getPrimaryKeyTableName().equals("SOMETHING_TMP"));
    assertThat(table.getImportedKeys()).anyMatch(o -> o.getPrimaryKeyColumnName().equals("IDENTIFIER"));
    assertThat(table.getImportedKeys()).anyMatch(o -> o.getPrimaryKeyColumnName().equals("EXPIRATION_TIMESTAMP"));
  }

  @Test
  public void testGetTableIndexes() throws SQLException {
    var table = fetcher.getTable("ARTICLE");
    assertThat(table.getIndexes()).hasSize(2);
    assertThat(table.getIndexes().stream().filter(o -> !o.isNonUnique())).hasSize(1);
  }

  private Optional<Column> getColumn(String tableName, String columnName) throws SQLException {
    var table = fetcher.getTable(tableName);
    return table.getColumns().stream().filter(o -> o.getName().equals(columnName)).findFirst();
  }
}
