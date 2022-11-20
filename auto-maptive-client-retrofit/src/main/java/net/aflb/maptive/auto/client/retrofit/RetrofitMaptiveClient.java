package net.aflb.maptive.auto.client.retrofit;

import com.google.gson.Gson;
import net.aflb.maptive.auto.core.client.MaptiveApiResponse;
import net.aflb.maptive.auto.core.client.MaptiveClient;
import net.aflb.maptive.auto.core.MaptiveData;
import net.aflb.maptive.auto.core.MaptiveId;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RetrofitMaptiveClient implements MaptiveClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrofitMaptiveClient.class);
    private static final GsonConverterFactory GSON_CONVERTER = GsonConverterFactory.create();
    private static final Gson GSON = new Gson();

    private final Retrofit client;
    private final RetrofitMaptiveApi api;
    private final String apiKey;
    private final String mapId;

    public static RetrofitMaptiveClient production(String apiKey, String mapId) {
        return new RetrofitMaptiveClient(ServerConfig.production(), apiKey, mapId);
    }

    public static RetrofitMaptiveClient to(String host, String apiKey, String mapId) {
        return new RetrofitMaptiveClient(ServerConfig.forHost(host), apiKey, mapId);
    }

    public RetrofitMaptiveClient(ServerConfig serverConfig, String apiKey, String mapId) {
        this.apiKey = apiKey;
        this.mapId = mapId;

        final var delegate = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                final var req = chain.request();
                if (LOGGER.isDebugEnabled()) {
                    final var url = req.url();
                    final var sanitisedQps = url.queryParameterNames().stream()
                        .filter(qp -> !qp.startsWith("data["))
                        .filter(qp -> !qp.equals("key"))
                        .filter(qp -> !qp.equals("map_id"))
                        .map(qp -> "%s=%s".formatted(qp, url.queryParameter(qp)))
                        .collect(Collectors.toSet());
                    LOGGER.debug("{} http{}://{}/{}?{}",
                        req.method(),
                        url.isHttps() ? "s" : "",
                        url.host(),
                        url.encodedPath(),
                        sanitisedQps);
                }
                return chain.proceed(req);
            })
            .addInterceptor(chain -> {
                final var req = chain.request();
                if ("true".equalsIgnoreCase(req.url().queryParameter("delete_all"))) {
                    LOGGER.warn("Increase timeout to 60s for delete all");
                    return chain.withReadTimeout(60, TimeUnit.SECONDS).proceed(req);
                }

                if (req.url().queryParameter("data[50][0]") != null) {
                    LOGGER.warn("Increase timeout to 60s for large add");
                    return chain
                        .withReadTimeout(60, TimeUnit.SECONDS)
                        .proceed(req);
                }

                return chain.proceed(req);
            })
            .build();

        client = new Retrofit.Builder()
            .baseUrl(serverConfig.baseUrl())
            .addConverterFactory(GSON_CONVERTER)
            .client(delegate)
            .build();

        api = client.create(RetrofitMaptiveApi.class);
    }

    @Override
    public MaptiveApiResponse getAll() throws IOException {
        LOGGER.debug("Get all records");
        final var resp = api.get(apiKey, mapId, Collections.emptyList()).execute();
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse get(List<String> ids) throws IOException{
        LOGGER.debug("Getting records: {}", ids);
        final var resp = api.get(apiKey, mapId, ids).execute();
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse add(MaptiveData data) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Adding record {}", data.get("QUERY #")); // TODO should be configurable/determined
        }
        final var resp = api.create(apiKey, mapId, data.getColumnData()).execute();
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse addAll(Collection<MaptiveData> data) throws IOException {
        Response<MaptiveApiResponse> resp = null;
        final Map<String, String> columnData = new LinkedHashMap<>();
        final List<String> ids = new ArrayList<>();
        int i = 0;
        for (final var item : data) {
            int j = 0;
            for (final var val : item.getData().values()) {
                if (j == 2) { // TODO should be configurable/determined
                    ids.add(val);
                }
                columnData.put("data[%d][%d]".formatted(i, j), val);
                j++;
            }
            i++;
            if (i == 5) {
                LOGGER.debug("Batch adding {} records: {}", ids.size(), ids);
                resp = api.create(apiKey, mapId, columnData).execute();
                LOGGER.debug("Batched add all response: {}", coerce(resp));
                i = 0;
                ids.clear();
                columnData.clear();
            }
        }

        if (i > 0) {
            LOGGER.debug("Batch adding {} records: {}", ids.size(), ids);
            resp = api.create(apiKey, mapId, columnData).execute();
            LOGGER.debug("Batched add all response: {}", coerce(resp));
        }
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse update(MaptiveId id, Map<String, String> data) throws IOException {
        LOGGER.debug("Updating record: {}", id);
        final var resp = api.edit(apiKey, mapId, id.id(), data).execute();
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse delete(List<String> ids) throws IOException {
        LOGGER.debug("Deleting records: {}", ids);
        final var resp = api.delete(apiKey, mapId, ids).execute();
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse deleteAll() throws IOException {
        LOGGER.debug("Deleting all records");
        final var resp = api.deleteAll(apiKey, mapId).execute();
        return coerce(resp);
    }

    private MaptiveApiResponse coerce(Response<MaptiveApiResponse> resp) throws IOException {
        if (resp.isSuccessful())
            return resp.body();

        final var errorBody = resp.errorBody();
        if (errorBody != null) {
            final var errorBodyString = Optional.of(resp)
                .map(Response::errorBody)
                .map(r -> {
                    try {
                        return r.string();
                    } catch (IOException e) {
                        LOGGER.error("Unable to read error response body:{}", e.getMessage());
                        return "{}";
                    }
                })
                .orElse("{}");
            try {
                return GSON.fromJson(errorBodyString, MaptiveApiResponse.class);
            } catch (Exception e) {
                LOGGER.error("Unable to decode error response body: {}", errorBodyString);
            }
        }

        final var unknown = new MaptiveApiResponse();
        unknown.setCode(String.valueOf(resp.code()));
        final var message = Optional.of(resp)
            .map(Response::message)
            .orElseGet(() -> {
                final var code = resp.code();
                if (code > 199) {
                    return "Success";
                } else if (code > 299) {
                    return "Redirection";
                } else if (code > 399) {
                    return "Authorization issue";
                } else if (code > 499) {
                    return "Maptive API issue";
                } else {
                    return "Unknown";
                }
            });
        unknown.setMessage(message);
        return unknown;
    }
}
