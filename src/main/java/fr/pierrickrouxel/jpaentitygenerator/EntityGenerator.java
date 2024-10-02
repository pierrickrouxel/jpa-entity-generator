package fr.pierrickrouxel.jpaentitygenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import fr.pierrickrouxel.jpaentitygenerator.config.EntityGeneratorConfig;
import fr.pierrickrouxel.jpaentitygenerator.metadata.Column;
import fr.pierrickrouxel.jpaentitygenerator.metadata.Index;
import fr.pierrickrouxel.jpaentitygenerator.metadata.Key;
import fr.pierrickrouxel.jpaentitygenerator.metadata.Table;
import fr.pierrickrouxel.jpaentitygenerator.rule.Annotation;
import fr.pierrickrouxel.jpaentitygenerator.rule.ClassAdditionalCommentRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.ClassAnnotationRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.ClassNameRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldAdditionalCommentRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldAnnotationRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldTypeRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.InterfaceRule;
import fr.pierrickrouxel.jpaentitygenerator.util.NameConverter;
import fr.pierrickrouxel.jpaentitygenerator.util.TypeConverter;
import lombok.extern.slf4j.Slf4j;

/**
 * Lombok-wired JPA entity code generator.
 */
@Slf4j
public class EntityGenerator {

  /**
   * /**
   * Generates entity source code.
   *
   * @param table  The table description
   * @param config The config
   * @return The source code
   */
  public static String getEntity(Table table, EntityGeneratorConfig config) {
    var className = NameConverter.toClassName(table.getName(), config.getClassNameRules());

    var fields = table.getColumns().stream()
        .map(o -> getField(o, className, table.getIndexes(), table.getImportedKeys(), config))
        .collect(Collectors.toList());

    var classSpecBuilder = TypeSpec.classBuilder(className)
        .addAnnotations(getClassAnnotations(table.getName(), className, config.getClassAnnotationRules()))
        .addFields(fields);

    var javaDoc = getClassJavaDoc(table.getRemarks(), className, config.getClassAdditionalCommentRules());
    if (javaDoc != null) {
      classSpecBuilder
          .addJavadoc(getClassJavaDoc(table.getRemarks(), className, config.getClassAdditionalCommentRules()));
    }

    getClassInterfaces(className, config.getInterfaceRules()).forEach(classSpecBuilder::addSuperinterface);

    var classSpec = classSpecBuilder.build();

    var javaFile = JavaFile.builder(config.getPackageName(), classSpec)
        .build();

    if (classSpec.fieldSpecs.stream()
        .noneMatch(o -> o.annotations.stream().anyMatch(o2 -> o2.type.toString().equals("jakarta.persistence.Id")))) {
      throw new IllegalStateException("Entity " + className + " has no @Id field");
    }

    return javaFile.toString();
  }

  /**
   * Generates javadoc for entity class.
   *
   * @param remarks   The SQL remarks
   * @param className The class name
   * @param rules     The additionnal comment rules
   * @return The javadoc
   */
  private static String getClassJavaDoc(String remarks, String className, List<ClassAdditionalCommentRule> rules) {
    var comment = Optional.ofNullable(remarks)
        .map(o -> Arrays.stream(o.split("\n")).filter(l -> l != null && !l.isEmpty()))
        .orElse(Stream.empty());

    var additionalComments = rules.stream()
        .filter(o -> o.matches(className))
        .map(ClassAdditionalCommentRule::getComment)
        .flatMap(o -> Arrays.stream(o.split("\n")));

    var allComments = Stream.concat(comment, additionalComments).toList();

    if (allComments.isEmpty()) {
      return null;
    }

    return allComments.stream().collect(joining("\n"));
  }

