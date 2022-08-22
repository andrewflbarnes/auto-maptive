package net.aflb.maptive.auto.core.client;

import java.util.List;

public class MaptiveApiResponse {
    private String code;
    private String description;
    private List<List<String>> result;
    private String message;
    private List<String> alreadyExistingIds;
    private int unableToProcess;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<List<String>> getResult() {
        return result;
    }

    public void setResult(List<List<String>> result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getAlreadyExistingIds() {
        return alreadyExistingIds;
    }

    public void setAlreadyExistingIds(List<String> alreadyExistingIds) {
        this.alreadyExistingIds = alreadyExistingIds;
    }

    public int getUnableToProcess() {
        return unableToProcess;
    }

    public void setUnableToProcess(int unableToProcess) {
        this.unableToProcess = unableToProcess;
    }

    @Override
    public String toString() {
        return "MaptiveApiResponse{" +
            "code='" + code + '\'' +
            ", description='" + description + '\'' +
            ", result=" + result +
            ", message='" + message + '\'' +
            ", alreadyExistingIds=" + alreadyExistingIds +
            ", unableToProcess=" + unableToProcess +
            '}';
    }
}
