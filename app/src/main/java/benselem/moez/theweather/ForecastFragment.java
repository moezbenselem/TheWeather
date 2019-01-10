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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment{

    public static String APPID = "32e71b2392320fed5d6d5117214377ff";
    String provider;
    LocationManager locationManager;
    Double lat=0.0,lon=0.0;
    Location loc;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;
    TextView tvCity,tvGeneral,tvDesc,tvTemp,tvWind;

    public ForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forecast, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        try {
            recyclerView =
                    (RecyclerView) getView().findViewById(R.id.recycler);

            layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            provider = locationManager.getBestProvider(new Criteria(), false);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);

            }


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

            MobileAds.initialize(getContext(), "ca-app-pub-7087198421941611~9925442089");
            AdView mAdView = (AdView) getView().findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
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
            String url = "http://api.openweathermap.org/data/2.5/forecast?q="+city+"&appid="+APPID;
            Calendar cal = Calendar.getInstance();
            final int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            final String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            final StringRequest stringRequest = new StringRequest(Request.Method.GET,url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("result === \n"+response);
                            ArrayList<Forecast> listForecasts = new ArrayList<>();
                            try {
                                //JSONArray jsonArray = new JSONArray(response);
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonList = jsonObject.getJSONArray("list");
                                DecimalFormat df = new DecimalFormat("0.##");

                                JSONObject jsonCity = jsonObject.getJSONObject("city");
                                System.out.println("json city ======== "+jsonCity);
                                String city = jsonCity.getString("name");
                                String country = jsonCity.getString("country");
                                System.out.println("country = "+country);


                                for(int i =0;i<jsonList.length();i++)
                                {

                                    JSONObject jsonItem = jsonList.getJSONObject(i);
                                    JSONObject jsonMain = jsonList.getJSONObject(1);
                                    //System.out.println("main == "+jsonMain);
                                    JSONObject jsonWind = jsonItem.getJSONObject("wind");
                                    Double windSpeed = jsonWind.getDouble("speed") * 3.6d;
                                    System.out.println("wind = "+windSpeed);
                                    Double temp = jsonItem.getJSONObject("main").getDouble("temp");
                                    double celsius = temp - 273.0d;
                                    //System.out.println(temp);

                                    String weatherInfo = jsonItem.getString("weather");
                                    JSONArray arr = new JSONArray(weatherInfo);

                                    String date = jsonItem.getString("dt_txt");
                                    //JSONObject jsonDate = new JSONObject("date");
                                    System.out.println("date ================== "+date);
                                    int time = Integer.parseInt(date.substring(11,13));
                                    System.out.println("time ========================= "+time);
                                    for (int j = 0; j < arr.length(); j++) {
                                        JSONObject jsonPart = arr.getJSONObject(j);

                                        int logo= R.drawable.sun;;

                                        System.out.println("jsonPart === "+jsonPart);
                                        String general = jsonPart.getString("main");


                                        String description = jsonPart.getString("description");

                                        if (general.equalsIgnoreCase("clear"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.sun;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon;
                                            }

                                        if (general.equalsIgnoreCase("clouds"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.sun_clouds;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon_clouds;
                                            }

                                        if (general.equalsIgnoreCase("braken clouds"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.sun_clouds;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon_clouds;
                                            }

                                        if (general.equalsIgnoreCase("scratted clouds"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.sun_few_clouds;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon_few_clouds;
                                            }

                                        if (general.equalsIgnoreCase("shower rain") || general.equalsIgnoreCase("rain"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.rain;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon_rain;
                                            }

                                        if (general.equalsIgnoreCase("snow"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.snow;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon_snow;
                                            }

                                        if (general.equalsIgnoreCase("mist"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.mist_icone;
                                            }
                                            else
                                            {
                                                logo = R.drawable.mist_icone;
                                            }


                                        System.out.println("General === "+general);
                                        //tvDesc.setText("Description :  " +jsonPart.get("description"));
                                        //tvTemp.setText("Temp :  " + df.format(celsius) + " °C");
                                        //tvWind.setText("Wind Speed :  " + df.format(windSpeed) + " Km/h");

                                        Forecast forecast = new Forecast(city,country,date,description,general,windSpeed,celsius,logo);
                                        listForecasts.add(forecast);


                                    }

                                    adapter = new RecyclerForecast(listForecasts,getContext());
                                    recyclerView.setAdapter(adapter);

                                }


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

            String url = "http://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&APPID="+APPID;
            System.out.println(url);
            Calendar cal = Calendar.getInstance();
            final int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            final String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("result === \n"+response);
                            ArrayList<Forecast> listForecasts = new ArrayList<>();
                            try {
                                //JSONArray jsonArray = new JSONArray(response);
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonList = jsonObject.getJSONArray("list");
                                DecimalFormat df = new DecimalFormat("0.##");

                                JSONObject jsonCity = jsonObject.getJSONObject("city");
                                System.out.println("json city ======== "+jsonCity);
                                String city = jsonCity.getString("name");
                                String country = jsonCity.getString("country");
                                System.out.println("country = "+country);


                                for(int i =0;i<jsonList.length();i++)
                                {

                                    JSONObject jsonItem = jsonList.getJSONObject(i);
                                    JSONObject jsonMain = jsonList.getJSONObject(1);
                                    //System.out.println("main == "+jsonMain);
                                    JSONObject jsonWind = jsonItem.getJSONObject("wind");
                                    Double windSpeed = jsonWind.getDouble("speed") * 3.6d;
                                    System.out.println("wind = "+windSpeed);
                                    Double temp = jsonItem.getJSONObject("main").getDouble("temp");
                                    double celsius = temp - 273.0d;
                                    //System.out.println(temp);

                                    String weatherInfo = jsonItem.getString("weather");
                                    JSONArray arr = new JSONArray(weatherInfo);

                                    String date = jsonItem.getString("dt_txt");
                                    int time = Integer.parseInt(date.substring(11,13));
                                    System.out.println("time ========================= "+time);
                                    for (int j = 0; j < arr.length(); j++) {
                                        JSONObject jsonPart = arr.getJSONObject(j);

                                        int logo= R.drawable.sun;;

                                        System.out.println("jsonPart === "+jsonPart);
                                        String general = jsonPart.getString("main");


                                        String description = jsonPart.getString("description");

                                        if (general.equalsIgnoreCase("clear"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.sun;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon;
                                            }

                                        if (general.equalsIgnoreCase("clouds"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.sun_clouds;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon_clouds;
                                            }

                                        if (general.equalsIgnoreCase("braken clouds"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.sun_clouds;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon_clouds;
                                            }

                                        if (general.equalsIgnoreCase("scratted clouds"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.sun_few_clouds;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon_few_clouds;
                                            }

                                        if (general.equalsIgnoreCase("shower rain") || general.equalsIgnoreCase("rain"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.rain;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon_rain;
                                            }

                                        if (general.equalsIgnoreCase("snow"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.snow;
                                            }
                                            else
                                            {
                                                logo = R.drawable.moon_snow;
                                            }

                                        if (general.equalsIgnoreCase("mist"))
                                            if(time>=06 && time<18)
                                            {
                                                logo = R.drawable.mist_icone;
                                            }
                                            else
                                            {
                                                logo = R.drawable.mist_icone;
                                            }


                                        System.out.println("General === "+general);
                                        //tvDesc.setText("Description :  " +jsonPart.get("description"));
                                        //tvTemp.setText("Temp :  " + df.format(celsius) + " °C");
                                        //tvWind.setText("Wind Speed :  " + df.format(windSpeed) + " Km/h");

                                        Forecast forecast = new Forecast(city,country,date,description,general,windSpeed,celsius,logo);
                                        listForecasts.add(forecast);

                                    }

                                    adapter = new RecyclerForecast(listForecasts,getContext());
                                    recyclerView.setAdapter(adapter);

                                }




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



}



