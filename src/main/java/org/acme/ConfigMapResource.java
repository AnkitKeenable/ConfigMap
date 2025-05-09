    package org.acme;

    import io.quarkus.runtime.annotations.QuarkusMain;
    import jakarta.ws.rs.*;
    import jakarta.ws.rs.core.MediaType;
    import jakarta.ws.rs.core.Response;
    import org.acme.util.*;

    import java.util.Map;
    import java.util.Properties;

    @Path("/configmap")
    public class ConfigMapResource {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public Response generateConfigMap(
                @QueryParam("blueprint") String blueprintPath,
                @QueryParam("config") String configPath) {
            try {
                // Validate inputs
                if (blueprintPath == null || blueprintPath.isEmpty() ||
                        configPath == null || configPath.isEmpty()) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Both blueprint and config parameters are required")
                            .build();
                }

                // 1. Extract patterns from blueprint
                Map<String, String> keyPatterns = BlueprintParser.extractKeyPatternsFromBlueprint(blueprintPath);

                // 2. Validate config
                Properties config = ConfigValidator.readAndValidateDummyConfig(configPath, keyPatterns);

                // 3. Generate ConfigMap
                String configMapYaml = ConfigMapGenerator.generateConfigMapAsString(config, keyPatterns);

                return Response.ok(configMapYaml).build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error generating ConfigMap: " + e.getMessage())
                        .build();
            }
        }
    }


    /*
    Purpose: Exposes the functionality as a web service

Example Request:

GET /configmap?blueprint=sample-blueprint.xml&config=sample-config.properties
Response:

yaml
HTTP 200 OK
Content-Type: text/yaml

apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  labels:
    generated-by: strict-configmap-generator
data:
  api_version: v1
  app: inventory
  env: prod
  prod_inventory: "null"
     */




    /*
    Blueprint defines needed configurations:

xml
<to uri="http://service/{{version}}/users/{{user}}_{{domain}}"/>
Config File provides values:

properties
version=2
user=admin
domain=example.com
System:

Parses blueprint → finds version, user_+domain patterns

Validates config → checks admin_example.com exists

Generates ConfigMap:

yaml
data:
  version: 2
  user: admin
  domain: example.com
  admin_example.com: "null"
REST API returns this YAML to be applied to Kubernetes.
     */