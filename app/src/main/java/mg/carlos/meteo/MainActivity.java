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
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m&timezone=auto";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                tvDetails.setText(response);
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