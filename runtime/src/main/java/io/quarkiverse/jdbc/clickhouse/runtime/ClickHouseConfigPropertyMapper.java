package io.quarkiverse.jdbc.clickhouse.runtime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

final class ClickHouseConfigPropertyMapper {
    private static final String DATASOURCE_PREFIX = "quarkus.datasource";
    private static final String CLICKHOUSE_SEGMENT = ".clickhouse.";
    private static final String JDBC_PROPERTIES_SEGMENT = ".jdbc.additional-jdbc-properties.";
    private static final String RAW_PROPERTIES_PREFIX = "properties.";

    private static final Map<String, String> SUPPORTED_PROPERTIES = Map.ofEntries(
            Map.entry("client-name", "client_name"),
            Map.entry("compress", "compress"),
            Map.entry("decompress", "decompress"),
            Map.entry("connection-timeout", "connection_timeout"),
            Map.entry("socket-timeout", "socket_timeout"),
            Map.entry("socket-keepalive", "socket_keepalive"),
            Map.entry("ssl", "ssl"),
            Map.entry("ssl-mode", "ssl_mode"),
            Map.entry("use-server-time-zone", "use_server_time_zone"),
            Map.entry("use-time-zone", "use_time_zone"),
            Map.entry("session-id", "session_id"),
            Map.entry("session-check", "session_check"),
            Map.entry("beta-row-binary-for-simple-insert", "beta.row_binary_for_simple_insert"));

    private ClickHouseConfigPropertyMapper() {
    }

    static Map<String, String> toJdbcProperties(Iterable<String> propertyNames, PropertyValueResolver resolver) {
        Map<String, String> mapped = new LinkedHashMap<>();
        for (String propertyName : propertyNames) {
            mapProperty(propertyName).ifPresent(targetProperty -> {
                String value = resolver.getValue(propertyName);
                if (value != null) {
                    mapped.put(targetProperty, value);
                }
            });
        }
        return mapped;
    }

    static Optional<String> mapProperty(String propertyName) {
        if (propertyName.startsWith(DATASOURCE_PREFIX + CLICKHOUSE_SEGMENT)) {
            return mapSuffix(DATASOURCE_PREFIX, propertyName.substring((DATASOURCE_PREFIX + CLICKHOUSE_SEGMENT).length()));
        }

        if (!propertyName.startsWith(DATASOURCE_PREFIX + ".\"")) {
            return Optional.empty();
        }

        int clickHouseIndex = propertyName.indexOf(CLICKHOUSE_SEGMENT, DATASOURCE_PREFIX.length());
        if (clickHouseIndex < 0) {
            return Optional.empty();
        }

        String datasourcePrefix = propertyName.substring(0, clickHouseIndex);
        String suffix = propertyName.substring(clickHouseIndex + CLICKHOUSE_SEGMENT.length());
        return mapSuffix(datasourcePrefix, suffix);
    }

    private static Optional<String> mapSuffix(String datasourcePrefix, String suffix) {
        if (suffix.isBlank()) {
            return Optional.empty();
        }

        if (suffix.startsWith(RAW_PROPERTIES_PREFIX)) {
            String rawKey = suffix.substring(RAW_PROPERTIES_PREFIX.length());
            if (rawKey.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(datasourcePrefix + JDBC_PROPERTIES_SEGMENT + rawKey);
        }

        String mappedKey = SUPPORTED_PROPERTIES.get(suffix);
        if (mappedKey == null) {
            return Optional.empty();
        }

        return Optional.of(datasourcePrefix + JDBC_PROPERTIES_SEGMENT + mappedKey);
    }

    @FunctionalInterface
    interface PropertyValueResolver {
        String getValue(String propertyName);
    }
}
