package com.abania.dragmanager;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;import android.content.res.ColorStateList;

public class MedicationScheduleActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteMedicine;
    private MaterialButton btnSearchMedicine;
    private TextView tvStockStatus;
    private EditText etDosage, etTotalQuantity, etInterval, etRetryMinutes;
    private TextView btnPickTime; // ✅ حالا باکس است، نه دکمه
    private FloatingActionButton btnSaveSchedule; // ✅ حالا فقط آیکون است
    private ListView listViewSchedules;
    private DatabaseHelper dbHelper;
    private List<Medicine> allMedicines;
    private String selectedTime = "";
    private String selectedMedicineName = "";

    // ✅ پنل قابل‌بازوبسته‌شدن مثل صفحه داروخانه
    private View addSchedulePanelScroll;
    private FloatingActionButton btnAddSchedule;
    private ImageView btnCloseScheduleForm;

    // ✅ هدر مشترک + نوار پایین ثابت + FAB خانه
    private ImageView ivBackButton;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_schedule);

        dbHelper = new DatabaseHelper(this);
        allMedicines = CsvImporter.readCsvFromAssets(this, "medicines.csv");

        autoCompleteMedicine = findViewById(R.id.autoCompleteMedicine);
        btnSearchMedicine = findViewById(R.id.btnSearchMedicine);
        tvStockStatus = findViewById(R.id.tvStockStatus);
        etDosage = findViewById(R.id.etDosage);
        etTotalQuantity = findViewById(R.id.etTotalQuantity);
        etInterval = findViewById(R.id.etInterval);
        etRetryMinutes = findViewById(R.id.etRetryMinutes);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
        listViewSchedules = findViewById(R.id.listViewSchedules);

        addSchedulePanelScroll = findViewById(R.id.addSchedulePanelScroll);
        btnAddSchedule = findViewById(R.id.btnAddSchedule);
        btnCloseScheduleForm = findViewById(R.id.btnCloseScheduleForm);

        // ✅ هدر: دکمه‌ی برگشت
        ivBackButton = findViewById(R.id.ivBackButton);
        ivBackButton.setOnClickListener(v -> goHome());

        // ✅ نوار پایین ثابت: این صفحه خودش «یادآوری» است
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_reminder);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reminder) {
                return true; // همین صفحه‌ایم
            } else if (id == R.id.nav_donate) {
                startActivity(new Intent(MedicationScheduleActivity.this, DonationActivity.class));
                return true;
            } else if (id == R.id.nav_request) {
                startActivity(new Intent(MedicationScheduleActivity.this, RequestListActivity.class));
                return true;
            } else if (id == R.id.nav_pharmacy) {
                startActivity(new Intent(MedicationScheduleActivity.this, AddMedicineActivity.class));
                return true;
            }
            return false;
        });

        // ✅ دکمه‌ی شناور خانه
		fabHome = findViewById(R.id.fabHome);
