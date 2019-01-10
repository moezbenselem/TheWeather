package benselem.moez.theweather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Moez on 30/04/2018.
 */

public class RecyclerFav extends RecyclerView.Adapter {

    Context context;
    ArrayList<Weather> listForecasts;
    HolderFav viewHolder;
    DecimalFormat df = new DecimalFormat("0.##");

    public RecyclerFav(ArrayList<Weather> wheathers , Context context) {

        this.listForecasts = wheathers;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_weather, parent, false);
        viewHolder = new HolderFav(v,listForecasts,context);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        try {
            viewHolder = (HolderFav) holder;


            viewHolder.tvCity.setText("City : " + listForecasts.get(position).city + " " + listForecasts.get(position).country);
            //viewHolder.tvDate.setText("Date : "+listForecasts.get(position).date);
            viewHolder.tvDesc.setText("Description : " + listForecasts.get(position).desc);
            viewHolder.tvGeneral.setText("General : " + listForecasts.get(position).general);
            viewHolder.tvHum.setText("Humidity : " + listForecasts.get(position).humidity+"%");
            viewHolder.tvWind.setText("Wind Speed: " + df.format(listForecasts.get(position).wind) + " Km/h "+listForecasts.get(position).direction);
            viewHolder.tvTemp.setText("Temp : " + df.format(listForecasts.get(position).temp) + " °C");
            viewHolder.btLogo.setBackgroundResource(listForecasts.get(position).logo);
            viewHolder.tvRise.setText(listForecasts.get(position).sunrise);
            viewHolder.tvSet.setText(listForecasts.get(position).sunset);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listForecasts.size();
    }
}
