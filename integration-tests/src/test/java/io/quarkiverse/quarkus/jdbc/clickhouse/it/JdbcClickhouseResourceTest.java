package io.quarkiverse.quarkus.jdbc.clickhouse.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JdbcClickhouseResourceTest {

    @Test
    public void testAgoralEndpoint() {
        // TODO: uncomment
        /*given()
                .when().get("/jdbc-clickhouse/agoral")
                .then()
                .statusCode(200)
                .body(is("1/leo/2/yui/"));*/
    }
}
