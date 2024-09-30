package fr.pierrickrouxel.jpaentitygenerator.metadata;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Table {
    private String name;
    private String schema;
    private String remarks;
    @Builder.Default
    private List<Column> columns = new ArrayList<>();
    @Builder.Default
    private List<Key> importedKeys = new ArrayList<>();
    @Builder.Default
    private List<Key> exportedKeys = new ArrayList<>();
}
