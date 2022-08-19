package io.quarkiverse.quarkus.jdbc.clickhouse.it;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@QuarkusTestResource(ContainerTestDeployer.class)
public class JdbcClickhouseResourceIT extends JdbcClickhouseResourceTest {
}
