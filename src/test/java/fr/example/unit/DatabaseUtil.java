package fr.example.unit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    public static void init() throws SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:h2:file:./build/db/test")) {
            conn.prepareStatement("create table if not exists blog (" +
                    "id integer primary key auto_increment not null, " +
                    "name varchar(30), " +
                    "active tinyint default 0, " +
                    "created_at timestamp not null" +
                    ")").execute();
            conn.prepareStatement("create table if not exists article (" +
                    "id integer primary key auto_increment not null, " +
                    "blog_id integer comment 'database comment for blog_id' references blog(id), " +
                    "name varchar(30), tags text, " +
                    "created_at timestamp not null" +
                    ")").execute();
            conn.prepareStatement("create table if not exists tag (" +
                    "id integer primary key auto_increment not null, " +
                    "tag varchar(100), " +
                    "average numeric(9,2), " +
                    "created_at timestamp not null" +
                    ")").execute();
            conn.prepareStatement("create table if not exists article_tag (" +
                    "id integer primary key auto_increment not null, " +
                    "article_id integer not null comment 'database comment for article_id' references article(id), " +
                    "tag_id integer not null comment 'database comment for blog_id' references tag(id), " +
                    "created_at timestamp not null" +
                    ")").execute();
            conn.prepareStatement("create table if not exists abtest (" +
                    "identifier varchar(50) primary key not null, " +
                    "expiration_timestamp integer not null, " +
                    "config text" +
                    ")").execute();
            conn.prepareStatement("create table if not exists something_tmp (" +
                    "identifier varchar(50) primary key not null, " +
                    "expiration_timestamp integer not null, " +
                    "config text" +
                    ")").execute();
            conn.prepareStatement("create table if not exists something_tmp2 (" +
                    "identifier varchar(50) primary key not null, " +
                    "expiration_timestamp integer not null, " +
                    "config text" +
                    ")").execute();
        }

    }
}
