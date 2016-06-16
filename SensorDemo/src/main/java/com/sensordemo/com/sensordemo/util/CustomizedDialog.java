package com.sensordemo.com.sensordemo.util;

/**
 * Created by herbertxu on 5/28/16.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.Service;
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
    public static void createAlarm(final DetectService service){
        //final EditText password = new EditText(service);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //if(!service.verifyIdentity(password.getText().toString())){
                 //   createAlarm(service);
                //}
                //else{
                    //TODO:The operation if the password matches
                //}
            }

        };
        AlertDialog alert = new AlertDialog.Builder(service)
                .setTitle("Suspicious User")
                .setMessage("Please verify your identification")
                //.setView(password)
                .setPositiveButton("Verify",listener)
                .create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }
}
