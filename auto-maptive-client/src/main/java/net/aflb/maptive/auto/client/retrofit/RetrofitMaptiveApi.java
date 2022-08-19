package net.aflb.maptive.auto.client.retrofit;

import net.aflb.maptive.auto.client.MaptiveApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import java.util.List;
import java.util.Map;

public interface RetrofitMaptiveApi {

    @GET("markers")
    Call<MaptiveApiResponse> get(
        @Query("key") String apiKey,
        @Query("map_id") String mapId,
        @Query("ids") List<String> ids);

    @POST("markers?method=create")
    Call<MaptiveApiResponse> create(
        @Query("key") String apiKey,
        @Query("map_id") String mapId,
        @QueryMap Map<String, String> columns);

    @POST("markers?method=edit")
    Call<MaptiveApiResponse> edit(
        @Query("key") String apiKey,
        @Query("map_id") String mapId,
        @Query("index_col") String itemKey,
        @QueryMap Map<String, String> columns);

    @POST("markers?method=delete")
    Call<MaptiveApiResponse> delete(
        @Query("key") String apiKey,
        @Query("map_id") String mapId,
        @Query("index_col") List<String> itemKeys);

    @POST("markers?method=delete&delete_all=true")
    Call<MaptiveApiResponse> deleteAll(
        @Query("key") String apiKey,
        @Query("map_id") String mapId);
}
