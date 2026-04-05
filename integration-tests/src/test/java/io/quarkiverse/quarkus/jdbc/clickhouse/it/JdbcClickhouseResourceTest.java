package io.quarkiverse.quarkus.jdbc.clickhouse.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ContainerTestDeployer.class)
public class JdbcClickhouseResourceTest {

    @Test
    public void testAgoralEndpoint() {
        given()
                .when().get("/jdbc-clickhouse/agoral")
                .then()
                .statusCode(200)
                .body(is("1/leo/"));
    }

    @Test
    public void testClickHouseConfigAliases() {
        given()
                .when().get("/jdbc-clickhouse/config")
                .then()
                .statusCode(200)
                .body(is("client_name=quarkus-it-client;socket_keepalive=true"));
    }

    @Test
    public void testNamedDataSourceClickHouseConfigAliases() {
        given()
                .when().get("/jdbc-clickhouse/config/analytics")
                .then()
                .statusCode(200)
                .body(is("client_name=quarkus-analytics-client;socket_keepalive=false"));
    }
}
