package net.aflb.maptive.auto.simple.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class AppConfig {

    public static AppConfig from(String file) {
        final var fileContents = readFromFile(file);
        final var props = fileToProps(fileContents);
        return new AppConfig(
            getStringProp(props, "key"),
            getStringProp(props, "map"),
            getStringProp(props, "file"),
            getLongProp(props, "schedule"),
            getStringProp(props, "id-col"),
            getIntProp(props, "id-col-index"),
            getStringProp(props, "date-format"));
    }

    private static File absoluteFile(String file) {
        final var simplePath = Paths.get(file);
        return simplePath.toAbsolutePath().toFile();
    }

    private static byte[] readFromFile(String file) {
        final var f = absoluteFile(file);
        final var strAbsPath = f.getAbsolutePath();
        if (!f.exists()) {
            throw new AppException("Config file does not exist: " + strAbsPath);
        }

        try {
            return Files.readAllBytes(f.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read config file: " + strAbsPath, e);
        }
    }

    private static Map<String, String> fileToProps(byte[] fileContents) {
        return Arrays.stream(new String(fileContents).split("\r*\n"))
            .filter(prop -> !prop.isBlank())
            .filter(prop -> !prop.startsWith("#"))
            .map(prop -> prop.split("="))
            .collect(Collectors.toMap(e -> e[0], e -> e[1]));
    }

    private static String getStringProp(Map<String, String> props, String key) {
        final var val = props.get(key);
        if (val == null || val.isBlank()) {
            throw new AppException("Property is missing or not set: " + key);
        }

        return val;
    }

    private static long getLongProp(Map<String, String> props, String key) {
        final var strProp = getStringProp(props, key);
        try {
            return Long.parseLong(strProp);
        } catch (NumberFormatException e) {
            throw new AppException("Invalid "+ key + " property, must be a number: " + strProp);
        }
    }

    private static int getIntProp(Map<String, String> props, String key) {
        final var strProp = getStringProp(props, key);
        try {
            return Integer.parseInt(strProp);
        } catch (NumberFormatException e) {
            throw new AppException("Invalid "+ key + " property, must be a number: " + strProp);
        }
    }

    private final String apiKey;
    private final String mapId;
    private final String file;
    private final long schedule;
    private final String idColumn;
    private final int idColumnIndex;
    private final String dateFormat;

    public AppConfig(String apiKey, String mapId, String file, long schedule, String idColumn, int idColumnIndex,
                     String dateFormat) {
        this.apiKey = apiKey;
        this.mapId = mapId;
        this.file = validateToAbsoluteFile(file);
        this.schedule = schedule;
        this.idColumn = idColumn;
        this.idColumnIndex = idColumnIndex;
        this.dateFormat = dateFormat;
    }

    private String validateToAbsoluteFile(String file) {
        final var f = absoluteFile(file);
        final var ret = f.getAbsolutePath();
        if (!f.exists() || !f.isFile()) {
            throw new AppException("Local data file does not exist: " + ret);
        }

        return ret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getMapId() {
        return mapId;
    }

    public String getFile() {
        return file;
    }

    public long getSchedule() {
        return schedule;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public int getIdColumnIndex() {
        return idColumnIndex;
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
