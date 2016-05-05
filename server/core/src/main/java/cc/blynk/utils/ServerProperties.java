package cc.blynk.utils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Map;
import java.util.Properties;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/12/2015.
 */
public class ServerProperties extends Properties {

    public static final String SERVER_PROPERTIES_FILENAME = "server.properties";

    public ServerProperties() {
        initProperties(SERVER_PROPERTIES_FILENAME);
    }

    public ServerProperties(Map<String, String> cmdProperties) {
        String propertiesFileName = cmdProperties.get(SERVER_PROPERTIES_FILENAME);
        if (propertiesFileName == null) {
            initProperties(SERVER_PROPERTIES_FILENAME);
        } else {
            initProperties(Paths.get(propertiesFileName));
        }
        putAll(cmdProperties);
    }

    public ServerProperties(String propertiesFileName) {
        initProperties(propertiesFileName);
    }

    public static Path getFileInCurrentDir(String filename) {
        try {
            CodeSource codeSource = ServerProperties.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            String jarDir = jarFile.getParentFile().getPath();
            return Paths.get(jarDir, filename);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * First loads properties file from class path after that from current folder.
     * So properties file in current folder is always overrides properties in classpath.
     *
     * @param filePropertiesName - name of properties file, for example "twitter4j.properties"
     */
    private void initProperties(String filePropertiesName) {
        if (!filePropertiesName.startsWith("/")) {
            filePropertiesName = "/" + filePropertiesName;
        }

        try (InputStream classPath = ServerProperties.class.getResourceAsStream(filePropertiesName)) {
            if (classPath != null) {
                load(classPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting properties file : " + filePropertiesName, e);
        }

        Path curDirPath = getFileInCurrentDir(filePropertiesName);
        if (Files.exists(curDirPath)) {
            try (InputStream curFolder = Files.newInputStream(curDirPath)) {
                if (curFolder != null) {
                    load(curFolder);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error getting properties file : " + filePropertiesName, e);
            }
        }

    }

    private void initProperties(Path path) {
        if (!Files.exists(path)) {
            System.out.println("Path " + path + " not found.");
            System.exit(1);
        }

        try (InputStream curFolder = Files.newInputStream(path)) {
            if (curFolder != null) {
                load(curFolder);
            }
        } catch (Exception e) {
            System.out.println("Error reading properties file : '" + path + "'. Reason : " + e.getMessage());
            System.exit(1);
        }
    }

    public int getIntProperty(String propertyName) {
        return ParseUtil.parseInt(getProperty(propertyName));
    }

    public int getIntProperty(String propertyName, int defaultValue) {
        if (getProperty(propertyName) == null || "".equals(getProperty(propertyName))) {
            return defaultValue;
        }
        return ParseUtil.parseInt(getProperty(propertyName));
    }

    public boolean getBoolProperty(String propertyName) {
        return Boolean.parseBoolean(getProperty(propertyName));
    }

    public long getLongProperty(String propertyName) {
        return ParseUtil.parseLong(getProperty(propertyName));
    }

    public String[] getCommaSeparatedValueAsArray(String propertyName) {
        String val = getProperty(propertyName);
        if (val == null) {
            return null;
        }
        return val.toLowerCase().split(",");
    }

}
