package com.abania.dragmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MedicineDB.db";
    private static final int DATABASE_VERSION = 5; // ✅ افزایش برای ستون request_type

    // جدول داروها
    private static final String TABLE_MEDICINES = "medicines";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_DOSAGE = "dosage";
    private static final String COL_EXPIRY = "expiry";
    private static final String COL_TYPE = "type";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_DISEASE = "disease";
    private static final String COL_ROUTINE = "routine";
    private static final String COL_INSTRUCTIONS = "instructions";

    // جدول برنامه مصرف دارو
    private static final String TABLE_SCHEDULE = "medication_schedule";
    private static final String COL_SCHEDULE_ID = "id";
    private static final String COL_SCHEDULE_MEDICINE = "medicine_name";
    private static final String COL_SCHEDULE_DOSAGE = "dosage";
    private static final String COL_SCHEDULE_TOTAL = "total_quantity";
    private static final String COL_SCHEDULE_REMAINING = "remaining_quantity";
    private static final String COL_SCHEDULE_INTERVAL = "interval_hours";
    private static final String COL_SCHEDULE_START_TIME = "start_time";
    private static final String COL_SCHEDULE_END_DATE = "end_date";
    private static final String COL_SCHEDULE_ACTIVE = "is_active";
    private static final String COL_SCHEDULE_TYPE = "schedule_type";
    private static final String COL_SCHEDULE_RETRY_MINUTES = "retry_minutes";

    // جدول درخواست‌های اهدا (اهدا + درخواست هر دو اینجا ذخیره می‌شوند)
    private static final String TABLE_DONATION_REQUESTS = "donation_requests";
    private static final String COL_DONATION_ID = "id";
    private static final String COL_DONATION_MEDICINE_NAME = "medicine_name";
    private static final String COL_DONATION_MEDICINE_ID = "medicine_id";
    private static final String COL_DONATION_QUANTITY = "quantity";
    private static final String COL_DONATION_DONOR_NAME = "donor_name";
    private static final String COL_DONATION_DONOR_PHONE = "donor_phone";
    private static final String COL_DONATION_STATUS = "status";
    private static final String COL_DONATION_TIMESTAMP = "timestamp";
    private static final String COL_DONATION_MESSAGE = "message";
    private static final String COL_DONATION_REQUEST_TYPE = "request_type"; // ✅ ستون جدید: "DONATION" یا "REQUEST"

    // ✅ جدول لیست اهدایی
    private static final String TABLE_DONATION_LIST = "donation_list";
    private static final String COL_DONATION_LIST_ID = "id";
    private static final String COL_DONATION_LIST_MEDICINE_NAME = "medicine_name";
    private static final String COL_DONATION_LIST_MEDICINE_DOSAGE = "medicine_dosage";
    private static final String COL_DONATION_LIST_MEDICINE_TYPE = "medicine_type";
    private static final String COL_DONATION_LIST_QUANTITY = "quantity";
    private static final String COL_DONATION_LIST_DONOR_NAME = "donor_name";
    private static final String COL_DONATION_LIST_DONOR_PHONE = "donor_phone";
    private static final String COL_DONATION_LIST_DATE = "donation_date";
    private static final String COL_DONATION_LIST_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // جدول داروها
        String createTableMedicine = "CREATE TABLE " + TABLE_MEDICINES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_DOSAGE + " TEXT, " +
                COL_EXPIRY + " TEXT, " +
                COL_TYPE + " TEXT, " +
                COL_QUANTITY + " INTEGER, " +
                COL_DISEASE + " TEXT, " +
                COL_ROUTINE + " TEXT, " +
                COL_INSTRUCTIONS + " TEXT)";
        db.execSQL(createTableMedicine);

        // جدول برنامه مصرف دارو
        String createTableSchedule = "CREATE TABLE " + TABLE_SCHEDULE + " (" +
                COL_SCHEDULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SCHEDULE_MEDICINE + " TEXT, " +
                COL_SCHEDULE_DOSAGE + " TEXT, " +
                COL_SCHEDULE_TOTAL + " INTEGER, " +
                COL_SCHEDULE_REMAINING + " INTEGER, " +
                COL_SCHEDULE_INTERVAL + " INTEGER, " +
                COL_SCHEDULE_START_TIME + " TEXT, " +
                COL_SCHEDULE_END_DATE + " TEXT, " +
                COL_SCHEDULE_ACTIVE + " INTEGER, " +
                COL_SCHEDULE_TYPE + " TEXT, " +
                COL_SCHEDULE_RETRY_MINUTES + " INTEGER DEFAULT 0)";
        db.execSQL(createTableSchedule);

        // جدول درخواست‌های اهدا (+ ستون جدید request_type)
        String createTableDonationRequests = "CREATE TABLE " + TABLE_DONATION_REQUESTS + " (" +
                COL_DONATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DONATION_MEDICINE_NAME + " TEXT, " +
                COL_DONATION_MEDICINE_ID + " INTEGER, " +
                COL_DONATION_QUANTITY + " INTEGER, " +
                COL_DONATION_DONOR_NAME + " TEXT, " +
                COL_DONATION_DONOR_PHONE + " TEXT, " +
                COL_DONATION_STATUS + " TEXT, " +
                COL_DONATION_TIMESTAMP + " LONG, " +
                COL_DONATION_MESSAGE + " TEXT, " +
                COL_DONATION_REQUEST_TYPE + " TEXT DEFAULT 'DONATION')";
        db.execSQL(createTableDonationRequests);

        // ✅ جدول لیست اهدایی
        String createTableDonationList = "CREATE TABLE " + TABLE_DONATION_LIST + " (" +
                COL_DONATION_LIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DONATION_LIST_MEDICINE_NAME + " TEXT, " +
                COL_DONATION_LIST_MEDICINE_DOSAGE + " TEXT, " +
                COL_DONATION_LIST_MEDICINE_TYPE + " TEXT, " +
                COL_DONATION_LIST_QUANTITY + " INTEGER, " +
                COL_DONATION_LIST_DONOR_NAME + " TEXT, " +
                COL_DONATION_LIST_DONOR_PHONE + " TEXT, " +
                COL_DONATION_LIST_DATE + " TEXT, " +
                COL_DONATION_LIST_STATUS + " TEXT)";
        db.execSQL(createTableDonationList);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ✅ ارتقا نسخه‌محور: به‌جای پاک‌کردن کل داده‌ها، فقط ستون جدید اضافه می‌شود
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_DONATION_REQUESTS +
                        " ADD COLUMN " + COL_DONATION_REQUEST_TYPE + " TEXT DEFAULT 'DONATION'");
            } catch (Exception e) {
                // اگر ستون از قبل وجود داشت یا جدول موجود نبود، نادیده گرفته می‌شود
            }
        }
    }

    // =============== متدهای جدول داروها ===============
    
    public boolean addMedicine(Medicine medicine) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, medicine.getName());
        cv.put(COL_DOSAGE, medicine.getDosage());
        cv.put(COL_EXPIRY, medicine.getExpiryDate());
        cv.put(COL_TYPE, medicine.getType());
        cv.put(COL_QUANTITY, medicine.getQuantity());
        cv.put(COL_DISEASE, medicine.getDisease());
        cv.put(COL_ROUTINE, medicine.getRoutine());
        cv.put(COL_INSTRUCTIONS, medicine.getInstructions());
        long result = db.insert(TABLE_MEDICINES, null, cv);
        db.close();
        return result != -1;
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEDICINES, null, null, null, null, null, COL_NAME + " ASC");
        if (cursor.moveToFirst()) {
            do {
                list.add(new Medicine(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DOSAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPIRY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DISEASE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ROUTINE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_INSTRUCTIONS))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<Medicine> searchMedicines(String keyword) {
        List<Medicine> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_MEDICINES +
                " WHERE " + COL_NAME + " LIKE ? OR " +
                COL_TYPE + " LIKE ? OR " +
                COL_DISEASE + " LIKE ?";
        String[] args = {"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"};
        Cursor cursor = db.rawQuery(query, args);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Medicine(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DOSAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPIRY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DISEASE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ROUTINE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_INSTRUCTIONS))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean deleteMedicine(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_MEDICINES, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public boolean updateMedicine(Medicine medicine) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, medicine.getName());
        cv.put(COL_DOSAGE, medicine.getDosage());
        cv.put(COL_EXPIRY, medicine.getExpiryDate());
        cv.put(COL_TYPE, medicine.getType());
        cv.put(COL_QUANTITY, medicine.getQuantity());
        cv.put(COL_DISEASE, medicine.getDisease());
        cv.put(COL_ROUTINE, medicine.getRoutine());
        cv.put(COL_INSTRUCTIONS, medicine.getInstructions());
        int result = db.update(TABLE_MEDICINES, cv, COL_ID + "=?", new String[]{String.valueOf(medicine.getId())});
        db.close();
        return result > 0;
    }

    // =============== متدهای جدول برنامه مصرف دارو ===============
    
    public long addSchedule(MedicationSchedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SCHEDULE_MEDICINE, schedule.getMedicineName());
        cv.put(COL_SCHEDULE_DOSAGE, schedule.getDosage());
        cv.put(COL_SCHEDULE_TOTAL, schedule.getTotalQuantity());
        cv.put(COL_SCHEDULE_REMAINING, schedule.getRemainingQuantity());
        cv.put(COL_SCHEDULE_INTERVAL, schedule.getIntervalHours());
        cv.put(COL_SCHEDULE_START_TIME, schedule.getStartTime());
        cv.put(COL_SCHEDULE_END_DATE, schedule.getEndDate());
        cv.put(COL_SCHEDULE_ACTIVE, schedule.isActive() ? 1 : 0);
        cv.put(COL_SCHEDULE_TYPE, schedule.getScheduleType());
        cv.put(COL_SCHEDULE_RETRY_MINUTES, schedule.getRetryMinutes());
        long result = db.insert(TABLE_SCHEDULE, null, cv);
        db.close();
        return result;
    }

    public List<MedicationSchedule> getActiveSchedules() {
        List<MedicationSchedule> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SCHEDULE, null, COL_SCHEDULE_ACTIVE + "=1", null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                MedicationSchedule schedule = new MedicationSchedule(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCHEDULE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SCHEDULE_MEDICINE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SCHEDULE_DOSAGE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCHEDULE_TOTAL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCHEDULE_REMAINING)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCHEDULE_INTERVAL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SCHEDULE_START_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SCHEDULE_END_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCHEDULE_ACTIVE)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SCHEDULE_TYPE))
                );
                schedule.setRetryMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCHEDULE_RETRY_MINUTES)));
                list.add(schedule);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean updateRemainingQuantity(int scheduleId, int newRemaining) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SCHEDULE_REMAINING, newRemaining);
        int result = db.update(TABLE_SCHEDULE, cv, COL_SCHEDULE_ID + "=?", new String[]{String.valueOf(scheduleId)});
        db.close();
        return result > 0;
    }

    public boolean deactivateSchedule(int scheduleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SCHEDULE_ACTIVE, 0);
        int result = db.update(TABLE_SCHEDULE, cv, COL_SCHEDULE_ID + "=?", new String[]{String.valueOf(scheduleId)});
        db.close();
        return result > 0;
    }

    // =============== متدهای جدول درخواست‌های اهدا/درخواست ===============

    public boolean saveDonationRequest(DonationRequest request) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COL_DONATION_MEDICINE_NAME, request.getMedicineName());
        values.put(COL_DONATION_MEDICINE_ID, request.getMedicineId());
        values.put(COL_DONATION_QUANTITY, request.getQuantity());
        values.put(COL_DONATION_DONOR_NAME, request.getDonorName());
        values.put(COL_DONATION_DONOR_PHONE, request.getDonorPhone());
        values.put(COL_DONATION_STATUS, request.getStatus());
        values.put(COL_DONATION_TIMESTAMP, request.getTimestamp());
        values.put(COL_DONATION_MESSAGE, request.getMessage());
        // ✅ ستون جدید: اگر ست نشده بود، پیش‌فرض DONATION
        values.put(COL_DONATION_REQUEST_TYPE,
                request.getRequestType() != null ? request.getRequestType() : "DONATION");
        
        long result = db.insert(TABLE_DONATION_REQUESTS, null, values);
        db.close();
        return result != -1;
    }

    public List<DonationRequest> getDonationRequests() {
        List<DonationRequest> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_DONATION_REQUESTS, 
                null, null, null, null, null, COL_DONATION_TIMESTAMP + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                requests.add(mapCursorToDonationRequest(cursor));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return requests;
    }

    public List<DonationRequest> getDonationRequestsByStatus(String status) {
        List<DonationRequest> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_DONATION_REQUESTS, 
                null, COL_DONATION_STATUS + " = ?", new String[]{status}, 
                null, null, COL_DONATION_TIMESTAMP + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                requests.add(mapCursorToDonationRequest(cursor));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return requests;
    }

    public boolean updateDonationRequestStatus(int requestId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DONATION_STATUS, newStatus);
        
        int result = db.update(TABLE_DONATION_REQUESTS, values, COL_DONATION_ID + " = ?", 
                new String[]{String.valueOf(requestId)});
        db.close();
        return result > 0;
    }

    public boolean deleteDonationRequest(int requestId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_DONATION_REQUESTS, COL_DONATION_ID + " = ?", 
                new String[]{String.valueOf(requestId)});
        db.close();
        return result > 0;
    }

    public int getDonationRequestsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DONATION_REQUESTS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public List<DonationRequest> getDonationRequestsByMedicine(int medicineId) {
        List<DonationRequest> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_DONATION_REQUESTS, 
                null, COL_DONATION_MEDICINE_ID + " = ?", new String[]{String.valueOf(medicineId)}, 
                null, null, COL_DONATION_TIMESTAMP + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                requests.add(mapCursorToDonationRequest(cursor));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return requests;
    }

    // ✅ متد کمکی مشترک برای ساخت DonationRequest از cursor (کد تکراری قبلی حذف شد)
    private DonationRequest mapCursorToDonationRequest(Cursor cursor) {
        DonationRequest request = new DonationRequest();
        request.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONATION_ID)));
        request.setMedicineName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_MEDICINE_NAME)));
        request.setMedicineId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONATION_MEDICINE_ID)));
        request.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONATION_QUANTITY)));
        request.setDonorName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_DONOR_NAME)));
        request.setDonorPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_DONOR_PHONE)));
        request.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_STATUS)));
        request.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_DONATION_TIMESTAMP)));
        request.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_MESSAGE)));
        int typeIdx = cursor.getColumnIndex(COL_DONATION_REQUEST_TYPE);
        if (typeIdx != -1) {
            request.setRequestType(cursor.getString(typeIdx));
        }
        return request;
    }

    // ✅ متد جدید: تعداد کل رکوردهای یک نوع خاص (DONATION یا REQUEST)
    public int getRequestCountByType(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_DONATION_REQUESTS + " WHERE " + COL_DONATION_REQUEST_TYPE + " = ?",
                new String[]{type});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // ✅ متد جدید: تعداد رکوردهای یک نوع خاص با یک وضعیت خاص
    public int getRequestCountByTypeAndStatus(String type, String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_DONATION_REQUESTS +
                        " WHERE " + COL_DONATION_REQUEST_TYPE + " = ? AND " + COL_DONATION_STATUS + " = ?",
                new String[]{type, status});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // =============== ✅ متدهای جدول لیست اهدایی ===============

    public boolean addToDonationList(Medicine medicine, int quantity, String donorName, String donorPhone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DONATION_LIST_MEDICINE_NAME, medicine.getName());
        values.put(COL_DONATION_LIST_MEDICINE_DOSAGE, medicine.getDosage());
        values.put(COL_DONATION_LIST_MEDICINE_TYPE, medicine.getType());
        values.put(COL_DONATION_LIST_QUANTITY, quantity);
        values.put(COL_DONATION_LIST_DONOR_NAME, donorName);
        values.put(COL_DONATION_LIST_DONOR_PHONE, donorPhone);
        values.put(COL_DONATION_LIST_DATE, new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(new Date()));
        values.put(COL_DONATION_LIST_STATUS, "PENDING");
        
        long result = db.insert(TABLE_DONATION_LIST, null, values);
        db.close();
        return result != -1;
    }

    public List<DonationItem> getDonationList() {
        List<DonationItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DONATION_LIST, null, null, null, null, null, COL_DONATION_LIST_DATE + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                DonationItem item = new DonationItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_ID)));
                item.setMedicineName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_MEDICINE_NAME)));
                item.setMedicineDosage(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_MEDICINE_DOSAGE)));
                item.setMedicineType(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_MEDICINE_TYPE)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_QUANTITY)));
                item.setDonorName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_DONOR_NAME)));
                item.setDonorPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_DONOR_PHONE)));
                item.setDonationDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_DATE)));
                item.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_STATUS)));
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<DonationItem> getDonationListByStatus(String status) {
        List<DonationItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DONATION_LIST, 
                null, COL_DONATION_LIST_STATUS + " = ?", new String[]{status}, 
                null, null, COL_DONATION_LIST_DATE + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                DonationItem item = new DonationItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_ID)));
                item.setMedicineName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_MEDICINE_NAME)));
                item.setMedicineDosage(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_MEDICINE_DOSAGE)));
                item.setMedicineType(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_MEDICINE_TYPE)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_QUANTITY)));
                item.setDonorName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_DONOR_NAME)));
                item.setDonorPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_DONOR_PHONE)));
                item.setDonationDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_DATE)));
                item.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_DONATION_LIST_STATUS)));
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean updateDonationListStatus(int itemId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DONATION_LIST_STATUS, newStatus);
        
        int result = db.update(TABLE_DONATION_LIST, values, COL_DONATION_LIST_ID + " = ?", 
                new String[]{String.valueOf(itemId)});
        db.close();
        return result > 0;
    }

    public boolean deleteFromDonationList(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_DONATION_LIST, COL_DONATION_LIST_ID + " = ?", 
                new String[]{String.valueOf(itemId)});
        db.close();
        return result > 0;
    }
}