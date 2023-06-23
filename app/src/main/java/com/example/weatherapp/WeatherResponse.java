package com.example.weatherapp;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("main")
    private WeatherMain weatherMain;

    @SerializedName("weather")
    private WeatherCondition[] weatherConditions;

    public WeatherMain getWeatherMain() {
        return weatherMain;
    }

    public void setWeatherMain(WeatherMain weatherMain) {
        this.weatherMain = weatherMain;
    }

    public WeatherCondition[] getWeatherConditions() {
        return weatherConditions;
    }

    public void setWeatherConditions(WeatherCondition[] weatherConditions) {
        this.weatherConditions = weatherConditions;
    }
}




