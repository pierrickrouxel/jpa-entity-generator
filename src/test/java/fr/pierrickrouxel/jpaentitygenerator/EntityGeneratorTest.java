package fr.pierrickrouxel.jpaentitygenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.squareup.javapoet.AnnotationSpec;

import fr.pierrickrouxel.jpaentitygenerator.config.EntityGeneratorConfig;
import fr.pierrickrouxel.jpaentitygenerator.metadata.Column;
import fr.pierrickrouxel.jpaentitygenerator.metadata.Index;
import fr.pierrickrouxel.jpaentitygenerator.metadata.Key;
import fr.pierrickrouxel.jpaentitygenerator.metadata.Table;
import fr.pierrickrouxel.jpaentitygenerator.rule.Annotation;
import fr.pierrickrouxel.jpaentitygenerator.rule.ClassAnnotationRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldAnnotationRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldDefaultValueRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldTypeRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.Interface;
import fr.pierrickrouxel.jpaentitygenerator.rule.InterfaceRule;

public class EntityGeneratorTest {

  @Test
  public void testGenerateEntity() throws IOException, URISyntaxException {
    var table = Table.builder().name("ARTICLE").build();
    var idColumn = Column.builder().name("ID").typeCode(4).typeName("INTEGER").autoIncrement(true).primaryKey(true)
        .build();
    var nameColumn = Column.builder().name("NAME").typeCode(12).typeName("VARCHAR").columnSize(50).build();
    table.getColumns().addAll(List.of(idColumn, nameColumn));

    var entity = EntityGenerator.getEntity(table, new EntityGeneratorConfig());
    assertThat(entity).isEqualTo(getExample("Article") + "\n");
  }

  @Test
  public void testGetClassInterfaceRules() {
    var table = Table.builder().name("ARTICLE").build();
    var idColumn = Column.builder().name("ID").typeCode(4).typeName("INTEGER").autoIncrement(true).primaryKey(true)
        .build();
    table.getColumns().add(idColumn);
    var articleInterfaces = List
        .of(Interface.builder().name("ArticleInterface").genericsClassNames(List.of("ArticleGeneric")).build());
    var blogInterfaces = List
        .of(Interface.builder().name("BlogInterface").genericsClassNames(List.of("BlogGeneric")).build());
    var interfaceRules = List.of(
        InterfaceRule.builder().className("Article").interfaces(articleInterfaces).build(),
        InterfaceRule.builder().className("Blog").interfaces(blogInterfaces).build());
    var entity = EntityGenerator.getEntity(table,
        EntityGeneratorConfig.builder().interfaceRules(interfaceRules).build());
    assertThat(entity).contains("ArticleInterface<ArticleGeneric>");
    assertThat(entity).doesNotContain("BlogInterface");
  }

  @Test
  public void testGetTableAnnotation() {
    var annotation = EntityGenerator.getTableAnnotation("table_name");
    assertThat(annotation.toString()).isEqualTo("@jakarta.persistence.Table(name = \"table_name\")");
  }

  @Test
  public void testGetClassAnnotationRules() {
    var articleAnnotations = List.of(Annotation.builder().className("ArticleAnnotation").build());
    var blogAnnotations = List.of(Annotation.builder().className("BlogAnnotation").build());
    var classAnnotationRules = List.of(
        ClassAnnotationRule.builder().className("Article").annotations(articleAnnotations).build(),
        ClassAnnotationRule.builder().className("Blog").annotations(blogAnnotations).build());
    var classAnnotationSpecs = EntityGenerator.getClassAnnotations("ARTICLE", "Article", classAnnotationRules);
    assertThat(classAnnotationSpecs.stream().map(AnnotationSpec::toString)).contains("@ArticleAnnotation");
    assertThat(classAnnotationSpecs.stream().map(AnnotationSpec::toString)).doesNotContain("@BlogAnnotation");
  }

