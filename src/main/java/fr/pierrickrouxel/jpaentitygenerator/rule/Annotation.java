package fr.pierrickrouxel.jpaentitygenerator.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents Java annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Annotation implements Serializable {

    private String className;
    @Builder.Default
    private List<AnnotationAttribute> attributes = new ArrayList<>();

}
