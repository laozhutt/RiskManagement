package com.sensordemo.utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by herbertxu on 5/29/16.
 */
public class ResourceManager  extends Application{
    private static Context context;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }
}
