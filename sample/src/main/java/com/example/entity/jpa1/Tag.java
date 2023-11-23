package com.example.entity.jpa1;

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
@Entity(name = "com.example.entity.jpa1.Tag")
@Table(name = "tag")
public class Tag implements Serializable {

  @Id
  @GeneratedValue
  @Column(name = "`id`", nullable = false)
  private int id;
  @Nullable
  @Column(name = "`tag`", nullable = true, length = 100)
  private String tag;
  @Nullable
  @Column(name = "`average`", nullable = true, precision = 9, scale = 2)
  private java.math.BigDecimal average;
  @Nonnull
  @Column(name = "`created_at`", nullable = false)
  private Timestamp createdAt;
  @OneToMany(mappedBy = "tag")
  private java.util.List<BlogArticleTag> listOfBlogArticleTag;
}