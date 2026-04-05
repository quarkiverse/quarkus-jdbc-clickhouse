package io.quarkiverse.jdbc.clickhouse.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;

import io.quarkiverse.jdbc.clickhouse.runtime.ClickHouseConfigSourceFactory;

public class JdbcClickhouseDevModeTest {
    @Test
    void aliasesNamedDatasourceProperties() {
        ConfigSource configSource = new ClickHouseConfigSourceFactory()
                .getConfigSources(new JdbcClickhouseTest.MapBackedContext(Map.of(
                        "quarkus.datasource.\"analytics\".clickhouse.socket-keepalive", "true",
                        "quarkus.datasource.\"analytics\".clickhouse.session-id", "analytics-session")))
                .iterator()
                .next();

        assertEquals("true",
                configSource.getValue(
                        "quarkus.datasource.\"analytics\".jdbc.additional-jdbc-properties.socket_keepalive"));
        assertEquals("analytics-session",
                configSource.getValue(
                        "quarkus.datasource.\"analytics\".jdbc.additional-jdbc-properties.session_id"));
    }
}
