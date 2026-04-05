package io.quarkiverse.jdbc.clickhouse.runtime;

import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilderCustomizer;

public final class ClickHouseConfigBuilder implements SmallRyeConfigBuilderCustomizer {
    @Override
    public void configBuilder(SmallRyeConfigBuilder builder) {
        builder.withSources(new ClickHouseConfigSourceFactory());
    }
}
