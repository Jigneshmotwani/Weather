package com.example.weatherapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WeatherDataWorker extends Worker {

    public WeatherDataWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Perform the network call to get the latest weather data
        fetchWeatherData();

        return Result.success();
    }

    private void fetchWeatherData() {
    }
}
