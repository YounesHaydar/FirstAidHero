package com.example.firstaidhero;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.firstaidhero.db.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Map;

/** Statistics screen: displays emergency counts grouped by danger level using GROUP BY. */
public class StatisticsActivity extends AppCompatActivity {

    private DatabaseHelper db;

    private TextView textTotalEmergencies;
    private TextView textTotalTreated;

    private TextView textCountCritical;
    private TextView textCountMedium;
    private TextView textCountMinor;

    private ProgressBar progressCritical;
    private ProgressBar progressMedium;
    private ProgressBar progressMinor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);

        applyInsets(findViewById(R.id.statsRoot), findViewById(R.id.statsHeader));

        db = DatabaseHelper.getInstance(this);

        textTotalEmergencies = findViewById(R.id.textTotalEmergencies);
        textTotalTreated = findViewById(R.id.textTotalTreated);

        textCountCritical = findViewById(R.id.textCountCritical);
        textCountMedium = findViewById(R.id.textCountMedium);
        textCountMinor = findViewById(R.id.textCountMinor);

        progressCritical = findViewById(R.id.progressCritical);
        progressMedium = findViewById(R.id.progressMedium);
        progressMinor = findViewById(R.id.progressMinor);

        setupNavigation();
        loadStatistics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_stats);
        }
        loadStatistics();
    }

    private void loadStatistics() {
        int totalEmergencies = db.getTotalEmergenciesCount();
        int totalTreated = db.getTotalTreatedCasesCount();

        textTotalEmergencies.setText(String.valueOf(totalEmergencies));
        textTotalTreated.setText(String.valueOf(totalTreated));

        Map<String, Integer> dangerCounts = db.getEmergencyCountByDangerLevel();

        int criticalCount = 0;
        int mediumCount = 0;
        int minorCount = 0;

        for (Map.Entry<String, Integer> entry : dangerCounts.entrySet()) {
            String key = entry.getKey() == null ? "" : entry.getKey().toLowerCase().trim();
            int val = entry.getValue();

            if (key.contains("حرِج") || key.contains("حرج") || key.contains("critical")) {
                criticalCount += val;
            } else if (key.contains("متوسط") || key.contains("medium")) {
                mediumCount += val;
            } else if (key.contains("طفيف") || key.contains("minor")) {
                minorCount += val;
            } else {
                // Default fallback if unmapped danger level
                criticalCount += val;
            }
        }

        textCountCritical.setText(String.valueOf(criticalCount));
        textCountMedium.setText(String.valueOf(mediumCount));
        textCountMinor.setText(String.valueOf(minorCount));

        if (totalEmergencies > 0) {
            progressCritical.setProgress((criticalCount * 100) / totalEmergencies);
            progressMedium.setProgress((mediumCount * 100) / totalEmergencies);
            progressMinor.setProgress((minorCount * 100) / totalEmergencies);
        } else {
            progressCritical.setProgress(0);
            progressMedium.setProgress(0);
            progressMinor.setProgress(0);
        }
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_stats);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_stats) {
                return true;
            } else if (itemId == R.id.nav_emergencies) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
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
            }
            return false;
        });
    }

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