package io.quarkiverse.jdbc.clickhouse.runtime;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.common.MapBackedConfigSource;

public final class ClickHouseConfigSourceFactory implements ConfigSourceFactory {
    private static final int ORDINAL = 275;

    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        Map<String, String> mapped = ClickHouseConfigPropertyMapper.toJdbcProperties(
                context::iterateNames,
                propertyName -> {
                    ConfigValue value = context.getValue(propertyName);
                    return value != null ? value.getValue() : null;
                });

        if (mapped.isEmpty()) {
            return List.of();
        }

        return List.of(new ClickHouseAliasConfigSource(mapped));
    }

    private static final class ClickHouseAliasConfigSource extends MapBackedConfigSource {
        private ClickHouseAliasConfigSource(Map<String, String> properties) {
            super("clickhouse-datasource-aliases", properties, ORDINAL);
        }
    }
}
