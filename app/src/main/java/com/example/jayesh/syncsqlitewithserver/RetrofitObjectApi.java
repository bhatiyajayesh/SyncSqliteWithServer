package com.example.jayesh.syncsqlitewithserver;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Jayesh on 20-Apr-18.
 */

public interface RetrofitObjectApi {

    @POST("/syncdemo/syncinfo.php")
    @FormUrlEncoded
    Call<AddPostResponse> addPost(@Field("name") String name);

}
