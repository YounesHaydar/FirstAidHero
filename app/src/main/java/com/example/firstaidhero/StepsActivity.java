package com.example.firstaidhero;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstaidhero.adapter.StepAdapter;
import com.example.firstaidhero.db.DatabaseHelper;
import com.example.firstaidhero.model.Contact;
import com.example.firstaidhero.model.Emergency;
import com.example.firstaidhero.model.Step;

import java.util.List;

/** Shows one case: title, danger level, symptoms and its numbered steps. */
public class StepsActivity extends AppCompatActivity {

    /** Key of the case id passed from the dashboard through the Intent. */
    public static final String EXTRA_CASE_ID = "case_id";

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_steps);

        TextView header = findViewById(R.id.stepsHeader);
        applyInsets(findViewById(R.id.stepsRoot), header);

        db = DatabaseHelper.getInstance(this);

        // 1) Which case was tapped?
        int caseId = getIntent().getIntExtra(EXTRA_CASE_ID, -1);
        Emergency emergency = (caseId == -1) ? null : db.getEmergencyById(caseId);
        if (emergency == null) {
            Toast.makeText(this, R.string.case_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2) Title, danger badge and symptoms
        header.setText(emergency.getTitle());

        TextView danger = findViewById(R.id.textDanger);
        danger.setText(emergency.getDangerLevel());
        ViewCompat.setBackgroundTintList(danger,
                ColorStateList.valueOf(dangerColor(emergency.getDangerLevel())));

        TextView symptoms = findViewById(R.id.textSymptoms);
        symptoms.setText(emergency.getSymptoms());

        // 3) The numbered steps
        List<Step> steps = db.getStepsForCase(caseId);
        RecyclerView recyclerSteps = findViewById(R.id.recyclerSteps);
        recyclerSteps.setLayoutManager(new LinearLayoutManager(this));
        recyclerSteps.setAdapter(new StepAdapter(steps));
        findViewById(R.id.emptySteps).setVisibility(steps.isEmpty() ? View.VISIBLE : View.GONE);

        // 4) Quick-call button
        Button buttonCall = findViewById(R.id.buttonCall);
        buttonCall.setOnClickListener(v -> callEmergencyContact());
    }

    /**
     * Opens the phone dialer with the first saved contact.
     * ACTION_DIAL needs no permission: the user presses the green call button.
     */
    private void callEmergencyContact() {
        List<Contact> contacts = db.getAllContacts();
        if (contacts.isEmpty()) {
            Toast.makeText(this, R.string.no_contact, Toast.LENGTH_LONG).show();
            return;
        }
        Uri number = Uri.fromParts("tel", contacts.get(0).getPhoneNumber(), null);
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, number));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_dialer, Toast.LENGTH_LONG).show();
        }
    }

    /** Same colors as the dashboard: red = critical, orange = medium, green = minor. */
    private int dangerColor(String level) {
        int colorRes;
        if ("critical".equalsIgnoreCase(level)) {
            colorRes = R.color.danger_critical;
        } else if ("medium".equalsIgnoreCase(level)) {
            colorRes = R.color.danger_medium;
        } else if ("minor".equalsIgnoreCase(level)) {
            colorRes = R.color.danger_minor;
        } else {
            colorRes = R.color.text_gray;
        }
        return ContextCompat.getColor(this, colorRes);
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