package fr.pierrickrouxel.jpaentitygenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import fr.pierrickrouxel.jpaentitygenerator.config.CodeGeneratorConfig;
import fr.pierrickrouxel.jpaentitygenerator.metadata.Column;
import fr.pierrickrouxel.jpaentitygenerator.metadata.Table;
import fr.pierrickrouxel.jpaentitygenerator.metadata.TableMetaDataFetcher;
import fr.pierrickrouxel.jpaentitygenerator.rule.AdditionalCodePosition;
import fr.pierrickrouxel.jpaentitygenerator.rule.Annotation;
import fr.pierrickrouxel.jpaentitygenerator.rule.AnnotationAttribute;
import fr.pierrickrouxel.jpaentitygenerator.rule.ClassAdditionalCommentRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.ClassAnnotationRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldAdditionalCommentRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldDefaultValueRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldTypeRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.ImportRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.Interface;
import fr.pierrickrouxel.jpaentitygenerator.rule.TableScanRule;
import fr.pierrickrouxel.jpaentitygenerator.util.NameConverter;
import fr.pierrickrouxel.jpaentitygenerator.util.TypeConverter;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

/**
 * Lombok-wired JPA entity code generator.
 */
@Slf4j
public class CodeGenerator {

    private static final List<String> EXPECTED_ID_JAKARTA_ANNOTATION_CLASS_NAMES = Arrays.asList("Id", "jakarta.persistence.Id");

    private static final Predicate<CodeRenderer.RenderingData.Field> primaryKeyPredicate = (f) -> {
        boolean isPrimaryKey = f.isPrimaryKey();
        boolean hasIdAnnotation = f.getAnnotations().stream()
                .anyMatch(a -> EXPECTED_ID_JAKARTA_ANNOTATION_CLASS_NAMES.contains(a.getClassName()));
        return isPrimaryKey || hasIdAnnotation;
    };

