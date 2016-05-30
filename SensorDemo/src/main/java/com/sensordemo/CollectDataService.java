package com.sensordemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import com.sensordemo.com.sensordemo.util.ConnectionHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.telephony.TelephonyManager;
import android.util.Log;

public class CollectDataService extends Service implements SensorEventListener{

    private Timer timerAll;
    private Timer timer;
    private int repeatNum = 1;
    private int repeat = repeatNum;
    private int timestamp = 15000;
    private int interval = 20;
    private int num = 100;

    private SensorManager sensorManager;
    private SensorData sensorData;
    private List<SensorData> sensorDataList;

    private ConnectionHandler connection;

    private String FilePath;

    private final CollectDataBinder serviceBinder = new CollectDataBinder();

    public boolean isFinish;

    @Override
    public void onCreate(){
        super.onCreate();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorData = new SensorData();
        sensorDataList = new ArrayList<SensorData>();
        Log.d("configureObject",String.valueOf(MainActivity.configureObject));
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        connection = new ConnectionHandler(getString(R.string.server_address),
                getString(R.string.server_port),
                tm.getDeviceId());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return serviceBinder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                sensorData.accelerometerX = event.values[0];
                sensorData.accelerometerY = event.values[1];
                sensorData.accelerometerZ = event.values[2];
                break;
            case Sensor.TYPE_GYROSCOPE:
                sensorData.gyroscopeX = event.values[0];
                sensorData.gyroscopeY = event.values[1];
                sensorData.gyroscopeZ = event.values[2];
                break;
            case Sensor.TYPE_GRAVITY:
                sensorData.gravityX = event.values[0];
                sensorData.gravityY = event.values[1];
                sensorData.gravityZ = event.values[2];
                break;
            default:
                break;
        }
        if(repeat<=0){
            sensorManager.unregisterListener(this);
        }
    }

    private void saveData(){
        if(isFinish == true){
            num = 1000;
        }
        else{
            num = 100;
        }
        repeat = repeatNum;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                while(sensorDataList.size()<num) {
                    SensorData data = new SensorData();
                    data.accelerometerX = sensorData.accelerometerX;
                    data.accelerometerY = sensorData.accelerometerY;
                    data.accelerometerZ = sensorData.accelerometerZ;
                    data.gravityX = sensorData.gravityX;
                    data.gravityY = sensorData.gravityY;
                    data.gravityZ = sensorData.gravityZ;
                    data.gyroscopeX = sensorData.gyroscopeX;
                    data.gyroscopeY = sensorData.gyroscopeY;
                    data.gyroscopeZ = sensorData.gyroscopeZ;
                    sensorDataList.add(data);
                    try {
                        Thread.sleep(interval);
                    }catch (Exception e) {
                        Log.e("CollectDataService", e.getMessage());
                    }
                }
                writeTxt();
                try {
                    if(isFinish){
                        //TODO:After the server is setup, this should be uncommented
                        //connection.postData(FilePath,getString(R.string.server_test_method));
                    }
                    else {
                        //TODO:Same as above
                        //if (connection.postData(FilePath, getString(R.string.server_train_method))) {
                        //    isFinish = true;
                        //}
                        isFinish = true;
                    }
                }
                catch(Exception e){
                    Log.e("Post Train",e.getMessage());
                }
            }
        },0);

    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void writeTxt(){

        String dir = Environment.getExternalStorageDirectory().getPath()+"/SensorDemoData";
        File fileDir = new File(dir);
        if(!fileDir.exists()){
            fileDir.mkdir();
        }
        try {
            String path = Environment.getExternalStorageDirectory().getPath()+"/SensorDemoData/"+System.currentTimeMillis()+".txt";
            FilePath = path;
            File file = new File(path);
            Writer writer = new FileWriter(file,true);

            for(SensorData sensorData : sensorDataList){
                Double ad = Math.sqrt(sensorData.accelerometerX*sensorData.accelerometerX+
                        sensorData.accelerometerY*sensorData.accelerometerY+
                        sensorData.accelerometerZ*sensorData.accelerometerZ);
                String string = sensorData.accelerometerX+"\n"+
                        sensorData.accelerometerY+"\n"+
                        sensorData.accelerometerZ+"\n"+
                        sensorData.gyroscopeX+"\n"+
                        sensorData.gyroscopeY+"\n"+
                        sensorData.gyroscopeZ+"\n"+
                        sensorData.gravityX+"\n"+
                        sensorData.gravityY+"\n"+
                        sensorData.gravityZ+"\n";
                writer.write(string);
            }
            writer.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    public void collect(){
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorDataList.clear();
        saveData();

    }


    public void pauseCollect(){
        sensorManager.unregisterListener(this);
        timer.cancel();
    }

    public int checkProcess(){
        return sensorDataList.size();
    }


    private class SensorData{
        public float accelerometerX;
        public float accelerometerY;
        public float accelerometerZ;
        public float gyroscopeX;
        public float gyroscopeY;
        public float gyroscopeZ;
        public float gravityX;
        public float gravityY;
        public float gravityZ;
    }

    class CollectDataBinder extends Binder{
        public CollectDataService getService(){
            return CollectDataService.this;
        }
    }
}
