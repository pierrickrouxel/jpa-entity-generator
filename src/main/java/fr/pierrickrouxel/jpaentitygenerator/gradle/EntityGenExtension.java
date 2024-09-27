package fr.pierrickrouxel.jpaentitygenerator.gradle;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * <pre>
 * entityGen {
 *   configPath = "src/main/resources/entityGenConfig.yml"
 * }
 * </pre>
 */
@Data
public class EntityGenExtension {
    private String configPath = "entityGenConfig.yml";
    private Map<String, String> environment = new HashMap<>();
}
