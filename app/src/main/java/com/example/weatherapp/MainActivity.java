package com.example.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView cityWeatherRecyclerView;

    private WeatherApi weatherApi;




    private static final String API_KEY = "5058a050dd28490f3aa3ea1c89243a9b";
    private static final String API_BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private ImageView backgroundImageView;
    private TextView locationTextView;
    private TextView temperatureTextView;
    private TextView weatherDescriptionTextView;
    private TextView highLowTemperatureTextView;
    private TextView humidityTextView;
    private TextView pressureTextView;
    private TextView newYorkTemperatureTextView;;
    private CardView newYorkBackgroundImageView;
    private TextView singaporeTemperatureTextView;;
    private CardView singaporeBackgroundImageView;
    private TextView mumbaiTemperatureTextView;;
    private CardView mumbaiBackgroundImageView;
    private TextView delhiTemperatureTextView;;
    private CardView delhiBackgroundImageView;
    private TextView sydneyTemperatureTextView;;
    private CardView sydneyBackgroundImageView;
    private TextView melbourneTemperatureTextView;;
    private CardView melbourneBackgroundImageView;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private String cityName = "Current Location";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        backgroundImageView = findViewById(R.id.backgroundImageView);
        locationTextView = findViewById(R.id.locationTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        weatherDescriptionTextView = findViewById(R.id.weatherDescriptionTextView);
        highLowTemperatureTextView = findViewById(R.id.highLowTemperatureTextView);
        pressureTextView = findViewById(R.id.pressureTextView);
        humidityTextView = findViewById(R.id.humidityTextView);

        newYorkTemperatureTextView = findViewById(R.id.newYorkTemperatureTextView);
        singaporeTemperatureTextView = findViewById(R.id.singaporeTemperatureTextView);
        mumbaiTemperatureTextView = findViewById(R.id.mumbaiTemperatureTextView);
        delhiTemperatureTextView = findViewById(R.id.delhiTemperatureTextView);
        sydneyTemperatureTextView = findViewById(R.id.sydneyTemperatureTextView);
        melbourneTemperatureTextView = findViewById(R.id.melbourneTemperatureTextView);

        newYorkBackgroundImageView = findViewById(R.id.newYorkBackgroundImageView);
        singaporeBackgroundImageView = findViewById(R.id.singaporeBackgroundImageView);
        mumbaiBackgroundImageView = findViewById(R.id.mumbaiBackgroundImageView);
        delhiBackgroundImageView = findViewById(R.id.delhiBackgroundImageView);
        sydneyBackgroundImageView = findViewById(R.id.sydneyBackgroundImageView);
        melbourneBackgroundImageView = findViewById(R.id.melbourneBackgroundImageView);

        // Update the UI with the saved weather data


        // Retrofit initialization with Gson converter
        Gson gson = new GsonBuilder().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        weatherApi = retrofit.create(WeatherApi.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }

        fetchWeatherData();

    } // end of OnCreate

    private void fetchWeatherData() {
        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherService weatherService = retrofit.create(WeatherService.class);

        // Fetch weather data for each location
        String[] locations = {
                "New York",
                "Singapore",
                "Mumbai",
                "Delhi",
                "Sydney",
                "Melbourne"
        };

        for (String location : locations) {
            Call<WeatherResponse> call = weatherService.getWeatherData(location, API_KEY);
            call.enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    if (response.isSuccessful()) {
                        WeatherResponse weatherResponse = response.body();

                        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                                WeatherDataWorker.class,
                                1, TimeUnit.HOURS)
                                .build();

                        WorkManager.getInstance(MainActivity.this).enqueueUniquePeriodicWork(
                                "weatherDataUpdate",
                                ExistingPeriodicWorkPolicy.REPLACE,
                                periodicWorkRequest);

                        if (weatherResponse != null) {
                            WeatherMain weatherMain = weatherResponse.getWeatherMain();
                            double temperature = weatherMain != null ? weatherMain.getTemperature() : 0.0;
                            WeatherCondition[] weatherConditions = weatherResponse.getWeatherConditions();
                            String weatherCondition = "";
                            if (weatherConditions != null && weatherConditions.length > 0) {
                                weatherCondition = weatherConditions[0].getMain();
                            }
                            updateTemperatureTextView(location, temperature);
                            updateBackgroundImage(location, weatherCondition);
                        }
                    }
                }

                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {

                }
            });
        }

    }







    private void updateTemperatureTextView(String location, double temperature) {
        TextView temperatureTextView;
        switch (location) {
            case "New York":
                temperatureTextView = newYorkTemperatureTextView;
                break;
            case "Singapore":
                temperatureTextView = singaporeTemperatureTextView;
                break;
            case "Mumbai":
                temperatureTextView = mumbaiTemperatureTextView;
                break;
            case "Delhi":
                temperatureTextView = delhiTemperatureTextView;
                break;
            case "Sydney":
                temperatureTextView = sydneyTemperatureTextView;
                break;
            case "Melbourne":
                temperatureTextView = melbourneTemperatureTextView;
                break;
            default:
                return;
        }

        int roundedTemperature = (int) Math.round(temperature - 273);
        String formattedTemperature = roundedTemperature + "°C";

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                temperatureTextView.setText(String.valueOf(formattedTemperature));
            }
        });
    }

    private void updateBackgroundImage(String location, String weatherCondition) {
        // Update background image based on the weather condition and location
        int backgroundImageResId;
        switch (weatherCondition) {
            case "Clouds":
                backgroundImageResId = R.drawable.cloudy;
                break;
            case "Rain":
            case "Drizzle":
                backgroundImageResId = R.drawable.rainy;
                break;
            default:
                backgroundImageResId = R.drawable.sunny;
                break;
        }

        switch (location) {
            case "New York":
                newYorkBackgroundImageView.setBackgroundResource(backgroundImageResId);
                break;
            case "Singapore":
                singaporeBackgroundImageView.setBackgroundResource(backgroundImageResId);
                break;
            case "Mumbai":
                mumbaiBackgroundImageView.setBackgroundResource(backgroundImageResId);
                break;
            case "Delhi":
                delhiBackgroundImageView.setBackgroundResource(backgroundImageResId);
                break;
            case "Sydney":
                sydneyBackgroundImageView.setBackgroundResource(backgroundImageResId);
                break;
            case "Melbourne":
                melbourneBackgroundImageView.setBackgroundResource(backgroundImageResId);
                break;
            default:
                return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(MainActivity.this)
                        .load(backgroundImageResId)
                        .centerCrop()
                        .into(backgroundImageView);

                backgroundImageView.setVisibility(View.VISIBLE);
            }
        });
    }



    private String getCityName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                return address.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void fetchCurrentLocationAndWeatherData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                cityName = getCityName(latitude, longitude);
                fetchWeatherData(latitude, longitude);
            } else {
                // Handle case of no last known location

            }
        });

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location lastLocation = locationResult.getLastLocation();
                    double latitude = lastLocation.getLatitude();
                    double longitude = lastLocation.getLongitude();
                    fetchWeatherData(latitude, longitude);
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void fetchWeatherData(double latitude, double longitude) {
        locationTextView.setText(cityName);
        Call<WeatherResponse> call = weatherApi.getWeatherData(latitude, longitude, API_KEY);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    if (weatherResponse != null) {
                        updateUI(weatherResponse);
                    }
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
            }
        });
    }

    private void updateUI(WeatherResponse weatherResponse) {
        locationTextView.setText(cityName);
        WeatherMain weatherMain = weatherResponse.getWeatherMain();
        WeatherCondition[] weatherConditions = weatherResponse.getWeatherConditions();
        int temperature = (int) Math.round(weatherMain.getTemperature() - 273.15);
        String temperatureText = temperature + "°C";
        temperatureTextView.setText(temperatureText);
        if (weatherConditions != null && weatherConditions.length > 0) {
            String capitalizedDescription = capitalizeWords(weatherConditions[0].getDescription());
            weatherDescriptionTextView.setText(capitalizedDescription);
        } else {
            weatherDescriptionTextView.setText("N/A");
        }
        String highLowTemperature = "H: " + Math.round(weatherMain.getTempMax() - 273.15) + "°C  L: " + Math.round(weatherMain.getTempMin() - 273.15) + "°C";
        highLowTemperatureTextView.setText(highLowTemperature);
        String pressure = "Pressure: " + weatherMain.getPressure() + " hPa";
        pressureTextView.setText(pressure);
        String humidity = "Humidity: " + weatherMain.getHumidity() + "%";
        humidityTextView.setText(humidity);

       ;

        int backgroundResId;
        switch (weatherConditions[0].getMain()) {
            case "Clear":
                backgroundResId = R.drawable.sunny_bg;
                break;
            case "Clouds":
                backgroundResId = R.drawable.cloudy_bg;
                break;
            case "Rain":
                backgroundResId = R.drawable.rainy_bg;
                break;
            default:
                backgroundResId = R.drawable.sunny_bg; // Use sunny background as default
                break;
        }
        backgroundImageView.setImageResource(backgroundResId);
    }

    private void saveWeatherData(String prefsKey, WeatherResponse weatherResponse) {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(prefsKey, new Gson().toJson(weatherResponse));
        editor.apply();
    }

    private WeatherResponse getSavedWeatherData(String prefsKey) {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherData", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(prefsKey, null);
        if (json != null) {
            return new Gson().fromJson(json, WeatherResponse.class);
        } else {
            return null;
        }
    }

    private String capitalizeWords(String text) {
        String[] words = text.split("\\s");
        StringBuilder capitalizedText = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                String capitalizedWord = Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
                capitalizedText.append(capitalizedWord).append(" ");
            }
        }
        return capitalizedText.toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocationAndWeatherData();
            } else {
                Toast.makeText(this, "Location permission denied. Weather data cannot be fetched.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}