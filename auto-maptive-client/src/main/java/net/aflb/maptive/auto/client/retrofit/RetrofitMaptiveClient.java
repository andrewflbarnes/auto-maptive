package net.aflb.maptive.auto.client.retrofit;

import com.google.gson.Gson;
import net.aflb.maptive.auto.client.MaptiveApiResponse;
import net.aflb.maptive.auto.client.MaptiveClient;
import net.aflb.maptive.auto.core.MaptiveData;
import net.aflb.maptive.auto.core.MaptiveId;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RetrofitMaptiveClient implements MaptiveClient {

    public static final String DEFAULT_PROTO = "https";
    public static final String DEFAULT_HOST = "fortress.maptive.com";
    public static final String DEFAULT_BASE_PATH = "ver4/classes/api/v1.0/";

    private static final GsonConverterFactory GSON_CONVERTER = GsonConverterFactory.create();
    private static final Gson GSON = new Gson();

    private final Retrofit client;
    private final RetrofitMaptiveApi api;

    public static RetrofitMaptiveClient production() {
        return to(DEFAULT_HOST);
    }

    public static RetrofitMaptiveClient to(String host) {
        return new RetrofitMaptiveClient(host, DEFAULT_BASE_PATH);
    }

    public RetrofitMaptiveClient(String host, String basePath) {
        this(DEFAULT_PROTO, host, basePath);
    }

    public RetrofitMaptiveClient(String proto, String host, String basePath) {
        final var delegate = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                final var req = chain.request();
                if (req.url().queryParameter("data[50][0]") == null) {
                    System.out.println(req.url());
                } else {
                    System.out.println("TRUNCATING LARGE ADD");
                    System.out.println(req.url().host());
                    System.out.println(req.url().encodedPath());
                }
                System.out.println(req.method());
                return chain.proceed(req);
            })
            .addInterceptor(chain -> {
                final var req = chain.request();
                if ("true".equalsIgnoreCase(req.url().queryParameter("delete_all"))) {
                    System.out.println("Increase timeout to 60s for delete all");
                    return chain.withReadTimeout(60, TimeUnit.SECONDS).proceed(req);
                }

                if (req.url().queryParameter("data[50][0]") != null) {
                    System.out.println("Increase timeout to 60s for large add");
                    return chain
                        .withReadTimeout(60, TimeUnit.SECONDS)
                        .proceed(req);
                }

                return chain.proceed(req);
            })
            .build();

        client = new Retrofit.Builder()
            .baseUrl("%s://%s/%s".formatted(proto, host, basePath))
            .addConverterFactory(GSON_CONVERTER)
            .client(delegate)
            .build();

        api = client.create(RetrofitMaptiveApi.class);
    }

    @Override
    public MaptiveApiResponse getAll(String apiKey, String mapId) throws IOException {
        final var resp = api.get(apiKey, mapId, Collections.emptyList()).execute();
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse get(String apiKey, String mapId, List<String> ids) throws IOException{
        final var resp = api.get(apiKey, mapId, ids).execute();
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse add(String apiKey, String mapId, MaptiveData data) throws IOException {
        System.out.println(data);
        System.out.println(data.getColumnData());
        final var resp = api.create(apiKey, mapId, data.getColumnData()).execute();
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse addAll(String apiKey, String mapId, Collection<MaptiveData> data) throws IOException {
        // TODO batch?
        Response<MaptiveApiResponse> resp = null;
        final Map<String, String> columnData = new LinkedHashMap<>();
        int i = 0;
        for (final var item : data) {
            int j = 0;
            for (final var val : item.getData().values()) {
                columnData.put("data[%d][%d]".formatted(i, j), val);
                j++;
            }
            i++;
            if (i == 5) {
                resp = api.create(apiKey, mapId, columnData).execute();
                System.out.println(coerce(resp));
                i=0;
                columnData.clear();
            }
        }

//        columnData.forEach((k, v) -> System.out.printf("%s=%s%n", k, v));
        if (i > 0) {
            resp = api.create(apiKey, mapId, columnData).execute();
            System.out.println(coerce(resp));
        }
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse update(String apiKey, String mapId, MaptiveId id, Map<String, String> data) throws IOException {
        final var resp = api.edit(apiKey, mapId, id.id(), data).execute();
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse delete(String apiKey, String mapId, List<String> ids) throws IOException {
        final var resp = api.delete(apiKey, mapId, ids).execute();
        return coerce(resp);
    }

    @Override
    public MaptiveApiResponse deleteAll(String apiKey, String mapId) throws IOException {
        final var resp = api.deleteAll(apiKey, mapId).execute();
        return coerce(resp);
    }

    private MaptiveApiResponse coerce(Response<MaptiveApiResponse> resp) throws IOException {
        if (resp.isSuccessful())
            return resp.body();

        final var errorBody = resp.errorBody();
        if (errorBody != null) {
            return GSON.fromJson(resp.errorBody().string(), MaptiveApiResponse.class);
        }

        final var unknown = new MaptiveApiResponse();
        unknown.setCode(String.valueOf(resp.code()));
        unknown.setMessage(resp.message());
        return unknown;
    }
}
