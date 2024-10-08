package fr.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(
    name = "\"ARTICLE\""
)
public class Article {
  @Column(
      name = "\"ID\"",
      nullable = false,
      unique = false
  )
  @Id
  @GeneratedValue(
      strategy = GenerationType.IDENTITY
  )
  private Integer id;

  @Column(
      name = "\"NAME\"",
      nullable = false,
      unique = false,
      length = 50
  )
  private String name;
}
