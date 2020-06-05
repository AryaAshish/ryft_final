package com.architectica.socialcomponents.api;

import com.architectica.socialcomponents.model.News;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("top-headlines")
    Call<News> getNews(

            @Query("sources") String source ,
            @Query("apiKey") String apiKey

    );

}
