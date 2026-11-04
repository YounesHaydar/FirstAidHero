package com.example.firstaidhero.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstaidhero.R;
import com.example.firstaidhero.model.TreatedCase;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** RecyclerView adapter for displaying treated case records. */
public class TreatedCaseAdapter extends RecyclerView.Adapter<TreatedCaseAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(TreatedCase treatedCase);
    }

    private final List<TreatedCase> items = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;

    public TreatedCaseAdapter(List<TreatedCase> initialItems, OnDeleteClickListener deleteListener) {
        this.items.addAll(initialItems);
        this.deleteListener = deleteListener;
    }

    public void updateList(List<TreatedCase> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treated_case, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TreatedCase item = items.get(position);

        holder.textDate.setText(item.getDate());
        holder.textDesc.setText(item.getDescription());

        if (item.getProcedures() != null && !item.getProcedures().trim().isEmpty()) {
            holder.textProcedures.setText(item.getProcedures());
            holder.textProcedures.setVisibility(View.VISIBLE);
        } else {
            holder.textProcedures.setVisibility(View.GONE);
        }

        String photoPath = item.getPhotoPath();
        if (photoPath != null && !photoPath.trim().isEmpty()) {
            loadPhoto(holder.imagePhoto, photoPath);
        } else {
            holder.imagePhoto.setVisibility(View.GONE);
        }

        holder.buttonDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(item);
            }
        });
    }

    private void loadPhoto(ImageView imageView, String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                    return;
                }
            }
            Uri uri = Uri.parse(path);
            InputStream is = imageView.getContext().getContentResolver().openInputStream(uri);
            if (is != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                    return;
                }
            }
            imageView.setVisibility(View.GONE);
        } catch (Exception e) {
            imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textDate;
        final TextView textDesc;
        final TextView textProcedures;
        final ImageView imagePhoto;
        final ImageButton buttonDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textTreatedDate);
            textDesc = itemView.findViewById(R.id.textTreatedDesc);
            textProcedures = itemView.findViewById(R.id.textTreatedProcedures);
            imagePhoto = itemView.findViewById(R.id.imageTreatedPhoto);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteTreated);
        }
    }
}