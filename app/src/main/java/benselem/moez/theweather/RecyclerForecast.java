package benselem.moez.theweather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Moez on 28/04/2018.
 */

public class RecyclerForecast extends RecyclerView.Adapter {

    Context context;
    ArrayList<Forecast> listForecasts;
    HolderForecast viewHolder;
    DecimalFormat df = new DecimalFormat("0.##");

    public RecyclerForecast(ArrayList<Forecast> forecasts , Context context) {

        this.listForecasts = forecasts;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_forecast, parent, false);
        viewHolder = new HolderForecast(v,listForecasts,context);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        viewHolder = (HolderForecast) holder;

        viewHolder.tvCity.setText("City : "+listForecasts.get(position).city+" "+listForecasts.get(position).country);
        viewHolder.tvDate.setText("Date : "+listForecasts.get(position).date);
        viewHolder.tvDesc.setText("Description : "+listForecasts.get(position).desc);
        viewHolder.tvGeneral.setText("General : "+listForecasts.get(position).general);
        viewHolder.tvWind.setText("Wind Speed: "+df.format(listForecasts.get(position).wind)+" Km/h");
        viewHolder.tvTemp.setText("Temp : "+df.format(listForecasts.get(position).temp)+ " °C");
        viewHolder.btLogo.setBackgroundResource(listForecasts.get(position).state);

    }

    @Override
    public int getItemCount() {
        return listForecasts.size();
    }
}
