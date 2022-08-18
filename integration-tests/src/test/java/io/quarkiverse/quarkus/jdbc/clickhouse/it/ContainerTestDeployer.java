package io.quarkiverse.quarkus.jdbc.clickhouse.it;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.util.Map;
import java.util.Optional;

import org.testcontainers.containers.ClickHouseContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class ContainerTestDeployer implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    private Optional<String> containerNetworkId;
    private JdbcDatabaseContainer container;

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        containerNetworkId = context.containerNetworkId();
    }

    @Override
    public Map<String, String> start() {
        container = new QuarkusClickHouseContainer(ClickHouseContainer.IMAGE)
                .withLogConsumer(outputFrame -> {
                });

        containerNetworkId.ifPresent(container::withNetworkMode);
        container.start();
        await().until(container::isRunning);
        String jdbcUrl = container.getJdbcUrl();
        if (containerNetworkId.isPresent()) {
            // Replace hostname + port in the provided JDBC URL with the hostname of the Docker container
            // running PostgreSQL and the listening port.
            jdbcUrl = fixJdbcUrl(jdbcUrl);
        }

        // return a map containing the configuration the application needs to use the service
        return ImmutableMap.of(
                "quarkus.datasource.username", container.getUsername(),
                "quarkus.datasource.password", container.getPassword(),
                "quarkus.datasource.jdbc.url", jdbcUrl);
    }

    private String fixJdbcUrl(String jdbcUrl) {
        // Part of the JDBC URL to replace
        String hostPort = container.getHost() + ':' + container.getMappedPort(ClickHouseContainer.HTTP_PORT);

        // Host/IP on the container network plus the unmapped port
        String networkHostPort = container.getCurrentContainerInfo().getConfig().getHostName()
                + ':'
                + ClickHouseContainer.HTTP_PORT;

        return jdbcUrl.replace(hostPort, networkHostPort);
    }

    @Override
    public void stop() {
        // close container
    }

    private static class QuarkusClickHouseContainer extends ClickHouseContainer {

        public QuarkusClickHouseContainer(String dockerImageName) {
            super(dockerImageName);
        }

        @Override
        public String getDriverClassName() {
            String className = "com.clickhouse.jdbc.ClickHouseDriver";
            try {
                Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return className;
        }
    }
}
