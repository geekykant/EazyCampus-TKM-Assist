package com.diyandroid.eazycampus.service;

import com.diyandroid.eazycampus.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GetDataService {

    //TODO: Can be implemented with Cookies too...

    @POST("/eazycampus/login")
    Call<APIResponse> doLogin(@Body User user);

    @POST("/eazycampus/attendance")
    Call<APIResponse> getAttendance(@Body User user);

    @POST("/eazycampus/assignment")
    Call<APIResponse> getAssignmentMarks(@Body User user);

    @POST("/eazycampus/performance")
    Call<APIResponse> getPerformanceMarks(@Body User user);

    @POST("/eazycampus/normalised_marks")
    Call<APIResponse> getNormalisedMarks(@Body User user);
}
