package eu.hansolo.fx.glucopi;

import eu.hansolo.jdktools.versioning.VersionNumber;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public enum Properties {
    INSTANCE;

    public static final String               VERSION_PROPERTIES          = "version.properties";
    public static final String               GLUCOPI_PROPERTIES          = "glucopi.properties";
    public static final String               PROPERTY_VERSION            = "version";
    public static final String               PROPERTY_NIGHTSCOUT_URL     = "nightscout_url";
    public static final String               PROPERTY_NIGHTSCOUT_TOKEN   = "nightscout_token";
    public static final String               PROPERTY_NIGHTSCOUT_API_KEY = "nightscout_api_key";
    private             java.util.Properties properties;
    private             java.util.Properties versionProperties;


    // ******************** Constructors **************************************
    Properties() {
        properties = new java.util.Properties();
        // Load properties
        final String jvmiviewerPropertiesFilePath = new StringBuilder(Constants.HOME_FOLDER).append(GLUCOPI_PROPERTIES).toString();

        // Create properties file if not exists
        Path path = Paths.get(jvmiviewerPropertiesFilePath);
        if (!Files.exists(path)) { createProperties(properties); }

        // Load properties file
        try (FileInputStream jvmiviewerPropertiesFile = new FileInputStream(jvmiviewerPropertiesFilePath)) {
            properties.load(jvmiviewerPropertiesFile);
        } catch (IOException ex) {
            System.out.println("Error reading GlucoPi properties file. " + ex);
        }

        // If properties empty, fill with default values
        if (properties.isEmpty()) {
            createProperties(properties);
        } else {
            validateProperties();
        }

        // Version number properties
        versionProperties = new java.util.Properties();
        try {
            versionProperties.load(Main.class.getResourceAsStream(VERSION_PROPERTIES));
        } catch (IOException ex) {
            System.out.println("Error reading version properties file. " + ex);
        }

    }


    // ******************** Methods *******************************************
    public java.util.Properties getProperties() { return properties; }

    public Object get(final String KEY) { return properties.getOrDefault(KEY, ""); }
    public void set(final String KEY, final String VALUE) {
        properties.setProperty(KEY, VALUE);
        storeProperties();
    }

    public String getString(final String key) { return properties.getOrDefault(key, "").toString(); }
    public void setString(final String key, final String value) { properties.setProperty(key, value); }

    public double getDouble(final String key) { return getDouble(key, 0); }
    public double getDouble(final String key, final double defaultValue) { return Double.parseDouble(properties.getOrDefault(key, Double.toString(defaultValue)).toString()); }
    public void setDouble(final String key, final double value) { properties.setProperty(key, Double.toString(value)); }

    public float getFloat(final String key) { return getFloat(key, 0); }
    public float getFloat(final String key, final float defaultValue) { return Float.parseFloat(properties.getOrDefault(key, Float.toString(defaultValue)).toString()); }
    public void setFloat(final String key, final float value) { properties.setProperty(key, Float.toString(value)); }

    public int getInt(final String key) { return getInt(key, 0); }
    public int getInt(final String key, final int defaultValue) { return Integer.parseInt(properties.getOrDefault(key, Integer.toString(defaultValue)).toString()); }
    public void setInt(final String key, final int value) { properties.setProperty(key, Integer.toString(value)); }

    public long getLong(final String key) { return getLong(key, 0); }
    public long getLong(final String key, final long defaultValue) { return Long.parseLong(properties.getOrDefault(key, Long.toString(defaultValue)).toString()); }
    public void setLong(final String key, final long value) { properties.setProperty(key, Long.toString(value)); }

    public boolean getBoolean(final String key) { return getBoolean(key, false); }
    public boolean getBoolean(final String key, final boolean defaultValue) { return Boolean.parseBoolean(properties.getOrDefault(key, Boolean.toString(defaultValue)).toString()); }
    public void setBoolean(final String key, final boolean value) { properties.setProperty(key, Boolean.toString(value)); }

    public boolean hasKey(final String key) { return properties.containsKey(key); }

    public void storeProperties() {
        if (null == properties) { return; }
        final String propFilePath = new StringBuilder(Constants.HOME_FOLDER).append(GLUCOPI_PROPERTIES).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public VersionNumber getVersionNumber() {
        return VersionNumber.fromText(versionProperties.getProperty(PROPERTY_VERSION));
    }


    // ******************** Properties ****************************************
    private void validateProperties() {
        if (null == properties) { return; }
        boolean storeProperties = false;
        if (!properties.containsKey(PROPERTY_NIGHTSCOUT_URL)) { properties.put(PROPERTY_NIGHTSCOUT_URL, ""); storeProperties = true; } else {
            String nightscoutUrl = properties.getProperty(PROPERTY_NIGHTSCOUT_URL);
            if (nightscoutUrl.endsWith("/")) {
                nightscoutUrl = nightscoutUrl.substring(0, nightscoutUrl.length() - 1);
                properties.put(PROPERTY_NIGHTSCOUT_URL, nightscoutUrl);
                storeProperties = true;
            }
        }
        if (!properties.containsKey(PROPERTY_NIGHTSCOUT_TOKEN))   { properties.put(PROPERTY_NIGHTSCOUT_TOKEN,   ""); storeProperties = true; }
        if (!properties.containsKey(PROPERTY_NIGHTSCOUT_API_KEY)) { properties.put(PROPERTY_NIGHTSCOUT_API_KEY, ""); storeProperties = true; }

        if (storeProperties) { storeProperties(); }
    }

    private void createProperties(java.util.Properties properties) {
        final String propFilePath = new StringBuilder(Constants.HOME_FOLDER).append(GLUCOPI_PROPERTIES).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            properties.put(PROPERTY_NIGHTSCOUT_URL, "");
            properties.put(PROPERTY_NIGHTSCOUT_TOKEN, "");
            properties.put(PROPERTY_NIGHTSCOUT_API_KEY, "");
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
