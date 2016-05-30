package com.sensordemo.com.sensordemo.util;

/**
 * Created by herbertxu on 5/29/16.
 */
import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import com.sensordemo.R;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.net.URL;

public class ConnectionHandler {
    HttpURLConnection connection;
    URL url;
    String baseurl;
    String IMEI;
    Context context;

    public ConnectionHandler(String address, String port, String imei){
        baseurl = address+":"+port;
        IMEI = imei;
        context = ResourceManager.getContext();

    }

    public boolean getResult() throws Exception{
        String name;
        try {
            url = new URL(baseurl+"/"+context.getString(R.string.server_get_result_method)+"?"+context.getString(R.string.property_imei)+"="+IMEI);
            connection = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(connection.getInputStream());
            JsonReader jsonReader = new JsonReader(in);
            jsonReader.beginObject();
            name = jsonReader.nextName();
            if(name.equals("result")){
                return jsonReader.nextBoolean();
            }
            return false;
        }
        catch(Exception e){
            throw e;
        }
    }

    public boolean postData(String filePath, String method) throws Exception{
        try{
            url = new URL(baseurl+"/"+method+"/");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestProperty(context.getString(R.string.property_imei),IMEI);

            DataOutputStream ds = new DataOutputStream(connection.getOutputStream());
            FileInputStream fStream = new FileInputStream(filePath);
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = -1;
            while ((length = fStream.read(buffer)) != -1)
            {
                ds.write(buffer, 0, length);
            }
            fStream.close();
            ds.flush();
            //get the response
            InputStream is = connection.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1)
            {
                b.append((char) ch);
            }
            ds.close();
            if(b.toString().equals(context.getString(R.string.server_response_ok))){
                return true;
            }
            Log.e("Post Train","Server Response: "+b.toString());
            return false;
        }catch (Exception e){
            throw e;
        }
    }

}
