package fr.pierrickrouxel.jpaentitygenerator.gradle;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class EntityGenExtension {
    private String configPath = "src/main/resources/jpa-entity-generator.yaml";
    private Map<String, String> environment = new HashMap<>();
}
