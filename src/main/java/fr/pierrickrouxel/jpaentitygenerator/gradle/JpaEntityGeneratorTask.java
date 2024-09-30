
package fr.pierrickrouxel.jpaentitygenerator.gradle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import fr.pierrickrouxel.jpaentitygenerator.EntityGenerator;
import fr.pierrickrouxel.jpaentitygenerator.config.EntityGeneratorConfig;
import fr.pierrickrouxel.jpaentitygenerator.metadata.TableMetaDataFetcher;
import fr.pierrickrouxel.jpaentitygenerator.rule.TableExclusionRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.TableScanRule;
import fr.pierrickrouxel.jpaentitygenerator.util.NameConverter;

/**
 * entityGen Gradle task.
 */

public class JpaEntityGeneratorTask extends DefaultTask {

    @TaskAction
    public void generateAll() throws IOException, SQLException {
        var extension = getProject().getExtensions().getByType(JpaEntityGeneratorExtension.class);
        if (extension == null) {
            extension = new JpaEntityGeneratorExtension();
        }
        var config = EntityGeneratorConfig.load(extension.getConfigPath(), extension.getEnvironment());

        generateAll(config);
    }

    public void generateAll(EntityGeneratorConfig config) throws SQLException, IOException {
        var directory = Paths.get(config.getOutputDirectory(), config.getPackageName().split("\\."));
        Files.createDirectories(directory);

        var metaDataFetcher = new TableMetaDataFetcher(config.getJdbcSettings());
        var allTableNames = metaDataFetcher.getTableNames();
        var tableNames = filterTableNames(config, allTableNames);

        for (var tableName : tableNames) {
          generateEntity(tableName, metaDataFetcher, directory, config);
        }
    }

    private void generateEntity(String tableName, TableMetaDataFetcher metaDataFetcher, Path directory, EntityGeneratorConfig config) throws SQLException, IOException {
      var table = metaDataFetcher.getTable(tableName);
      var entitySource = EntityGenerator.getEntity(table, config);
      var filePath = directory.resolve(NameConverter.toClassName(tableName, config.getClassNameRules()) + ".java");
      Files.writeString(filePath, entitySource);
    }

    private List<String> filterTableNames(EntityGeneratorConfig config, List<String> allTableNames) {
        var scannedTables = scanTableNames(allTableNames, config.getTableScanRules());
        return excludeTableNames(scannedTables, config.getTableExclusionRules());
    }

    private List<String> scanTableNames(List<String> allTableNames, List<TableScanRule> tableScanRules) {
        if (tableScanRules.isEmpty()) {
            return allTableNames;
        }

        return allTableNames.stream()
                .filter(o -> tableScanRules.stream()
                .anyMatch(o2 -> o2.matches(o))
                )
                .collect(Collectors.toList());
    }

    private List<String> excludeTableNames(List<String> scannedTableNames, List<TableExclusionRule> tableExclusionRules) {
      return scannedTableNames.stream()
        .filter(o -> tableExclusionRules.stream()
        .noneMatch(o2 -> o2.matches(o))
        )
        .collect(Collectors.toList());
    }
}
