package benselem.moez.theweather;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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

import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Moez on 30/04/2018.
 */

public class CustomDialog  extends Dialog implements
        android.view.View.OnClickListener {

    SharedPreferences sharedPreferences;
    public Activity c;
    public Dialog d;
    public static String APPID ="32e71b2392320fed5d6d5117214377ff";
    public Button yes, no;
    EditText city;

    public CustomDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_layout);
        city = (EditText) findViewById(R.id.et_city_dialog);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:

                getWeather(city.getText().toString());

                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }


    public void getWeather(String city) {
        try {
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
                                /*JSONArray arr = new JSONArray(weatherInfo);

                                double celsius = Double.parseDouble(temp) - 273.0d;
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject jsonPart = arr.getJSONObject(i);


                                    //state.setBackgroundResource(logo);

                                }*/

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                int n = sharedPreferences.getInt("index",0);
                                n+=1;
                                System.out.println(cityName);
                                editor.putString("city"+(n),cityName);
                                editor.putInt("index",n);

                                editor.apply();
                                System.out.println("num from dialog === "+ n);
                                System.out.println("last inserted from dialog === "
                                        + sharedPreferences.getString("city"+(n),"null"));

                                //FavoriteFragment.mSwipeRefreshLayout.setRefreshing(true);

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
