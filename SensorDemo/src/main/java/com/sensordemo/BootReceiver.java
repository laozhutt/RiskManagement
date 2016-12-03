package com.sensordemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sensordemo.LongService;

/**
 * Created by zyqu on 7/20/16.
 */
public class BootReceiver extends BroadcastReceiver {
    private static String debug_Label = "Boot Receiver";

    @Override
    public void onReceive(Context context, Intent intent){
        Log.d(debug_Label, "Boot finished");
        context.startService(new Intent(context, LongService.class));
    }
}