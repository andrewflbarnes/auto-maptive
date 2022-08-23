package net.aflb.maptive.auto.simple.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppConfig {

    public static AppConfig from(String file) {
        final var fileContents = readFromFile(file);
        final var props = fileToProps(fileContents);
        final var resolved = getProps(props, "key", "map", "file", "schedule");
        final var strSchedule = resolved.get(3);
        final long schedule;
        try {
            schedule = Long.parseLong(strSchedule);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid schedule property, must be a number: " + strSchedule);
        }
        return new AppConfig(
            resolved.get(0),
            resolved.get(1),
            resolved.get(2),
            schedule);
    }

    private static byte[] readFromFile(String file) {
        final var simplePath = Paths.get(file);
        final var f = simplePath.toAbsolutePath().toFile();
        final var strAbsPath = f.getAbsolutePath();
        if (!f.exists()) {
            throw new IllegalArgumentException("Config file does not exist: " + strAbsPath);
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

    private static List<String> getProps(Map<String, String> props, String ...keys) {
        final List<String> resolvedProps = new ArrayList<>();
        for (final var key : keys) {
            final var val = props.get(key);
            if (val == null || val.isBlank()) {
                throw new IllegalArgumentException("Property is missing or not set: " + key);
            }
            resolvedProps.add(val);
        }

        return resolvedProps;
    }

    private final String apiKey;
    private final String mapId;
    private final String file;
    private final long schedule;

    public AppConfig(String apiKey, String mapId, String file, long schedule) {
        this.apiKey = apiKey;
        this.mapId = mapId;
        this.file = file;
        this.schedule = schedule;
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
}
