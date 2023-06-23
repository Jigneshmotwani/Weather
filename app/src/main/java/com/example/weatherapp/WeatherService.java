package com.example.weatherapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("weather")
    Call<WeatherResponse> getWeatherData(@Query("q") String location, @Query("appid") String apiKey);

    @GET("weather")
    Call<WeatherResponse> getWeatherData(@Query("lat") double latitude, @Query("lon") double longitude, @Query("appid") String apiKey);
}
