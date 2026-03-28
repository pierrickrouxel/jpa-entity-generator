package fr.pierrickrouxel.jpaentitygenerator.config;

import fr.pierrickrouxel.jpaentitygenerator.rule.Annotation;
import fr.pierrickrouxel.jpaentitygenerator.rule.ClassNameRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldAnnotationRule;
import fr.pierrickrouxel.jpaentitygenerator.rule.FieldTypeRule;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static fr.pierrickrouxel.jpaentitygenerator.config.EntityGeneratorConfig.hasEnvVariables;
import static fr.pierrickrouxel.jpaentitygenerator.config.EntityGeneratorConfig.replaceEnvVariables;
import static org.assertj.core.api.Assertions.assertThat;

public class CodeGeneratorConfigTest {

  @Test
  public void testHasEnvVariables() {
    assertThat(hasEnvVariables("")).isFalse();
    assertThat(hasEnvVariables(" ")).isFalse();
    assertThat(hasEnvVariables("foo")).isFalse();
    assertThat(hasEnvVariables("foo barr")).isFalse();
    assertThat(hasEnvVariables("foo $ test")).isFalse();

    assertThat(hasEnvVariables("${TEST}")).isTrue();
    assertThat(hasEnvVariables("Something is ${WRONG}")).isTrue();
    assertThat(hasEnvVariables("${WHAT} is happening?")).isTrue();

  }

  @Test
  public void testReplaceEnvVariables() {
    var env = System.getenv();
    var keys = new ArrayList<String>(env.keySet());
    var k1 = keys.get(0);
    var v1 = env.get(k1);
    var k2 = keys.get(1);
    var v2 = env.get(k2);
    var k3 = keys.get(2);
    var v3 = env.get(k3);

    var environment = new HashMap<String, String>();
    var k4 = "test";
    var v4 = "test";
    environment.put(k4, v4);

    assertThat(replaceEnvVariables("${" + k1 + "} is missing!", environment).equals(v1 + " is missing!")).isTrue();
    assertThat(replaceEnvVariables("You are the ${" + k2 + "}", environment).equals("You are the " + v2)).isTrue();
    assertThat(replaceEnvVariables("${" + k3 + "}", environment).equals(v3)).isTrue();
    assertThat(replaceEnvVariables("You can override ${" + k4 + "}", environment).equals("You can override " + v4))
        .isTrue();
    assertThat(replaceEnvVariables("as is $", environment).equals("as is $")).isTrue();
    assertThat(replaceEnvVariables("${" + k1 + "}${" + k2 + "}", environment).equals(v1 + v2)).isTrue();
  }

  @Test
  public void testLoad() throws IOException {
    final var path = new File(Objects.requireNonNull(getClass().getResource("/jpa-entity-generator.yaml")).getPath()).getAbsolutePath();
    final var generator = EntityGeneratorConfig.load(path, new HashMap<>());
    assertThat(generator).isNotNull();
    final var classNameRule = new ClassNameRule();
    classNameRule.setClassName("Author");
    classNameRule.setTableName("author");
    assertThat(generator.getClassNameRules()).contains(classNameRule);

    assertThat(generator.getFieldTypeRules()).contains(
      new FieldTypeRule("Author", new ArrayList<>(), "name", new ArrayList<>(), "String")
    );
    assertThat(generator.getFieldAnnotationRules()).contains(
      new FieldAnnotationRule("Author", new ArrayList<>(), "tags", new ArrayList<>(),
        List.of(new Annotation("Deprecated", new ArrayList<>())))
    );

  }

}
