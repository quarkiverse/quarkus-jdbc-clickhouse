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
        // TODO: uncomment
        System.out.println("Start IT test");
        given()
                .when().get("/jdbc-clickhouse/agoral")
                .then()
                .statusCode(200)
                .body(is("1/leo/"));
        System.out.println("IT finished");

    }
}
