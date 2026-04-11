# Quarkus JDBC ClickHouse extension
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

[![Build](https://github.com/quarkiverse/quarkus-jdbc-clickhouse/workflows/Build/badge.svg)](https://github.com/quarkiverse/quarkus-jdbc-clickhouse/actions?query=workflow%3ABuild)
[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.jdbc/quarkus-jdbc-clickhouse?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.jdbc/quarkus-jdbc-clickhouse)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)

Quarkus JDBC ClickHouse is a Quarkus extension for the ClickHouse database.

## Configuration

Besides the standard `quarkus.datasource.jdbc.*` settings, the extension also supports
ClickHouse-specific aliases under `quarkus.datasource.clickhouse.*`.

Example:

```properties
quarkus.datasource.db-kind=clickhouse
quarkus.datasource.jdbc.url=jdbc:clickhouse:http://localhost:8123/default
quarkus.datasource.clickhouse.client-name=my-service
quarkus.datasource.clickhouse.compress=true
quarkus.datasource.clickhouse.socket-keepalive=true
quarkus.datasource.clickhouse.properties.beta.row_binary_for_simple_insert=true
```

Named datasources are supported as well:

```properties
quarkus.datasource."analytics".db-kind=clickhouse
quarkus.datasource."analytics".jdbc.url=jdbc:clickhouse:http://localhost:8123/default
quarkus.datasource."analytics".clickhouse.client-name=analytics-client
quarkus.datasource."analytics".clickhouse.socket-keepalive=false
```

The aliases are translated to `quarkus.datasource[."name"].jdbc.additional-jdbc-properties.*`,
so raw driver properties remain available through `...clickhouse.properties.*`.

Currently supported aliases include:

- `client-name`
- `compress`
- `decompress`
- `connection-timeout`
- `socket-timeout`
- `socket-keepalive`
- `ssl`
- `ssl-mode`
- `use-server-time-zone`
- `use-time-zone`
- `session-id`
- `session-check`
- `beta-row-binary-for-simple-insert`
- `properties.*`

Each property is documented in the Quarkiverse documentation with its purpose and effect on the
underlying ClickHouse JDBC driver configuration.

## Testing

The project follows the usual Quarkus extension split between `runtime`, `deployment`, and
`integration-tests`.

- `mvn verify -Pit` runs the JVM integration tests
- `mvn verify -Pit -Dnative` builds the native runner and executes native integration tests

## User Documentation

https://quarkiverse.github.io/quarkiverse-docs/quarkus-jdbc-clickhouse/dev/index.html

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://www.linkedin.com/in/sharandin/"><img src="https://avatars.githubusercontent.com/u/41162858?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Alexey Sharandin</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-jdbc-clickhouse/commits?author=alexeysharandin" title="Code">💻</a> <a href="#maintenance-alexeysharandin" title="Maintenance">🚧</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
