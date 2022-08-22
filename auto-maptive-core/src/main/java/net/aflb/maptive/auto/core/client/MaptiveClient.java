package net.aflb.maptive.auto.core.client;

import net.aflb.maptive.auto.core.MaptiveData;
import net.aflb.maptive.auto.core.MaptiveId;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MaptiveClient {

    MaptiveApiResponse get(List<String> ids) throws IOException;

    MaptiveApiResponse getAll() throws IOException;

    MaptiveApiResponse add(MaptiveData data) throws IOException;

    MaptiveApiResponse addAll(Collection<MaptiveData> data) throws IOException;

    MaptiveApiResponse update(MaptiveId id, Map<String, String> data) throws IOException;

    MaptiveApiResponse delete(List<String> ids) throws IOException;

    MaptiveApiResponse deleteAll() throws IOException;
}
