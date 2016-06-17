package com.sensordemo.utils;

/**
 * Created by herbertxu on 5/28/16.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.WindowManager;
import android.widget.EditText;

import com.sensordemo.DetectService;

public class CustomizedDialog{
    public static void createSimpleDialog(final Activity activity, String title, String message, boolean finish){
        DialogInterface.OnClickListener listener = null;
        if(finish == true){
            listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    activity.finish();
                }
            };
        }
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Confirm",listener)
                .show();

    }
    public static void createAlarm(final DetectService service, String info){
        if(!info.equals("")){
            info += "\n";
        }
        //final EditText password = new EditText(service);
        final EditText password = new EditText(service);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                service.checkPassword(password.getText().toString());
            }

        };
        AlertDialog alert = new AlertDialog.Builder(service)
                .setTitle("Suspicious User")
                .setMessage(info+"Please verify your identification")
                .setView(password)
                .setPositiveButton("Verify",listener)
                .create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }
}
