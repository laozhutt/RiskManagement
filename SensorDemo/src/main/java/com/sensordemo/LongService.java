package com.sensordemo;

import android.app.ActivityManager;
import android.app.Service;
//import android.app.usage.UsageEvents;
//import android.app.usage.UsageStats;
//import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.ContextThemeWrapper;


import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.sensordemo.utils.ProcessManager;
import com.sensordemo.utils.ResourceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by zyqu on 7/20/16.
 */
public class LongService extends Service {

    private static String debugLabel = "LongService";
    public class LocalBinder extends Binder {
        LongService getService() {
            return LongService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();
    private final long loopCycle = 30000;
    public static Set<String> oldForeApps = new HashSet<String>();
    public static final String configureFilePath = "config.xml";
    public static Properties configureObject;
    public static String progressState;
    private String appname = "com.sensordemo";
    private String qqPackageName = "com.tencent.mobileqq";
    private String wechatPackageName = "com.tencent.mm";
    private List<String> homePackageName;


    private Handler mHandler = new Handler();
    private Runnable mRunningAppChecker = new Runnable() {
        @Override
        public void run() {
            try{



                if (isRunningApp() && isScreenOn()){
//                    getRunningApp();
                    configureObject.loadFromXML(getApplicationContext().openFileInput(configureFilePath));
                    String st = configureObject.getProperty(getString(R.string.property_state));
                    if(st.equals("null")){//设置为正在训练状态
                        configureObject.setProperty(getString(R.string.property_state), getString(R.string.property_training_state));
                        recordFile();
                    }
                    progressState = configureObject.getProperty(getString(R.string.property_state));
                    Log.e("progressState",progressState);
                    //start to collect data, you may send a boardcast intent
                    //相当于每次开启新的应用，点击一下搜集的按钮。
                    if(progressState.equals(getString(R.string.property_training_state))){
//                        MainActivity.trainButton.performClick();

                        Intent serviceIntent = new Intent();
                        serviceIntent.setAction("com.train.test");
                        serviceIntent.putExtra("state", "train");
                        sendBroadcast(serviceIntent);

                    }
//                    else if(MainActivity.isCollecting){
//                        //正在搜集的话。先取消，再点
//                        MainActivity.lockButton.performClick();
//                        MainActivity.lockButton.performClick();
//                    }
                    else{
//                        MainActivity.lockButton.performClick();
//                        Intent serviceIntent = new Intent();
//                        serviceIntent.setAction("com.train.test");
//                        serviceIntent.putExtra("state", "test");
//                        sendBroadcast(serviceIntent);
                    }



                }else{
                    //do nothing
                }
            }catch(Exception e){
                Log.d(debugLabel, e.toString());
            }finally{
                mHandler.postDelayed(mRunningAppChecker, loopCycle);
            }

        }
    };

    private boolean isRunning = false;

    private void startRunning(){
        if (!isRunning){
            mRunningAppChecker.run();
            isRunning = true;
        }
    }
    private void stopRunning(){
        try{
            mHandler.removeCallbacks(mRunningAppChecker);
        }catch(Exception e){
            Log.d(debugLabel, e.toString());
        }finally{
            isRunning = false;
        }
    }

    private boolean isRunningApp(){
        Log.e("sdkver",Build.VERSION.SDK_INT+"");
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
                Log.e(debugLabel, "No app runing");
                return false;
            }
            else {
                Log.e(debugLabel, "App runing");
                return true;
            }
        }else{
            return true;
        }

    }

    private Set<String> getForeAppsNames(Context mContext){
        Set<String> apps = new HashSet<String>();
        List<AndroidAppProcess> lstProcess = com.jaredrummler.android.processes.ProcessManager.getRunningForegroundApps(mContext);
        for (int i = 0; i < lstProcess.size(); i++){
            apps.add(lstProcess.get(i).getPackageName());
//            Log.e("foreappname",lstProcess.get(i).getPackageName());
        }
        return apps;

    }

    private boolean isNewAppStart(){
        boolean res = false;
        Context mContext = getApplicationContext();

        Set<String> curForeApps = getForeAppsNames(mContext);
        Set<String> tmpForeApps = new HashSet<String>(curForeApps);

        try{
            tmpForeApps.removeAll(oldForeApps);
            if (tmpForeApps.size() > 0){
                res = true;
                Iterator itr = tmpForeApps.iterator();
                while(itr.hasNext()){
                    String newApp = (String) itr.next();
                    Log.d(debugLabel, newApp);
                    if(newApp.equals(appname)){
                        res = false;//应用本身不启动服务
                    }
                }
            }else{
                res = false;
            }
            if (res){
                Log.e(debugLabel, "New app start");
            }else{
                Log.e(debugLabel, "No new app");
            }
            return res;
        }catch(Exception e){
            Log.e(debugLabel, e.toString());
            res = false;
            return res;
        }finally {
            oldForeApps.clear();
            oldForeApps.addAll(curForeApps);
        }
    }

//    /**
//     * Detect if wechat or qq are opened by user
//     * @return
//     */
//    private boolean isSpecifiedAppStart(){
//        boolean res = false;
//        String topActivityClassName=getTopActivityName(this);
//        Log.e("topName",topActivityClassName);
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
//



    private boolean isScreenOn(){
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        boolean screen = pm.isScreenOn();
        return screen;
    }


    @Override
    public IBinder onBind(Intent intent){
        return mBinder;
    }

    @Override
    public void onDestroy(){
        Log.d(debugLabel, "Long service destoryed");
        stopRunning();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(debugLabel, "Long service started");
        stopRunning();
        startRunning();
        homePackageName = getHomes();
        configureObject = new Properties();



        try {



            configureObject.loadFromXML(getApplicationContext().openFileInput(configureFilePath));
            String st = configureObject.getProperty(getString(R.string.property_state));


            if(st.equals("null")){//设置为正在训练状态
                configureObject.setProperty(getString(R.string.property_state), getString(R.string.property_training_state));
                recordFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return START_STICKY;
    }

    //记录
    public void recordFile(){
        try {
            configureObject.storeToXML(getApplicationContext().openFileOutput(configureFilePath, ContextThemeWrapper.MODE_PRIVATE), null);
        }catch(Exception e){
            Log.e("recordFile", e.getMessage());
        }
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
            Log.e("ztt", "packageName =" + ri.activityInfo.packageName);
        }
        return names;
    }

}
