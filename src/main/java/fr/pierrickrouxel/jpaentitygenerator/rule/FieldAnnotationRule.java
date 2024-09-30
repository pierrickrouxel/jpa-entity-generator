package fr.pierrickrouxel.jpaentitygenerator.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rule used to generate annotations for a given field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldAnnotationRule implements Serializable, FieldMatcher {

    private String className;
    @Builder.Default
    private List<String> classNames = new ArrayList<>();
    private String fieldName;
    @Builder.Default
    private List<String> fieldNames = new ArrayList<>();

    @Builder.Default
    private List<Annotation> annotations = new ArrayList<>();
}
