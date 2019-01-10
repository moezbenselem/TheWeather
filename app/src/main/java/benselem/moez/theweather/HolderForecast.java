package benselem.moez.theweather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import java.util.ArrayList;

/**
 * Created by Moez on 28/04/2018.
 */

public class HolderForecast extends RecyclerView.ViewHolder {

    //public ImageView itemImage;
    public TextView tvCity,tvGeneral,tvDesc,tvTemp,tvWind,tvDate;
    Button btLogo ;
    static Context context;

    public HolderForecast(View itemView, final ArrayList<Forecast> forecasts, final Context context) {
        super(itemView);

        this.context = context;
        //imageSociete = (ImageView)itemView.findViewById(R.id.imageViewSociete);
        tvCity = (TextView)itemView.findViewById(R.id.item_city);
        tvDate = (TextView)itemView.findViewById(R.id.item_date);
        tvDesc = (TextView)itemView.findViewById(R.id.item_desc);
        tvTemp = (TextView)itemView.findViewById(R.id.item_temp);
        tvGeneral = (TextView)itemView.findViewById(R.id.item_general);
        tvWind = (TextView)itemView.findViewById(R.id.item_wind);
        btLogo = (Button)itemView.findViewById(R.id.btState);
        itemView.setVisibility(View.VISIBLE);

    }


}


