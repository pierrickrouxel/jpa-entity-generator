# jpa-entity-generator

This is a Java library which generates Lombok-wired JPA entity source code. The project provides Gradle plugin and Maven plugin.

## Getting Started

### build.gradle

```groovy
buildscript {
  dependencies {
    classpath 'com.h2database:h2:2.3.232'
  }
}

plugins {
  id 'java'
  id 'fr.pierrickrouxel.jpa-entity-generator' version '1.0.0'
}

repositories {
  mavenCentral()
}

jpaEntityGenerator {
  configPath = 'src/main/resources/jpa-entity-generator.yaml'
}
```

### src/main/resources/jpa-entity-generator.yaml

```yaml
jdbcSettings:
  url: "jdbc:h2:file:./build/db/test"
  driverClassName: "org.h2.Driver"

packageName: "com.example.entity"
```

### entityGen task

```bash
./gradlew entityGen compileJava
```

`entityGen` task generates entity classes for all the existing tables in the database.

### Test project

To run the unit tests, simply run the following command:

```sh
./gradlew test
```

## Thanks

This repository is a fork of [https://github.com/smartnews/jpa-entity-generator]().
Since the project seems to be abandonned, we decide to fork it dropping compatibility with legacy java versions.
Thank you for you job.
