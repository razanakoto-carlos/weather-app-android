package mg.carlos.meteo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Champ de recherche
    private SearchView searchView;

    // Vues principales affichant la météo actuelle
    private TextView tvTemperature, tvDate, tvCity, tvMin, tvMax, tvWind, tvHumidity;
    private ImageView ivWeatherIcon;

    // Titre de la section prévisions et carte de détails
    private TextView tvForecastTitle;
    private LinearLayout llDetails;

    // RecyclerView pour les prévisions 7 jours
    private RecyclerView rvForecast;
    private ForecastAdapter forecastAdapter;
    private ArrayList<ForecastDay> forecastList = new ArrayList<>();

    // File d'attente Volley réutilisée pour toutes les requêtes
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Une seule instance de RequestQueue pour toute l'activité
        requestQueue = Volley.newRequestQueue(this);

        searchView = findViewById(R.id.searchView);

        // Personnalisation des couleurs de la barre de recherche
        EditText searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchText.setTextColor(Color.WHITE);
        searchText.setHintTextColor(Color.LTGRAY);

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setColorFilter(Color.WHITE);

        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeButton.setColorFilter(Color.WHITE);


        // Liaison des vues avec leurs IDs XML
        tvDate = findViewById(R.id.tvDate);
        tvCity = findViewById(R.id.tvCity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvMax = findViewById(R.id.tvMax);
        tvMin = findViewById(R.id.tvMin);
        tvWind = findViewById(R.id.tvWind);
        tvHumidity = findViewById(R.id.tvHumidity);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        ivWeatherIcon.setColorFilter(Color.WHITE);
        tvForecastTitle = findViewById(R.id.tvForecastTitle);
        llDetails = findViewById(R.id.llDetails);

        // Initialisation du RecyclerView
        rvForecast = findViewById(R.id.rvForecast);
        forecastAdapter = new ForecastAdapter(forecastList);
        rvForecast.setLayoutManager(new LinearLayoutManager(this));
        rvForecast.setAdapter(forecastAdapter);
        rvForecast.setHasFixedSize(true);

        // On cache tout au lancement, rien ne s'affiche avant une recherche
        setContentVisible(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Ferme le clavier et vide la barre après soumission
                searchView.clearFocus();
                searchView.setQuery("", false);

                // Lance la recherche de la ville via l'API Open-Meteo Geocoding
                // Plus fiable que le Geocoder Android qui dépend de Google Play Services
                rechercherVille(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    // Recherche les coordonnées d'une ville via l'API Open-Meteo Geocoding
    // Remplace le Geocoder Android qui est instable sur certains appareils
    private void rechercherVille(String nomVille) {

        // Affiche un message pendant le chargement
        tvCity.setText("Recherche...");
        setContentVisible(true);

        String url = "https://geocoding-api.open-meteo.com/v1/search"
                + "?name=" + nomVille
                + "&count=1"
                + "&language=fr"
                + "&format=json";

        StringRequest geoRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);

                            // L'API renvoie un tableau "results", vide si ville inconnue
                            if (!json.has("results")) {
                                setContentVisible(false);
                                tvCity.setVisibility(View.VISIBLE);
                                tvCity.setText("Ville introuvable");
                                return;
                            }

                            JSONArray results = json.getJSONArray("results");
                            if (results.length() == 0) {
                                setContentVisible(false);
                                tvCity.setVisibility(View.VISIBLE);
                                tvCity.setText("Ville introuvable");
                                return;
                            }

                            JSONObject ville = results.getJSONObject(0);
                            double lat = ville.getDouble("latitude");
                            double lon = ville.getDouble("longitude");
                            String cityName = ville.getString("name");

                            // Affiche le nom officiel retourné par l'API
                            tvCity.setText(cityName);

                            // Lance la requête météo avec les coordonnées trouvées
                            getMeteo(lat, lon);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            tvCity.setText("Erreur de parsing");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setContentVisible(false);
                        tvCity.setVisibility(View.VISIBLE);
                        tvCity.setText("Pas de connexion");
                    }
                });

        requestQueue.add(geoRequest);
    }

    // Récupère la météo actuelle et les prévisions 7 jours
    private void getMeteo(double latitude, double longitude) {
        // forecast_days=8 : index 0 = aujourd'hui, index 1 à 7 = les 7 prochains jours

        String latStr = String.format(Locale.US, "%.6f", latitude);
        String lonStr = String.format(Locale.US, "%.6f", longitude);

        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + latStr
                + "&longitude=" + lonStr
                + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code"
                + "&daily=temperature_2m_max,temperature_2m_min,weather_code"
                + "&forecast_days=8"
                + "&timezone=auto";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseComplete = new JSONObject(response);

                            // --- Météo actuelle ---
                            JSONObject objectCurrent = responseComplete.getJSONObject("current");
                            double temperature = objectCurrent.getDouble("temperature_2m");
                            String dateServeurBrute = objectCurrent.getString("time");
                            int weatherCode = objectCurrent.getInt("weather_code");
                            double vent = objectCurrent.getDouble("wind_speed_10m");
                            int humidite = objectCurrent.getInt("relative_humidity_2m");

                            tvTemperature.setText(temperature + " °C");
                            tvWind.setText(Math.round(vent) + " km/h");
                            tvHumidity.setText(humidite + "%");

                            // Icône principale selon le code météo WMO
                            switch (weatherCode) {
                                case 0:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_day_sunny);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#FFD700")); // jaune soleil
                                    break;
                                case 1:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_day_sunny_overcast);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#FFC04D")); // jaune pâle
                                    break;
                                case 2:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_day_cloudy);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#B0C4DE")); // gris bleuté
                                    break;
                                case 3:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_cloudy);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#A0A8B0")); // gris nuage
                                    break;
                                case 45:
                                case 48:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_fog);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#C8D0D8")); // gris brume
                                    break;
                                case 51:
                                case 53:
                                case 55:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_sprinkle);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#6AB0E0")); // bleu clair bruine
                                    break;
                                case 56:
                                case 57:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_rain_mix);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#88C0E8")); // bleu glacé
                                    break;
                                case 61:
                                case 63:
                                case 65:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_rain);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#4A90D9")); // bleu pluie
                                    break;
                                case 66:
                                case 67:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_rain_mix);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#78A8D0")); // bleu gris verglas
                                    break;
                                case 71:
                                case 73:
                                case 75:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_snow);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#DDEEFF")); // blanc bleuté neige
                                    break;
                                case 77:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_snowflake_cold);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#B0D8FF")); // bleu flocon
                                    break;
                                case 80:
                                case 81:
                                case 82:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_showers);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#3A80CC")); // bleu averse
                                    break;
                                case 85:
                                case 86:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_day_snow);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#C8E8FF")); // bleu neige fondante
                                    break;
                                case 95:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_thunderstorm);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#9B6FD0")); // violet orage
                                    break;
                                case 96:
                                case 99:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_storm_showers);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#7B50B8")); // violet foncé grêle
                                    break;
                                default:
                                    ivWeatherIcon.setImageResource(R.drawable.ic_wi_na);
                                    ivWeatherIcon.setColorFilter(Color.parseColor("#A0A0A0")); // gris inconnu
                                    break;
                            }

                            // --- Données journalières (index 0 = aujourd'hui) ---
                            JSONObject objectDaily = responseComplete.getJSONObject("daily");
                            double max = objectDaily.getJSONArray("temperature_2m_max").getDouble(0);
                            double min = objectDaily.getJSONArray("temperature_2m_min").getDouble(0);

                            tvMax.setText(Math.round(max) + "°");
                            tvMin.setText(Math.round(min) + "°");

                            // Formatage de la date actuelle affichée sous le nom de ville
                            try {
                                SimpleDateFormat formatServeur = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.FRANCE);
                                SimpleDateFormat formatAffichage = new SimpleDateFormat("E d MMM", Locale.FRANCE);
                                Date dateConvertie = formatServeur.parse(dateServeurBrute);
                                tvDate.setText(formatAffichage.format(dateConvertie));
                            } catch (Exception e) {
                                tvDate.setText("Date indisponible");
                            }

                            // --- Prévisions 7 jours ---
                            JSONArray dateArr = objectDaily.getJSONArray("time");
                            JSONArray codeArr = objectDaily.getJSONArray("weather_code");
                            JSONArray maxArr = objectDaily.getJSONArray("temperature_2m_max");
                            JSONArray minArr = objectDaily.getJSONArray("temperature_2m_min");

                            SimpleDateFormat dayInFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
                            SimpleDateFormat dayOutFmt = new SimpleDateFormat("EEE d", Locale.FRANCE);

                            // On vide la liste avant de la remplir pour éviter les doublons
                            // si l'utilisateur recherche une deuxième ville
                            forecastList.clear();

                            for (int i = 0; i < 7; i++) {
                                // idx commence à 1 pour sauter aujourd'hui (index 0)
                                int idx = i + 1;

                                String dateStr = dateArr.getString(idx);
                                int code = codeArr.getInt(idx);
                                double dayMax = maxArr.getDouble(idx);
                                double dayMin = minArr.getDouble(idx);

                                String dayLabel;
                                try {
                                    Date d = dayInFmt.parse(dateStr);
                                    dayLabel = dayOutFmt.format(d).toUpperCase(Locale.FRANCE);
                                } catch (Exception e) {
                                    dayLabel = dateStr;
                                }

                                forecastList.add(new ForecastDay(
                                        dayLabel,
                                        emojiForCode(code),
                                        Math.round(dayMin) + "°",
                                        Math.round(dayMax) + "°"
                                ));
                            }

                            // Notifie l'adapter que les données ont changé pour rafraîchir l'affichage
                            forecastAdapter.notifyDataSetChanged();

                            // Révèle tout le contenu maintenant que les données sont prêtes
                            setContentVisible(true);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            tvCity.setText("Erreur format JSON");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String detail = error.networkResponse != null
                                ? new String(error.networkResponse.data)
                                : error.toString();
                        tvCity.setText("Erreur: " + detail);
                    }
                });

        requestQueue.add(stringRequest);
    }

    // Convertit un code météo WMO en emoji pour les lignes de prévisions
    private String emojiForCode(int code) {
        if (code == 0) return "☀️";
        if (code <= 2) return "🌤️";
        if (code == 3) return "☁️";
        if (code <= 48) return "🌫️";
        if (code <= 57) return "🌦️";
        if (code <= 67) return "🌧️";
        if (code <= 77) return "❄️";
        if (code <= 82) return "🌧️";
        if (code <= 86) return "🌨️";
        if (code <= 99) return "⛈️";
        return "🌡️";
    }

    // Affiche ou cache tout le contenu météo d'un seul coup
    private void setContentVisible(boolean visible) {
        int v = visible ? View.VISIBLE : View.INVISIBLE;
        tvCity.setVisibility(v);
        tvDate.setVisibility(v);
        tvTemperature.setVisibility(v);
        ivWeatherIcon.setVisibility(v);
        llDetails.setVisibility(v);
        tvForecastTitle.setVisibility(v);
        rvForecast.setVisibility(v);
    }
}