package fr.pierrickrouxel.jpaentitygenerator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility to reader classpath resources.
 */
public class ResourceReader {

    private ResourceReader() {
    }

    public static InputStream getResourceAsStream(String path) throws IOException {
        var classPathResource = ResourceReader.class.getClassLoader().getResourceAsStream(path);
        if (classPathResource != null) {
            return classPathResource;
        }
        var fileResource = new FileInputStream(new File(path));
        return fileResource;
    }

}