if (fabHome != null) {
    fabHome.setOnClickListener(v -> goHome());
    fabHome.setBackgroundTintList(ColorStateList.valueOf(0xFFF7F3B0)); // سفید
    fabHome.setImageTintList(ColorStateList.valueOf(0xFF9AA0A6));      // خاکستری
}

        // ✅ پنل افزودن برنامه: پیش‌فرض بسته
        addSchedulePanelScroll.setVisibility(View.GONE);

        btnAddSchedule.setOnClickListener(v -> {
            if (addSchedulePanelScroll.getVisibility() == View.GONE) {
                addSchedulePanelScroll.setVisibility(View.VISIBLE);
            } else {
                addSchedulePanelScroll.setVisibility(View.GONE);
                clearFields();
            }
        });

        btnCloseScheduleForm.setOnClickListener(v -> {
            addSchedulePanelScroll.setVisibility(View.GONE);
            clearFields();
        });

        // دکمه‌ی جستجو فقط تمرکز را روی فیلد انتخاب دارو می‌برد (ظاهری، مثل صفحه داروخانه)
        btnSearchMedicine.setOnClickListener(v -> autoCompleteMedicine.requestFocus());

        setupAutoComplete();
        loadSchedules();

        autoCompleteMedicine.setOnItemClickListener((parent, view, position, id) -> {
            selectedMedicineName = parent.getItemAtPosition(position).toString();
            checkStock(selectedMedicineName);
        });

        btnPickTime.setOnClickListener(v -> showTimePickerDialog());
        btnSaveSchedule.setOnClickListener(v -> saveSchedule());

        listViewSchedules.setOnItemClickListener((parent, view, position, id) -> {
            List<MedicationSchedule> schedules = dbHelper.getActiveSchedules();
            if (position >= schedules.size()) return;
            MedicationSchedule selected = schedules.get(position);

            new AlertDialog.Builder(this)
                    .setTitle("مدیریت مصرف")
                    .setMessage("دارو: " + selected.getMedicineName() + "\n" +
                            "دوز: " + selected.getDosage() + "\n" +
                            "هر " + selected.getIntervalHours() + " ساعت\n" +
                            "شروع: " + selected.getStartTime())
                    .setPositiveButton("حذف", (dialog, which) -> {
                        dbHelper.deactivateSchedule(selected.getId());
                        loadSchedules();
                        Toast.makeText(this, "✅ برنامه مصرف حذف شد", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("بستن", null)
                    .show();
        });
    }
	
	private void goHome() {
    Intent homeIntent = new Intent(MedicationScheduleActivity.this, MainActivity.class);
    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(homeIntent);
    finish();
}

@Override
public void onBackPressed() {
    goHome();
}

    private void setupAutoComplete() {
        Set<String> uniqueNames = new HashSet<>();
        for (Medicine m : allMedicines) {
            uniqueNames.add(m.getName());
            if (m.getEnglishName() != null && !m.getEnglishName().isEmpty()) {
                uniqueNames.add(m.getEnglishName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(uniqueNames));
        autoCompleteMedicine.setAdapter(adapter);
        autoCompleteMedicine.setThreshold(1);
    }

    private void checkStock(String medicineName) {
        List<Medicine> medicines = dbHelper.getAllMedicines();
        boolean found = false;
        int totalStock = 0;

        for (Medicine m : medicines) {
            if (m.getName().equals(medicineName)) {
                found = true;
                totalStock += m.getQuantity();
            }
        }

        if (found) {
            tvStockStatus.setVisibility(View.VISIBLE);
            tvStockStatus.setText("✅ موجودی داروخانه: " + totalStock + " عدد");
            tvStockStatus.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            tvStockStatus.setVisibility(View.VISIBLE);
            tvStockStatus.setText("⚠️ این دارو در داروخانه موجود نیست");
            tvStockStatus.setTextColor(getColor(android.R.color.holo_red_dark));
        }
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    btnPickTime.setText("🕐 ساعت شروع: " + selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void loadSchedules() {
        List<MedicationSchedule> schedules = dbHelper.getActiveSchedules();
        List<String> displayList = new ArrayList<>();

        for (MedicationSchedule s : schedules) {
            String item = s.getMedicineName() + " (" + s.getDosage() + ")\n" +
                    "هر " + s.getIntervalHours() + " ساعت - شروع: " + s.getStartTime() +
                    " | باقیمانده: " + s.getRemainingQuantity();
            displayList.add(item);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listViewSchedules.setAdapter(adapter);
    }

    private void saveSchedule() {
        String medicineName = autoCompleteMedicine.getText().toString().trim();
        if (medicineName.isEmpty()) {
            Toast.makeText(this, "❌ لطفاً نام دارو را انتخاب کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        String dosage = etDosage.getText().toString().trim();
        if (dosage.isEmpty()) {
            Toast.makeText(this, "❌ لطفاً دوز مصرفی را وارد کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        String quantityStr = etTotalQuantity.getText().toString().trim();
        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "❌ لطفاً تعداد کل را وارد کنید", Toast.LENGTH_SHORT).show();
            return;
        }
        int totalQuantity = Integer.parseInt(quantityStr);

        String intervalStr = etInterval.getText().toString().trim();
        if (intervalStr.isEmpty()) {
            Toast.makeText(this, "❌ لطفاً فاصله زمانی را وارد کنید", Toast.LENGTH_SHORT).show();
            return;
        }
        int intervalHours = Integer.parseInt(intervalStr);

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "❌ لطفاً ساعت شروع را انتخاب کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        String retryStr = etRetryMinutes.getText().toString().trim();
        int retryMinutes = retryStr.isEmpty() ? 0 : Integer.parseInt(retryStr);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 4);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        String endDate = sdf.format(calendar.getTime());

        MedicationSchedule schedule = new MedicationSchedule(
                medicineName,
                dosage,
                totalQuantity,
                intervalHours,
                selectedTime,
                "hourly"
        );
        schedule.setEndDate(endDate);
        schedule.setRetryMinutes(retryMinutes);

        long id = dbHelper.addSchedule(schedule);
        if (id != -1) {
            schedule.setId((int) id);
            scheduleAlarm(schedule);
            Toast.makeText(this, "✅ برنامه مصرف با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show();

            // ✅ به‌جای بستن کل صفحه، فقط پنل بسته و لیست رفرش می‌شود
            clearFields();
            addSchedulePanelScroll.setVisibility(View.GONE);
            loadSchedules();
        } else {
            Toast.makeText(this, "❌ خطا در ذخیره برنامه", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        autoCompleteMedicine.setText("");
        etDosage.setText("");
        etTotalQuantity.setText("");
        etInterval.setText("");
        etRetryMinutes.setText("");
        btnPickTime.setText("🕐 انتخاب ساعت شروع");
        selectedTime = "";
        selectedMedicineName = "";
        tvStockStatus.setVisibility(View.GONE);
    }

    private void scheduleAlarm(MedicationSchedule schedule) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medicine_name", schedule.getMedicineName());
        intent.putExtra("dosage", schedule.getDosage());
        intent.putExtra("schedule_id", schedule.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                schedule.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        String[] timeParts = schedule.getStartTime().split(":");
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        Toast.makeText(this, "⏰ آلارم برای " + schedule.getMedicineName() + " در ساعت " + schedule.getStartTime() + " تنظیم شد", Toast.LENGTH_LONG).show();
    }
}