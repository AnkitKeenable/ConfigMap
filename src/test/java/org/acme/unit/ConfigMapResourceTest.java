package org.acme.unit;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
testGenerateConfigMapEndpoint()

Tests the complete workflow via HTTP

Verifies:

Successful 200 response

Proper YAML structure

Contains required Kubernetes fields

Uses classpath resources for blueprint and config

Tests integration between components
 */

//@QuarkusTest
//public class ConfigMapResourceTest {
//
//    @Test
//    public void testGenerateConfigMapEndpoint() {
//        // Using classpath-relative paths
//        String blueprint = "sample-blueprint.xml";
//        String config = "sample-dummy.cfg";
//
//        given()
//                .queryParam("blueprint", blueprint)
//                .queryParam("config", config)
//                .when()
//                .get("/configmap")
//                .then()
//                .statusCode(200)
//                .body(containsString("kind: ConfigMap"))
//                .body(containsString("data:"));
//    }
//}


