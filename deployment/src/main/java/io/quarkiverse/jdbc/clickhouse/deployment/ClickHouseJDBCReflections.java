package io.quarkiverse.jdbc.clickhouse.deployment;

import static io.quarkiverse.jdbc.clickhouse.deployment.JdbcClickhouseProcessor.DATA_SOURCE_NAME;
import static io.quarkiverse.jdbc.clickhouse.deployment.JdbcClickhouseProcessor.DRIVER_NAME;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

/**
 * Registers the {@code org.sqlite.JDBC} so that it can be loaded
 * by reflection, as commonly expected.
 *
 * @author Alexey Sharandin <sanders@yandex.ru>
 */

@SuppressWarnings("unused")
public class ClickHouseJDBCReflections {
    @BuildStep
    void build(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        //Not strictly necessary when using Agroal, as it also registers
        //any JDBC driver being configured explicitly through its configuration.
        //We register it for the sake of people not using Agroal.
        reflectiveClass.produce(new ReflectiveClassBuildItem(false, false, DRIVER_NAME));
        reflectiveClass.produce(new ReflectiveClassBuildItem(false, false, DATA_SOURCE_NAME));
    }
}
