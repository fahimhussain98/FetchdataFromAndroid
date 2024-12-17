package com.example.spyapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyBootReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MyService.class));
     //   Intent in = new Intent(context, MainActivity.class);
//        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(in);
        Toast.makeText(context, "Broadcast Receiver started", Toast.LENGTH_SHORT).show();
    }
}
