package ${packageName};

<#list importRules as rule>
import ${rule.importValue};
</#list>

<#if classComment?has_content>
${classComment}
</#if>
<#list classAnnotationRules as rule>
<#list rule.annotations as annotation>
${annotation.toString()}
</#list>
</#list>
<#-- NOTICE: the name attribute of @Table is intentionally unquoted  -->
@Table(name = "${tableName}")<#if primaryKeyFields.size() \gt 1>
@IdClass(${className}.PrimaryKeys.class)</#if>
public class ${className}<#if interfaceNames.size() \gt 0> implements ${interfaceNames?join(", ")}</#if> {
<#if primaryKeyFields.size() \gt 1>
  @Data
  public static class PrimaryKeys implements Serializable {
  <#list primaryKeyFields as field>
    private ${field.type} ${field.name}<#if field.defaultValue??> = ${field.defaultValue}</#if>;
  </#list>
  }
</#if>

<#list topAdditionalCodeList as code>
${code}

</#list>
<#list fields as field>
<#if field.comment?has_content>
${field.comment}
</#if>
<#if field.primaryKey>
  @Id
</#if>
<#if field.autoIncrement>
  <#if field.generatedValueStrategy?has_content>
  @GeneratedValue(strategy = GenerationType.${field.generatedValueStrategy})
  <#else>
  @GeneratedValue
  </#if>
</#if>
<#list field.annotations as annotation>
  ${annotation.toString()}
</#list>
  @Column(name = "\"${field.columnName}\"", nullable = ${field.nullable?c}<#if field.length??>, length = ${field.length?c}</#if><#if field.precision??>, precision = ${field.precision?c}</#if><#if field.scale??>, scale = ${field.scale?c}</#if>)
  private ${field.type} ${field.name}<#if field.defaultValue??> = ${field.defaultValue}</#if>;
</#list>
<#list foreignKeyFields as foreignKey>
  @ManyToOne
  @JoinColumn(name = "\"${foreignKey.joinColumn.columnName}\"", referencedColumnName = "\"${foreignKey.joinColumn.referencedColumnName}\"", insertable = ${generateRelationshipsInsertable?c}, updatable = ${generateRelationshipsUpdatable?c})
  private ${foreignKey.type} ${foreignKey.name};
</#list>
<#list foreignCompositeKeyFields as foreignCompositeKey>
  @ManyToOne
  @JoinColumns({
  <#list foreignCompositeKey.joinColumns as joinColumn>
    @JoinColumn(name = "\"${joinColumn.columnName}\"", referencedColumnName = "\"${joinColumn.referencedColumnName}\"", insertable = ${generateRelationshipsInsertable?c}, updatable = ${generateRelationshipsUpdatable?c}),
  </#list>
  })
  private ${foreignCompositeKey.type} ${foreignCompositeKey.name};
</#list>
<#list importedKeyFields as importedKey>
  @OneToMany(mappedBy = "${importedKey.mappedBy}")
  private java.util.List<${importedKey.name}> listOf${importedKey.name};
</#list>
<#list bottomAdditionalCodeList as code>

${code}
</#list>
}
