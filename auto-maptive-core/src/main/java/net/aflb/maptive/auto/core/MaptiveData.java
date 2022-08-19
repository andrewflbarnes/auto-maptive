package net.aflb.maptive.auto.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MaptiveData {

    private final String idCol;
    private final Map<String, String> data;

    private MaptiveData(String idCol, Map<String, String> data) {
        this.idCol = idCol;
        this.data = new LinkedHashMap<>(data);
    }

    public MaptiveData(String idCol) {
        this(idCol, new LinkedHashMap<>());
    }

    public String getIdCol() {
        return idCol;
    }

    public Map<String, String> getData() {
        return new LinkedHashMap<>(data);
    }

    public String put(String key, String val) {
        return data.put(key, val);
    }

    public String get(String key) {
        return data.get(key);
    }

    public Map<String, String> getIdLessColumnData() {
        return getColumnData(true);
    }

    public Map<String, String> getColumnData() {
        return getColumnData(false);
    }

    private Map<String, String> getColumnData(boolean idLess) {
        final Map<String, String> columnData = new HashMap<>();
        int i = 0;
        for (final var entry : data.entrySet()) {
            if (!idLess || !entry.getKey().equalsIgnoreCase(idCol)) {
                columnData.put("column_%d".formatted(i), entry.getValue());
            }
            i++;
        }

        return columnData;
    }

    @Override
    public String toString() {
        return "MaptiveData{" +
            "idCol=" + idCol +
            ", data=" + data +
            '}';
    }
}
