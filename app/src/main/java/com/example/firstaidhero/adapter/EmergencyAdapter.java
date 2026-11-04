package com.example.firstaidhero.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstaidhero.R;
import com.example.firstaidhero.model.Emergency;

import java.util.ArrayList;
import java.util.List;

/** Feeds the dashboard RecyclerView with Emergency rows. */
public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.ViewHolder> {

    /** The activity implements this to react when a row is tapped. */
    public interface OnItemClickListener {
        void onItemClick(Emergency emergency);
    }

    private final List<Emergency> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public EmergencyAdapter(List<Emergency> initialItems, OnItemClickListener listener) {
        this.items.addAll(initialItems);
        this.listener = listener;
    }

    /** Replaces the list content (used by live search) and redraws. */
    public void updateList(List<Emergency> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    // Creates the view of ONE row (from item_emergency.xml). Called only a few times,
    // because RecyclerView reuses row views while scrolling.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency, parent, false);
        return new ViewHolder(view);
    }

    // Fills a row with the data of the item at `position`.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Emergency emergency = items.get(position);

        holder.title.setText(emergency.getTitle());
        holder.symptoms.setText(emergency.getSymptoms());
        holder.danger.setText(emergency.getDangerLevel());

        int color = colorFor(holder.itemView.getContext(), emergency.getDangerLevel());
        ViewCompat.setBackgroundTintList(holder.danger, ColorStateList.valueOf(color));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(emergency));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Red = critical, orange = medium, green = minor, gray = anything else. */
    private int colorFor(Context context, String dangerLevel) {
        int colorRes;
        if ("critical".equalsIgnoreCase(dangerLevel)) {
            colorRes = R.color.danger_critical;
        } else if ("medium".equalsIgnoreCase(dangerLevel)) {
            colorRes = R.color.danger_medium;
        } else if ("minor".equalsIgnoreCase(dangerLevel)) {
            colorRes = R.color.danger_minor;
        } else {
            colorRes = R.color.text_gray;
        }
        return ContextCompat.getColor(context, colorRes);
    }

    /** Holds the views of one row so findViewById runs once per row view, not on every scroll. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView symptoms;
        final TextView danger;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            symptoms = itemView.findViewById(R.id.textSymptoms);
            danger = itemView.findViewById(R.id.textDanger);
        }
    }
}