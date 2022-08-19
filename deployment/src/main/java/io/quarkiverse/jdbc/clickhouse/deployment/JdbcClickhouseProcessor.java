package io.quarkiverse.jdbc.clickhouse.deployment;

import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.jdbc.ClickHouseDataSource;
import com.clickhouse.jdbc.ClickHouseDriver;

import io.quarkiverse.jdbc.clickhouse.runtime.ClickHouseAgroalConnectionConfigurer;
import io.quarkus.agroal.spi.JdbcDriverBuildItem;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.datasource.deployment.spi.DefaultDataSourceDbKindBuildItem;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceConfigurationHandlerBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.SslNativeConfigBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;

@SuppressWarnings("unused")
class JdbcClickhouseProcessor {

    private static final String FEATURE = "jdbc-clickhouse";

    static final String DB_KIND = "clickhouse";
    static final String DRIVER_NAME = ClickHouseDriver.class.getName();
    static final String DATA_SOURCE_NAME = ClickHouseDataSource.class.getName();

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerDriver(BuildProducer<JdbcDriverBuildItem> jdbcDriver,
            SslNativeConfigBuildItem sslNativeConfigBuildItem) {

        jdbcDriver.produce(
                new JdbcDriverBuildItem(
                        DB_KIND,
                        DRIVER_NAME,
                        DATA_SOURCE_NAME));
    }

    @BuildStep
    void registerServices(BuildProducer<ServiceProviderBuildItem> items) {
        items.produce(new ServiceProviderBuildItem(
                ClickHouseClient.class.getName(), com.clickhouse.client.http.ClickHouseHttpClient.class.getName()));
        //items.produce(new ServiceProviderBuildItem(
        //        ClickHouseClient.class.getName(), com.clickhouse.client.cli.ClickHouseCommandLineClient.class.getName()));

        //items.produce(new ServiceProviderBuildItem(
        //        ClickHouseClient.class.getName(), com.clickhouse.client.grpc.ClickHouseGrpcClient.class.getName()));

    }

    @BuildStep
    DevServicesDatasourceConfigurationHandlerBuildItem devDbHandler() {
        return DevServicesDatasourceConfigurationHandlerBuildItem.jdbc(DB_KIND);
    }

    @BuildStep
    void configureAgroalConnection(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            Capabilities capabilities) {
        if (capabilities.isPresent(Capability.AGROAL)) {
            additionalBeans
                    .produce(new AdditionalBeanBuildItem.Builder().addBeanClass(ClickHouseAgroalConnectionConfigurer.class)
                            .setDefaultScope(BuiltinScope.APPLICATION.getName())
                            .setUnremovable()
                            .build());
        }
    }

    @BuildStep
    void registerDefaultDbType(BuildProducer<DefaultDataSourceDbKindBuildItem> dbKind) {
        dbKind.produce(new DefaultDataSourceDbKindBuildItem(DB_KIND));
    }
}
