package com.example.firstaidhero;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstaidhero.adapter.ContactAdapter;
import com.example.firstaidhero.db.DatabaseHelper;
import com.example.firstaidhero.model.Contact;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/** Emergency contacts: tap to call, add, edit and delete. */
public class ContactsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private ContactAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contacts);

        applyInsets(findViewById(R.id.contactsRoot), findViewById(R.id.contactsHeader));

        db = DatabaseHelper.getInstance(this);
        emptyView = findViewById(R.id.emptyContacts);

        adapter = new ContactAdapter(new ArrayList<>(), new ContactAdapter.Listener() {
            @Override
            public void onCall(Contact contact) {
                dial(contact);
            }

            @Override
            public void onEdit(Contact contact) {
                showContactDialog(contact);
            }

            @Override
            public void onDelete(Contact contact) {
                confirmDelete(contact);
            }
        });

        RecyclerView recycler = findViewById(R.id.recyclerContacts);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.buttonAddContact).setOnClickListener(v -> showContactDialog(null));

        setupNavigation();
        refresh();
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_contacts);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_contacts) {
                return true;
            } else if (itemId == R.id.nav_emergencies) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_treated) {
                Intent intent = new Intent(this, TreatedCasesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_stats) {
                Intent intent = new Intent(this, StatisticsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_contacts);
        }
        refresh();
    }

    /** Reloads the contacts from the database and shows the empty message if needed. */
    private void refresh() {
        List<Contact> contacts = db.getAllContacts();
        adapter.updateList(contacts);
        emptyView.setVisibility(contacts.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Opens the dialer with the contact's number (no permission needed). */
    private void dial(Contact contact) {
        Uri number = Uri.fromParts("tel", contact.getPhoneNumber(), null);
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, number));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_dialer, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * One dialog for both adding (existing == null) and editing.
     * The Save button is wired after show() so the dialog stays open when validation fails.
     */
    @SuppressLint("InflateParams")
    private void showContactDialog(@Nullable Contact existing) {
        View view = getLayoutInflater().inflate(R.layout.dialog_contact, null);
        EditText editName = view.findViewById(R.id.editContactName);
        EditText editPhone = view.findViewById(R.id.editContactPhone);

        boolean isEdit = existing != null;
        if (isEdit) {
            editName.setText(existing.getName());
            editPhone.setText(existing.getPhoneNumber());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEdit ? R.string.edit_contact : R.string.add_contact)
                .setView(view)
                .setPositiveButton(R.string.btn_save, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String name = editName.getText().toString().trim();
                    // allow spaces and dashes while typing, store digits only (and a leading +)
                    String phone = editPhone.getText().toString().trim().replaceAll("[\\s-]", "");

                    if (name.isEmpty()) {
                        editName.setError(getString(R.string.error_name_required));
                        return;
                    }
                    if (!phone.matches("\\+?[0-9]{3,15}")) {
                        editPhone.setError(getString(R.string.error_phone_invalid));
                        return;
                    }

                    boolean saved;
                    if (isEdit) {
                        existing.setName(name);
                        existing.setPhoneNumber(phone);
                        saved = db.updateContact(existing) > 0;
                    } else {
                        saved = db.addContact(new Contact(name, phone)) != -1;
                    }

                    if (!saved) {
                        Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
                        return;
                    }
                    dialog.dismiss();
                    refresh();
                }));

        dialog.show();
    }

    private void confirmDelete(Contact contact) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.delete_confirm, contact.getName()))
                .setPositiveButton(R.string.btn_delete, (d, which) -> {
                    if (db.deleteContact(contact.getId()) > 0) {
                        refresh();
                    } else {
                        Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    /** Keeps the header below the status bar and the button above the navigation bar. */
    private void applyInsets(View root, View header) {
        final int headerTop = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(header.getPaddingLeft(), headerTop + bars.top,
                    header.getPaddingRight(), header.getPaddingBottom());
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            return insets;
        });
    }
}