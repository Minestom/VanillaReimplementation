package net.minestom.vanilla.system;

import java.io.*;
import java.util.Properties;

/**
 * Helper class to load and save the contents of the server.properties file
 */
public class ServerProperties {

    private final File source;
    private final Properties properties;

    /**
     * Creates a new property list from a given file. Will attempt create the file and fill with defaults if it does not exist
     * @param source
     * @throws IOException
     */
    public ServerProperties(File source) throws IOException {
        properties = new Properties();
        loadDefault();
        this.source = source;
        if(source.exists()) {
            load();
        } else {
            save(); // write defaults to file
        }
    }

    public ServerProperties(String source) throws IOException {
        properties = new Properties();
        properties.load(new StringReader(source));
        this.source = null;

    }

    private void loadDefault() throws IOException {
        try(var defaultInput = new InputStreamReader(ServerProperties.class.getResourceAsStream("/server.properties.default"))) {
            properties.load(defaultInput);
        }
    }

    public void load() throws IOException {
        try(var reader = new FileReader(source)) {
            properties.load(reader);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public void set(String key, String value) {
        properties.put(key, value);
    }

    public void save() throws IOException {
        try(var writer = new FileWriter(source)) {
            properties.store(writer, "Minestom server properties");
        }
    }
}
