package com.diyandroid.eazycampus.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.diyandroid.eazycampus.AsyncTaskResult;
import com.diyandroid.eazycampus.BuildConfig;
import com.diyandroid.eazycampus.ExceptionHandlingAsyncTask;
import com.diyandroid.eazycampus.R;
import com.diyandroid.eazycampus.SubjectAttendance;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;


public class AttendanceGrabber extends ExceptionHandlingAsyncTask<String, Void, Element> {
    private Map<String, String> loginCookies;

    private boolean got_attendance;

    private String fromAttendance, toAttendance;

    private Element table;
    private Connection.Response response;

    public interface AsyncResponse {
        void processFinish(ArrayList<SubjectAttendance> sa);
    }

    private AsyncResponse delegate;

    public AttendanceGrabber(Context context, AsyncResponse delegate) {
        super(context);

        this.delegate = delegate;

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

//        this.jsonCookies = getIntent().getStringExtra("COOKIES");

        fromAttendance = mFirebaseRemoteConfig.getString("FROM_ATTENDANCE_DATE_TAG");
        toAttendance = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
//
//    public ArrayList<SubjectAttendance> getAttendanceList() {
//        Log.i("sada", "onPostExecute2: 9999231" + got_attendance);
//        if (!got_attendance) {
//            return null;
//        }
//
//        return attendanceList;
//    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        got_attendance = false;
        //Progress bar implementations
    }

    @Override
    protected Element doInBackground2(String... strings) {

        String intentCookies = strings[0];
        loginCookies = new Gson().fromJson(intentCookies, new TypeToken<Map<String, String>>() {
        }.getType());

        Log.i("sada", "onPostExecute2: " + intentCookies);
        Log.i("sada", "onPostExecute2: 2739");

        try {
            response = Jsoup.connect("https://tkmce.linways.com/student/attendance/ajax/ajax_subjectwise_attendance.php?action=GET_REPORT&" +
                    "fromDate=" +
                    fromAttendance +
                    "&toDate=" +
                    toAttendance
            )
                    .ignoreContentType(true)
                    .cookies(loginCookies)
                    .referrer("https://tkmce.linways.com/student/student.php?menu=attendance&action=subjectwise")
                    .method(Connection.Method.GET)
                    .userAgent("Mozilla")
                    .timeout(30 * 1000)
                    .execute();

            JsonElement element = new Gson().fromJson(response.body(), JsonElement.class);
            JsonObject jsonObj = element.getAsJsonObject();

            if (jsonObj.get("success").toString().equals("true")) {
                String raw_table = jsonObj.get("data").toString().replaceAll("\\\\n", "").replaceAll("\\\\", "");

                table = Jsoup.parse(raw_table).select("tbody").first();
                got_attendance = true;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            got_attendance = false;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        return table;
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<Element> result) {
        super.onPostExecute(result);
        if (got_attendance && table != null && response.statusCode() == 200) {

            Log.i("sada", "onPostExecute2: hee1231");

            //Add the SubjectAttendance objects to an ArrayList
            ArrayList<SubjectAttendance> attendanceList = new ArrayList<>();

            Elements row = table.getElementsByTag("tr");
            Elements details;

            attendanceList.add(new SubjectAttendance("Subject Name", "Total Classes", "Attended", "% Attendance"));

            for (int i = 0; i < row.size(); i++) {
                details = row.get(i).getElementsByTag("td");
                attendanceList.add(new SubjectAttendance(details.get(1).text(), details.get(3).text(), details.get(2).text(), details.get(4).text()));
            }

            delegate.processFinish(attendanceList);
        } else {
            Log.i("AttendanceGrabber", "Failed retrieving data!");
            Toast.makeText(getContext(), "Failed to get attendance!", Toast.LENGTH_SHORT).show();
            got_attendance = false;
        }
    }

    @Override
    protected void onPostExecute2(Element element) {
    }
}
