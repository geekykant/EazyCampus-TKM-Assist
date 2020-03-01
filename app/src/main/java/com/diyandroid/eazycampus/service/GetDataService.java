package com.diyandroid.eazycampus.service;

import com.diyandroid.eazycampus.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;

public interface GetDataService {

    //TODO: Can be implemented with Cookies too...

    @GET("/eazycampus/login")
    Call<APIResponse> doLogin(@Body User user);

    @GET("/eazycampus/attendance")
    Call<APIResponse> getAttendance(@Body User user);

    @GET("/eazycampus/assignment")
    Call<APIResponse> getAssignmentMarks(@Body User user);

    @GET("/eazycampus/performance")
    Call<APIResponse> getPerformanceMarks(@Body User user);

    @GET("/eazycampus/normalised_marks")
    Call<APIResponse> getNormalisedMarks(@Body User user);
}