    public static String generateSource(CodeGeneratorConfig originalConfig, Table table) throws IOException, TemplateException {
        CodeGeneratorConfig config = SerializationUtils.clone(originalConfig);
        CodeRenderer.RenderingData data = new CodeRenderer.RenderingData();
        data.setGenerateRelationshipsInsertable(config.isGenerateRelationshipsInsertable());
        data.setGenerateRelationshipsUpdatable(config.isGenerateRelationshipsUpdatable());

        data.setPackageName(config.getPackageName());

        String className = NameConverter.toClassName(table.getName(), config.getClassNameRules());
        data.setClassName(className);
        data.setTableName(table.getName());

        ClassAnnotationRule entityClassAnnotationRule = new ClassAnnotationRule();
        String entityClassName = "jakarta.persistence.Entity";
        Annotation entityAnnotation = Annotation.fromClassName(entityClassName);
        AnnotationAttribute entityAnnotationValueAttr = new AnnotationAttribute();
        entityAnnotationValueAttr.setName("name");
        entityAnnotationValueAttr.setValue("\"" + data.getPackageName() + "." + data.getClassName() + "\"");
        entityAnnotation.getAttributes().add(entityAnnotationValueAttr);
        entityClassAnnotationRule.setAnnotations(Arrays.asList(entityAnnotation));
        entityClassAnnotationRule.setClassName(className);
        config.getClassAnnotationRules().add(entityClassAnnotationRule);

        data.setClassComment(buildClassComment(className, table, config.getClassAdditionalCommentRules()));

        data.setImportRules(config.getImportRules().stream()
                .filter(r -> r.matches(className))
                .collect(toList()));

        List<CodeRenderer.RenderingData.Field> fields = table.getColumns().stream().map(c -> {
            CodeRenderer.RenderingData.Field f = new CodeRenderer.RenderingData.Field();

            String fieldName = NameConverter.toFieldName(c.getName());
            f.setName(fieldName);
            f.setColumnName(c.getName());
            f.setNullable(c.isNullable());

            f.setComment(buildFieldComment(className, f.getName(), c, config.getFieldAdditionalCommentRules()));

            f.setAnnotations(config.getFieldAnnotationRules().stream()
                    .filter(rule -> rule.matches(className, f.getName()))
                    .flatMap(rule -> rule.getAnnotations().stream())
                    .peek(a -> a.setClassName(collectAndConvertFQDN(a.getClassName(), data.getImportRules())))
                    .collect(toList()));

            Optional<FieldTypeRule> fieldTypeRule
                    = orEmptyListIfNull(config.getFieldTypeRules()).stream()
                            .filter(b -> b.matches(className, fieldName)).findFirst();
            if (fieldTypeRule.isPresent()) {
                f.setType(fieldTypeRule.get().getTypeName());
                f.setPrimitive(isPrimitive(f.getType()));
            } else {
                f.setType(TypeConverter.toJavaType(c.getTypeCode()));
                if (!c.isNullable() && config.isUsePrimitiveForNonNullField()) {
                    f.setType(TypeConverter.toPrimitiveTypeIfPossible(f.getType()));
                }
                f.setPrimitive(isPrimitive(f.getType()));
            }

            if ("String".equals(f.getType())) {
                f.setLength(c.getColumnSize());
            }

            if ("java.math.BigDecimal".equals(f.getType())) {
                f.setPrecision(c.getColumnSize());
                f.setScale(c.getDecimalDigits());
            }

            Optional<FieldDefaultValueRule> fieldDefaultValueRule
                    = orEmptyListIfNull(config.getFieldDefaultValueRules()).stream()
                            .filter(r -> r.matches(className, fieldName)).findFirst();
            if (fieldDefaultValueRule.isPresent()) {
                f.setDefaultValue(fieldDefaultValueRule.get().getDefaultValue());
            }
            if (StringUtils.isNotEmpty(config.getGeneratedValueStrategy())) {
                f.setGeneratedValueStrategy(config.getGeneratedValueStrategy());
            }

            f.setAutoIncrement(c.isAutoIncrement());
            f.setPrimaryKey(c.isPrimaryKey());
            return f;

        }).collect(toList());

        table.getExportedKeys().forEach((value) -> {
            var f = new CodeRenderer.RenderingData.ForeignCompositeKeyField();
            var jc = new CodeRenderer.RenderingData.JoinColumn();
            jc.setColumnName(value.getPrimaryKeyColumnName());
            jc.setReferencedColumnName(value.getForeignKeyColumnName());
            f.setJoinColumns(List.of(jc));
            f.setName(NameConverter.toFieldName(value.getForeignKeyTableName()));
            f.setType(NameConverter.toClassName(value.getForeignKeyTableName(), config.getClassNameRules()));
            data.getForeignCompositeKeyFields().add(f);
        });

        if (fields.stream().noneMatch(primaryKeyPredicate)) {
            throw new IllegalStateException("Entity class " + data.getClassName() + " has no @Id field!");
        }

        data.setFields(fields);
        data.setPrimaryKeyFields(fields.stream().filter(CodeRenderer.RenderingData.Field::isPrimaryKey).collect(toList()));

        data.setInterfaceNames(orEmptyListIfNull(config.getInterfaceRules()).stream()
                .filter(r -> r.matches(className))
                .peek(rule -> {
                    for (Interface i : rule.getInterfaces()) {
                        i.setName(collectAndConvertFQDN(i.getName(), data.getImportRules()));
                        i.setGenericsClassNames(i.getGenericsClassNames().stream()
                                .map(cn -> collectAndConvertFQDN(cn, data.getImportRules()))
                                .collect(toList()));
                    }
                })
                .flatMap(r -> r.getInterfaces().stream().map(i -> {
            String genericsPart = !i.getGenericsClassNames().isEmpty()
                    ? i.getGenericsClassNames().stream()
                            .map(n -> n.equals("{className}") ? className : n)
                            .collect(Collectors.joining(", ", "<", ">"))
                    : "";
            return i.getName() + genericsPart;
        }))
                .collect(toList()));

        data.setClassAnnotationRules(orEmptyListIfNull(config.getClassAnnotationRules()).stream()
                .filter(r -> r.matches(className))
                .peek(rule -> rule.getAnnotations().forEach(a -> {
            a.setClassName(collectAndConvertFQDN(a.getClassName(), data.getImportRules()));
        }))
                .collect(toList()));

        orEmptyListIfNull(config.getAdditionalCodeRules()).forEach(rule -> {
            if (rule.matches(className)) {
                String code = rule.getCode();

                if (code != null) {
                    StringJoiner joiner = new StringJoiner("\n  ", "  ", "");
                    for (String line : code.split("\\n")) {
                        joiner.add(line);
                    }
                    String optimizedCode = joiner.toString();
                    if (rule.getPosition() == AdditionalCodePosition.Top) {
                        data.getTopAdditionalCodeList().add(optimizedCode);
                    } else {
                        data.getBottomAdditionalCodeList().add(optimizedCode);
                    }
                }
            }
        });

        orEmptyListIfNull(data.getImportRules()).sort(Comparator.comparing(ImportRule::getImportValue));

        return CodeRenderer.render("entityGen/entity.ftl", data);
    }

    public static void generateOne(CodeGeneratorConfig config, Table table) throws IOException, TemplateException {
        String code = generateSource(config, table);
        String className = NameConverter.toClassName(table.getName(), config.getClassNameRules());

        String filepath = config.getOutputDirectory() + "/" + config.getPackageName().replaceAll("\\.", "/") + "/" + className + ".java";
        Path path = Paths.get(filepath);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        Files.write(path, code.getBytes());

        log.debug("path: {}, code: {}", path, code);
    }