  /**
   * Generates interfaces for entity class.
   *
   * @param className      The class name
   * @param interfaceRules The interface rules
   * @return The interfaces
   */
  public static List<TypeName> getClassInterfaces(String className, List<InterfaceRule> interfaceRules) {
    return interfaceRules.stream()
        .filter(o -> o.matches(className))
        .flatMap(o -> o.getInterfaces().stream())
        .map(o -> ParameterizedTypeName.get(ClassName.bestGuess(o.getName()),
            o.getGenericsClassNames().stream()
                .map(ClassName::bestGuess)
                .toArray(ClassName[]::new)))
        .collect(Collectors.toList());
  }

  /**
   * Generates annotation for entity class.
   *
   * @param className            The class name
   * @param tableName            The table name
   * @param classAnnotationRules The annotation rules
   * @return The annotations
   */
  public static List<AnnotationSpec> getClassAnnotations(String className, String tableName,
      List<ClassAnnotationRule> classAnnotationRules) {
    var annotationSpecs = new ArrayList<AnnotationSpec>();

    annotationSpecs.add(AnnotationSpec.builder(ClassName.bestGuess("lombok.Data")).build());
    annotationSpecs.add(AnnotationSpec.builder(ClassName.bestGuess("lombok.Builder")).build());
    annotationSpecs.add(AnnotationSpec.builder(ClassName.bestGuess("lombok.NoArgsConstructor")).build());
    annotationSpecs.add(AnnotationSpec.builder(ClassName.bestGuess("lombok.AllArgsConstructor")).build());
    annotationSpecs.add(AnnotationSpec.builder(ClassName.bestGuess("jakarta.persistence.Entity")).build());
    annotationSpecs.add(AnnotationSpec.builder(ClassName.bestGuess("jakarta.persistence.Table"))
        .addMember("name", "$S", tableName).build());

    classAnnotationRules.stream()
        .filter(o -> o.matches(className))
        .flatMap(o -> o.getAnnotations().stream())
        .map(o -> getAnnotation(o))
        .forEach(annotationSpecs::add);

    return annotationSpecs;
  }

  /**
   * Generates field from column.
   *
   * @param column       The column
   * @param className    The class name
   * @param indexes      The list of indexes
   * @param importedKeys The list of imported keys
   * @param config       The config
   * @return The field
   */
  public static FieldSpec getField(Column column, String className, List<Index> indexes, List<Key> importedKeys,
      EntityGeneratorConfig config) {
    var fieldName = NameConverter.toFieldName(column.getName());

    var isUnique = checkColumnUnique(column, indexes);

    var columnImportedKey = importedKeys.stream()
        .filter(o -> o.getForeignKeyColumnName().equals(column.getName()))
        .findAny()
        .orElse(null);
    if (columnImportedKey != null) {
      return getManyToOneField(column, columnImportedKey, config.getClassNameRules());
    }

    var typeName = getFieldType(fieldName, column.getTypeCode(), className, config.getFieldTypeRules());

    var fieldSpecBuilder = FieldSpec.builder(typeName, NameConverter.toFieldName(column.getName()), Modifier.PRIVATE);

    var fieldComment = getFieldJavaDoc(column.getRemarks(), className, fieldName,
        config.getFieldAdditionalCommentRules());
    if (fieldComment != null) {
      fieldSpecBuilder.addJavadoc(fieldComment);
    }

    fieldSpecBuilder.addAnnotations(getFieldAnnotations(column, isUnique, className, fieldName,
        config.getGeneratedValueStrategy(), config.getFieldAnnotationRules()));

    config.getFieldDefaultValueRules().stream()
        .filter(r -> r.matches(className, fieldName))
        .findAny()
        .ifPresent(o -> fieldSpecBuilder.initializer(o.getDefaultValue()));

    return fieldSpecBuilder.build();
  }

