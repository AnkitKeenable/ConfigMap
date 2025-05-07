package org.acme.unit;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
testGenerateConfigMapEndpoint()
Tests the REST endpoint that generates ConfigMaps.

blueprint: sample-blueprint.xml

config: sample-dummy.cfg

HTTP 200 response

Response contains "kind: ConfigMap" and "data:"


 */

@QuarkusTest
public class ConfigMapResourceTest {

    @Test
    public void testGenerateConfigMapEndpoint() {
        String blueprint = "src/test/resources/sample-blueprint.xml";
        String config = "src/test/resources/sample-dummy.cfg";

        Response response = given()
                .queryParam("blueprint", blueprint)
                .queryParam("config", config)
                .when()
                .get("/configmap")
                .then()
                .statusCode(200)
                .extract().response();

        String yaml = response.asString();
        assertTrue(yaml.contains("kind: ConfigMap"));
        assertTrue(yaml.contains("data:"));
    }
}


