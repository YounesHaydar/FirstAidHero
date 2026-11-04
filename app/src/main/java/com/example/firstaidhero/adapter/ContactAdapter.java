package com.example.firstaidhero.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstaidhero.R;
import com.example.firstaidhero.model.Contact;

import java.util.ArrayList;
import java.util.List;

/** Shows the emergency contacts with call / edit / delete actions. */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    /** The activity implements this to react to the three actions. */
    public interface Listener {
        void onCall(Contact contact);
        void onEdit(Contact contact);
        void onDelete(Contact contact);
    }

    private final List<Contact> items = new ArrayList<>();
    private final Listener listener;

    public ContactAdapter(List<Contact> initialItems, Listener listener) {
        this.items.addAll(initialItems);
        this.listener = listener;
    }

    /** Replaces the list content and redraws. */
    public void updateList(List<Contact> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = items.get(position);

        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhoneNumber());

        holder.itemView.setOnClickListener(v -> listener.onCall(contact));   // tap row = call
        holder.edit.setOnClickListener(v -> listener.onEdit(contact));
        holder.delete.setOnClickListener(v -> listener.onDelete(contact));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView phone;
        final ImageButton edit;
        final ImageButton delete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textContactName);
            phone = itemView.findViewById(R.id.textContactPhone);
            edit = itemView.findViewById(R.id.buttonEdit);
            delete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}