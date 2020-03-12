package com.diyandroid.eazycampus.helper;

import android.util.Log;

import com.diyandroid.eazycampus.model.AssignmentMarks;
import com.diyandroid.eazycampus.model.User;
import com.diyandroid.eazycampus.service.APIResponse;
import com.diyandroid.eazycampus.service.GetDataService;
import com.diyandroid.eazycampus.util.RetrofitClientInstance;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignmentHelper {
    private static final String TAG = AssignmentHelper.class.getSimpleName();

    private AssignmentListener listener;
    private GetDataService service;

    public AssignmentHelper(AssignmentListener listener) {
        this.listener = listener;
        this.service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
    }

    public void fetchAssignment(User user) {
        Call<APIResponse> call = service.getAssignmentMarks(user);

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                APIResponse apiResponse = response.body();

                Log.d(TAG, "onResponse: ");

                if (response.isSuccessful() && apiResponse.getAssignments() != null) {
                    listener.onAssignmentSuccess(apiResponse.getAssignments());
                } else {
                    listener.onAssignmentFailed(apiResponse.getMessage());
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                t.printStackTrace();
                listener.onAssignmentFailed("Error connecting.");
            }
        });
    }

    public interface AssignmentListener {
        void onAssignmentSuccess(ArrayList<AssignmentMarks> assignments);

        void onAssignmentFailed(String message);
    }
}
