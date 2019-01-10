package benselem.moez.theweather;

import android.graphics.Bitmap;

/**
 * Created by Moez on 28/04/2018.
 */

public class Forecast {

    String city,country,date,desc,general;
    Double wind,temp;
    int state;


    public Forecast(String city, String country, String date, String desc, String general, Double wind, Double temp,int logo) {
        this.city = city;
        this.country = country;
        this.date = date;
        this.desc = desc;
        this.general = general;
        this.wind = wind;
        this.temp = temp;
        this.state=logo;
    }
}
