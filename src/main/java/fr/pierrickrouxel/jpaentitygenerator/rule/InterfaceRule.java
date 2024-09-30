package fr.pierrickrouxel.jpaentitygenerator.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rule used to generate an interface.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceRule implements Serializable, ClassMatcher {

    /**
     * A single partial-matching rule.
     */
    private String className;

    /**
     * multiple partial-matching rule.
     */
    @Builder.Default
    private List<String> classNames = new ArrayList<>();

    /**
     * The interfaces to be implemented.
     */
    @Builder.Default
    private List<Interface> interfaces = new ArrayList<>();

}
