package mg.carlos.meteo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

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
    }
}