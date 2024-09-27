package fr.pierrickrouxel.jpaentitygenerator.gradle;

import fr.pierrickrouxel.jpaentitygenerator.CodeGenerator;
import fr.pierrickrouxel.jpaentitygenerator.config.CodeGeneratorConfig;
import freemarker.template.TemplateException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.sql.SQLException;

/**
 * entityGen Gradle task.
 */
public class EntityGenTask extends DefaultTask {

    @TaskAction
    public void generateAll() throws IOException, SQLException, TemplateException {
        EntityGenExtension ext = getProject().getExtensions().getByType(EntityGenExtension.class);
        if (ext == null) {
            ext = new EntityGenExtension();
        }
        CodeGeneratorConfig config = CodeGeneratorConfig.load(ext.getConfigPath(), ext.getEnvironment());

        CodeGenerator.generateAll(config);
    }
}
