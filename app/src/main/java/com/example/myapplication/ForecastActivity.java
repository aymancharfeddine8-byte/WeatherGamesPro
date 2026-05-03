package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.adapters.ForecastAdapter;
import com.example.myapplication.models.ForecastItem;
import com.example.myapplication.network.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ForecastActivity extends AppCompatActivity {

    private List<ForecastItem> list;
    private ForecastAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        RecyclerView recycler = findViewById(R.id.recyclerForecast);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        list = new ArrayList<>();
        adapter = new ForecastAdapter(list);
        recycler.setAdapter(adapter);

        String city = getIntent().getStringExtra("CITY_NAME");
        if (city == null || city.isEmpty()) city = "Tunis";

        getForecast(city);
    }

    private void getForecast(String city) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?q="
                + Uri.encode(city) + "&units=metric&appid=" + BuildConfig.WEATHER_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray arr = response.getJSONArray("list");
                        list.clear();
                        for (int i = 0; i < arr.length(); i += 8) {
                            JSONObject obj = arr.getJSONObject(i);
                            double temp = obj.getJSONObject("main").getDouble("temp");
                            String icon = obj.getJSONArray("weather").getJSONObject(0).getString("icon");
                            
                            String dateText = obj.getString("dt_txt").split(" ")[0];
                            String[] parts = dateText.split("-");
                            String displayDate = parts[2] + "/" + parts[1];

                            list.add(new ForecastItem(displayDate, temp, icon));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Could not load forecast", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
