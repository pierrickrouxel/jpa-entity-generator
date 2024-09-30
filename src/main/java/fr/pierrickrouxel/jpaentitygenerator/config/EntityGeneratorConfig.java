package fr.pierrickrouxel.jpaentitygenerator.config;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import fr.pierrickrouxel.jpaentitygenerator.rule.ClassAdditionalCommentRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.ClassAnnotationRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.ClassNameRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldAdditionalCommentRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldAnnotationRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldDefaultValueRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldTypeRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.InterfaceRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.TableExclusionRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.TableScanRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Code generator's configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityGeneratorConfig implements Serializable {

  /**
   * The settings for JDBC connection.
   */
  private JdbcSettings jdbcSettings;

  /**
   * The rules for table scan.
   */
  @Builder.Default
  private List<TableScanRule> tableScanRules = new ArrayList<>();

  /**
   * The rules for table exclusions.
   */
  @Builder.Default
  private List<TableExclusionRule> tableExclusionRules = new ArrayList<>();

  /**
   * GeneratedValue(strategy = GenerationType.IDENTITY)
   * Possible values: TABLE, SEQUENCE, IDENTITY, AUTO
   * If you don't need to specify the `strategy`, set null value.
   */
  @Builder.Default
  private String generatedValueStrategy = "IDENTITY";

  /**
   * The output directory for class generation.
   */
  @Builder.Default
  private String outputDirectory = "src/main/java";

  /**
   * The package name for class generation.
   */
  @Builder.Default
  private String packageName = "fr.example";

  /**
   * The class name rules.
   */
  @Builder.Default
  private List<ClassNameRule> classNameRules = new ArrayList<>();

  /**
   * The class annotations rules.
   */
  @Builder.Default
  private List<ClassAnnotationRule> classAnnotationRules = new ArrayList<>();

  /**
   * The interface rules.
   */
  @Builder.Default
  private List<InterfaceRule> interfaceRules = new ArrayList<>();

  /**
   * The additionnal comment rules for classes.
   */
  @Builder.Default
  private List<ClassAdditionalCommentRule> classAdditionalCommentRules = new ArrayList<>();

  /**
   * The field type rules.
   */
  @Builder.Default
  private List<FieldTypeRule> fieldTypeRules = new ArrayList<>();

  /**
   * The field annotation rules.
   */
  @Builder.Default
  private List<FieldAnnotationRule> fieldAnnotationRules = new ArrayList<>();

  /**
   * The field default values rules.
   */
  @Builder.Default
  private List<FieldDefaultValueRule> fieldDefaultValueRules = new ArrayList<>();

  /**
   * The field additional comment rules.
   */
  @Builder.Default
  private List<FieldAdditionalCommentRule> fieldAdditionalCommentRules = new ArrayList<>();

  /**
   * Load configuration replacing environment variables.
   *
   * @param path        The config file path
   * @param environment The environment variables
   * @return The config
   * @throws IOException
   */
  public static EntityGeneratorConfig load(String path, Map<String, String> environment) throws IOException {
    var yaml = new Yaml();
    try (var inputStream = EntityGeneratorConfig.class.getResourceAsStream(path)) {
      var config = yaml.loadAs(inputStream, EntityGeneratorConfig.class);
      config.loadEnvVariables(environment);
      return config;
    }
  }

  /**
   * Load jdbc settings from environment variables.
   *
   * @param environment The environment variables
   */
  public void loadEnvVariables(Map<String, String> environment) {
    // JDBC settings
    var settings = getJdbcSettings();
    if (hasEnvVariables(settings.getUrl())) {
      settings.setUrl(replaceEnvVariables(settings.getUrl(), environment));
    }
    if (hasEnvVariables(settings.getUsername())) {
      settings.setUsername(replaceEnvVariables(settings.getUsername(), environment));
    }
    if (hasEnvVariables(settings.getPassword())) {
      settings.setPassword(replaceEnvVariables(settings.getPassword(), environment));
    }
    if (hasEnvVariables(settings.getDriverClassName())) {
      settings.setDriverClassName(replaceEnvVariables(settings.getDriverClassName(), environment));
    }
  }

  /**
   * Check if value contains environment variable.
   *
   * @param value The value
   * @return `true` if enviroment variable is found
   */
  static boolean hasEnvVariables(String value) {
    return value != null && value.contains("${");
  }

  /**
   * Replace environment variable by its value.
   *
   * @param value       The value
   * @param environment The enviroment variables
   * @return The new value
   */
  static String replaceEnvVariables(String value, Map<String, String> environment) {
    var envMap = new HashMap<String, String>(environment);
    envMap.putAll(System.getenv());
    var text = value;

    for (Map.Entry<String, String> entry : envMap.entrySet()) {
      var k = entry.getKey();
      var v = entry.getValue();
      text = text.replaceAll("\\$\\{" + k + "}", v);
    }
    return text;
  }
}