  @Test
  public void testGetFieldUnique() {
    var indexes = List.of(
        Index.builder().name("CODE_CONSTRAINT").columnName("CODE").nonUnique(false).build(),
        Index.builder().name("NAME_CONSTRAINT").columnName("NAME").nonUnique(true).build(),
        // Some index names be null (see MSSQL tableIndexClustered)
        Index.builder().nonUnique(true).build());
    var codeColumn = Column.builder().name("CODE").typeCode(4).build();
    var nameColumn = Column.builder().name("NAME").typeCode(4).build();

    assertThat(EntityGenerator
        .getField(codeColumn, "Article", indexes, new EntityGeneratorConfig()).toString())
        .startsWith("""
            @jakarta.persistence.Column(
                name = "CODE",
                nullable = false,
                unique = true
            )
            """);
    assertThat(EntityGenerator
        .getField(nameColumn, "Article", indexes, new EntityGeneratorConfig()).toString())
        .startsWith("""
            @jakarta.persistence.Column(
                name = "NAME",
                nullable = false,
                unique = false
            )
            """);
  }

  @Test
  public void testGetFieldWithValue() {
    var column = Column.builder().name("CODE").typeCode(4).build();
    var defaultValueRules = List
        .of(FieldDefaultValueRule.builder().className("Article").fieldName("code").defaultValue("1").build());
    var config = EntityGeneratorConfig.builder().fieldDefaultValueRules(defaultValueRules).build();

    assertThat(EntityGenerator.getField(column, "Article", Collections.emptyList(), config).initializer.toString()).isEqualTo("1");
  }

  @Test
  public void testGetManyToOneField() {
    var columns = List.of(Column.builder().name("BLOG_ID").typeCode(4).build());
    var importedKeys = List.of(Key.builder().primaryKeyTableName("BLOG").primaryKeyColumnName("ID")
        .foreignKeyTableName("ARTICLE").foreignKeyColumnName("BLOG_ID").build());

    assertThat(EntityGenerator
        .getManyToOneField("BLOG", importedKeys, columns, Collections.emptyList()).toString())
        .isEqualTo("""
            @jakarta.persistence.ManyToOne
            @jakarta.persistence.JoinColumn(
                name = "BLOG_ID",
                referencedColumnName = "ID",
                nullable = false
            )
            private Blog blog;
            """);
  }

  @Test
  public void testGetManyToOneFieldWithCompositeRelationship() {
    var columns = List.of(Column.builder().name("PHONE").typeCode(4).build(),
    Column.builder().name("EMAIL").typeCode(4).build());
    var importedKeys = List.of(
      Key.builder().primaryKeyTableName("USER").primaryKeyColumnName("PHONE")
        .foreignKeyTableName("ARTICLE").foreignKeyColumnName("USER_PHONE").build(),
        Key.builder().primaryKeyTableName("USER").primaryKeyColumnName("EMAIL")
          .foreignKeyTableName("ARTICLE").foreignKeyColumnName("USER_EMAIL").build()
        );

    assertThat(EntityGenerator
        .getManyToOneField("USER", importedKeys, columns, Collections.emptyList()).toString())
        .isEqualTo("""
            @jakarta.persistence.ManyToOne
            @jakarta.persistence.JoinColumns({
                @jakarta.persistence.JoinColumn(name = "USER_PHONE", referencedColumnName = "PHONE", nullable = false),
                @jakarta.persistence.JoinColumn(name = "USER_EMAIL", referencedColumnName = "EMAIL", nullable = false)
            })
            private User user;
            """);
  }

  @Test
  public void testGetOneToManyFieldsWithCompositeRelationship() {
    var exportedKeys = List.of(
      Key.builder().primaryKeyTableName("USER").primaryKeyColumnName("PHONE")
        .foreignKeyTableName("ARTICLE").foreignKeyColumnName("USER_EMAIL").build(),
        Key.builder().primaryKeyTableName("USER").primaryKeyColumnName("EMAIL")
          .foreignKeyTableName("ARTICLE").foreignKeyColumnName("USER_EMAIL").build()
    );

    assertThat(EntityGenerator.getOneToManyFields(exportedKeys, Collections.emptyList())).hasSize(1);
  }

