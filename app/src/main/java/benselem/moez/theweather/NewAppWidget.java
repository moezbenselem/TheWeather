package benselem.moez.theweather;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    public static String APPID ="32e71b2392320fed5d6d5117214377ff";
    static Context c;
    static RemoteViews views;
    static int appWId;
    static String provider;
    private static boolean serviceRunning = false;
    private static Intent serviceIntent;
    static LocationManager locationManager;
    static Double lat=0.0,lon=0.0;
    Location loc;
    private static final String MyOnClick = "myOnClickTag";

    Intent mServiceIntent;
    private MyService mYourService;

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                System.out.println("service is running");
                return true;
            }else
            {
                System.out.println("service is not running");
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        c = context;
        appWId = appWidgetId;
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);


        mYourService = new MyService();
        mServiceIntent = new Intent(c, mYourService.getClass());

        if (!isMyServiceRunning(mYourService.getClass())) {
            c.startService(mServiceIntent);
            System.out.println("service started from widget");
        }



        //views.setTextViewText(R.id.appwidget_text, widgetText);
        LocationManager locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
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


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context,NewAppWidget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (MyOnClick.equals(intent.getAction())){

            if(serviceRunning) {
                context.stopService(serviceIntent);
                Toast.makeText(context, "serviceStopped from widget", Toast.LENGTH_SHORT).show();
            } else {
                context.startService(serviceIntent);
                Toast.makeText(context, "serviceStarted from widget", Toast.LENGTH_SHORT).show();
            }
            serviceRunning=!serviceRunning;


            System.out.println("just clicked");
            //Intent i = new Intent(context,MainActivity.class);
            try{
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {

                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    System.out.println("longgggg ======  " + lon);
                    System.out.println("laaatttt ======  " + lat);
                }
                //getWeather("chebba");
                getWeatherByLocation(lon,lat);

            }catch (SecurityException e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

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
                                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(c);
                                    AppWidgetManager.getInstance(c).updateAppWidget(
                                            new ComponentName(c, NewAppWidget.class),views);
                                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(c, NewAppWidget.class));
                                    onUpdate(c, appWidgetManager, appWidgetIds);

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
                            Toast.makeText(c, "No internet connection", Toast.LENGTH_LONG).show();

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
                RequestQueue requestQueue = Volley.newRequestQueue(c);
                requestQueue.add(stringRequest);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }

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
                                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(c);
                                    AppWidgetManager.getInstance(c).updateAppWidget(
                                            new ComponentName(c, NewAppWidget.class),views);
                                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(c, NewAppWidget.class));


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
                            Toast.makeText(c, "No internet connection", Toast.LENGTH_LONG).show();

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
                RequestQueue requestQueue = Volley.newRequestQueue(c);
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


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {


            c.stopService(mServiceIntent);
            System.out.println("service stopped from widget");


        super.onDeleted(context, appWidgetIds);
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

