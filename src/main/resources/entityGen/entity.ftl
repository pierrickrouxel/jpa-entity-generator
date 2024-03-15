package ${packageName};
<#if headerComment??>

${headerComment}</#if><#list importRules as rule>import ${rule.importValue};
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
@Table(<#if schemaName??>schema = "${schemaName}", </#if>name = "${tableName}")<#if primaryKeyFields.size() \gt 1>
@IdClass(${className}.PrimaryKeys.class)</#if>
public class ${className}<#if interfaceNames.size() \gt 0> implements ${interfaceNames?join(", ")}</#if> {
<#if primaryKeyFields.size() \gt 1><#if foldCode>
// <editor-fold defaultstate="collapsed" desc="Generated PK-class">
</#if>  @Data
    public static class PrimaryKeys implements Serializable {
    <#list primaryKeyFields as field>
      private ${field.type} ${field.name}<#if field.defaultValue??> = ${field.defaultValue}</#if>;
    </#list>
    }<#if foldCode>
// </editor-fold>
</#if></#if>

<#list topAdditionalCodeList as code>
${code}

</#list><#if foldCode>
// <editor-fold defaultstate="collapsed" desc="Generated fields">
</#if><#list fields as field>
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
<#if requireJSR305 && !field.primitive>
    <#if field.nullable>@Nullable<#else>@Nonnull</#if>
</#if>
    @Column(name = "<#if jpa1Compatible>`<#else><#if quotedColumnNames>\"</#if></#if>${field.columnName}<#if jpa1Compatible>`<#else><#if quotedColumnNames>\"</#if></#if>", nullable = ${field.nullable?c}<#if field.length??>, length = ${field.length?c}</#if><#if field.precision??>, precision = ${field.precision?c}</#if><#if field.scale??>, scale = ${field.scale?c}</#if>)
    private ${field.type} ${field.name}<#if field.defaultValue??> = ${field.defaultValue}</#if>;
</#list>

<#list foreignKeyFields as foreignKey>
    <#if foreignKey.oneToOne>@OneToOne<#else>@ManyToOne</#if>
    @JoinColumn(name = "<#if jpa1Compatible>`<#else><#if quotedColumnNames>\"</#if></#if>${foreignKey.joinColumn.columnName}<#if jpa1Compatible>`<#else><#if quotedColumnNames>\"</#if></#if>", referencedColumnName = "<#if jpa1Compatible>`<#else><#if quotedColumnNames>\"</#if></#if>${foreignKey.joinColumn.referencedColumnName}<#if jpa1Compatible>`<#else><#if quotedColumnNames>\"</#if></#if>", insertable = ${generateRelationshipsInsertable?c}, updatable = ${generateRelationshipsUpdatable?c})
    private ${foreignKey.type} ${foreignKey.name};
</#list>
<#list foreignCompositeKeyFields as foreignCompositeKey>
    <#if foreignCompositeKey.oneToOne>@OneToOne<#else>@ManyToOne</#if>
    @JoinColumns({
    <#list foreignCompositeKey.joinColumns as joinColumn>
        @JoinColumn(name = "<#if jpa1Compatible>`<#else><#if quotedColumnNames>\"</#if></#if>${joinColumn.columnName}<#if jpa1Compatible>`<#else><#if quotedColumnNames>\"</#if></#if>", referencedColumnName = "<#if jpa1Compatible>`<#else><#if quotedColumnNames>\"</#if></#if>${joinColumn.referencedColumnName}<#if jpa1Compatible>`<#else><#if quotedColumnNames>\"</#if></#if>", insertable = ${generateRelationshipsInsertable?c}, updatable = ${generateRelationshipsUpdatable?c})<#sep>,</#sep>
    </#list>
    })
    private ${foreignCompositeKey.type} ${foreignCompositeKey.name};
</#list>
<#list importedKeyFields as importedKey><#if importedKey.oneToOne>
    @OneToOne(mappedBy = "${importedKey.mappedBy}")
    private ${importedKey.type} ${importedKey.name};
<#else>
    @OneToMany(mappedBy = "${importedKey.mappedBy}")
    private java.util.List<${importedKey.type}> ${importedKey.name}List;
</#if></#list><#if foldCode>
// </editor-fold>

</#if><#list bottomAdditionalCodeList as code>

${code}
</#list>
}

