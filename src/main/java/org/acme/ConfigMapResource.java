////    package org.acme;
////
////    import io.quarkus.runtime.annotations.QuarkusMain;
////    import jakarta.ws.rs.*;
////    import jakarta.ws.rs.core.MediaType;
////    import jakarta.ws.rs.core.Response;
////    import org.acme.util.*;
////
////    import java.util.Map;
////    import java.util.Properties;
////
////    @Path("/configmap")
////    public class ConfigMapResource {
////
////        @GET
////        @Produces(MediaType.TEXT_PLAIN)
////        public Response generateConfigMap(
////                @QueryParam("blueprint") String blueprintPath,
////                @QueryParam("config") String configPath) {
////            try {
////                // Validate inputs
////                if (blueprintPath == null || blueprintPath.isEmpty() ||
////                        configPath == null || configPath.isEmpty()) {
////                    return Response.status(Response.Status.BAD_REQUEST)
////                            .entity("Both blueprint and config parameters are required")
////                            .build();
////                }
////
////                // 1. Extract patterns from blueprint
////                Map<String, String> keyPatterns = BlueprintParser.extractKeyPatternsFromBlueprint(blueprintPath);
////
////                // 2. Validate config
////                Properties config = ConfigValidator.readAndValidateDummyConfig(configPath, keyPatterns);
////
////                // 3. Generate ConfigMap
////                String configMapYaml = ConfigMapGenerator.generateConfigMapAsString(config, keyPatterns);
////
////                return Response.ok(configMapYaml).build();
////            } catch (Exception e) {
////                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
////                        .entity("Error generating ConfigMap: " + e.getMessage())
////                        .build();
////            }
////        }
////    }
////
////
////    /*
////    Purpose: Exposes the functionality as a web service
////
////Example Request:
////
////GET /configmap?blueprint=sample-blueprint.xml&config=sample-config.properties
////Response:
////
////yaml
////HTTP 200 OK
////Content-Type: text/yaml
////
////apiVersion: v1
////kind: ConfigMap
////metadata:
////  name: app-config
////  labels:
////    generated-by: strict-configmap-generator
////data:
////  api_version: v1
////  app: inventory
////  env: prod
////  prod_inventory: "null"
////     */
////
////
////
////
////    /*
////    Blueprint defines needed configurations:
////
////xml
////<to uri="http://service/{{version}}/users/{{user}}_{{domain}}"/>
////Config File provides values:
////
////properties
////version=2
////user=admin
////domain=example.com
////System:
////
////Parses blueprint → finds version, user_+domain patterns
////
////Validates config → checks admin_example.com exists
////
////Generates ConfigMap:
////
////yaml
////data:
////  version: 2
////  user: admin
////  domain: example.com
////  admin_example.com: "null"
////REST API returns this YAML to be applied to Kubernetes.
////     */
//
//
//
////package org.acme;
////
////import jakarta.ws.rs.*;
////import jakarta.ws.rs.core.MediaType;
////import jakarta.ws.rs.core.Response;
////import org.acme.util.*;
////import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
////
////import java.io.InputStream;
////import java.util.Map;
////import java.util.Properties;
////
////@Path("/configmap")
////public class ConfigMapResource {
////
////    @GET
////    @Produces(MediaType.TEXT_HTML)
////    public InputStream getUploadForm() {
////        return getClass().getClassLoader().getResourceAsStream("META-INF/resources/index.html");
////    }
////
////    @POST
////    @Path("/upload")
////    @Consumes(MediaType.MULTIPART_FORM_DATA)
////    @Produces(MediaType.TEXT_PLAIN)
////    public Response generateConfigMapFromUpload(
////            @MultipartForm FileUploadForm form) {
////        try {
////            if (form.blueprintFile == null || form.configFile == null) {
////                return Response.status(Response.Status.BAD_REQUEST)
////                        .entity("Both blueprint and config files are required")
////                        .build();
////            }
////
////            Map<String, String> keyPatterns = BlueprintParser.extractKeyPatternsFromStream(form.blueprintFile);
////            Properties config = new Properties();
////            config.load(form.configFile);
////            config = ConfigValidator.validateProperties(config, keyPatterns);
////
////            String configMapYaml = ConfigMapGenerator.generateConfigMapAsString(config, keyPatterns);
////
////            // Use custom filename if provided, otherwise use default
////            String fileName = form.customFileName != null && !form.customFileName.isEmpty()
////                    ? form.customFileName
////                    : "configmap.yaml";
////
////            // Ensure filename has .yaml extension
////            if (!fileName.toLowerCase().endsWith(".yaml") && !fileName.toLowerCase().endsWith(".yml")) {
////                fileName += ".yaml";
////            }
////
////            return Response.ok(configMapYaml)
////                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
////                    .build();
////        } catch (Exception e) {
////            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
////                    .entity("Error generating ConfigMap: " + e.getMessage())
////                    .build();
////        }
////    }
////
////    public static class FileUploadForm {
////        @FormParam("blueprint")
////        public InputStream blueprintFile;
////
////        @FormParam("config")
////        public InputStream configFile;
////
////        @FormParam("customFileName")
////        public String customFileName;
////    }
////}
//
//package org.acme;
//
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//import org.acme.util.*;
//import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
//import org.jboss.resteasy.plugins.providers.multipart.InputPart;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.util.*;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
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
//    @Produces("application/zip")
//    public Response generateConfigMapsFromDirectory(MultipartFormDataInput input) {
//        try {
//            Map<String, List<InputPart>> formParts = input.getFormDataMap();
//
//            // Get the XML files from directory upload
//            List<InputPart> xmlFilesParts = formParts.get("xmlDirectory");
//            if (xmlFilesParts == null || xmlFilesParts.isEmpty()) {
//                return Response.status(Response.Status.BAD_REQUEST)
//                        .entity("XML directory is required")
//                        .build();
//            }
//
//            // Get the config file
//            List<InputPart> configParts = formParts.get("config");
//            if (configParts == null || configParts.isEmpty()) {
//                return Response.status(Response.Status.BAD_REQUEST)
//                        .entity("Config file is required")
//                        .build();
//            }
//            InputStream configStream = configParts.get(0).getBody(InputStream.class, null);
//            Properties config = new Properties();
//            config.load(configStream);
//
//            // Get custom folder name if provided
//            List<InputPart> folderNameParts = formParts.get("folderName");
//            String folderName = folderNameParts != null && !folderNameParts.isEmpty()
//                    ? folderNameParts.get(0).getBody(String.class, null)
//                    : "configmaps";
//
//            // Create a temporary zip file
//            File zipFile = File.createTempFile(folderName, ".zip");
//
//            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
//                // Process each XML file
//                for (InputPart part : xmlFilesParts) {
//                    String fileName = getFileName(part);
//                    if (fileName.toLowerCase().endsWith(".xml")) {
//                        InputStream xmlStream = part.getBody(InputStream.class, null);
//
//                        // Parse XML and generate ConfigMap
//                        Map<String, String> keyPatterns = BlueprintParser.extractKeyPatternsFromStream(xmlStream);
//                        Properties validatedConfig = ConfigValidator.validateProperties(config, keyPatterns);
//                        String configMapYaml = ConfigMapGenerator.generateConfigMapAsString(validatedConfig, keyPatterns);
//
//                        // Generate ConfigMap filename
//                        String configMapName = fileName.replace(".xml", "-configmap.yaml");
//
//                        // Add to zip
//                        zos.putNextEntry(new ZipEntry(configMapName));
//                        zos.write(configMapYaml.getBytes());
//                        zos.closeEntry();
//
//                        // Reset config stream for next XML file
//                        configStream.reset();
//                        config.load(configStream);
//                    }
//                }
//            }
//
//            return Response.ok(zipFile)
//                    .header("Content-Disposition", "attachment; filename=\"" + folderName + ".zip\"")
//                    .build();
//
//        } catch (Exception e) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                    .entity("Error generating ConfigMaps: " + e.getMessage())
//                    .build();
//        }
//    }
//
//    private String getFileName(InputPart part) {
//        String[] contentDisposition = part.getHeaders().getFirst("Content-Disposition").split(";");
//        for (String filename : contentDisposition) {
//            if (filename.trim().startsWith("filename")) {
//                return filename.split("=")[1].trim().replace("\"", "");
//            }
//        }
//        return "unknown.xml";
//    }
//}
//package org.acme;
//
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.Path;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//import org.acme.util.*;
//import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
//import org.jboss.resteasy.plugins.providers.multipart.InputPart;
//
//
//import java.io.*;
//import java.nio.file.*;
//import java.nio.file.attribute.FileTime;
//import java.util.*;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
//
//import static io.quarkus.arc.impl.UncaughtExceptions.LOGGER;
//
//@Path("/configmap")
//public class ConfigMapResource {
//
//    private static final String MAIN_FOLDER_NAME = "MainConfigMapFolder";
//    private static final java.nio.file.Path MAIN_FOLDER_PATH =
//            Paths.get(System.getProperty("java.io.tmpdir"), MAIN_FOLDER_NAME);
//    private static final java.nio.file.Path PROCESSED_FILES_DIR =
//            Paths.get(System.getProperty("java.io.tmpdir"), "processed-files");
//
//    // Helper methods
//    private String getSourceFolderName(InputPart part) {
//       try {
//           String[] contentDisposition = part.getHeaders().getFirst("Content-Disposition").split(";");
//           for (String filename : contentDisposition) {
//               if (filename.trim().startsWith("filename")) {
//                   String fullPath = filename.split("=")[1].trim().replace("\"", "");
//                   return Paths.get(fullPath).getParent().getFileName().toString();
//               }
//           }
//       } catch (Exception e) {
//           LOGGER.warn("Failed to extract source folder name from part headers", e);
//
//       }
//        return "unknown-folder";
//    }
//
//    private String getFileName(InputPart part) {
//        try {
//            String[] contentDisposition = part.getHeaders().getFirst("Content-Disposition").split(";");
//            for (String filename : contentDisposition) {
//                if (filename.trim().startsWith("filename")) {
//                    return Paths.get(filename.split("=")[1].trim().replace("\"", "")).getFileName().toString();
//                }
//            }
//        } catch (Exception e) {
//            LOGGER.warn("Failed to extract filename from part headers", e);
//        }
//        return "unknown.xml";
//    }
//
//    private boolean shouldProcessFile(InputPart part, java.nio.file.Path processedFile) throws IOException {
//        if (!Files.exists(processedFile)) {
//            LOGGER.debug((Object) "Processed marker file not found: {}", (Throwable) processedFile);
//            return true;
//        }
//        String lastModifiedHeader = part.getHeaders().getFirst("Last-Modified");
//        long inputLastModified = lastModifiedHeader != null
//                ? Date.parse(lastModifiedHeader)
//                : System.currentTimeMillis();
//        long processedLastModified = Files.getLastModifiedTime(processedFile).toMillis();
//        boolean shouldProcess = inputLastModified > processedLastModified;
//
//        LOGGER.debug("File {} shouldProcess: inputLastModified={}, processedLastModified={}");
//        return shouldProcess;
//
//    }
//
//    private void markFileAsProcessed(InputPart part, java.nio.file.Path processedFile) throws IOException {
//        String lastModifiedHeader = part.getHeaders().getFirst("Last-Modified");
//        long lastModified = lastModifiedHeader != null
//                ? Date.parse(lastModifiedHeader)
//                : System.currentTimeMillis();
//        Files.write(processedFile, new byte[0], StandardOpenOption.CREATE);
//        Files.setLastModifiedTime(processedFile, FileTime.fromMillis(lastModified));
//        LOGGER.info("Marked file as processed: {} with timestamp {}");
//
//    }
//
//    @GET
//    @Produces(MediaType.TEXT_HTML)
//    public InputStream getUploadForm() {
//        LOGGER.info("Serving upload form");
//        return getClass().getClassLoader().getResourceAsStream("META-INF/resources/index.html");
//    }
//
//    @POST
//    @Path("/upload")
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces("application/zip")
//    public Response generateConfigMapsFromDirectory(MultipartFormDataInput input) {
//        LOGGER.info("Received request to generate config maps from uploaded directory");
//        try {
//            // Ensure directories exist
//            Files.createDirectories(PROCESSED_FILES_DIR);
//            Files.createDirectories(MAIN_FOLDER_PATH);
//
//            Map<String, List<InputPart>> formParts = input.getFormDataMap();
//
//            // Validate inputs
//            List<InputPart> xmlFilesParts = formParts.get("xmlDirectory");
//            if (xmlFilesParts == null || xmlFilesParts.isEmpty()) {
//                String msg = "XML directory is required";
//                LOGGER.warn(msg);
//                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
//
//            }
//
//            List<InputPart> configParts = formParts.get("config");
//            if (configParts == null || configParts.isEmpty()) {
//                return Response.status(Response.Status.BAD_REQUEST)
//                        .entity("Config file is required")
//                        .build();
//            }
//
//            // Load config
//            InputStream configStream = configParts.get(0).getBody(InputStream.class, null);
//            Properties config = new Properties();
//            config.load(configStream);
//
//            // Process XML files
//            String sourceFolderName = getSourceFolderName(xmlFilesParts.get(0));
//            String subFolderName = sourceFolderName + "-configmaps";
//            java.nio.file.Path subFolderPath = MAIN_FOLDER_PATH.resolve(subFolderName);
//            Files.createDirectories(subFolderPath);
//
//            // Process each XML file
//            for (InputPart part : xmlFilesParts) {
//                String fileName = getFileName(part);
//                if (fileName.toLowerCase().endsWith(".xml")) {
//                    java.nio.file.Path processedFile = PROCESSED_FILES_DIR.resolve(fileName + ".processed");
//                    java.nio.file.Path configMapFile = subFolderPath.resolve(fileName.replace(".xml", "-configmap.yaml"));
//
//                    if (shouldProcessFile(part, processedFile)) {
//                        InputStream xmlStream = part.getBody(InputStream.class, null);
//                        Map<String, String> keyPatterns = BlueprintParser.extractKeyPatternsFromStream(xmlStream);
//                        Properties validatedConfig = ConfigValidator.validateProperties(config, keyPatterns);
//                        String configMapYaml = ConfigMapGenerator.generateConfigMapAsString(validatedConfig, keyPatterns);
//                        Files.write(configMapFile, configMapYaml.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//                        markFileAsProcessed(part, processedFile);
//                        LOGGER.info("Processed and generated ConfigMap YAML for file: {}");
//
//                    }else {
//                        LOGGER.info("Skipping processing for file {} as it is not updated");
//                    }
//                }
//                configStream.reset();
//                config.load(configStream);
//            }
//
//            // Create zip from MAIN_FOLDER_PATH
//            File zipFile = File.createTempFile(MAIN_FOLDER_NAME, ".zip");
//            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
//                Files.walk(MAIN_FOLDER_PATH)
//                        .filter(path -> !Files.isDirectory(path))
//                        .forEach(path -> {
//                            try {
//                                String entryName = MAIN_FOLDER_PATH.relativize(path).toString();
//                                zos.putNextEntry(new ZipEntry(entryName));
//                                zos.write(Files.readAllBytes(path));
//                                zos.closeEntry();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                LOGGER.error("Error adding file to ZIP: " + path, e);
//                            }
//                        });
//            }
//            LOGGER.info("Successfully generated ZIP archive for ConfigMaps: {}");
//
//            return Response.ok(zipFile)
//                    .header("Content-Disposition", "attachment; filename=\"" + MAIN_FOLDER_NAME + ".zip\"")
//                    .build();
//
//        } catch (Exception e) {
//            LOGGER.error("Error generating ConfigMaps", e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                    .entity("Error generating ConfigMaps: " + e.getMessage())
//                    .build();
//        }
//    }
//}



