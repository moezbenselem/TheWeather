package benselem.moez.theweather;

/**
 * Created by Moez on 30/04/2018.
 */

public class Weather {

    String city , country,general,desc,sunrise,sunset,direction,humidity;
    Double wind, temp;
    int logo;

    public Weather(String city, String country, String general, String desc, String sunrise, String sunset, Double wind,String direction, Double temp, String humidity, int logo) {
        this.city = city;
        this.country = country;
        this.general = general;
        this.desc = desc;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.wind = wind;
        this.temp = temp;
        this.humidity = humidity;
        this.logo = logo;
        this.direction = direction;
    }
}
