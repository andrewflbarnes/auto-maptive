package net.aflb.maptive.auto.client;

import net.aflb.maptive.auto.core.MaptiveData;
import net.aflb.maptive.auto.core.MaptiveId;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MaptiveClient {

    MaptiveApiResponse get(String apiKey, String mapId, List<String> ids) throws IOException;

    MaptiveApiResponse getAll(String apiKey, String mapId) throws IOException;

    MaptiveApiResponse add(String apiKey, String mapId, MaptiveData data) throws IOException;

    MaptiveApiResponse addAll(String apiKey, String mapId, Collection<MaptiveData> data) throws IOException;

    MaptiveApiResponse update(String apiKey, String mapId, MaptiveId id, Map<String, String> data) throws IOException;

    MaptiveApiResponse delete(String apiKey, String mapId, List<String> ids) throws IOException;

    MaptiveApiResponse deleteAll(String apiKey, String mapId) throws IOException;
}
