package com.abania.dragmanager;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CloudStorageHelper {

    private static final String BASE_URL = "https://api.jsonbin.io/v3/b/";
    private static final String API_KEY = "$2a$10$TZJ064G4vZ.FriA4folxIuhUCyrmFV78UPmIxmEbp82RxT091trk6";
	
    // Bin IDهای هر فایل
    public static final String BIN_DONATIONS = "6a95a38fda38895dfe26e22b";
    public static final String BIN_USERS = "6a95a3d4da38895dfe26e304"; // شناسه سوم
    public static final String BIN_USER_DONATIONS = "6a95a438f5f4af5e29587135";
	public static final String BIN_REQUESTS = "6a99b1e0da38895dfe34ed8f";

    // کلید API (از پنل JSONBin دریافت کنید)
    public static final String BIN_MESSAGES = "شناسه_بین_پیام‌ها_در_JSONBin";
	
    public interface CloudCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    // ===== خواندن فایل از JSONBin =====
    public static void readFile(String binId, CloudCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(BASE_URL + binId + "/latest");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");
                    if (API_KEY != null && !API_KEY.isEmpty() && !API_KEY.equals("YOUR_MASTER_KEY_HERE")) {
                        conn.setRequestProperty("X-Master-key", API_KEY);
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream())
                        );
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        reader.close();
                        return result.toString();
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("خطا در دریافت اطلاعات");
                }
            }
        }.execute();
    }

    // ===== نوشتن در JSONBin (بروزرسانی فایل) =====
    public static void writeFile(String binId, String jsonData, CloudCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    URL url = new URL(BASE_URL + binId);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json");
                    if (API_KEY != null && !API_KEY.isEmpty() && !API_KEY.equals("YOUR_MASTER_KEY_HERE")) {
                        conn.setRequestProperty("X-Master-key", API_KEY);
                    }
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonData.getBytes());
                    os.flush();
                    os.close();

                    return conn.getResponseCode() == 200;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    callback.onSuccess("داده با موفقیت ذخیره شد");
                } else {
                    callback.onError("خطا در ذخیره داده");
                }
            }
        }.execute();
    }
}