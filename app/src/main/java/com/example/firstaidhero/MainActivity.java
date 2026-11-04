package com.example.firstaidhero;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstaidhero.adapter.EmergencyAdapter;
import com.example.firstaidhero.db.DatabaseHelper;
import com.example.firstaidhero.model.Emergency;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/** Dashboard: search bar + list of first-aid cases. */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EmergencyAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Keep the header below the status bar and the list above the navigation bar.
        View root = findViewById(R.id.root);
        View header = findViewById(R.id.header);
        final int headerTop = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(header.getPaddingLeft(), headerTop + bars.top,
                    header.getPaddingRight(), header.getPaddingBottom());
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            return insets;
        });

        db = DatabaseHelper.getInstance(this);

        EditText searchBar = findViewById(R.id.searchBar);
        RecyclerView recyclerView = findViewById(R.id.recyclerEmergencies);
        emptyView = findViewById(R.id.emptyView);

        // Tapping a row opens the steps screen and passes the case id.
        adapter = new EmergencyAdapter(db.getAllEmergencies(), emergency -> {
            Intent intent = new Intent(this, StepsActivity.class);
            intent.putExtra(StepsActivity.EXTRA_CASE_ID, emergency.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Live search: runs on every keystroke.
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showResults(db.searchEmergencies(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        setupNavigation();
        showResults(db.getAllEmergencies());
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_emergencies);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_emergencies) {
                return true;
            } else if (itemId == R.id.nav_contacts) {
                Intent intent = new Intent(this, ContactsActivity.class);
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
            bottomNav.setSelectedItemId(R.id.nav_emergencies);
        }
    }

    /** Refreshes the list and shows "No cases found" when it is empty. */
    private void showResults(List<Emergency> results) {
        adapter.updateList(results);
        emptyView.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
    }
}