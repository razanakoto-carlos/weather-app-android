package mg.carlos.meteo;

public class ForecastDay {
    public String day;    // "SAM 14"
    public String icon;   // "🌧️"
    public String min;    // "14°"
    public String max;    // "20°"

    public ForecastDay(String day, String icon, String min, String max) {
        this.day  = day;
        this.icon = icon;
        this.min  = min;
        this.max  = max;
    }
}