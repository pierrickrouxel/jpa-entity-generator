# jpa-entity-generator

![GitHub Release](https://img.shields.io/github/v/release/pierrickrouxel/jpa-entity-generator)
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/pierrickrouxel/jpa-entity-generator/build.yaml)
[![Donate](https://img.shields.io/badge/donate-buy%20me%20a%20coffee-yellow?logo=buy-me-a-coffee)](https://www.buymeacoffee.com/pierrickrouxel)

This is a Java library which generates Lombok-wired JPA entity source code.
The project provides a Gradle plugin.

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
