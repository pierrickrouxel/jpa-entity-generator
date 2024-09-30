package fr.pierrickrouxel.jpaentitygenerator.config;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JdbcSettings implements Serializable {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String schemaPattern;
}