  @Test
  public void testGetOneToManyField() {
    var exportedKey = Key.builder().primaryKeyTableName("BLOG").primaryKeyColumnName("ID")
        .foreignKeyTableName("ARTICLE").foreignKeyColumnName("BLOG_ID").build();

    assertThat(EntityGenerator.getOneToManyField(exportedKey, Collections.emptyList()).toString()).isEqualTo("""
        @jakarta.persistence.OneToMany(
            mappedBy = "blog"
        )
        private java.util.List<Article> articles;
        """);
  }

  @Test
  public void testGetFieldType() {
    assertThat(EntityGenerator.getFieldType("code", 4, "Article", Collections.emptyList()).toString())
        .isEqualTo("Integer");
  }

  @Test
  public void testGetFieldTypeWithRules() {
    var fieldTypeRule = FieldTypeRule.builder().className("Article").fieldName("code").typeName("String").build();
    assertThat(EntityGenerator.getFieldType("code", 4, "Article", List.of(fieldTypeRule)).toString())
        .isEqualTo("String");
  }

  @Test
  public void testGetFieldAnnotationRules() {
    var column = Column.builder().name("name").build();
    var articleNameAnnotations = List.of(Annotation.builder().className("ArticleNameAnnotation").build());
    var articleCodeAnnotations = List.of(Annotation.builder().className("ArticleCodeAnnotation").build());
    var blogNameAnnotations = List.of(Annotation.builder().className("BlogNameAnnotation").build());
    var fieldAnnotationRules = List.of(
        FieldAnnotationRule.builder().className("Article").fieldName("name").annotations(articleNameAnnotations)
            .build(),
        FieldAnnotationRule.builder().className("Article").fieldName("code").annotations(articleCodeAnnotations)
            .build(),
        FieldAnnotationRule.builder().className("Blog").fieldName("name").annotations(blogNameAnnotations).build());
    var classAnnotationSpecs = EntityGenerator.getFieldAnnotations(column, false, "Article", "name", null,
        fieldAnnotationRules);
    assertThat(classAnnotationSpecs.stream().map(AnnotationSpec::toString)).contains("@ArticleNameAnnotation");
    assertThat(classAnnotationSpecs.stream().map(AnnotationSpec::toString)).doesNotContain("@BlogNameAnnotation",
        "@ArticleCodeAnnotation");
  }

  @Test
  public void testGetColumnAnnotationString() {
    var column = Column.builder().name("NAME").typeCode(12).columnSize(50).build();
    assertThat(EntityGenerator.getColumnAnnotation(column, false).toString())
        .isEqualTo("@jakarta.persistence.Column(name = \"NAME\", nullable = false, unique = false, length = 50)");
  }

  @Test
  public void testGetColumnAnnotationInteger() {
    var column = Column.builder().name("CODE").typeCode(4).build();
    assertThat(EntityGenerator.getColumnAnnotation(column, false).toString())
        .isEqualTo("@jakarta.persistence.Column(name = \"CODE\", nullable = false, unique = false)");
  }

  @Test
  public void testGetColumnAnnotationBigDecimal() {
    var column = Column.builder().name("AVERAGE").typeCode(2).columnSize(9).decimalDigits(2).build();
    assertThat(EntityGenerator.getColumnAnnotation(column, false).toString()).isEqualTo(
        "@jakarta.persistence.Column(name = \"AVERAGE\", nullable = false, unique = false, precision = 9, scale = 2)");
  }

  private String getExample(String entityName) throws IOException, URISyntaxException {
    var path = Paths.get(getClass().getClassLoader()
        .getResource(String.format("example/%s.java", entityName)).toURI());
    try (var lines = Files.lines(path)) {
      return lines.collect(Collectors.joining("\n"));
    }
  }
}
