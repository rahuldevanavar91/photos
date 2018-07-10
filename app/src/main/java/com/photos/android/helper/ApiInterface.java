package com.photos.android.helper;

import com.photos.android.model.DataRepponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Rahul D on 7/10/18.
 */
public interface ApiInterface {

    @GET("tags/test/media/recent")
    Call<DataRepponse> getMovieDetails(@Query("access_token") String apiKey);
}
