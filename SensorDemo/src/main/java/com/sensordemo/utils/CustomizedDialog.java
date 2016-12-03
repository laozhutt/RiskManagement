package com.sensordemo.utils;

/**
 * Created by herbertxu on 5/28/16.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;

import com.sensordemo.DetectService;

public class CustomizedDialog{
    private static String whoIsUsedResult;
    private static String disableLengthResult;

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
//        String[] str={ "self" , "others" };

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                service.checkPassword(password.getText().toString());
            }

        };
        AlertDialog alert = new AlertDialog.Builder(service)
                .setTitle("Suspicious User")
                .setMessage(info + "Please verify your identification")
                .setView(password)
                .setPositiveButton("Verify", listener)
                .setCancelable(false)
                .create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }

    public static void generateWhoIsUsed(final DetectService service, String info){
        if(!info.equals("")){
            info += "\n";
        }
        //final EditText password = new EditText(service);
//        final EditText password = new EditText(service);
        final String[] str={ "others" , "self" };

        whoIsUsedResult = str[0];

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                service.whoInUsed(whoIsUsedResult);
            }

        };
        AlertDialog alert = new AlertDialog.Builder(service)
                .setTitle("Check who was used the phone")
                .setSingleChoiceItems(str, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        whoIsUsedResult = str[which];

                    }
                })
                .setPositiveButton("Submit", listener)
                .setCancelable(false)
                .create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }

    public static void generateDisableLength(final DetectService service, String info){
        if(!info.equals("")){
            info += "\n";
        }
        //final EditText password = new EditText(service);
//        final EditText password = new EditText(service);
        final String[] str={ "never" , "30min","60min","always" };

        disableLengthResult = str[0];
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                DetectService.rep = 0;
                service.disableLength(disableLengthResult);
            }

        };
        AlertDialog alert = new AlertDialog.Builder(service)
                .setTitle("Disable the verification?")
                .setSingleChoiceItems(str, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        disableLengthResult = str[which];

                    }
                })
                .setPositiveButton("Submit", listener)
                .setCancelable(false)
                .create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }
}
