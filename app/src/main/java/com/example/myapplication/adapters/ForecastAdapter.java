package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageRequest;
import com.example.myapplication.R;
import com.example.myapplication.models.ForecastItem;
import com.example.myapplication.network.VolleySingleton;

import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private final List<ForecastItem> list;

    public ForecastAdapter(List<ForecastItem> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvTemp;
        ImageView imgWeather;

        public ViewHolder(View view) {
            super(view);
            tvDay = view.findViewById(R.id.tvDay);
            tvTemp = view.findViewById(R.id.tvTemp);
            imgWeather = view.findViewById(R.id.imgWeather);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForecastItem item = list.get(position);

        holder.tvDay.setText(item.day);
        holder.tvTemp.setText(Math.round(item.temp) + "°C");

        String url = "https://openweathermap.org/img/wn/" + item.icon + "@2x.png";

        ImageRequest request = new ImageRequest(url,
                holder.imgWeather::setImageBitmap, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                error -> {
                    // Handle error if needed
                });

        VolleySingleton.getInstance(holder.itemView.getContext())
                .addToRequestQueue(request);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
