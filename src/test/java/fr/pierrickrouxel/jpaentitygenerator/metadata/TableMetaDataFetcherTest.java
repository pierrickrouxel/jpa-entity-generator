package fr.pierrickrouxel.jpaentitygenerator.metadata;

import java.sql.SQLException;
import java.util.List;

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
}
