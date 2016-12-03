package com.sensordemo;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.sensordemo.utils.ConnectionHandler;
import com.sensordemo.utils.ProcessManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Message;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

public class CollectDataService extends Service implements SensorEventListener{

    private Timer timerAll;
    private Timer timer;
    private Timer detectForeAppTimer;
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

    public static boolean isFinish;
    public boolean uploadTrain = false;
    public static boolean uploadTest = false;
    public static final String configureFilePath = "config.xml";
    public static Properties configureObject;

    private String qqPackageName = "com.tencent.mobileqq";
    private String wechatPackageName = "com.tencent.mm";
    private boolean isAppRunning;
    private boolean isStartDetectForeAppTimer = false;

    final static int MESSAGE_LEGAL_FILE =5;

    private List<String> homePackageName;
    JniUtils ju;

    @Override
    public void onCreate(){
        super.onCreate();
        ju = new JniUtils();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorData = new SensorData();
        sensorDataList = new ArrayList<SensorData>();
        Log.d("configureObject",String.valueOf(MainActivity.configureObject));
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        connection = new ConnectionHandler(getString(R.string.server_address),
                getString(R.string.server_port),
                tm.getDeviceId());
        Log.e("collectService","begin");
        homePackageName = getHomes();

        startDetectForeAppTimer();


        configureObject = new Properties();
        try {
            configureObject.loadFromXML(getApplicationContext().openFileInput(configureFilePath));
            String fn = configureObject.getProperty(getString(R.string.property_file_number));
            if(fn.equals("null")){
                configureObject.setProperty(getString(R.string.property_file_number), "0");
                recordFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        uploadTest = false;
        isAppRunning = true;


        if(isFinish == true){
            num = 150;
        }
        else{
            num = 150;
        }
        repeat = repeatNum;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                while(sensorDataList.size()<num) {

//                    Log.e("actualdata",String.valueOf(sensorDataList.size()));
                    if(isScreenOn(CollectDataService.this) && isAppRunning){
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
                        Log.e("actualdata", String.valueOf(sensorDataList.size()));
                    }
                    else{
                        //invaild data
//                        SensorData data = new SensorData();
//                        data.accelerometerX = 0;
//                        data.accelerometerY = 0;
//                        data.accelerometerZ = 0;
//                        data.gravityX = 0;
//                        data.gravityY = 0;
//                        data.gravityZ = 0;
//                        data.gyroscopeX = 0;
//                        data.gyroscopeY = 0;
//                        data.gyroscopeZ = 0;
//                        sensorDataList.add(data);

//                        detectForeAppTimer.cancel();

                        break;
                    }

                    try {
                        //50HZ
                        Thread.sleep(interval);
                    }catch (Exception e) {
                        Log.e("CollectDataService", e.getMessage());
                    }
                }


                getMatrix();//判断一下正确率
                writeTxt();
                try {

//                    configureObject.loadFromXML(getApplicationContext().openFileInput(configureFilePath));
//                    int fileNumber = Integer.parseInt(configureObject.getProperty(getString(R.string.property_file_number)));
                    if(connection.isTrainFinished()){
                        isFinish = true;

                    }
//                    else{
//                        isFinish = false;
//                    }
                    if(isFinish){
                        //TODO:After the server is setup, this should be uncommented
                        Log.e("postTestData", "begin");
                        uploadTest = connection.postData(FilePath, getString(R.string.server_train_method));
//                        connection.getResult();
                    }
                    else {
                        //TODO:Same as above
                        Log.e("postTrainData", "begin");
                        if (connection.postData(FilePath, getString(R.string.server_train_method))) {
                            uploadTrain = true;//每次训练完修改文字
//                            isFinish = true;
                        }
//                        isFinish = true;
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

    public void getMatrix(){

        List<SensorData> newSensorDataList = new ArrayList<SensorData>();
        for(int i = 0; i<sensorDataList.size();i++){
            if(Math.abs(sensorDataList.get(i).gravityX)<0.1 && Math.abs(sensorDataList.get(i).gravityY)<0.1 && Math.abs(sensorDataList.get(i).gravityZ)<0.1){
                continue;
            }
            else if(Math.abs(sensorDataList.get(i).gravityX)<1.5 && Math.abs(sensorDataList.get(i).gravityY)<1.5 && Math.abs(sensorDataList.get(i).gravityZ)>9){
                continue;
            }
            else {
                newSensorDataList.add(sensorDataList.get(i));
            }
        }
        float[] ax = new float[10];
        float[] ay = new float[10];
        float[] az = new float[10];
        float[] wx = new float[10];
        float[] wy = new float[10];
        float[] wz = new float[10];
        float[] gx = new float[10];
        float[] gy = new float[10];
        float[] gz = new float[10];
        int total = newSensorDataList.size();
        int start = 0;
        int count = 0;//计算行数，62列
        int row = total/5 -1;//滑动窗口数量
        if(row < 1){
            return;
        }
        int[] index = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
                33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
                49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62 };
        float[][] values = new float[row][62];
        int[][] indices = new int[row][index.length];
        int[] groundTruth = null;
        int[] labels = new int[row];
        int isProb = 0; // Not probability prediction
        String modelFileLoc = Environment.getExternalStorageDirectory()
                + "/train.scale.model";
        double[] probs = new double[row];
        while(start + 10 <= total ){
            for(int i = 0;i < 10; i++){
                ax[i] = newSensorDataList.get(start+i).accelerometerX;
                ay[i] = newSensorDataList.get(start+i).accelerometerY;
                az[i] = newSensorDataList.get(start+i).accelerometerZ;
                wx[i] = newSensorDataList.get(start+i).gyroscopeX;
                wy[i] = newSensorDataList.get(start+i).gyroscopeY;
                wz[i] = newSensorDataList.get(start+i).gyroscopeZ;
                gx[i] = newSensorDataList.get(start+i).gravityX;
                gy[i] = newSensorDataList.get(start+i).gravityY;
                gz[i] = newSensorDataList.get(start+i).gravityZ;

            }
            float[]ret = ju.getMatrixNative(10,ax,ay,az,wx,wy,wz,gx,gy,gz);//return 62 dimen
            values[count] = ret;
            count++;
            start = start + 5;
        }


        if (ju.callSVM(values, indices, groundTruth, isProb, modelFileLoc, labels,
                probs) != 0) {
            Log.d("SVM", "Classification is incorrect");
        } else {
            String m = "";
            for (int l : labels)
                m += l + ", ";
            Log.e("SVM","Classification is done, the result is " + m);
//            Toast.makeText(this, "Classification is done, the result is " + m, Toast.LENGTH_SHORT).show();
        }


    }


    public void writeTxt(){



        boolean legalTxt = false;//判断文件是否为有数据的文件
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
//                Double ad = Math.sqrt(sensorData.accelerometerX*sensorData.accelerometerX+
//                        sensorData.accelerometerY*sensorData.accelerometerY+
//                        sensorData.accelerometerZ*sensorData.accelerometerZ);
                if(Math.abs(sensorData.gravityX)<0.1 && Math.abs(sensorData.gravityY)<0.1 && Math.abs(sensorData.gravityZ)<0.1){
                    Log.e("device","error");
                    continue;
                }
                if(Math.abs(sensorData.gravityX)<1.5 && Math.abs(sensorData.gravityY)<1.5 && Math.abs(sensorData.gravityZ)>9){
                    Log.e("device","flat");
                    continue;
                }
                legalTxt = true;
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

            //test jni
//            ju.test_native();
            if(legalTxt){
                Message m = new Message();
                m.what = MESSAGE_LEGAL_FILE;
                MainActivity.messageHandler.sendMessage(m);
                //停止检测多久，给mainactivity发消息
            }
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

    /**
     * screen on or off
     * @param context
     * @return
     */
    public boolean isScreenOn(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean screen = pm.isScreenOn();
        if(screen){
//            Log.e("screen","screen on");
            return true;
        }
//        Log.e("screen","screen off");
        return false;
    }

    class CollectDataBinder extends Binder{
        public CollectDataService getService(){
            return CollectDataService.this;
        }
    }

    //记录
    public void recordFile(){
        try {
            configureObject.storeToXML(getApplicationContext().openFileOutput(configureFilePath, ContextThemeWrapper.MODE_PRIVATE), null);
        }catch(Exception e){
            Log.e("recordFile", e.getMessage());
        }
    }

    private boolean isRunningApp(){
        String foregroundAppName = null;
        if (Build.VERSION.SDK_INT <=20){//5.0以下用get_task
            foregroundAppName = ProcessManager.getTopActivityName();
        }
        else{
            foregroundAppName = ProcessManager.getForegroundApp();
        }
        Log.e("running app name",foregroundAppName);
        if(homePackageName != null && homePackageName.size() > 0){
            boolean isHomeRunning = homePackageName.contains(foregroundAppName);
            if(isHomeRunning){
//                Log.e(debugLabel, "No app runing");
                return false;
            }
            else {
//                Log.e(debugLabel, "App runing");
                return true;
            }
        }else{
            return true;
        }

    }

    private void startDetectForeAppTimer(){
        detectForeAppTimer = new Timer();
        detectForeAppTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                try {
                    if (!isRunningApp()) {
                        Log.e("Interrupt", "Stop running");
                        isAppRunning = false;
//                        detectForeAppTimer.cancel();
                    }
                }catch (Exception e){
                    Log.e("CollectDataService", e.getMessage());
                    isAppRunning = false;
//                    detectForeAppTimer.cancel();
                }

            }

        }, 0, 1000);
    }

    /**
     * 获得属于桌面的应用的应用包名称
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        //属性
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for(ResolveInfo ri : resolveInfo){
            names.add(ri.activityInfo.packageName);
            Log.i("ztt", "packageName =" + ri.activityInfo.packageName);
        }
        return names;
    }
//    /**
//     * Detect if wechat or qq are opened by user
//     * @return
//     */
//    private boolean isSpecifiedAppStart(){
//        boolean res = false;
//        String topActivityClassName=getTopActivityName(this);
////        Log.e("topName",topActivityClassName);
//        if ((topActivityClassName.equals(qqPackageName)) || (topActivityClassName.equals(wechatPackageName))){
//            res = true;
//            Log.e("start","wechat or qq");
//        }
//        else {
//            res = false;
//        }
//
//        return  res;
//    }
//
//    /**
//     * Used to get the package name of the top activity
//     * @param context
//     * @return
//     */
//    private  String getTopActivityName(Context context){
//        String topActivityClassName=null;
//        ActivityManager activityManager =
//                (ActivityManager)(context.getSystemService(android.content.Context.ACTIVITY_SERVICE )) ;
//        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
//        if(runningTaskInfos != null){
//            ComponentName f=runningTaskInfos.get(0).topActivity;
//            topActivityClassName=f.getPackageName();
//        }
//        return topActivityClassName;
//    }
}
