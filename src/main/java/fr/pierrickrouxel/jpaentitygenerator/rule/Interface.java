package fr.pierrickrouxel.jpaentitygenerator.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an interface.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interface implements Serializable {

    private String name;
    @Builder.Default
    private List<String> genericsClassNames = new ArrayList<>();
}
