package fr.pierrickrouxel.jpaentitygenerator.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * entityGen Gradle plugin.
 */
public class EntityGenPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("jpaEntityGenerator", EntityGenExtension.class);
        project.getTasks().create("generateEntities", EntityGenTask.class);
    }
}
