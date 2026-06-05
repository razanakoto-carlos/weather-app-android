package mg.carlos.meteo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private SearchView searchView;
    private TextView tvTemperature, tvDate, tvCity, tvDetails;
    private ImageView ivWeatherIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SearchView searchView = findViewById(R.id.searchView);
        EditText searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        searchText.setTextColor(Color.WHITE);
        searchText.setHintTextColor(Color.LTGRAY);

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setColorFilter(Color.WHITE);

        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeButton.setColorFilter(Color.WHITE);

        tvDate = findViewById(R.id.tvDate);
        tvCity = findViewById(R.id.tvCity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvDetails = findViewById(R.id.tvDetails);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                try {
                    List<Address> adresses = geocoder.getFromLocationName(query, 1);
                    if (adresses != null && !adresses.isEmpty()) {
                        Address resultAddress = adresses.get(0);

                        double lat = resultAddress.getLatitude();
                        double lon = resultAddress.getLongitude();

                        tvCity.setText(resultAddress.getLocality());

                        getMeteo(lat, lon);

                        searchView.setQuery("", false);

                    } else {
                        tvCity.setText("Ville introuvable");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    tvCity.setText("Erreur de connexion");
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void getMeteo(double latitude, double longitude) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=auto";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject responseComplete = new JSONObject(response);
                    JSONObject objectCurrent = responseComplete.getJSONObject("current");
                    double temperature = objectCurrent.getDouble("temperature_2m");
                    String dateServeurBrute = objectCurrent.getString("time");
                    SimpleDateFormat formatServeur = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.FRANCE);
                    tvTemperature.setText(temperature + " °C");
                    int weatherCode = objectCurrent.getInt("weather_code");
                    switch (weatherCode) {
                        case 0:
                            ivWeatherIcon.setImageResource(R.drawable.ic_soleil);
                            break;
                        case 1:
                        case 2:
                        case 3:
                        case 45:
                        case 48:
                            ivWeatherIcon.setImageResource(R.drawable.ic_nuage);
                            break;
                        default:
                            ivWeatherIcon.setImageResource(R.drawable.ic_pluie);
                            break;
                    }

                    double vent = objectCurrent.getDouble("wind_speed_10m");
                    int humidite = objectCurrent.getInt("relative_humidity_2m");


                    JSONObject objectDaily = responseComplete.getJSONObject("daily");

                    double max = objectDaily.getJSONArray("temperature_2m_max").getDouble(0);
                    double min = objectDaily.getJSONArray("temperature_2m_min").getDouble(0);

                    String texteDetails = "max " + Math.round(max) + "°  |  min " + Math.round(min) + "°  |  " + Math.round(vent) + " km/h  |  " + humidite + "%";

                    tvDetails.setText(texteDetails);

                    try {
                        Date dateConvertie = formatServeur.parse(dateServeurBrute);
                        SimpleDateFormat formatAffichage = new SimpleDateFormat("E d MMM", Locale.FRANCE);
                        String dateFinale = formatAffichage.format(dateConvertie);

                        tvDate.setText(dateFinale);
                    } catch (Exception e) {
                        tvDate.setText("Date indisponible");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    tvTemperature.setText("Erreur format");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tvDetails.setText("Erreur de téléchargement météo");
            }
        });
        queue.add(stringRequest);
    }
}