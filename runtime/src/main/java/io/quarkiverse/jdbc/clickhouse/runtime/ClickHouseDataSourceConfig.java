package io.quarkiverse.jdbc.clickhouse.runtime;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithName;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;

/**
 * ClickHouse-specific datasource properties.
 */
@ConfigMapping(prefix = "quarkus.datasource")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ClickHouseDataSourceConfig {
    /**
     * ClickHouse-specific properties for default and named datasources.
     */
    @ConfigDocMapKey("datasource-name")
    @WithParentName
    @WithDefaults
    @WithUnnamedKey("<default>")
    Map<String, DataSourceOuterNamedClickHouseConfig> dataSources();

    interface DataSourceOuterNamedClickHouseConfig {
        /**
         * ClickHouse driver settings for this datasource.
         */
        ClickHouseRuntimeConfig clickhouse();
    }

    @ConfigGroup
    interface ClickHouseRuntimeConfig {
        /**
         * Client name sent by the ClickHouse JDBC driver.
         */
        @WithName("client-name")
        Optional<String> clientName();

        /**
         * Enables request compression.
         */
        Optional<Boolean> compress();

        /**
         * Enables response decompression.
         */
        Optional<Boolean> decompress();

        /**
         * Connection timeout passed to the ClickHouse driver.
         */
        @WithName("connection-timeout")
        Optional<String> connectionTimeout();

        /**
         * Socket timeout passed to the ClickHouse driver.
         */
        @WithName("socket-timeout")
        Optional<String> socketTimeout();

        /**
         * Enables TCP keepalive on sockets created by the ClickHouse driver.
         */
        @WithName("socket-keepalive")
        Optional<Boolean> socketKeepalive();

        /**
         * Enables SSL.
         */
        Optional<Boolean> ssl();

        /**
         * SSL mode used by the ClickHouse driver.
         */
        @WithName("ssl-mode")
        Optional<String> sslMode();

        /**
         * Uses the server time zone when reading temporal values.
         */
        @WithName("use-server-time-zone")
        Optional<Boolean> useServerTimeZone();

        /**
         * Explicit time zone used by the ClickHouse driver.
         */
        @WithName("use-time-zone")
        Optional<String> useTimeZone();

        /**
         * Session identifier.
         */
        @WithName("session-id")
        Optional<String> sessionId();

        /**
         * Enables session validation.
         */
        @WithName("session-check")
        Optional<Boolean> sessionCheck();

        /**
         * Enables the beta row binary optimization for simple inserts.
         */
        @WithName("beta-row-binary-for-simple-insert")
        Optional<Boolean> betaRowBinaryForSimpleInsert();

        /**
         * Additional raw ClickHouse JDBC properties appended as-is.
         */
        Map<String, String> properties();
    }
}
