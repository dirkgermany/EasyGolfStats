package de.easygolfstats.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Settings {
    private Properties properties;
    private String filePath;

    public Settings (String filePath) {
        this.filePath = filePath;
        readProperties();
    }

    public String getValue(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public Boolean getValue(String key, boolean defaultValue) {
        String booleanDefaultString = Boolean.toString(defaultValue);
        String booleanString = properties.getProperty(key, booleanDefaultString);
        return Boolean.parseBoolean(booleanString);
    }

    public void setValue(String key, String value) {
        properties.setProperty(key, value);
        writeProperties();
    }

    public void writeProperties() {
        try {
            OutputStream stream = new FileOutputStream(filePath);
            properties.store(stream, "write");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readProperties() {
        properties = new Properties();

        try {
            InputStream stream = new FileInputStream(filePath);
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
