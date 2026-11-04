package com.example.firstaidhero.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.firstaidhero.model.Contact;
import com.example.firstaidhero.model.Emergency;
import com.example.firstaidhero.model.Step;
import com.example.firstaidhero.model.TreatedCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Opens the pre-populated SQLite database shipped in /assets.
 * On first launch the file is copied to the app's private database folder.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DB_NAME = "firstaid.db";
    private static final String ASSET_DB_PATH = "databases/FirstAid_Hero.db";
    private static final int DB_VERSION = 1;

    // ---- tables matching FirstAid_Hero.db ----
    private static final String T_EMERGENCIES = "Emergency";
    private static final String T_STEPS = "steps";
    private static final String T_CONTACTS = "custom_contacts";
    private static final String T_TREATED = "treated_cases";

    // ---- emergencies columns ----
    private static final String C_EM_ID = "case_id";
    private static final String C_EM_TITLE = "title";
    private static final String C_EM_DANGER = "danger_level";
    private static final String C_EM_SYMPTOMS = "symptoms";

    // ---- steps columns ----
    private static final String C_ST_ID = "step_id";
    private static final String C_ST_CASE_ID = "case_id";
    private static final String C_ST_NUMBER = "step_number";
    private static final String C_ST_TEXT = "instruction_text";

    // ---- contacts columns ----
    private static final String C_CT_ID = "contact_id";
    private static final String C_CT_NAME = "contact_name";
    private static final String C_CT_PHONE = "phone_number";

    // ---- treated_cases columns ----
    private static final String C_TC_ID = "case_id";
    private static final String C_TC_DESC = "description";
    private static final String C_TC_DATE = "date";
    private static final String C_TC_PHOTO = "photo";
    private static final String C_TC_PROC = "procedures";

    private static DatabaseHelper instance;
    private final Context context;

    /** Always use this instead of `new`, so the whole app shares one helper. */
    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        copyDatabaseIfNeeded();
    }

    // =====================================================================
    //  Copy database from assets (first launch)
    // =====================================================================

    private void copyDatabaseIfNeeded() {
        File dbFile = context.getDatabasePath(DB_NAME);
        if (dbFile.exists()) {
            ensureDefaultContacts();
            return;
        }

        File dir = dbFile.getParentFile();
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Could not create the database folder");
        }

        try (InputStream in = context.getAssets().open(ASSET_DB_PATH);
             OutputStream out = new FileOutputStream(dbFile)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error copying database from assets", e);
            dbFile.delete(); // do not keep a half-copied file
            throw new RuntimeException("Could not copy database from assets", e);
        }

        ensureDefaultContacts();
    }

    private void ensureDefaultContacts() {
        try {
            if (getAllContacts().isEmpty()) {
                addContact(new Contact("الإسعاف / Emergency", "110"));
                addContact(new Contact("الهلال الأحمر / Red Crescent", "123"));
                addContact(new Contact("الشرطة / Police", "112"));
                addContact(new Contact("الإطفاء / Fire Department", "113"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to ensure default contacts", e);
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    // =====================================================================
    //  Emergencies
    // =====================================================================

    public List<Emergency> getAllEmergencies() {
        return searchEmergencies("");
    }

    /** Matches the query against title and symptoms. Empty query returns everything. */
    public List<Emergency> searchEmergencies(String query) {
        List<Emergency> list = new ArrayList<>();
        String q = query == null ? "" : query.trim();

        String sql = "SELECT * FROM " + T_EMERGENCIES;
        String[] args = null;
        if (!q.isEmpty()) {
            sql += " WHERE " + C_EM_TITLE + " LIKE ? OR " + C_EM_SYMPTOMS + " LIKE ?";
            args = new String[]{"%" + q + "%", "%" + q + "%"};
        }
        sql += " ORDER BY " + C_EM_TITLE;

        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) {
            while (c.moveToNext()) {
                list.add(emergencyFromCursor(c));
            }
        }
        return list;
    }

    public Emergency getEmergencyById(int caseId) {
        String sql = "SELECT * FROM " + T_EMERGENCIES + " WHERE " + C_EM_ID + " = ?";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(caseId)})) {
            return c.moveToFirst() ? emergencyFromCursor(c) : null;
        }
    }

    private Emergency emergencyFromCursor(Cursor c) {
        return new Emergency(
                c.getInt(c.getColumnIndexOrThrow(C_EM_ID)),
                c.getString(c.getColumnIndexOrThrow(C_EM_TITLE)),
                c.getString(c.getColumnIndexOrThrow(C_EM_DANGER)),
                c.getString(c.getColumnIndexOrThrow(C_EM_SYMPTOMS)));
    }

    /** Number of cases per danger level (for statistics). */
    public Map<String, Integer> getEmergencyCountByDangerLevel() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT " + C_EM_DANGER + ", COUNT(*) AS total FROM " + T_EMERGENCIES
                + " GROUP BY " + C_EM_DANGER;
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            while (c.moveToNext()) {
                stats.put(c.getString(0), c.getInt(1));
            }
        }
        return stats;
    }

    public int getTotalEmergenciesCount() {
        String sql = "SELECT COUNT(*) FROM " + T_EMERGENCIES;
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    public int getTotalTreatedCasesCount() {
        String sql = "SELECT COUNT(*) FROM " + T_TREATED;
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    // =====================================================================
    //  Steps
    // =====================================================================

    public List<Step> getStepsForCase(int caseId) {
        List<Step> list = new ArrayList<>();
        String sql = "SELECT * FROM " + T_STEPS + " WHERE " + C_ST_CASE_ID + " = ?"
                + " ORDER BY " + C_ST_NUMBER;
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(caseId)})) {
            while (c.moveToNext()) {
                list.add(new Step(
                        c.getInt(c.getColumnIndexOrThrow(C_ST_ID)),
                        c.getInt(c.getColumnIndexOrThrow(C_ST_CASE_ID)),
                        c.getInt(c.getColumnIndexOrThrow(C_ST_NUMBER)),
                        c.getString(c.getColumnIndexOrThrow(C_ST_TEXT))));
            }
        }
        return list;
    }

    // =====================================================================
    //  Contacts (full CRUD)
    // =====================================================================

    public List<Contact> getAllContacts() {
        List<Contact> list = new ArrayList<>();
        String sql = "SELECT * FROM " + T_CONTACTS + " ORDER BY " + C_CT_NAME;
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            while (c.moveToNext()) {
                list.add(new Contact(
                        c.getInt(c.getColumnIndexOrThrow(C_CT_ID)),
                        c.getString(c.getColumnIndexOrThrow(C_CT_NAME)),
                        c.getString(c.getColumnIndexOrThrow(C_CT_PHONE))));
            }
        }
        return list;
    }

    /** @return the new row id, or -1 if the insert failed. */
    public long addContact(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(C_CT_NAME, contact.getName());
        values.put(C_CT_PHONE, contact.getPhoneNumber());
        return getWritableDatabase().insert(T_CONTACTS, null, values);
    }

    /** @return number of rows updated (1 on success). */
    public int updateContact(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(C_CT_NAME, contact.getName());
        values.put(C_CT_PHONE, contact.getPhoneNumber());
        return getWritableDatabase().update(T_CONTACTS, values,
                C_CT_ID + " = ?", new String[]{String.valueOf(contact.getId())});
    }

    /** @return number of rows deleted (1 on success). */
    public int deleteContact(int contactId) {
        return getWritableDatabase().delete(T_CONTACTS,
                C_CT_ID + " = ?", new String[]{String.valueOf(contactId)});
    }

    // =====================================================================
    //  Treated cases
    // =====================================================================

    /** @return the new row id, or -1 if the insert failed. */
    public long addTreatedCase(TreatedCase tc) {
        ContentValues values = new ContentValues();
        values.put(C_TC_DESC, tc.getDescription());
        values.put(C_TC_DATE, tc.getDate());
        values.put(C_TC_PHOTO, tc.getPhotoPath());
        values.put(C_TC_PROC, tc.getProcedures());
        return getWritableDatabase().insert(T_TREATED, null, values);
    }

    public List<TreatedCase> getAllTreatedCases(boolean newestFirst) {
        return searchTreatedCases("", newestFirst);
    }

    /** Matches the query against description and procedures, sorted by date. */
    public List<TreatedCase> searchTreatedCases(String query, boolean newestFirst) {
        List<TreatedCase> list = new ArrayList<>();
        String q = query == null ? "" : query.trim();

        String sql = "SELECT * FROM " + T_TREATED;
        String[] args = null;
        if (!q.isEmpty()) {
            sql += " WHERE " + C_TC_DESC + " LIKE ? OR " + C_TC_PROC + " LIKE ?";
            args = new String[]{"%" + q + "%", "%" + q + "%"};
        }
        sql += " ORDER BY " + C_TC_DATE + (newestFirst ? " DESC" : " ASC");

        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) {
            while (c.moveToNext()) {
                list.add(new TreatedCase(
                        c.getInt(c.getColumnIndexOrThrow(C_TC_ID)),
                        c.getString(c.getColumnIndexOrThrow(C_TC_DESC)),
                        c.getString(c.getColumnIndexOrThrow(C_TC_DATE)),
                        c.getString(c.getColumnIndexOrThrow(C_TC_PHOTO)),
                        c.getString(c.getColumnIndexOrThrow(C_TC_PROC))));
            }
        }
        return list;
    }

    /** @return number of rows deleted (1 on success). */
    public int deleteTreatedCase(int id) {
        return getWritableDatabase().delete(T_TREATED,
                C_TC_ID + " = ?", new String[]{String.valueOf(id)});
    }
}