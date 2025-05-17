//    package org.acme;
//
//    import io.quarkus.runtime.annotations.QuarkusMain;
//    import jakarta.ws.rs.*;
//    import jakarta.ws.rs.core.MediaType;
//    import jakarta.ws.rs.core.Response;
//    import org.acme.util.*;
//
//    import java.util.Map;
//    import java.util.Properties;
//
//    @Path("/configmap")
//    public class ConfigMapResource {
//
//        @GET
//        @Produces(MediaType.TEXT_PLAIN)
//        public Response generateConfigMap(
//                @QueryParam("blueprint") String blueprintPath,
//                @QueryParam("config") String configPath) {
//            try {
//                // Validate inputs
//                if (blueprintPath == null || blueprintPath.isEmpty() ||
//                        configPath == null || configPath.isEmpty()) {
//                    return Response.status(Response.Status.BAD_REQUEST)
//                            .entity("Both blueprint and config parameters are required")
//                            .build();
//                }
//
//                // 1. Extract patterns from blueprint
//                Map<String, String> keyPatterns = BlueprintParser.extractKeyPatternsFromBlueprint(blueprintPath);
//
//                // 2. Validate config
//                Properties config = ConfigValidator.readAndValidateDummyConfig(configPath, keyPatterns);
//
//                // 3. Generate ConfigMap
//                String configMapYaml = ConfigMapGenerator.generateConfigMapAsString(config, keyPatterns);
//
//                return Response.ok(configMapYaml).build();
//            } catch (Exception e) {
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                        .entity("Error generating ConfigMap: " + e.getMessage())
//                        .build();
//            }
//        }
//    }
//
//
//    /*
//    Purpose: Exposes the functionality as a web service
//
//Example Request:
//
//GET /configmap?blueprint=sample-blueprint.xml&config=sample-config.properties
//Response:
//
//yaml
//HTTP 200 OK
//Content-Type: text/yaml
//
//apiVersion: v1
//kind: ConfigMap
//metadata:
//  name: app-config
//  labels:
//    generated-by: strict-configmap-generator
//data:
//  api_version: v1
//  app: inventory
//  env: prod
//  prod_inventory: "null"
//     */
//
//
//
//
//    /*
//    Blueprint defines needed configurations:
//
//xml
//<to uri="http://service/{{version}}/users/{{user}}_{{domain}}"/>
//Config File provides values:
//
//properties
//version=2
//user=admin
//domain=example.com
//System:
//
//Parses blueprint → finds version, user_+domain patterns
//
//Validates config → checks admin_example.com exists
//
//Generates ConfigMap:
//
//yaml
//data:
//  version: 2
//  user: admin
//  domain: example.com
//  admin_example.com: "null"
//REST API returns this YAML to be applied to Kubernetes.
//     */



//package org.acme;
//
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//import org.acme.util.*;
//import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
//
//import java.io.InputStream;
//import java.util.Map;
//import java.util.Properties;
//
//@Path("/configmap")
//public class ConfigMapResource {
//
//    @GET
//    @Produces(MediaType.TEXT_HTML)
//    public InputStream getUploadForm() {
//        return getClass().getClassLoader().getResourceAsStream("META-INF/resources/index.html");
//    }
//
//    @POST
//    @Path("/upload")
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response generateConfigMapFromUpload(
//            @MultipartForm FileUploadForm form) {
//        try {
//            if (form.blueprintFile == null || form.configFile == null) {
//                return Response.status(Response.Status.BAD_REQUEST)
//                        .entity("Both blueprint and config files are required")
//                        .build();
//            }
//
//            Map<String, String> keyPatterns = BlueprintParser.extractKeyPatternsFromStream(form.blueprintFile);
//            Properties config = new Properties();
//            config.load(form.configFile);
//            config = ConfigValidator.validateProperties(config, keyPatterns);
//
//            String configMapYaml = ConfigMapGenerator.generateConfigMapAsString(config, keyPatterns);
//
//            // Use custom filename if provided, otherwise use default
//            String fileName = form.customFileName != null && !form.customFileName.isEmpty()
//                    ? form.customFileName
//                    : "configmap.yaml";
//
//            // Ensure filename has .yaml extension
//            if (!fileName.toLowerCase().endsWith(".yaml") && !fileName.toLowerCase().endsWith(".yml")) {
//                fileName += ".yaml";
//            }
//
//            return Response.ok(configMapYaml)
//                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
//                    .build();
//        } catch (Exception e) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                    .entity("Error generating ConfigMap: " + e.getMessage())
//                    .build();
//        }
//    }
//
//    public static class FileUploadForm {
//        @FormParam("blueprint")
//        public InputStream blueprintFile;
//
//        @FormParam("config")
//        public InputStream configFile;
//
//        @FormParam("customFileName")
//        public String customFileName;
//    }
//}

package org.acme;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.util.*;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Path("/configmap")
public class ConfigMapResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream getUploadForm() {
        return getClass().getClassLoader().getResourceAsStream("META-INF/resources/index.html");
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/zip")
    public Response generateConfigMapsFromDirectory(MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> formParts = input.getFormDataMap();

            // Get the XML files from directory upload
            List<InputPart> xmlFilesParts = formParts.get("xmlDirectory");
            if (xmlFilesParts == null || xmlFilesParts.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("XML directory is required")
                        .build();
            }

            // Get the config file
            List<InputPart> configParts = formParts.get("config");
            if (configParts == null || configParts.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Config file is required")
                        .build();
            }
            InputStream configStream = configParts.get(0).getBody(InputStream.class, null);
            Properties config = new Properties();
            config.load(configStream);

            // Get custom folder name if provided
            List<InputPart> folderNameParts = formParts.get("folderName");
            String folderName = folderNameParts != null && !folderNameParts.isEmpty()
                    ? folderNameParts.get(0).getBody(String.class, null)
                    : "configmaps";

            // Create a temporary zip file
            File zipFile = File.createTempFile(folderName, ".zip");

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                // Process each XML file
                for (InputPart part : xmlFilesParts) {
                    String fileName = getFileName(part);
                    if (fileName.toLowerCase().endsWith(".xml")) {
                        InputStream xmlStream = part.getBody(InputStream.class, null);

                        // Parse XML and generate ConfigMap
                        Map<String, String> keyPatterns = BlueprintParser.extractKeyPatternsFromStream(xmlStream);
                        Properties validatedConfig = ConfigValidator.validateProperties(config, keyPatterns);
                        String configMapYaml = ConfigMapGenerator.generateConfigMapAsString(validatedConfig, keyPatterns);

                        // Generate ConfigMap filename
                        String configMapName = fileName.replace(".xml", "-configmap.yaml");

                        // Add to zip
                        zos.putNextEntry(new ZipEntry(configMapName));
                        zos.write(configMapYaml.getBytes());
                        zos.closeEntry();

                        // Reset config stream for next XML file
                        configStream.reset();
                        config.load(configStream);
                    }
                }
            }

            return Response.ok(zipFile)
                    .header("Content-Disposition", "attachment; filename=\"" + folderName + ".zip\"")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error generating ConfigMaps: " + e.getMessage())
                    .build();
        }
    }

    private String getFileName(InputPart part) {
        String[] contentDisposition = part.getHeaders().getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if (filename.trim().startsWith("filename")) {
                return filename.split("=")[1].trim().replace("\"", "");
            }
        }
        return "unknown.xml";
    }
}