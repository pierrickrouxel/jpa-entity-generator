package fr.pierrickrouxel.jpaentitygenerator.rule;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule used to generate annotations for a given field.
 */
@Data
public class FieldAnnotationRule implements Serializable, FieldMatcher {

    private String className;
    private List<String> classNames = new ArrayList<>();
    private String fieldName;
    private List<String> fieldNames = new ArrayList<>();

    private List<Annotation> annotations = new ArrayList<>();
}
