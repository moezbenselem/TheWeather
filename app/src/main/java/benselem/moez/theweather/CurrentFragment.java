package benselem.moez.theweather;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.Provider;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class CurrentFragment extends Fragment {

    public static String APPID ="32e71b2392320fed5d6d5117214377ff";
    String provider;
    LocationManager locationManager;
    Double lat=0.0,lon=0.0;
    Location loc;
    TextView tvCity,tvGeneral,tvDesc,tvTemp,tvWind,tvHum,tvSunset,tvSunrise;
    Button state;
    CardView card_weather;

    public CurrentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_current, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        try {

            card_weather = (CardView) getView().findViewById(R.id.card_weather);

            tvCity = (TextView) card_weather.findViewById(R.id.item_city);
            tvDesc = (TextView) card_weather.findViewById(R.id.item_desc);
            tvGeneral = (TextView) card_weather.findViewById(R.id.item_general);
            tvTemp = (TextView) card_weather.findViewById(R.id.item_temp);
            tvWind = (TextView) card_weather.findViewById(R.id.item_wind);
            state = (Button) card_weather.findViewById(R.id.btState);
            tvHum = (TextView) card_weather.findViewById(R.id.item_humidity);
            tvSunset = (TextView) card_weather.findViewById(R.id.item_sunset);
            tvSunrise = (TextView) card_weather.findViewById(R.id.item_sunsrise);

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            provider = locationManager.getBestProvider(new Criteria(), false);


            /*Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {

                lat = location.getLatitude();
                lon = location.getLongitude();
                System.out.println("longgggg ======  " + lon);
                System.out.println("laaatttt ======  " + lat);
            }*/

            MobileAds.initialize(getContext(), "ca-app-pub-7087198421941611~9925442089");
            AdView mAdView = (AdView) getView().findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            final EditText etCity = (EditText) getView().findViewById(R.id.editText1);
            Button btSearch = (Button) getView().findViewById(R.id.btSearch);
            Button btMap = (Button) getView().findViewById(R.id.btMap);
            btSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    getWeather(etCity.getText().toString());


                }
            });

            loc = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000,
                    1, mLocationListener);

            btMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Double lat = 0.0, lon = 0.0;
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                                , 10);


                    }

                    lat = loc.getLatitude();
                    lon = loc.getLongitude();
                    System.out.println("longgggg ======  " + lon);
                    System.out.println("laaatttt ======  " + lat);
                    getWeatherByLocation(lon, lat);






                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
        super.onViewCreated(view, savedInstanceState);
    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            loc = location;
            lon = location.getLongitude();
            lat = location.getLatitude();

            System.out.println("longgggg from listner ======  " +lon);
            System.out.println("laaatttt from listner ======  " +lat);
            //getWeatherByLocation(lon, lat);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void getWeather(String city) {
        try {
            MainActivity.nbr++;
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putInt("nbr",MainActivity.nbr);
            editor.apply();

            Calendar cal = Calendar.getInstance();
            final int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            final String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&APPID="+APPID;
            final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("result === \n"+response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                DecimalFormat df = new DecimalFormat("0.##");

                                String weatherInfo = jsonObject.getString("weather");
                                String cityName = jsonObject.getString("name");
                                String main = jsonObject.getString("main");
                                String sys = jsonObject.getString("sys");

                                Long rise = jsonObject.getJSONObject("sys").getLong("sunrise");
                                Long set = jsonObject.getJSONObject("sys").getLong("sunset");
                                String sunrise = getDate(rise);
                                String sunset = getDate(set);
                                tvSunrise.setText(sunrise);
                                tvSunset.setText(sunset);
                                System.out.println("sys : " + sys);
                                String[] arrStr = sys.split(",");
                                String country = arrStr[3].substring(11, 13);
                                System.out.println(arrStr[3]);

                                JSONObject jsonWind = jsonObject.getJSONObject("wind");
                                String WIND = jsonWind.getString("speed");
                                Double deg = jsonWind.getDouble("deg");
                                System.out.println("WIND === "+WIND);
                                System.out.println("deg === "+toTextualDescription(deg));

                                double windSpeed = Double.parseDouble(WIND) * 3.6d;

                                System.out.println("maiin = " + main);
                                String cityTemp = main.substring(7, 15);
                                String humidity = jsonObject.getJSONObject("main").getString("humidity");
                                System.out.println("humidity === "+humidity);
                                System.out.println("cityyyy temppp ::: " + cityTemp);
                                String temp = "";
                                for (int i = 0; i < cityTemp.length(); i++) {
                                    if (Character.isDigit(cityTemp.charAt(i))) {
                                        temp = new StringBuilder(String.valueOf(temp)).append(cityTemp.charAt(i)).toString();
                                    } else if (cityTemp.charAt(i) == '.') {
                                        temp = new StringBuilder(String.valueOf(temp)).append(cityTemp.charAt(i)).toString();
                                    }
                                }
                                System.out.println(temp);
                                JSONArray arr = new JSONArray(weatherInfo);

                                double celsius = Double.parseDouble(temp) - 273.0d;
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject jsonPart = arr.getJSONObject(i);
                                    tvCity.setText("City :  " + cityName + " " + country);
                                    tvGeneral.setText("General :  " + jsonPart.get("main"));
                                    tvDesc.setText("Description :  " +jsonPart.get("description"));
                                    tvTemp.setText("Temp :  " + df.format(celsius) + " °C");
                                    tvWind.setText("Wind Speed :  " + df.format(windSpeed) + " Km/h "+toTextualDescription(deg));
                                    tvHum.setText("Humidity : "+humidity+"%");

                                    String general = jsonPart.get("main").toString();
                                    Integer logo =R.drawable.sun;

                                    if (general.equalsIgnoreCase("clear"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.sun;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon;
                                        }

                                    if (general.equalsIgnoreCase("clouds"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.sun_clouds;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon_clouds;
                                        }

                                    if (general.equalsIgnoreCase("braken clouds"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.sun_clouds;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon_clouds;
                                        }

                                    if (general.equalsIgnoreCase("scratted clouds"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.sun_few_clouds;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon_few_clouds;
                                        }

                                    if (general.equalsIgnoreCase("shower rain") || general.equalsIgnoreCase("rain"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.rain;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon_rain;
                                        }

                                    if (general.equalsIgnoreCase("snow"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.snow;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon_snow;
                                        }

                                    if (general.equalsIgnoreCase("mist"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.mist_icone;
                                        }
                                        else
                                        {
                                            logo = R.drawable.mist_icone;
                                        }
                                    state.setBackgroundResource(logo);

                                }
                                card_weather.setVisibility(View.VISIBLE);

                                testInter();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_LONG).show();

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new Hashtable<>();

                    return params;
                }
            };
            {
                int socketTimeout = 30000;
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                stringRequest.setRetryPolicy(policy);
                RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                requestQueue.add(stringRequest);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }
    InterstitialAd mInterstitialAd;
    public void testInter(){

        System.out.println(MainActivity.nbr);
        if(MainActivity.nbr==4) {
            MainActivity.nbr =0;
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putInt("nbr",MainActivity.nbr);
            editor.apply();

            mInterstitialAd = new InterstitialAd(getActivity());
            mInterstitialAd.setAdUnitId("ca-app-pub-7087198421941611/1272907939");
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mInterstitialAd.show();

                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when the ad is displayed.
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                @Override
                public void onAdClosed() {


                }
            });
            if (mInterstitialAd.isLoaded()) {

            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.");
            }


        }



    }
    private void getWeatherByLocation(Double lon , Double lat) {
        try {
            MainActivity.nbr++;
            SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
            editor.putInt("nbr",MainActivity.nbr);
            editor.apply();

            String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&APPID="+APPID;
            System.out.println(url);
            Calendar cal = Calendar.getInstance();
            final int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            final String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            System.out.println("hourrrrrssss ==== "+currentHour);

            final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("result === \n"+response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                DecimalFormat df = new DecimalFormat("0.##");

                                String weatherInfo = jsonObject.getString("weather");
                                String cityName = jsonObject.getString("name");
                                String main = jsonObject.getString("main");
                                String sys = jsonObject.getString("sys");

                                Long rise = jsonObject.getJSONObject("sys").getLong("sunrise");
                                Long set = jsonObject.getJSONObject("sys").getLong("sunset");
                                String sunrise = getDate(rise);
                                String sunset = getDate(set);
                                tvSunrise.setText(sunrise);
                                tvSunset.setText(sunset);
                                System.out.println("sys : " + sys);
                                String[] arrStr = sys.split(",");
                                String country = arrStr[3].substring(11, 13);
                                System.out.println(arrStr[3]);

                                JSONObject jsonWind = jsonObject.getJSONObject("wind");
                                String WIND = jsonWind.getString("speed");
                                Double deg = jsonWind.getDouble("deg");
                                System.out.println("WIND === "+WIND);
                                System.out.println("deg === "+toTextualDescription(deg));

                                double windSpeed = Double.parseDouble(WIND) * 3.6d;

                                System.out.println("maiin = " + main);
                                String cityTemp = main.substring(7, 15);
                                String humidity = jsonObject.getJSONObject("main").getString("humidity");
                                System.out.println("humidity === "+humidity);
                                System.out.println("cityyyy temppp ::: " + cityTemp);
                                String temp = "";
                                for (int i = 0; i < cityTemp.length(); i++) {
                                    if (Character.isDigit(cityTemp.charAt(i))) {
                                        temp = new StringBuilder(String.valueOf(temp)).append(cityTemp.charAt(i)).toString();
                                    } else if (cityTemp.charAt(i) == '.') {
                                        temp = new StringBuilder(String.valueOf(temp)).append(cityTemp.charAt(i)).toString();
                                    }
                                }
                                System.out.println(temp);
                                JSONArray arr = new JSONArray(weatherInfo);

                                double celsius = Double.parseDouble(temp) - 273.0d;
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject jsonPart = arr.getJSONObject(i);
                                    tvCity.setText("City :  " + cityName + " " + country);
                                    tvGeneral.setText("General :  " + jsonPart.get("main"));
                                    tvDesc.setText("Description :  " +jsonPart.get("description"));
                                    tvTemp.setText("Temp :  " + df.format(celsius) + " °C");
                                    tvWind.setText("Wind Speed :  " + df.format(windSpeed) + " Km/h "+toTextualDescription(deg));
                                    tvHum.setText("Humidity : "+humidity+"%");

                                    String general = jsonPart.get("main").toString();
                                    Integer logo =R.drawable.sun;

                                    if (general.equalsIgnoreCase("clear"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.sun;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon;
                                        }

                                    if (general.equalsIgnoreCase("clouds"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.sun_clouds;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon_clouds;
                                        }

                                    if (general.equalsIgnoreCase("braken clouds"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.sun_clouds;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon_clouds;
                                        }

                                    if (general.equalsIgnoreCase("scratted clouds"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.sun_few_clouds;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon_few_clouds;
                                        }

                                    if (general.equalsIgnoreCase("shower rain") || general.equalsIgnoreCase("rain"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.rain;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon_rain;
                                        }

                                    if (general.equalsIgnoreCase("snow"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.snow;
                                        }
                                        else
                                        {
                                            logo = R.drawable.moon_snow;
                                        }

                                    if (general.equalsIgnoreCase("mist"))
                                        if(currentHour>=06 && currentHour<18)
                                        {
                                            logo = R.drawable.mist_icone;
                                        }
                                        else
                                        {
                                            logo = R.drawable.mist_icone;
                                        }
                                    state.setBackgroundResource(logo);

                                }
                                card_weather.setVisibility(View.VISIBLE);
                                testInter();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_LONG).show();

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new Hashtable<>();

                    return params;
                }
            };

            {
                int socketTimeout = 30000;
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                stringRequest.setRetryPolicy(policy);
                RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                requestQueue.add(stringRequest);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }


    public String  toTextualDescription(Double degree){
        if (degree>337.5) return "N";
        if (degree>292.5) return "NW";
        if(degree>247.5) return "W";
        if(degree>202.5) return "Sw";
        if(degree>157.5) return "S";
        if(degree>122.5) return "SE";
        if(degree>67.5) return "E";
        if(degree>22.5)return "NE";

        return "N";
    }

    private String getDate(long timeStamp){

        try{
            //SimpleDateFormat sdf = new SimpleDateFormat("HH/MM/SS");
            String time = new java.text.SimpleDateFormat("HH:mm").
                    format(new java.util.Date(timeStamp * 1000));
            System.out.println(time);
            return time;
        }
        catch(Exception ex){
            return "xx";
        }
    }

}
