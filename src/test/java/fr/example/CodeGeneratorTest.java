package fr.example;

import fr.pierrickrouxel.jpaentitygenerator.CodeGenerator;
import fr.pierrickrouxel.jpaentitygenerator.config.CodeGeneratorConfig;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.example.unit.DatabaseUtil;

public class CodeGeneratorTest {

    @BeforeAll
    public static void setupDatabase() throws Exception {
        DatabaseUtil.init();
    }

    @Test
    public void _01_generateAll_TableScanMode_Is_Default() throws Exception {
        CodeGeneratorConfig config = CodeGeneratorConfig.load("jpa-entity-generator.yaml", new HashMap<>());

        CodeGenerator.generateAll(config);
    }

    @Test
    public void _02_generateAll_TableScanMode_Is_RuleBased() throws Exception {
        CodeGeneratorConfig config = CodeGeneratorConfig.load("jpa-entity-generator2.yaml", new HashMap<>());

        CodeGenerator.generateAll(config);
    }

    @Test
    public void _03_test_foreign_key() throws Exception {
        CodeGeneratorConfig config = CodeGeneratorConfig.load("jpa-entity-generator3.yaml", new HashMap<>());

        CodeGenerator.generateAll(config);
    }


}
