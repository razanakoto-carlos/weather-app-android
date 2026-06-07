package mg.carlos.meteo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private List<ForecastDay> items;

    public ForecastAdapter(List<ForecastDay> items) {
        this.items = items;
    }

    // Called once per row to create the ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast_row, parent, false);
        return new ViewHolder(view);
    }

    // Called for each row to fill in the data
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForecastDay item = items.get(position);

        holder.tvDay.setText(item.day);
        holder.tvIcon.setText(item.icon);
        holder.tvMin.setText(item.min);
        holder.tvMax.setText(item.max);

        // Hide the divider on the last row so there's no line at the bottom
        if (position == items.size() - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Holds references to the views inside each row
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvIcon, tvMin, tvMax;
        View divider;

        ViewHolder(View itemView) {
            super(itemView);
            tvDay   = itemView.findViewById(R.id.tvDay);
            tvIcon  = itemView.findViewById(R.id.tvIcon);
            tvMin   = itemView.findViewById(R.id.tvMin);
            tvMax   = itemView.findViewById(R.id.tvMax);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}