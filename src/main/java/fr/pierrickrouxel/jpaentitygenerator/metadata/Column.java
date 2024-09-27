package fr.pierrickrouxel.jpaentitygenerator.metadata;

import java.util.Optional;

import lombok.Data;

/**
 * Database metadata: a column in a table
 */
@Data
public class Column {

    private String name;
    private int typeCode;
    private String typeName;
    private boolean nullable;
    private boolean primaryKey;
    private boolean autoIncrement;
    private int columnSize;
    private int decimalDigits;
    private Optional<String> description = Optional.empty();
}
