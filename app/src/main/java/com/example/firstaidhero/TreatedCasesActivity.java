package com.example.firstaidhero;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstaidhero.adapter.TreatedCaseAdapter;
import com.example.firstaidhero.db.DatabaseHelper;
import com.example.firstaidhero.model.TreatedCase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Treated Cases screen: list, date sort, live search, and record new case entry form. */
public class TreatedCasesActivity extends AppCompatActivity {

    private static final String TAG = "TreatedCasesActivity";

    private DatabaseHelper db;
    private TreatedCaseAdapter adapter;
    private TextView emptyView;
    private Button buttonSort;
    private EditText searchBar;

    private boolean newestFirst = true;

    // Temporary variables held during dialog photo picking
    private String currentPhotoPath = null;
    private ImageView dialogPhotoPreview;
    private Button dialogRemovePhotoButton;

    private ActivityResultLauncher<String> photoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_treated_cases);

        applyInsets(findViewById(R.id.treatedRoot), findViewById(R.id.treatedHeader));

        db = DatabaseHelper.getInstance(this);

        emptyView = findViewById(R.id.emptyTreatedCases);
        searchBar = findViewById(R.id.searchTreatedBar);
        buttonSort = findViewById(R.id.buttonSortDate);

        setupPhotoPicker();

        adapter = new TreatedCaseAdapter(new ArrayList<>(), this::confirmDelete);

        RecyclerView recycler = findViewById(R.id.recyclerTreatedCases);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.buttonAddTreatedCase).setOnClickListener(v -> showAddDialog());

        buttonSort.setOnClickListener(v -> {
            newestFirst = !newestFirst;
            buttonSort.setText(newestFirst ? R.string.sort_newest : R.string.sort_oldest);
            refresh();
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refresh();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        setupNavigation();
        refresh();
    }

    private void setupPhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String savedPath = copyUriToInternalStorage(uri);
                        if (savedPath != null) {
                            currentPhotoPath = savedPath;
                            updateDialogPhotoPreview();
                        } else {
                            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Nullable
    private String copyUriToInternalStorage(Uri uri) {
        try {
            File photosDir = new File(getFilesDir(), "treated_photos");
            if (!photosDir.exists() && !photosDir.mkdirs()) {
                return null;
            }
            File destFile = new File(photosDir, "photo_" + System.currentTimeMillis() + ".jpg");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(destFile)) {
                if (in == null) return null;
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error copying picked photo", e);
            return null;
        }
    }

    private void updateDialogPhotoPreview() {
        if (dialogPhotoPreview == null) return;

        if (currentPhotoPath != null && !currentPhotoPath.trim().isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            if (bitmap != null) {
                dialogPhotoPreview.setImageBitmap(bitmap);
                dialogPhotoPreview.setVisibility(View.VISIBLE);
                if (dialogRemovePhotoButton != null) {
                    dialogRemovePhotoButton.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
        dialogPhotoPreview.setVisibility(View.GONE);
        if (dialogRemovePhotoButton != null) {
            dialogRemovePhotoButton.setVisibility(View.GONE);
        }
    }

    @SuppressLint("InflateParams")
    private void showAddDialog() {
        currentPhotoPath = null;

        View view = getLayoutInflater().inflate(R.layout.dialog_treated_case, null);
        EditText editDesc = view.findViewById(R.id.editTreatedDesc);
        EditText editDate = view.findViewById(R.id.editTreatedDate);
        EditText editProc = view.findViewById(R.id.editTreatedProcedures);
        Button buttonPickPhoto = view.findViewById(R.id.buttonPickPhoto);
        dialogRemovePhotoButton = view.findViewById(R.id.buttonRemovePhoto);
        dialogPhotoPreview = view.findViewById(R.id.imageTreatedPreview);

        // Pre-fill today's date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        editDate.setText(sdf.format(calendar.getTime()));

        editDate.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (dpView, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        editDate.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        buttonPickPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));

        dialogRemovePhotoButton.setOnClickListener(v -> {
            currentPhotoPath = null;
            updateDialogPhotoPreview();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_treated_case)
                .setView(view)
                .setPositiveButton(R.string.btn_save, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String desc = editDesc.getText().toString().trim();
                    String date = editDate.getText().toString().trim();
                    String proc = editProc.getText().toString().trim();

                    if (desc.isEmpty()) {
                        editDesc.setError(getString(R.string.error_desc_required));
                        return;
                    }
                    if (date.isEmpty()) {
                        editDate.setError(getString(R.string.error_date_required));
                        return;
                    }
                    if (proc.isEmpty()) {
                        editProc.setError(getString(R.string.error_proc_required));
                        return;
                    }

                    TreatedCase tc = new TreatedCase(desc, date, currentPhotoPath, proc);
                    long id = db.addTreatedCase(tc);
                    if (id != -1) {
                        dialog.dismiss();
                        refresh();
                    } else {
                        Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                    }
                }));

        dialog.show();
    }

    private void confirmDelete(TreatedCase tc) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.delete_treated_confirm)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    if (db.deleteTreatedCase(tc.getId()) > 0) {
                        refresh();
                    } else {
                        Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void refresh() {
        String query = searchBar != null ? searchBar.getText().toString() : "";
        List<TreatedCase> list = db.searchTreatedCases(query, newestFirst);
        adapter.updateList(list);
        emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_treated);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_treated) {
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
            bottomNav.setSelectedItemId(R.id.nav_treated);
        }
        refresh();
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