    /**
     * Generates all entities from existing tables.
     */
    public static void generateAll(CodeGeneratorConfig originalConfig) throws SQLException, IOException, TemplateException {
        Path dir = Paths.get(originalConfig.getOutputDirectory() + "/"
                + originalConfig.getPackageName().replaceAll("\\.", "/"));
        Files.createDirectories(dir);

        TableMetaDataFetcher metaDataFetcher = new TableMetaDataFetcher(originalConfig.getJdbcSettings());
        List<String> allTableNames = metaDataFetcher.getTableNames();
        List<String> tableNames = filterTableNames(originalConfig, allTableNames);
        List<Table> tables = new ArrayList<>();
        for (String tableName : tableNames) {
            boolean shouldExclude = originalConfig.getTableExclusionRules().stream().anyMatch(rule -> rule.matches(tableName));
            if (shouldExclude) {
                log.debug("Skipped to generate entity for {}", tableName);
                continue;
            }
            tables.add(metaDataFetcher.getTable(null, tableName));
        }
        for (Table table : tables) {
            generateOne(originalConfig, table);
        }
    }

    private static List<String> filterTableNames(CodeGeneratorConfig config, List<String> allTableNames) {
        String tableScanMode = config.getTableScanMode();
        if (tableScanMode == null) {
            return allTableNames;
        }
        String normalizedTableScanMode = tableScanMode.trim().toLowerCase(Locale.ENGLISH);
        switch (normalizedTableScanMode) {
            case "all" -> {
                return allTableNames;
            }
            case "rulebased" -> {
                List<String> filteredTableNames = new ArrayList<>();
                for (String tableName : allTableNames) {
                    boolean isScanTarget = true;
                    for (TableScanRule rule : config.getTableScanRules()) {
                        if (!rule.matches(tableName)) {
                            isScanTarget = false;
                            break;
                        }
                    }
                    if (isScanTarget) {
                        filteredTableNames.add(tableName);
                    }
                }
                return filteredTableNames;
            }
            default ->
                throw new IllegalStateException("Invalid value (" + tableScanMode + ") is specified for tableScanName");
        }
    }

    private static String buildClassComment(String className, Table table, List<ClassAdditionalCommentRule> rules) {
        Stream<String> comment = Optional.ofNullable(table.getDescription())
                .map(c -> Arrays.stream(c.split("\n")).filter(l -> l != null && !l.isEmpty())).orElseGet(Stream::empty);
        Stream<String> additionalComments = rules.stream()
                .filter(r -> r.matches(className))
                .map(ClassAdditionalCommentRule::getComment)
                .flatMap(c -> Arrays.stream(c.split("\n")));

        List<String> allComments = Stream.concat(comment, additionalComments).toList();
        if (allComments.isEmpty()) {
            return null;
        } else {
            return allComments.stream().collect(joining("\n * ", "/**\n * ", "\n */"));
        }
    }

    private static String buildFieldComment(String className, String fieldName, Column column, List<FieldAdditionalCommentRule> rules) {
        List<String> comment = Optional.ofNullable(column.getRemarks())
                .map(c -> Arrays.stream(c.split("\n")).filter(l -> l != null && !l.isEmpty()).collect(toList()))
                .orElse(Collections.emptyList());
        List<String> additionalComments = rules.stream()
                .filter(r -> r.matches(className, fieldName))
                .map(FieldAdditionalCommentRule::getComment)
                .flatMap(c -> Arrays.stream(c.split("\n")))
                .collect(toList());
        comment.addAll(additionalComments);
        if (!comment.isEmpty()) {
            return comment.stream().collect(joining("\n   * ", "  /**\n   * ", "\n   */"));
        } else {
            return null;
        }
    }

    private static <T> List<T> orEmptyListIfNull(List<T> list) {
        return Optional.ofNullable(list).orElse(Collections.emptyList());
    }

    private static String collectAndConvertFQDN(String fqdn, List<ImportRule> imports) {
        if (fqdn != null && fqdn.contains(".") && fqdn.matches("^[a-zA-Z0-9.]+$")) {
            if (imports.stream().noneMatch(i -> i.importValueContains(fqdn))) {
                ImportRule rule = new ImportRule();
                rule.setImportValue(fqdn);
                imports.add(rule);
            }
            String[] elements = fqdn.split("\\.");
            return elements[elements.length - 1];
        } else {
            return fqdn;
        }
    }

    private static boolean isPrimitive(String type) {
        if (type == null) {
            return false;
        }
        if (type.contains(".")) {
            return false;
        }
        return Character.isLowerCase(type.charAt(0));
    }
}
