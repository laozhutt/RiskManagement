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
    private MainActivity mainActivity;
    private Timer FeedbackTimer;

    final static int MESSAGE_DETECT_NEGATIVE=1;
    final static int MESSAGE_STOP_DETECT =4;

    public static int rep = 0;

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        conn = new ConnectionHandler(getString(R.string.server_address),
                getString(R.string.server_port),
                tm.getDeviceId());
        Log.e("detectService","begin");
        messageHandler = new Handler(){
            public void handleMessage(Message m){
                switch(m.what){
                    case MESSAGE_DETECT_NEGATIVE:
//                        generateAlarm("");
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

                    Log.e("alarmDetect", "begin");

                    if (conn.getResult(ConnectionHandler.VERSION ) == false) {
                        Message m = new Message();
                        m.what = MESSAGE_DETECT_NEGATIVE;
                        messageHandler.sendMessage(m);
                        timer.cancel();

                    }
                    else{
                        timer.cancel();
                    }
                } catch (Exception e) {
                    Log.e("DetectService", e.getMessage());
                    timer.cancel();
                    instance.stopSelf();
                }
            }
        }, 1000, 10000);
    }

    /**
     * 反馈一次
     */
    private void startFeedbackTimer(final String version, final String signal){
        FeedbackTimer = new Timer();
        FeedbackTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                if(conn.selfOrOthers(version,signal)){
                    FeedbackTimer.cancel();
                }
            }
        },0,1000);
    }

    public void pauseDetectService(){
        timer.cancel();
    }

    public void startDetectService(){
        startTimer();
    }

    private void generateAlarm(String info){
        if(rep == 0){
            CustomizedDialog.createAlarm(this, info);
        }
//        rep = 1;

    }
        private void generateWhoIsUsed(String info){

        CustomizedDialog.generateWhoIsUsed(this, info);

    }

    private void generateDisableLength(String info){
        CustomizedDialog.generateDisableLength(this, info);
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
            generateWhoIsUsed("");
            return true;
        }
        else {
            generateAlarm("Wrong Password");
            return false;
        }
    }

    public boolean whoInUsed(String whoIsUsedResult){
        String result;
        if(whoIsUsedResult.equals("self")){
            result = "1";
        }
        else{
            result = "0";
        }
        //反馈学习
        startFeedbackTimer(ConnectionHandler.VERSION, result);
        generateDisableLength("");
        return true;
    }

    public boolean disableLength(String result){
        Log.e("disableLength",result);
        if(result.equals("never")){
            //continue to verify
            //重新开始弹窗线程,由启动应用控制，不必在此操作
//            startTimer();
        }
        else{
            pauseDetectService();
            Message m = new Message();
            m.what = MESSAGE_STOP_DETECT;
            MainActivity.messageHandler.sendMessage(m);
            //停止检测多久，给mainactivity发消息
        }
        return true;
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
