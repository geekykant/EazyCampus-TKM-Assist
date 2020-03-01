package com.diyandroid.eazycampus.helper;

import com.diyandroid.eazycampus.model.User;
import com.diyandroid.eazycampus.service.APIResponse;
import com.diyandroid.eazycampus.service.GetDataService;
import com.diyandroid.eazycampus.util.RetrofitClientInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginHelper {

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

                if (response.isSuccessful()) {
                    listener.onLoginSuccessful(apiResponse.getUser());
                } else {
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
        void onLoginSuccessful(User user);

        void onLoginFailed(String message, boolean show_error);
    }
}
