# FirstAid Hero 🚑

FirstAid Hero is a fully offline, native Android application designed to provide quick, reliable first-aid guidance during emergencies. Built as part of the Mobile Programming course (**ITE_BMP601**, Syrian Virtual University, F25).

---

## 📱 Features & Screens

1. **Dashboard (Search & Cases):**
   - Instant live search over emergency titles and symptoms using `TextWatcher`.
   - Color-coded danger level badges (**Critical**, **Medium**, **Minor**).
   - Tapping any case opens its detailed step-by-step emergency guide.

2. **Steps Screen:**
   - Detailed view showing case title, danger badge, symptoms, and numbered instructions in large, readable text.
   - Quick-call button (`ACTION_DIAL`) to instantly reach emergency services.

3. **Emergency Contacts CRUD:**
   - Pre-populated default emergency numbers (*Emergency 110*, *Red Crescent 123*, *Police 112*, *Fire Department 113*).
   - Add, edit, and delete custom emergency contacts with robust input validation.
   - Tap-to-call functionality for each contact.

4. **Treated Cases Journal:**
   - Record and log treated emergency cases with description, date (`DatePickerDialog`), procedures applied, and photo attachments (stored safely in internal app storage).
   - Search records and sort by date (**Newest** / **Oldest**).

5. **Statistics Screen:**
   - Real-time case breakdown per danger level using SQLite `GROUP BY` queries.
   - Summary cards displaying total emergency cases and total logged treated cases with visual progress bars.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Java
- **IDE:** Android Studio
- **Database:** SQLite (Pre-populated database shipped in `assets/databases/FirstAid_Hero.db`, copied on first launch via `SQLiteOpenHelper`)
- **UI Toolkit:** Android Views (`ConstraintLayout`, `LinearLayout`, `RecyclerView`, `BottomNavigationView`, Material Design Components)
- **Design System:** Red & Gray medical theme with high-contrast large fonts for emergency readability

---

## 🗄️ Database Schema (5 Tables)

- `Emergency`: `case_id`, `title`, `danger_level`, `symptoms`
- `steps`: `step_id`, `case_id` (FK), `step_number`, `instruction_text`
- `custom_contacts`: `contact_id`, `contact_name`, `phone_number`
- `treated_cases`: `case_id`, `description`, `date`, `photo`, `procedures`

---

## 🚀 Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/FirstAidHero.git
   ```
2. **Open in Android Studio:**
   - Open Android Studio and select *Open*, then choose the project root folder.
3. **Run the App:**
   - Connect an Android emulator or physical device (with USB debugging enabled).
   - Click the green **Run** button (`Shift + F10`).

---

## 📦 Building the APK

To build the debug APK:
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`
