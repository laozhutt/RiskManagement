package com.sensordemo;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Binder;

import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.sensordemo.utils.ConnectionHandler;
import com.sensordemo.utils.CustomizedDialog;


import java.util.Timer;
import java.util.TimerTask;


public class DetectService extends Service {
    ConnectionHandler conn;
    Timer timer;
    Service instance;
    private final DetectBinder serviceBinder = new DetectBinder();

    private Handler messageHandler;

    final static int MESSAGE_DETECT_NEGATIVE=1;

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        conn = new ConnectionHandler(getString(R.string.server_address),
                getString(R.string.server_port),
                tm.getDeviceId());
        messageHandler = new Handler(){
            public void handleMessage(Message m){
                switch(m.what){
                    case MESSAGE_DETECT_NEGATIVE:
                        generateAlarm("");
                    default:
                        break;
                }
            }
        };
    }

    private void startTimer(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    //if (conn.getResult() == false) {
                    Message m = new Message();
                    m.what = MESSAGE_DETECT_NEGATIVE;
                    messageHandler.sendMessage(m);
                    timer.cancel();
                    //}
                }
                catch (Exception e){
                    Log.e("DetectService",e.getMessage());
                    timer.cancel();
                    instance.stopSelf();
                }
            }
        },1000,10000);
    }

    public void pauseDetectService(){
        timer.cancel();
    }

    public void startDetectService(){
        startTimer();
    }

    private void generateAlarm(String info){
        CustomizedDialog.createAlarm(this,info);
    }



    public boolean verifyIdentity(String password){
        String hash = MainActivity.configureObject.getProperty(getString(R.string.password_property_name));
        if(String.valueOf(password.hashCode()).equals(hash))
            return true;
        return false;
    }

    public boolean checkPassword(String s){
        if(MainActivity.configureObject == null){
            Log.d("PasswordActivity","Cannot load configureProperties");
        }
        String hash = MainActivity.configureObject.getProperty(getString(R.string.password_property_name));
        if(String.valueOf(s.hashCode()).equals(hash)){
            return true;
        }
        else {
            generateAlarm("Wrong Password");
            return false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    class DetectBinder extends Binder{
        public DetectService getService(){
            return DetectService.this;
        }
    }
}
