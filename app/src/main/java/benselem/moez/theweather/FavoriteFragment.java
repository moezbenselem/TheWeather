package benselem.moez.theweather;


import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
public class FavoriteFragment extends Fragment {

    FloatingActionButton fab;
    Button state;
    public static String APPID ="32e71b2392320fed5d6d5117214377ff";
    TextView tvCity,tvGeneral,tvDesc,tvTemp,tvWind;
    SharedPreferences sharedPreferences;
    CardView cardView ;
    ArrayList<Weather> listWeather;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    static SwipeRefreshLayout mSwipeRefreshLayout;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        try {


            recyclerView =
                    (RecyclerView) getView().findViewById(R.id.recycler_fav);

            layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);
            listWeather = new ArrayList<>();
            adapter = new RecyclerFav(listWeather, getContext());
            recyclerView.setAdapter(adapter);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());


            //System.out.println("from shared === "+ sharedPreferences.getString("city","null"));
            //mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeLayout);
            init();
            /*mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    init();

                }
            });
*/

        }catch (Exception e){
            e.printStackTrace();
        }

        MobileAds.initialize(getContext(), "ca-app-pub-7087198421941611~9925442089");
        AdView mAdView = (AdView) getView().findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
  //                  final int fromPos = viewHolder.getAdapterPosition();
//                    final int toPos = viewHolder.getAdapterPosition();
                    // move item in `fromPos` to `toPos` in adapter.
                return false;// true if moved, false otherwise
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                //adapter.notifyItemRemoved(viewHolder.getLayoutPosition());
                int n = sharedPreferences.getInt("index", 0);
                updateHist(sharedPreferences,viewHolder.getLayoutPosition(),n);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        super.onViewCreated(view, savedInstanceState);
    }


    public void init(){

        int index = sharedPreferences.getInt("index", 0);

        try {

            if(index ==0)
            {
                recyclerView.removeAllViews();
            }
            if (index >0) {

                System.out.println("n from if === "+index);

                listWeather = new ArrayList<>();
                for (int i = 1; i <= index; i++) {
                    System.out.println("n men wost el for === " + i);
                    System.out.println("city from for === " + sharedPreferences.getString("city" + i, "null"));
                    final String city = sharedPreferences.getString("city" + i, "null");
                    getWeather(city);

                }

            }
            else
            {
                System.out.println("Errer Indexx === "+index);
            }
            //mSwipeRefreshLayout.setRefreshing(false);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_five, container, false);



        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.add_png, getContext().getTheme()));
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.add_png));
        }



        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                     Snackbar.make(view, "add new Favorite City !", Snackbar.LENGTH_LONG)
                           .setAction("Action", null).show();

                    CustomDialog cd = new CustomDialog(getActivity());
                    cd.show();



                }
            });
        }

        return view;



    }

    public void updateHist(SharedPreferences sharedPreferences , int pos , int n){

        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (int i =pos;i<=n;i++){

            editor.putString("city" + i,sharedPreferences.getString("city" + (i+1),""));
            editor.putInt("index", n-1);
            editor.apply();
        }

        recyclerView.removeViewAt(pos);
        init();
        adapter.notifyItemRemoved(pos);

    }

    private void getWeather(String city) {
        try {

            Calendar cal = Calendar.getInstance();
            final int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            final String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
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

                                Long rise = jsonObject.getJSONObject("sys").getLong("sunrise");
                                Long set = jsonObject.getJSONObject("sys").getLong("sunset");
                                String sunrise = getDate(rise);
                                String sunset = getDate(set);

                                String[] arrStr = sys.split(",");
                                String country = arrStr[3].substring(11, 13);
                                System.out.println(arrStr[3]);

                                JSONObject jsonWind = jsonObject.getJSONObject("wind");
                                String WIND = jsonWind.getString("speed");
                                Double deg = jsonWind.getDouble("deg");
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
                                String humidity = jsonObject.getJSONObject("main").getString("humidity");
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
                                String general ="", desc="";
                                Integer logo =R.drawable.sun;
                                double celsius = Double.parseDouble(temp) - 273.0d;
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject jsonPart = arr.getJSONObject(i);




                                    general = jsonPart.get("main").toString();
                                            desc = jsonPart.get("description").toString();

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




                                }
                                Weather w = new Weather(cityName, country,general,desc,sunrise,sunset,windSpeed,toTextualDescription(deg)
                                        ,celsius,humidity, logo);
                                listWeather.add(w);
                                adapter = new RecyclerFav(listWeather, getContext());


                                recyclerView.setAdapter(adapter);

                                mSwipeRefreshLayout.setRefreshing(false);


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
