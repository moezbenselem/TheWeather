package benselem.moez.theweather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Moez on 30/04/2018.
 */

public class HolderFav extends RecyclerView.ViewHolder {

    //public ImageView itemImage;
    public TextView tvCity,tvGeneral,tvDesc,tvTemp,tvWind,tvHum,tvRise,tvSet;
    Button btLogo ;
    static Context context;

    public HolderFav(View itemView, final ArrayList<Weather> weathers, final Context context) {

        super(itemView);

        try{

        this.context = context;
        //imageSociete = (ImageView)itemView.findViewById(R.id.imageViewSociete);
        tvCity = (TextView)itemView.findViewById(R.id.item_city);
        tvHum = (TextView)itemView.findViewById(R.id.item_humidity);
        tvDesc = (TextView)itemView.findViewById(R.id.item_desc);
        tvTemp = (TextView)itemView.findViewById(R.id.item_temp);
        tvGeneral = (TextView)itemView.findViewById(R.id.item_general);
        tvWind = (TextView)itemView.findViewById(R.id.item_wind);
        tvRise = (TextView)itemView.findViewById(R.id.item_sunsrise);
        tvSet = (TextView)itemView.findViewById(R.id.item_sunset);
        btLogo = (Button)itemView.findViewById(R.id.btState);
        itemView.setVisibility(View.VISIBLE);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }


}



