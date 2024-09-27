package fr.pierrickrouxel.jpaentitygenerator.config;

import java.io.Serializable;

import lombok.Data;

/**
 * JDBC connection settings.
 */
@Data
public class JDBCSettings implements Serializable {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String schemaPattern;
}
