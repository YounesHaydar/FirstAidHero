package com.example.firstaidhero.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstaidhero.R;
import com.example.firstaidhero.model.Step;

import java.util.List;

/** Shows the numbered instructions of one case. */
public class StepAdapter extends RecyclerView.Adapter<StepAdapter.ViewHolder> {

    private final List<Step> items;

    public StepAdapter(List<Step> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Step step = items.get(position);
        holder.number.setText(String.valueOf(step.getStepNumber()));
        holder.instruction.setText(step.getInstructionText());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView number;
        final TextView instruction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.textStepNumber);
            instruction = itemView.findViewById(R.id.textInstruction);
        }
    }
}