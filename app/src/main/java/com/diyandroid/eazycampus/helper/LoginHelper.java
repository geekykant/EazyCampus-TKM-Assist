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

public class LoginHelper {
    private static final String TAG = LoginHelper.class.getSimpleName();

    private LoginListener listener;
    private GetDataService service;

    public LoginHelper(LoginListener listener) {
        this.listener = listener;
        this.service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
    }

    public void doLogin(User user) {
        Call<APIResponse> call = service.doLogin(user);

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                APIResponse apiResponse = response.body();
                Log.d(TAG, "onResponse: ");


                if (response.isSuccessful()) {
                    if (apiResponse.getList() == null)
                        listener.onLoginFailed(apiResponse.getMessage(), false);
                    else
                        listener.onLoginSuccessful(apiResponse.getUser(), apiResponse.getList());
                }else{
                    listener.onLoginFailed(apiResponse.getMessage(), true);
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                t.printStackTrace();
                listener.onLoginFailed("Error connecting.", false);
            }
        });
    }

    public interface LoginListener {
        void onLoginSuccessful(User user, ArrayList<SubjectAttendance> attendanceList);

        void onLoginFailed(String message, boolean show_error);
    }
}
