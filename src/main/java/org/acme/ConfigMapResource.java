package org.acme;

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