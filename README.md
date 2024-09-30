# jpa-entity-generator

This is a Java library which generates Lombok-wired JPA entity source code.
The project provides Gradle plugin.

## Compatibility

- Java >= 21
- jakarta.persistence

## Getting Started

Configure you gradle project with the following build:

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

Add a configuration file for your generation:

```yaml
jdbcSettings:
  url: "jdbc:h2:file:mem:"
  driverClassName: "org.h2.Driver"

packageName: "com.example.entity"
```

You can find all values here: [src/test/resources/jpa-entity-generator.yaml]()

## Run the gradle task

```bash
./gradlew generateEntities
```

The `generateEntities` task generates entity classes for all the existing tables in the database.

### Test project

To run the unit tests, simply run the following command:

```sh
./gradlew test
```

## Thanks

This repository is a fork of [https://github.com/smartnews/jpa-entity-generator]().
Since the project seems to be abandonned, we decide to fork it dropping compatibility with legacy java versions.
Thank you for you job.
