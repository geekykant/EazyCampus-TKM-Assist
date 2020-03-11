package com.diyandroid.eazycampus.helper;

import android.util.Log;

import com.diyandroid.eazycampus.model.SubjectAttendance;
import com.diyandroid.eazycampus.model.User;
import com.diyandroid.eazycampus.service.APIResponse;
import com.diyandroid.eazycampus.service.GetDataService;
import com.diyandroid.eazycampus.util.RetrofitClientInstance;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceHelper {
    private static final String TAG = AttendanceHelper.class.getSimpleName();

    private AttendanceListener listener;
    private GetDataService service;


    public AttendanceHelper(AttendanceListener listener) {
        this.listener = listener;
        this.service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
    }

    public void fetchAttendance(User user) {
        Call<APIResponse> call = service.getAttendance(user);

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                APIResponse apiResponse = response.body();

                Log.d(TAG, "onResponse: ");

                if (response.isSuccessful() && apiResponse.getList() != null) {
                    listener.onAttendanceSuccess(apiResponse.getList());
                } else {
                    listener.onAttendanceFailed(apiResponse.getMessage());
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                t.printStackTrace();
                listener.onAttendanceFailed("Error connecting.");
            }
        });
    }

    public interface AttendanceListener {
        void onAttendanceSuccess(ArrayList<SubjectAttendance> sa);

        void onAttendanceFailed(String message);
    }
}
