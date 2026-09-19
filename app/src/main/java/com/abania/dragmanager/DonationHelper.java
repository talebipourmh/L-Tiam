package com.abania.dragmanager;

import android.os.AsyncTask;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class DonationHelper {

    // ⚠️ این لینک را با لینک فایل JSON خود در ابر آروان جایگزین کنید
    private static final String JSON_URL = "https://s3.ir-thr-at1.arvanstorage.com/donations-storage/donations.json";

    public interface DonationCallback {
        void onSuccess(List<Donation> donations);
        void onError(String error);
        void onNoInternet();
    }

    public static void fetchDonations(DonationCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // تست اینترنت
                    if (!isInternetAvailable()) {
                        return "NO_INTERNET";
                    }
                    
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(JSON_URL).build();
                    try (Response response = client.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            return "SERVER_ERROR";
                        }
                        return response.body().string();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return "NETWORK_ERROR";
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result == null) {
                    callback.onError("خطا در دریافت اطلاعات");
                } else if (result.equals("NO_INTERNET") || result.equals("NETWORK_ERROR")) {
                    callback.onNoInternet();
                } else if (result.equals("SERVER_ERROR")) {
                    callback.onError("خطا در ارتباط با سرور");
                } else {
                    try {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<Donation>>(){}.getType();
                        List<Donation> donations = gson.fromJson(result, listType);
                        callback.onSuccess(donations);
                    } catch (Exception e) {
                        callback.onError("خطا در پردازش داده‌ها");
                    }
                }
            }
        }.execute();
    }

    private static boolean isInternetAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://www.google.com").openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();
            return connection.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }
}