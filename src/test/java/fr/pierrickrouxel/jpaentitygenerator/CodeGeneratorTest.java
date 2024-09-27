package fr.pierrickrouxel.jpaentitygenerator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.pierrickrouxel.jpaentitygenerator.config.CodeGeneratorConfig;

public class CodeGeneratorTest {

    @BeforeAll
    public static void setupDatabase() throws Exception {
        TestDatabase.init();
    }

    @Test
    public void _01_generateAll_TableScanMode_Is_Default() throws Exception {
        var config = CodeGeneratorConfig.load("jpa-entity-generator.yaml");

        CodeGenerator.generateAll(config);
    }

    @Test
    public void _02_generateAll_TableScanMode_Is_RuleBased() throws Exception {
        var config = CodeGeneratorConfig.load("jpa-entity-generator2.yaml");

        CodeGenerator.generateAll(config);
    }

    @Test
    public void _03_test_foreign_key() throws Exception {
        var config = CodeGeneratorConfig.load("jpa-entity-generator3.yaml");

        CodeGenerator.generateAll(config);
    }


}
