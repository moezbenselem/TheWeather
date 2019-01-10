package benselem.moez.theweather;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.Provider;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {

    public int counter=0;
    public static String APPID ="32e71b2392320fed5d6d5117214377ff";
    RemoteViews views;
    Double lat=0.0,lon=0.0;
    Location loc;
        @Override
        public void onCreate() {
            super.onCreate();

                startForeground(1, new Notification());
            System.out.println("service started");
        }



        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            super.onStartCommand(intent, flags, startId);
            startTimer();
            return START_STICKY;
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            System.out.println("service destroyed");
            stoptimertask();

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartservice");
            broadcastIntent.setClass(this, Restarter.class);
            this.sendBroadcast(broadcastIntent);
        }


    private void getWeatherByLocation(Double lon , Double lat) {
        try {


            String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&APPID="+APPID;
            System.out.println(url);
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            System.out.println("NOOOOOWWW === "+currentDateTimeString);

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
                                System.out.println("sys : " + sys);
                                String[] arrStr = sys.split(",");
                                String country = arrStr[3].substring(11, 13);
                                System.out.println(arrStr[3]);

                                JSONObject jsonWind = jsonObject.getJSONObject("wind");
                                String WIND = jsonWind.getString("speed");
                                System.out.println("WIIND ==== "+WIND);
                                String rsltWind = "";
                                for (int i = 0; i < WIND.length(); i++) {
                                    if (Character.isDigit(WIND.charAt(i))) {
                                        rsltWind = new StringBuilder(String.valueOf(rsltWind)).append(WIND.charAt(i)).toString();
                                    } else if (WIND.charAt(i) == '.') {
                                        rsltWind = new StringBuilder(String.valueOf(rsltWind)).append(WIND.charAt(i)).toString();
                                    }
                                }
                                double windSpeed = Double.parseDouble(rsltWind) * 3.6d;

                                System.out.println("maiin = " + main);
                                String cityTemp = main.substring(7, 15);
                                System.out.println("cityyyy temppp ::: " + cityTemp);
                                String temp = "";
                                Long rise = jsonObject.getJSONObject("sys").getLong("sunrise");
                                Long set = jsonObject.getJSONObject("sys").getLong("sunset");
                                String sunrise = getDate(rise);
                                String sunset = getDate(set);
                                Double deg = jsonWind.getDouble("deg");
                                String humidity = jsonObject.getJSONObject("main").getString("humidity");
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
                                    views.setTextViewText(R.id.item_city,"City :  " + cityName + " " + country);

                                    views.setTextViewText(R.id.item_general,"General :  " + jsonPart.get("main"));
                                    views.setTextViewText(R.id.item_desc,"Description :  " +jsonPart.get("description"));
                                    views.setTextViewText(R.id.item_temp,"Temp :  " + df.format(celsius) + " °C");
                                    views.setTextViewText(R.id.item_humidity,"Humidity :  " + humidity + "%");
                                    views.setTextViewText(R.id.item_sunsrise,sunrise);
                                    views.setTextViewText(R.id.item_sunset,sunset);
                                    views.setTextViewText(R.id.item_wind,"Wind Speed :  " + df.format(windSpeed) + " Km/h "+toTextualDescription(deg));
                                    views.setTextViewText(R.id.item_time,mydate);
                                    //System.out.println("just clicked");
                                    //appWidgetManager.updateAppWidget(appWidgetId, views);
                                    // Update widgets

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
                                    //state.setBackgroundResource(logo);
                                    views.setImageViewResource(R.id.btState,logo);
                                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                                    AppWidgetManager.getInstance(getApplicationContext()).updateAppWidget(
                                            new ComponentName(getApplicationContext(), NewAppWidget.class),views);
                                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), NewAppWidget.class));


                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();

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
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(stringRequest);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }

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


    public void getWeather(String city) {
        try {


            Calendar cal = Calendar.getInstance();
            final int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            final String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            System.out.println("hourrrrrssss ==== "+currentHour);
            String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&APPID="+APPID;
            final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("result", response);
                            System.out.println("result === \n"+response);

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                DecimalFormat df = new DecimalFormat("0.##");

                                String weatherInfo = jsonObject.getString("weather");
                                String cityName = jsonObject.getString("name");
                                String main = jsonObject.getString("main");
                                String sys = jsonObject.getString("sys");
                                System.out.println("sys : " + sys);
                                String[] arrStr = sys.split(",");
                                String country = arrStr[3].substring(11, 13);
                                System.out.println(arrStr[3]);

                                JSONObject jsonWind = jsonObject.getJSONObject("wind");
                                String WIND = jsonWind.getString("speed");
                                System.out.println("WIIND ==== "+WIND);
                                String rsltWind = "";
                                for (int i = 0; i < WIND.length(); i++) {
                                    if (Character.isDigit(WIND.charAt(i))) {
                                        rsltWind = new StringBuilder(String.valueOf(rsltWind)).append(WIND.charAt(i)).toString();
                                    } else if (WIND.charAt(i) == '.') {
                                        rsltWind = new StringBuilder(String.valueOf(rsltWind)).append(WIND.charAt(i)).toString();
                                    }
                                }
                                double windSpeed = Double.parseDouble(rsltWind) * 3.6d;

                                System.out.println("maiin = " + main);
                                String cityTemp = main.substring(7, 15);
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
                                    views.setTextViewText(R.id.item_city,"City :  " + cityName + " " + country);

                                    views.setTextViewText(R.id.item_general,"General :  " + jsonPart.get("main"));
                                    views.setTextViewText(R.id.item_desc,"Description :  " +jsonPart.get("description"));
                                    views.setTextViewText(R.id.item_temp,"Temp :  " + df.format(celsius) + " °C");
                                    views.setTextViewText(R.id.item_wind,"Wind Speed :  " + df.format(windSpeed) + " Km/h");
                                    views.setTextViewText(R.id.item_time,mydate);
                                    //System.out.println("just clicked");
                                    //appWidgetManager.updateAppWidget(appWidgetId, views);
                                    // Update widgets

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

                                    if (general.equalsIgnoreCase("shower rain"))
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
                                    //state.setBackgroundResource(logo);
                                    views.setImageViewResource(R.id.btState,logo);
                                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                                    AppWidgetManager.getInstance(getApplicationContext()).updateAppWidget(
                                            new ComponentName(getApplicationContext(), NewAppWidget.class),views);
                                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), NewAppWidget.class));


                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();

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
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(stringRequest);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

        private Timer timer;
        private TimerTask timerTask;
        public void startTimer() {
            timer = new Timer();
            timerTask = new TimerTask() {
                public void run() {
                    Log.i("Count", "=========  "+ (counter++));
                    Context context = MyService.this;
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
                    ComponentName thisWidget = new ComponentName(context, NewAppWidget.class);
                    //remoteViews.setTextViewText(R.id.item_time, String.valueOf(counter));
                    LocationManager locationManager = (LocationManager) MyService.this.getSystemService(Context.LOCATION_SERVICE);
                    String provider = locationManager.getBestProvider(new Criteria(), false);


                    try{
                        loc = locationManager.getLastKnownLocation(provider);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000,
                                1, mLocationListener);
                        if (loc != null) {

                            lat = loc.getLatitude();
                            lon = loc.getLongitude();
                            System.out.println("longgggg widg ======  " + lon);
                            System.out.println("laaatttt widg ======  " + lat);
                        }
                        //getWeather("chebba");
                        System.out.println("update launched");
                        getWeatherByLocation(lon,lat);

                    }catch (SecurityException e){
                        e.printStackTrace();
                    }

                    appWidgetManager.updateAppWidget(thisWidget, views);
                }
            };
            timer.schedule(timerTask, 1000, 60000); //
        }

        public void stoptimertask() {
            if (timer != null) {
                timer.cancel();
                timer = null;
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

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
}
