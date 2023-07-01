package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.os.BuildCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.BuildConfig;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    int PERMISSION_ID = 44;

    FusedLocationProviderClient fusedLocationProviderClient;
    static String latitude = "0.00";
    static String longitude = "0.00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
    }


    @Override
    protected void onResume() {
        super.onResume();

        //Set action bar
        /*ActionBar actionBar = getSupportActionBar();
        //actionBar.setLogo(R.drawable.location);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setCustomView(R.layout.action_layout);*/
        //

        TextView temperatureText = findViewById(R.id.temperatureText);
        temperatureText.setText(R.string.temperature);
        ImageView weatherImage = findViewById(R.id.weatherImage);
        weatherImage.setImageResource(R.drawable.weather);

        //Start of long and lat check
        if (checkPermission()) {
            getLastLocation();

            if(latitude.equals("0.00")) {
                //Applying wait for 1 second using handler as getting location takes some time
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                            jsonRequest();

                    }
                }, 100);
            }
            else if (isLocationEnabled())
                jsonRequest();
                //actionBar.setTitle(total);

        }
        else
            Toast.makeText(this, "Please Provide Location Permission for this Application", Toast.LENGTH_SHORT).show();

        //End of long and lat check

    }



    //Start of Location methods in Activity Main
    //To request permission if it is not provided
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    //To check if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //To check if course location permission is provided
    private boolean checkPermission(){
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){
        Toast.makeText(this, "New location data requested", Toast.LENGTH_SHORT).show();
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5);
        locationRequest.setFastestInterval(0);
        locationRequest.setNumUpdates(1);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

   private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            TextView textView3 = findViewById(R.id.feelsLike);
            Location lastLocation = locationResult.getLastLocation();
            latitude = String.format("%.2f", lastLocation.getLatitude());
            longitude = String.format("%.2f", lastLocation.getLongitude());
            textView3.setText("Longitude: "+ lastLocation.getLongitude()+"");
            Log.v("TAG", "Other");
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_ID){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getLastLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        //Check if location permission is given
        Toast.makeText(this, "Getting last location", Toast.LENGTH_SHORT).show();

        if(checkPermission()){
            //Check if location is enabled
            if (isLocationEnabled()){
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            Toast.makeText(MainActivity.this, "No location data", Toast.LENGTH_SHORT).show();
                            requestNewLocationData();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Getting location data", Toast.LENGTH_SHORT).show();
                            latitude = String.format("%.2f", location.getLatitude());
                            Log.v("TAG", latitude);
                            longitude = String.format("%.2f", location.getLongitude());
                            Log.v("TAG", longitude);
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please Provide location Permission", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "Please Provide location permission for this Application", Toast.LENGTH_SHORT).show();
            requestPermissions();
        }
    }//Get last location ends
    //End of Longitude and latitude check methods


    public void jsonRequest(){

        //Request
        String apiKey = getString(R.string.api_key);
        String url = "https://api.openweathermap.org/data/2.5/onecall?lat=" + latitude + "&lon=" + longitude + "&exclude=hourly,daily&units=metric&appid=" + apiKey;

        //Create request queue
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //Get required values from response
                    JSONObject currentObj = response.getJSONObject("current");
                    String temperature = currentObj.getString("temp");
                    int tempInt = Math.round(Float.parseFloat(temperature));
                    String windSpeed = currentObj.getString("wind_speed");
                    long windSpeedInt = Math.round(Float.parseFloat(windSpeed) * (18.0 / 5.0 ));
                    String pressure = currentObj.getString("pressure");
                    String humidity = currentObj.getString("humidity");
                    String uvi = currentObj.getString("uvi");
                    Float uviInt = Float.parseFloat(uvi);
                    String clouds = currentObj.getString("clouds");
                    String feelsLike = currentObj.getString("feels_like");
                    int feelsLikeInt = Math.round(Float.parseFloat(feelsLike));
                    JSONArray jsonArray = (JSONArray) currentObj.get("weather");
                    String climate = jsonArray.getJSONObject(0).getString("main");
                    String tempDescription = jsonArray.getJSONObject(0).getString("description");
                    String iconId = jsonArray.getJSONObject(0).getString("icon");
                    String uviText;
                    String iconLink = "https://openweathermap.org/img/wn/" + iconId +"@2x.png";

                    //Get textviews
                    TextView climateText = findViewById(R.id.climateText);
                    TextView temperatureText = findViewById(R.id.temperatureText);
                    TextView windText = findViewById(R.id.windText);
                    TextView uviTextbox = findViewById(R.id.uviText);
                    TextView feelsLikeText = findViewById(R.id.feelsLike);
                    TextView humidityText = findViewById(R.id.humidity);
                    TextView pressureText = findViewById(R.id.pressure);
                    TextView tempDescriptionText = findViewById(R.id.tempDescription);
                    ImageView weatherImage = findViewById(R.id.weatherImage);
                    TextView cloudText = findViewById(R.id.cloud);


                    //Set textviews
                    climateText.setText(climate);
                    temperatureText.setText(tempInt + "°");
                    windText.setText("Wind Speed: " + windSpeedInt +  " km/hr");
                    feelsLikeText.setText("Feels like " + feelsLikeInt+ "°");
                    humidityText.setText("Humidity: "+ humidity + "%");
                    pressureText.setText("Pressure: "+ pressure + " mBar");
                    cloudText.setText("Cloud cover: "+ clouds + "%");
                    tempDescriptionText.setText(tempDescription);
                    Picasso.get().load(iconLink).into(weatherImage);

                    if(uviInt<2.5)
                        uviText = "Low";
                    else if(uviInt<5.5) uviText = "Moderate";
                    else if(uviInt<7.5) uviText = "High";
                    else uviText = "Very High";


                    uviTextbox.setText("UV Index: " + uviText);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Volley Error", Toast.LENGTH_LONG).show();
                Log.e("TAG", error.getMessage(), error);
            }
        });

        queue.add(jsonObjectRequest);
        //Weater API request ends

        String locationURL = "https://api.openweathermap.org/geo/1.0/reverse?lat="+ latitude +"&lon="+ longitude +"&limit=5&appid=" + apiKey;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(locationURL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    String locationName = response.getJSONObject(0).getString("name");
                    String state = response.getJSONObject(0).getString("state");
                    TextView location = findViewById(R.id.location);
                    location.setText(locationName + ", " + state);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.toString());
            }
        });

    queue.add(jsonArrayRequest);
    }

}
