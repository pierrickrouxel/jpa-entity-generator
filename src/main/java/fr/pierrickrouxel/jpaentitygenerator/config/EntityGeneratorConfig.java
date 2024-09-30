package fr.pierrickrouxel.jpaentitygenerator.config;

import java.io.IOException;
import java.io.InputStreamReader;
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
import fr.pierrickrouxel.jpaentitygenerator.util.ResourceReader;
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

    static boolean hasEnvVariables(String value) {
        return value != null && value.contains("${");
    }

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

    private JdbcSettings jdbcSettings;

    @Builder.Default
    private List<TableScanRule> tableScanRules = new ArrayList<>();
    @Builder.Default
    private List<TableExclusionRule> tableExclusionRules = new ArrayList<>();

    @Builder.Default
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Possible values: TABLE, SEQUENCE, IDENTITY, AUTO
    // If you don't need to specify the `strategy`, set null value.
    private String generatedValueStrategy = "IDENTITY";

    @Builder.Default
    private boolean generateRelationships = false;

    @Builder.Default
    private String outputDirectory = "src/main/java";
    @Builder.Default
    private String packageName = "fr.example";

    @Builder.Default
    private List<ClassNameRule> classNameRules = new ArrayList<>();
    @Builder.Default
    private List<ClassAnnotationRule> classAnnotationRules = new ArrayList<>();
    @Builder.Default
    private List<InterfaceRule> interfaceRules = new ArrayList<>();
    @Builder.Default
    private List<ClassAdditionalCommentRule> classAdditionalCommentRules = new ArrayList<>();

    @Builder.Default
    private List<FieldTypeRule> fieldTypeRules = new ArrayList<>();
    @Builder.Default
    private List<FieldAnnotationRule> fieldAnnotationRules = new ArrayList<>();
    @Builder.Default
    private List<FieldDefaultValueRule> fieldDefaultValueRules = new ArrayList<>();
    @Builder.Default
    private List<FieldAdditionalCommentRule> fieldAdditionalCommentRules = new ArrayList<>();

    private static final Yaml YAML = new Yaml();

    public static EntityGeneratorConfig load(String path) throws IOException {
        return load(path, new HashMap<>());
    }

    public static EntityGeneratorConfig load(String path, Map<String, String> environment) throws IOException {
        try (var is = ResourceReader.getResourceAsStream(path)) {
            try (var reader = new InputStreamReader(is)) {
                var config = YAML.loadAs(reader, EntityGeneratorConfig.class);
                config.loadEnvVariables(environment);
                return config;
            }
        }
    }

}
