package io.quarkiverse.jdbc.clickhouse.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;

import io.quarkiverse.jdbc.clickhouse.runtime.ClickHouseConfigSourceFactory;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigValue;

public class JdbcClickhouseTest {
    @Test
    void aliasesDefaultDatasourceProperties() {
        ConfigSource configSource = new ClickHouseConfigSourceFactory()
                .getConfigSources(new MapBackedContext(Map.of(
                        "quarkus.datasource.clickhouse.client-name", "quarkus-test-client",
                        "quarkus.datasource.clickhouse.properties.custom_flag", "enabled")))
                .iterator()
                .next();

        assertEquals("quarkus-test-client",
                configSource.getValue("quarkus.datasource.jdbc.additional-jdbc-properties.client_name"));
        assertEquals("enabled",
                configSource.getValue("quarkus.datasource.jdbc.additional-jdbc-properties.custom_flag"));
    }

    static final class MapBackedContext implements ConfigSourceContext {
        private final Map<String, String> properties;

        MapBackedContext(Map<String, String> properties) {
            this.properties = properties;
        }

        @Override
        public ConfigValue getValue(String name) {
            String value = properties.get(name);
            if (value == null) {
                return null;
            }
            return ConfigValue.builder()
                    .withName(name)
                    .withValue(value)
                    .withConfigSourceName("test")
                    .withConfigSourceOrdinal(1000)
                    .build();
        }

        @Override
        public Iterator<String> iterateNames() {
            return properties.keySet().iterator();
        }

        @Override
        public List<String> getProfiles() {
            return List.of();
        }
    }
}