  /**
   * Generates @ManyToOne annotated field.
   *
   * @param column         The colum
   * @param importedKey    The imported key
   * @param classNameRules The class name rules
   * @return The field
   */
  public static FieldSpec getManyToOneField(Column column, Key importedKey, List<ClassNameRule> classNameRules) {
    var fieldTypeName = NameConverter.toClassName(importedKey.getPrimaryKeyTableName(), classNameRules);
    var fieldName = NameConverter.toFieldName(importedKey.getPrimaryKeyTableName());

    var joinColumnAnnotation = AnnotationSpec.builder(ClassName.bestGuess("jakarta.persistence.JoinColumn"))
        .addMember("name", "$S", importedKey.getForeignKeyColumnName())
        .addMember("nullable", "$L", column.isNullable())
        .build();

    return FieldSpec.builder(ClassName.bestGuess(fieldTypeName), fieldName, Modifier.PRIVATE)
        .addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("jakarta.persistence.ManyToOne")).build())
        .addAnnotation(joinColumnAnnotation)
        .build();
  }

  /**
   * Generates @OneToMany annotated fields from exported keys.
   *
   * @param exportedKeys   The list of exported keys
   * @param classNameRules The class name rules
   * @return The list of fields
   */
  public static List<FieldSpec> getOneToManyFields(List<Key> exportedKeys, List<ClassNameRule> classNameRules) {
    return exportedKeys.stream().map(o -> getOneToManyField(o, classNameRules)).collect(Collectors.toList());
  }

  /**
   * Generates @OneToMany annotated field from an exported key.
   *
   * @param exportedKey    The exported key
   * @param classNameRules The class name rules
   * @return The field
   */
  public static FieldSpec getOneToManyField(Key exportedKey, List<ClassNameRule> classNameRules) {
    var fieldTypeName = NameConverter.toClassName(exportedKey.getForeignKeyTableName(), classNameRules);
    var fieldName = NameConverter.toListFieldName(exportedKey.getForeignKeyTableName());

    var annotation = AnnotationSpec.builder(ClassName.bestGuess("jakarta.persistence.OneToMany"))
        .addMember("mappedBy", "$S", NameConverter.toFieldName(exportedKey.getPrimaryKeyTableName()))
        .build();

    var fieldType = ClassName.bestGuess(fieldTypeName);
    var list = ClassName.get("java.util", "List");
    var listOfType = ParameterizedTypeName.get(list, fieldType);

    return FieldSpec.builder(listOfType, fieldName, Modifier.PRIVATE)
        .addAnnotation(annotation)
        .build();
  }

  /**
   * Generates field type from column type.
   *
   * @param fieldName      The field name
   * @param typeCode       The SQL type code
   * @param className      The class name
   * @param fieldTypeRules The type rules
   * @return The field type
   */
  public static TypeName getFieldType(String fieldName, int typeCode, String className,
      List<FieldTypeRule> fieldTypeRules) {
    var fieldType = fieldTypeRules.stream()
        .filter(o -> o.matches(className, fieldName))
        .findAny()
        .map(FieldTypeRule::getTypeName)
        .orElseGet(() -> TypeConverter.toJavaType(typeCode));
    return ClassName.bestGuess(fieldType);
  }

  /**
   * Generates javadoc of field.
   *
   * @param remarks   The SQL remarks
   * @param className The class name
   * @param fieldName The field name
   * @param rules     The additional comment rules
   * @return The javadoc
   */
  private static String getFieldJavaDoc(String remarks, String className, String fieldName,
      List<FieldAdditionalCommentRule> rules) {
    var comment = Optional.ofNullable(remarks)
        .map(c -> Arrays.stream(c.split("\n")).filter(l -> l != null && !l.isEmpty()).collect(toList()))
        .orElse(Collections.emptyList());

    var additionalComments = rules.stream()
        .filter(r -> r.matches(className, fieldName))
        .map(FieldAdditionalCommentRule::getComment)
        .flatMap(c -> Arrays.stream(c.split("\n")))
        .collect(toList());

    comment.addAll(additionalComments);

    if (comment.isEmpty()) {
      return null;
    }

    return comment.stream().collect(joining("\n"));
  }

  /**
   * Generates annotations of field.
   *
   * @param column                 The column
   * @param isUnique               `true` if column is unique
   * @param className              The class name
   * @param fieldName              The field name
   * @param generatedValueStrategy The generated value strategy
   *                               for @GeneratedValue annotation
   * @param fieldAnnotationRules   The field annotation rule
   * @return The list of annotations
   */
  public static List<AnnotationSpec> getFieldAnnotations(Column column, boolean isUnique, String className,
      String fieldName, String generatedValueStrategy, List<FieldAnnotationRule> fieldAnnotationRules) {
    var annotationSpecs = new ArrayList<AnnotationSpec>();
    annotationSpecs.add(getColumnAnnotation(column, isUnique));

    if (column.isPrimaryKey()) {
      annotationSpecs.add(getIdAnnotation());
    }

    if (column.isAutoIncrement()) {
      annotationSpecs.add(getGeneratedValueAnnotation(generatedValueStrategy));
    }

    fieldAnnotationRules.stream()
        .filter(o -> o.matches(className, fieldName))
        .flatMap(o -> o.getAnnotations().stream())
        .map(o -> getAnnotation(o))
        .forEach(annotationSpecs::add);

    return annotationSpecs;
  }

  /**
   * Generates the @Column annotation.
   *
   * @param column   The column
   * @param isUnique `true` if column is unique
   * @return The column annotation
   */
  public static AnnotationSpec getColumnAnnotation(Column column, boolean isUnique) {
    var columnAnnotationBuilder = AnnotationSpec.builder(ClassName.bestGuess("jakarta.persistence.Column"))
        .addMember("name", "$S", column.getName())
        .addMember("nullable", "$L", column.isNullable())
        .addMember("unique", "$L", isUnique);

    var javaType = TypeConverter.toJavaType(column.getTypeCode());

    if ("String".equals(javaType)) {
      columnAnnotationBuilder.addMember("length", "$L", column.getColumnSize());
    }

    if ("java.math.BigDecimal".equals(javaType)) {
      columnAnnotationBuilder.addMember("precision", "$S", column.getColumnSize());
      columnAnnotationBuilder.addMember("scale", "$S", column.getDecimalDigits());
    }

    return columnAnnotationBuilder.build();
  }

  /**
   * Generates the @Id annotation.
   *
   * @return The annotation
   */
  private static AnnotationSpec getIdAnnotation() {
    return AnnotationSpec.builder(ClassName.bestGuess("jakarta.persistence.Id"))
        .build();
  }

  /**
   * Generates the @GeneratedValue annotation.
   *
   * @param strategy The strategy
   * @return The annotation
   */
  private static AnnotationSpec getGeneratedValueAnnotation(String strategy) {
    var annotationSpecBuilder = AnnotationSpec.builder(ClassName.bestGuess("jakarta.persistence.GeneratedValue"));
    if (strategy != null && !strategy.isEmpty()) {
      annotationSpecBuilder.addMember("strategy", "$T.$L", ClassName.bestGuess("jakarta.persistence.GenerationType"),
          strategy);
    }
    return annotationSpecBuilder.build();
  }

  /**
   * Generates custom annotation from rule.
   *
   * @param annotation The annotation rule
   * @return The annotation
   */
  private static AnnotationSpec getAnnotation(Annotation annotation) {
    var annotationSpecBuilder = AnnotationSpec.builder(ClassName.bestGuess(annotation.getClassName()));
    annotation.getAttributes().forEach(o -> annotationSpecBuilder.addMember(o.getName(), o.getValue()));
    return annotationSpecBuilder.build();
  }

  /**
   * Check if column is unique from indexes.
   *
   * @param column  The column
   * @param indexes The list of indexes
   * @return `true` if column is unique
   */
  private static boolean checkColumnUnique(Column column, List<Index> indexes) {
    return indexes.stream()
        .anyMatch(o -> column.getName().equals(o.getColumnName()) && !o.isNonUnique());
  }
}
