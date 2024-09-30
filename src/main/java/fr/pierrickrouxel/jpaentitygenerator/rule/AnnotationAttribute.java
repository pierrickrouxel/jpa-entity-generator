package fr.pierrickrouxel.jpaentitygenerator.rule;

import java.io.Serializable;

import lombok.Data;

/**
 * Represents an attribute of a Java annotation.
 */
@Data
public class AnnotationAttribute implements Serializable {

    private String name;
    private String value;
}
