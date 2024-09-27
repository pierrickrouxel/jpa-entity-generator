package fr.pierrickrouxel.jpaentitygenerator.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class Table {
    private String name;
    private String schema;
    private String description;
    private List<Column> columns = new ArrayList<>();
    private Map<String, ForeignKey> foreignKeyMap = new HashMap<>();
    private Map<String, List<ForeignKey>> foreignCompositeKeyMap = new HashMap<>();
    private List<String> importedKeys = new ArrayList<>();
}
