package io.quarkiverse.quarkus.jdbc.clickhouse.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class JdbcClickhouseProcessor {

    private static final String FEATURE = "jdbc-clickhouse";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
