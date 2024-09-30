package fr.pierrickrouxel.jpaentitygenerator.metadata;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Key {
  private String primaryKeyName;
  private String primaryKeyTableName;
  private String primaryKeyColumnName;
  private String foreignKeyName;
  private String foreignKeyTableName;
  private String foreignKeyColumnName;
}
