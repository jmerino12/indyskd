package com.jmb.indyskd.utils;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface Service {
    @Headers("Content-Type: application/json")
    @GET("/invitation")
    Call<String> getInvitation();
  }