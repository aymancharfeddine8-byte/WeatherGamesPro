package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "4c065084624752c5d3f2323bf62464e3";
    private static final String CHANNEL_ID = "weather_alerts";

    private EditText etCity;
    private Button btnSearch, btnForecast, btnActivities;
    private ProgressBar progressBar;
    private CardView cardWeather;
    private TextView tvCity, tvTemp, tvDesc, tvHumidity, tvWind, tvError;

    private RequestQueue requestQueue;
    private String lastSearchedCity = "Tunis";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();
        initViews();
        setupSearch();

        getWeather(lastSearchedCity);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Weather Alerts";
            String description = "Notifications for bad weather conditions";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void initViews() {
        etCity = findViewById(R.id.etCity);
        btnSearch = findViewById(R.id.btnSearch);
        btnForecast = findViewById(R.id.btnForecast);
        btnActivities = findViewById(R.id.btnActivities);
        progressBar = findViewById(R.id.progressBar);
        cardWeather = findViewById(R.id.cardWeather);
        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvDesc = findViewById(R.id.tvDesc);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvError = findViewById(R.id.tvError);

        requestQueue = Volley.newRequestQueue(this);
    }

    private void setupSearch() {
        btnSearch.setOnClickListener(v -> {
            String city = etCity.getText().toString().trim();
            if (!city.isEmpty()) {
                hideKeyboard();
                getWeather(city);
            } else {
                Toast.makeText(this, "Enter a city name", Toast.LENGTH_SHORT).show();
            }
        });

        btnForecast.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForecastActivity.class);
            intent.putExtra("CITY_NAME", lastSearchedCity);
            startActivity(intent);
        });

        btnActivities.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivitiesActivity.class));
        });

        etCity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String city = etCity.getText().toString().trim();
                if (!city.isEmpty()) {
                    hideKeyboard();
                    getWeather(city);
                }
                return true;
            }
            return false;
        });
    }

    private void getWeather(String city) {
        showLoading(true);
        hideError();

        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + Uri.encode(city) + "&units=metric&appid=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String cityName = response.getString("name");
                        lastSearchedCity = cityName;
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        int humidity = main.getInt("humidity");
                        JSONObject wind = response.getJSONObject("wind");
                        double windSpeed = wind.getDouble("speed");
                        JSONObject weatherArray = response.getJSONArray("weather").getJSONObject(0);
                        String description = weatherArray.getString("description");
                        int weatherId = weatherArray.getInt("id");

                        tvCity.setText(cityName);
                        tvTemp.setText(Math.round(temp) + "°C");
                        tvDesc.setText(description.substring(0, 1).toUpperCase() + description.substring(1));
                        tvHumidity.setText(humidity + "%");
                        tvWind.setText(windSpeed + " m/s");

                        checkBadWeather(weatherId, description);

                        showWeatherInfo(true);
                        showLoading(false);
                    } catch (Exception e) {
                        showError("Error parsing weather data");
                        showLoading(false);
                    }
                },
                error -> {
                    showLoading(false);
                    showError("Could not find city or connection error");
                }
        );

        requestQueue.add(request);
    }

    private void checkBadWeather(int weatherId, String description) {
        if (weatherId < 700 || weatherId == 781) {
            sendNotification("Bad Weather Alert!", "Be careful! " + description + " is expected. Stay safe indoors.");
        }
    }

    private void sendNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) cardWeather.setVisibility(View.GONE);
    }

    private void showWeatherInfo(boolean show) {
        cardWeather.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        cardWeather.setVisibility(View.GONE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
