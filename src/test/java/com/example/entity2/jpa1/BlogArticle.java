package com.example.entity2.jpa1;

import java.io.Serializable;
import java.sql.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import lombok.Data;
import lombok.ToString;

/**
 * Note: auto-generated by jpa-entity-generator
 */
@Data
@ToString
@Entity(name = "com.example.entity2.jpa1.BlogArticle")
@Table(name = "article")
public class BlogArticle implements Serializable {

  public Integer getId() { return this.id; }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "`id`", nullable = false)
  private int id;
  /**
   * database comment for blog_id
   */
  @Nullable
  @Column(name = "`blog_id`", nullable = true)
  private Integer blogId;
  @Nullable
  @Column(name = "`name`", nullable = true)
  private String name;
  @Deprecated
  @Nullable
  @Column(name = "`tags`", nullable = true)
  private Clob tags;
  @Nonnull
  @Column(name = "`created_at`", nullable = false)
  private Timestamp createdAt;
}