package org.acme;

import jakarta.ws.rs.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.util.*;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Path("/configmap")
public class ConfigMapResource {
    private static final String MAIN_FOLDER_NAME = "MainConfigMapFolder";
    private static final java.nio.file.Path MAIN_FOLDER_PATH =
            Paths.get(System.getProperty("java.io.tmpdir"), MAIN_FOLDER_NAME);
    private static final java.nio.file.Path PROCESSED_FILES_DIR =
            Paths.get(System.getProperty("java.io.tmpdir"), "processed-files");

    // Helper methods
    private String getSourceFolderName(InputPart part) {
        try {
            String[] contentDisposition = part.getHeaders().getFirst("Content-Disposition").split(";");
            for (String filename : contentDisposition) {
                if (filename.trim().startsWith("filename")) {
                    String fullPath = filename.split("=")[1].trim().replace("\"", "");
                    return Paths.get(fullPath).getParent().getFileName().toString();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to extract source folder name: " + e.getMessage());
        }
        return "unknown-folder";
    }

    private String getFileName(InputPart part) {
        try {
            String[] contentDisposition = part.getHeaders().getFirst("Content-Disposition").split(";");
            for (String filename : contentDisposition) {
                if (filename.trim().startsWith("filename")) {
                    return Paths.get(filename.split("=")[1].trim().replace("\"", "")).getFileName().toString();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to extract filename: " + e.getMessage());
        }
        return "unknown.xml";
    }

    private boolean shouldProcessFile(InputPart part, java.nio.file.Path processedFile) throws IOException {
        if (!Files.exists(processedFile)) {
            System.out.println("Processed marker file not found: " + processedFile);
            return true;
        }

        String lastModifiedHeader = part.getHeaders().getFirst("Last-Modified");
        long inputLastModified = lastModifiedHeader != null
                ? Date.parse(lastModifiedHeader)
                : System.currentTimeMillis();
        long processedLastModified = Files.getLastModifiedTime(processedFile).toMillis();

        System.out.printf("File comparison - input: %d, processed: %d%n",
                inputLastModified, processedLastModified);

        return inputLastModified > processedLastModified;
    }

    private void markFileAsProcessed(InputPart part, java.nio.file.Path processedFile) throws IOException {
        String lastModifiedHeader = part.getHeaders().getFirst("Last-Modified");
        long lastModified = lastModifiedHeader != null
                ? Date.parse(lastModifiedHeader)
                : System.currentTimeMillis();

        Files.createDirectories(processedFile.getParent());
        Files.write(processedFile, new byte[0], StandardOpenOption.CREATE);
        Files.setLastModifiedTime(processedFile, FileTime.fromMillis(lastModified));
        System.out.println("Marked file as processed: " + processedFile);
    }

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
            // Ensure directories exist
            Files.createDirectories(PROCESSED_FILES_DIR);
            Files.createDirectories(MAIN_FOLDER_PATH);

            Map<String, List<InputPart>> formParts = input.getFormDataMap();

            // Validate inputs
            List<InputPart> xmlFilesParts = formParts.get("xmlDirectory");
            if (xmlFilesParts == null || xmlFilesParts.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("XML directory is required")
                        .build();
            }

            List<InputPart> configParts = formParts.get("config");
            if (configParts == null || configParts.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Config file is required")
                        .build();
            }

            // Load config
            Properties config = new Properties();
            try (InputStream configStream = configParts.get(0).getBody(InputStream.class, null)) {
                config.load(configStream);
            }

            // Process XML files
            String sourceFolderName = getSourceFolderName(xmlFilesParts.get(0));
            String subFolderName = sourceFolderName + "-configmaps";
            java.nio.file.Path subFolderPath = MAIN_FOLDER_PATH.resolve(subFolderName);
            Files.createDirectories(subFolderPath);

            // Process each XML file
            for (InputPart part : xmlFilesParts) {
                String fileName = getFileName(part);
                if (fileName.toLowerCase().endsWith(".xml")) {
                    java.nio.file.Path processedFile = PROCESSED_FILES_DIR.resolve(fileName + ".processed");
                    java.nio.file.Path configMapFile = subFolderPath.resolve(fileName.replace(".xml", "-configmap.yaml"));

                    if (shouldProcessFile(part, processedFile)) {
                        try (InputStream xmlStream = part.getBody(InputStream.class, null)) {
                            Map<String, String> keyPatterns = BlueprintParser.extractKeyPatternsFromStream(xmlStream);
                            Properties validatedConfig = ConfigValidator.validateProperties(config, keyPatterns);
                            String configMapYaml = ConfigMapGenerator.generateConfigMapAsString(validatedConfig, keyPatterns);
                            Files.write(configMapFile, configMapYaml.getBytes(),
                                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                            markFileAsProcessed(part, processedFile);
                            System.out.println("Generated ConfigMap: " + configMapFile);
                        }
                    } else {
                        System.out.println("Skipping unchanged file: " + fileName);
                    }
                }
            }

            // Create zip
            File zipFile = File.createTempFile(MAIN_FOLDER_NAME, ".zip");
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                Files.walk(MAIN_FOLDER_PATH)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            try {
                                String entryName = MAIN_FOLDER_PATH.relativize(path).toString();
                                zos.putNextEntry(new ZipEntry(entryName));
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                System.err.println("Error adding to ZIP: " + path);
                                e.printStackTrace();
                            }
                        });
            }

            return Response.ok(zipFile)
                    .header("Content-Disposition", "attachment; filename=\"configmaps.zip\"")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error: " + e.getMessage())
                    .build();
        }
    }
}