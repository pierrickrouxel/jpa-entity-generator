package fr.pierrickrouxel.jpaentitygenerator;

import fr.pierrickrouxel.jpaentitygenerator.config.JdbcSettings;

import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDatabase {
  public final static JdbcSettings jdbcSettings = JdbcSettings.builder()
      .url("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
      .driverClassName("org.h2.Driver")
      .schemaPattern("PUBLIC")
      .build();

  public static void init() throws SQLException {
    try (var conn = DriverManager.getConnection(jdbcSettings.getUrl())) {
      conn.prepareStatement("CREATE TABLE IF NOT EXISTS blog (" +
          "id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL, " +
          "name VARCHAR(30), " +
          "active TINYINT DEFAULT 0, " +
          "created_at TIMESTAMP NOT NULL" +
          ")").execute();
      conn.prepareStatement("CREATE TABLE IF NOT EXISTS article (" +
          "id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL, " +
          "blog_id INTEGER COMMENT 'database comment for blog_id' references blog(id), " +
          "name VARCHAR(30), " +
          "tags text, " +
          "created_at TIMESTAMP NOT NULL" +
          ")").execute();
      conn.prepareStatement("CREATE TABLE IF NOT EXISTS tag (" +
          "id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL, " +
          "tag VARCHAR(100), " +
          "average NUMERIC(9,2), " +
          "created_at TIMESTAMP NOT NULL" +
          ")").execute();
      conn.prepareStatement("CREATE TABLE IF NOT EXISTS article_tag (" +
          "id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL, " +
          "article_id INTEGER NOT NULL COMMENT 'database comment for article_id' REFERENCES ARTICLE(id), " +
          "tag_id INTEGER NOT NULL COMMENT 'database comment for blog_id' REFERENCES TAG(id), " +
          "created_at TIMESTAMP NOT NULL" +
          ")").execute();
      conn.prepareStatement("CREATE TABLE IF NOT EXISTS something_tmp (" +
          "identifier VARCHAR(50) PRIMARY KEY NOT NULL, " +
          "expiration_timestamp INTEGER NOT NULL, " +
          "config TEXT" +
          ")").execute();
      conn.prepareStatement("ALTER TABLE something_tmp ADD CONSTRAINT uk_something_tmp " +
          "UNIQUE (" +
          "identifier, expiration_timestamp" +
          ")").execute();
      conn.prepareStatement("CREATE TABLE IF NOT EXISTS something2_tmp (" +
          "identifier VARCHAR(50) PRIMARY KEY NOT NULL, " +
          "expiration_timestamp INTEGER NOT NULL, " +
          "config TEXT" +
          ")").execute();
      conn.prepareStatement("ALTER TABLE something2_tmp ADD CONSTRAINT fk_something2_tmp " +
          "FOREIGN KEY (" +
          "identifier, expiration_timestamp" +
          ") REFERENCES something_tmp (" +
          "identifier, expiration_timestamp" +
          ")").execute();
      conn.prepareStatement("CREATE TABLE IF NOT EXISTS sales (" +
        " id_sales VARCHAR(50) PRIMARY KEY NOT NULL, " +
        " warehouse char(2), " +
        " reference bigint NULL, " +
        " customer char(30)," +
        " CONSTRAINT u_sales UNIQUE (warehouse,reference)" +
        ")").execute();
      conn.prepareStatement("CREATE TABLE IF NOT EXISTS detail (" +
        "id_detail VARCHAR(50) PRIMARY KEY NOT NULL, " +
        " warehouse char(2), " +
        " reference bigint NULL, " +
        " position int NOT NULL," +
        " price int," +
        " CONSTRAINT u_detail UNIQUE (warehouse,reference,position)," +
        " CONSTRAINT fk_2 FOREIGN KEY (warehouse,reference) REFERENCES sales(warehouse,reference)" +
        ")").execute();
      conn.prepareStatement("CREATE TABLE IF NOT EXISTS detail_information (" +
        " id_detail VARCHAR(50) PRIMARY KEY NOT NULL, " +
        " warehouse char(2), " +
        " reference bigint NULL, " +
        " position int NOT NULL," +
        " article char(30)," +
        " CONSTRAINT fk_1 FOREIGN KEY (warehouse,reference,position) REFERENCES detail(warehouse,reference,position)" +
        ")").execute();
    }
  }

}
