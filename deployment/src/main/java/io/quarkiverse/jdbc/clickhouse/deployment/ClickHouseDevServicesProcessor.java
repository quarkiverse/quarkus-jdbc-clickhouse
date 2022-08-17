package io.quarkiverse.jdbc.clickhouse.deployment;

import static io.quarkus.datasource.deployment.spi.DatabaseDefaultSetupConfig.DEFAULT_DATABASE_NAME;
import static io.quarkus.datasource.deployment.spi.DatabaseDefaultSetupConfig.DEFAULT_DATABASE_PASSWORD;
import static io.quarkus.datasource.deployment.spi.DatabaseDefaultSetupConfig.DEFAULT_DATABASE_USERNAME;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.jboss.logging.Logger;
import org.testcontainers.containers.ClickHouseContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.datasource.deployment.spi.DevServicesDatasourceProvider;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceProviderBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerShutdownCloseable;

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
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem) {
        return new DevServicesDatasourceProviderBuildItem(JdbcClickhouseProcessor.DB_KIND,
                (username, password, datasourceName, containerConfig, launchMode, startupTimeout) -> {
                    QuarkusClickHouseSQLContainer container = new QuarkusClickHouseSQLContainer(containerConfig.getImageName(),
                            containerConfig.getFixedExposedPort(),
                            !devServicesSharedNetworkBuildItem.isEmpty());
                    startupTimeout.ifPresent(container::withStartupTimeout);

                    String effectiveUsername = containerConfig.getUsername().orElse(username.orElse(DEFAULT_DATABASE_USERNAME));
                    String effectivePassword = containerConfig.getPassword().orElse(password.orElse(DEFAULT_DATABASE_PASSWORD));
                    String effectiveDbName = containerConfig.getDbName().orElse(datasourceName.orElse(DEFAULT_DATABASE_NAME));

                    container.withUsername(effectiveUsername)
                            .withPassword(effectivePassword)
                            .withDatabaseName(effectiveDbName)
                            .withReuse(true);
                    containerConfig.getAdditionalJdbcUrlProperties().forEach(container::withUrlParam);
                    containerConfig.getCommand().ifPresent(container::setCommand);

                    container.start();

                    LOG.info("Dev Services for ClickHouse started.");
                    System.out.println("!!!Dev Services for ClickHouse started.");

                    return new DevServicesDatasourceProvider.RunningDevServicesDatasource(container.getContainerId(),
                            container.getEffectiveJdbcUrl(),
                            container.getReactiveUrl(),
                            container.getUsername(),
                            container.getPassword(),
                            new ContainerShutdownCloseable(container, "ClickHouseSQL"));
                });
    }

    private static class QuarkusClickHouseSQLContainer extends ClickHouseContainer {
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
    }
}
