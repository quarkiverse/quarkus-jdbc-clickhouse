package io.quarkiverse.jdbc.clickhouse.deployment;

import static io.quarkiverse.jdbc.clickhouse.deployment.JdbcClickhouseProcessor.DRIVER_NAME;
import static io.quarkus.datasource.deployment.spi.DatabaseDefaultSetupConfig.DEFAULT_DATABASE_NAME;
import static io.quarkus.datasource.deployment.spi.DatabaseDefaultSetupConfig.DEFAULT_DATABASE_PASSWORD;
import static io.quarkus.datasource.deployment.spi.DatabaseDefaultSetupConfig.DEFAULT_DATABASE_USERNAME;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.jboss.logging.Logger;
import org.testcontainers.containers.ClickHouseContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.datasource.deployment.spi.DatasourceStartable;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceContainerConfig;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceProvider;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceProviderBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesComposeProjectBuildItem;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.Labels;
import io.quarkus.devservices.common.Volumes;
import io.quarkus.runtime.LaunchMode;

public class ClickHouseDevServicesProcessor {

    private static final Logger LOG = Logger.getLogger(ClickHouseDevServicesProcessor.class);

    /*
     * @BuildStep
     * ConsoleCommandBuildItem psqlCommand(DevServicesLauncherConfigResultBuildItem devServices) {
     * return new ConsoleCommandBuildItem(new PostgresCommand(devServices));
     * }
     */

    @BuildStep
    DevServicesDatasourceProviderBuildItem setupClickHouse(
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            DevServicesComposeProjectBuildItem composeProjectBuildItem) {

        return new DevServicesDatasourceProviderBuildItem(JdbcClickhouseProcessor.DB_KIND, new DevServicesDatasourceProvider() {
            @Override
            public String getFeature() {
                return "jdbc-clickhouse";
            }

            @Override
            public DatasourceStartable createDatasourceStartable(Optional<String> username, Optional<String> password,
                    String datasourceName, DevServicesDatasourceContainerConfig containerConfig,
                    LaunchMode launchMode, boolean useSharedNetwork, Optional<Duration> startupTimeout) {
                QuarkusClickHouseSQLContainer container = new QuarkusClickHouseSQLContainer(containerConfig.getImageName(),
                        containerConfig.getFixedExposedPort(),
                        !devServicesSharedNetworkBuildItem.isEmpty());
                startupTimeout.ifPresent(container::withStartupTimeout);

                String effectiveUsername = containerConfig.getUsername().orElse(username.orElse(DEFAULT_DATABASE_USERNAME));
                String effectivePassword = containerConfig.getPassword().orElse(password.orElse(DEFAULT_DATABASE_PASSWORD));
                String effectiveDbName = containerConfig.getDbName()
                        .orElse(DataSourceUtil.isDefault(datasourceName) ? DEFAULT_DATABASE_NAME : datasourceName);

                container.withUsername(effectiveUsername)
                        .withPassword(effectivePassword)
                        .withDatabaseName(effectiveDbName)
                        .withReuse(true);

                Labels.addDataSourceLabel(container, datasourceName);
                Volumes.addVolumes(container, containerConfig.getVolumes());

                container.withEnv(containerConfig.getContainerEnv());

                containerConfig.getAdditionalJdbcUrlProperties().forEach(container::withUrlParam);
                containerConfig.getCommand().ifPresent(container::setCommand);
                //containerConfig.getInitScriptPath().ifPresent(container::withInitScript);

                return container;
            }

            @Override
            public Optional<DevServicesDatasourceProvider.RunningDevServicesDatasource> findRunningComposeDatasource(
                    LaunchMode launchMode, boolean useSharedNetwork, DevServicesDatasourceContainerConfig containerConfig,
                    DevServicesComposeProjectBuildItem composeProjectBuildItem) {
                // No compose support
                return Optional.empty();
            }
        });
    }

    private static class QuarkusClickHouseSQLContainer extends ClickHouseContainer implements DatasourceStartable {
        private final OptionalInt fixedExposedPort;
        private final boolean useSharedNetwork;

        private String hostName = null;

        public QuarkusClickHouseSQLContainer(Optional<String> imageName, OptionalInt fixedExposedPort,
                boolean useSharedNetwork) {
            super(DockerImageName
                    .parse(imageName.orElseGet(() -> ConfigureUtil.getDefaultImageNameFor("clickhouse")))
                    .asCompatibleSubstituteFor(DockerImageName.parse(ClickHouseContainer.IMAGE)));
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;
        }

        @Override
        protected void configure() {
            super.configure();

            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, "clickhouse");
                return;
            }

            if (fixedExposedPort.isPresent()) {
                addFixedExposedPort(fixedExposedPort.getAsInt(), ClickHouseContainer.HTTP_PORT);
            } else {
                addExposedPort(ClickHouseContainer.HTTP_PORT);
            }
        }

        @Override
        public String getDriverClassName() {
            return DRIVER_NAME;
        }

        // this is meant to be called by Quarkus code and is not strictly needed
        // in the ClickHouseSQL case as testcontainers does not try to establish
        // a connection to determine if the container is ready, but we do it anyway to be consistent across
        // DB containers
        public String getEffectiveJdbcUrl() {
            if (useSharedNetwork) {
                // in this case we expose the URL using the network alias we created in 'configure'
                // and the container port since the application communicating with this container
                // won't be doing port mapping
                String additionalUrlParams = constructUrlParameters("?", "&");
                return "jdbc:ch://" + hostName + ":" + ClickHouseContainer.HTTP_PORT
                        + "/" + getDatabaseName() + additionalUrlParams;
            } else {
                return super.getJdbcUrl();
            }
        }

        public String getReactiveUrl() {
            return getEffectiveJdbcUrl().replaceFirst("jdbc:", "vertx-reactive:");
        }

        @Override
        public void close() {
            super.close();
        }

        @Override
        public String getConnectionInfo() {
            return getEffectiveJdbcUrl();
        }
    }
}
