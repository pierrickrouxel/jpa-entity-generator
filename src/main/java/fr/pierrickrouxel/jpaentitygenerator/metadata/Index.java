package fr.pierrickrouxel.jpaentitygenerator.metadata;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Index {
  private String name;
  private String columnName;
  private boolean nonUnique;
}
