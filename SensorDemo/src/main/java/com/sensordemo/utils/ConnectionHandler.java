package com.sensordemo.utils;

/**
 * Created by herbertxu on 5/29/16.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.JsonReader;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.sensordemo.MainActivity;
import com.sensordemo.R;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

public class ConnectionHandler {
    HttpURLConnection connection;
    URL url;
    String baseurl;
    String IMEI;
    Context context;

    public static String VERSION = "99999";
    public static final String configureFilePath = "config.xml";
    public static Properties configureObject;

    public ConnectionHandler(String address, String port, String imei){
        baseurl = address+":"+port;
        IMEI = imei;
        context = ResourceManager.getContext();


        configureObject = new Properties();
        try {
            configureObject.loadFromXML(context.getApplicationContext().openFileInput(configureFilePath));
            String fn = configureObject.getProperty(context.getString(R.string.property_file_number));
            if(fn.equals("null")){
                configureObject.setProperty(context.getString(R.string.property_file_number), "0");
                recordFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

//    public boolean getResult() throws Exception{
//        String name;
//        try {
//            url = new URL(baseurl+"/"+context.getString(R.string.server_get_result_method)+"?"+context.getString(R.string.property_imei)+"="+IMEI);
//            connection = (HttpURLConnection) url.openConnection();
//            InputStreamReader in = new InputStreamReader(connection.getInputStream());
//            JsonReader jsonReader = new JsonReader(in);
//            jsonReader.beginObject();
//            name = jsonReader.nextName();
//            if(name.equals("result")){
//                return jsonReader.nextBoolean();
//            }
//            return false;
//        }
//        catch(Exception e){
//            throw e;
//        }
//    }



//    public boolean postData(String filePath, String method) throws Exception{
//        try{
//            url = new URL(baseurl+"/"+method+"/");
//            connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//            connection.setUseCaches(false);
//            connection.setRequestProperty("Connection", "Keep-Alive");
//            connection.setRequestProperty("Charset", "UTF-8");
//            connection.setRequestProperty("Content-Type", "text/plain");
//            connection.setRequestProperty(context.getString(R.string.property_imei),IMEI);
//
//            DataOutputStream ds = new DataOutputStream(connection.getOutputStream());
//            FileInputStream fStream = new FileInputStream(filePath);
//            int bufferSize = 1024;
//            byte[] buffer = new byte[bufferSize];
//            int length = -1;
//            while ((length = fStream.read(buffer)) != -1)
//            {
//                ds.write(buffer, 0, length);
//            }
//            fStream.close();
//            ds.flush();
//            //get the response
//            InputStream is = connection.getInputStream();
//            int ch;
//            StringBuffer b = new StringBuffer();
//            while ((ch = is.read()) != -1)
//            {
//                b.append((char) ch);
//            }
//            ds.close();
//            if(b.toString().equals(context.getString(R.string.server_response_ok))){
//                return true;
//            }
//            Log.e("Post Train","Server Response: "+b.toString());
//            return false;
//        }catch (Exception e){
//            throw e;
//        }
//    }

    public boolean postData(final String filePath, String method) {
        HttpRequestManager post = new HttpRequestManager();
        post.setCharset(HTTP.UTF_8).setConnectionTimeout(5000000)
                .setSoTimeout(10000000);
        final ContentType TEXT_PLAIN = ContentType.create("text/plain",
                Charset.forName(HTTP.UTF_8));


        post.setOnHttpRequestListener(new HttpRequestManager.OnHttpRequestListener() {

            @Override
            public void onRequest(HttpRequestManager request)
                    throws Exception {
                // 设置发送请求的header信息

                // 配置要POST的数据
                MultipartEntityBuilder builder = request
                        .getMultipartEntityBuilder();
                builder.addTextBody("imei", IMEI, TEXT_PLAIN);// 中文

                // 附件部分
                builder.addBinaryBody("path", new File(filePath));

                request.buildPostEntity();
            }

            @Override
            public String onSucceed(int statusCode,
                                    HttpRequestManager request) throws Exception {
                return request.getInputStream();
            }

            @Override
            public String onFailed(int statusCode,
                                   HttpRequestManager request) throws Exception {
                return request.getInputStream();
            }
        });

        try {
            if(method.equals("train")){
//                Log.e("traintesult","nnn");
                String result  = post.post("http://"+baseurl+"/"+method+"/");
                JSONObject obj = new JSONObject(result);
                int i = obj.getInt("result");


//                Log.e("traintesult",String.valueOf(i));
                if(i == 0){
                    Log.e("trainUpload","ok");
                    return true;
                }
            }
            if(method.equals("test")){
                String result  = post.post("http://"+baseurl+"/"+method+"/");
                JSONObject obj = new JSONObject(result);
                int i = obj.getInt("max_version");

                if(i > 0){
                    Log.e("testUpload","ok");
                    Log.e("VERSION", String.valueOf(i));
                    VERSION = String.valueOf(i);

                    configureObject.loadFromXML(context.getApplicationContext().openFileInput(configureFilePath));
                    int fileNumber = Integer.parseInt(configureObject.getProperty(context.getString(R.string.property_file_number)));
                    Log.e("filenum",String.valueOf(fileNumber));
                    if(fileNumber > 99){
                        Intent serviceIntent = new Intent();
                        serviceIntent.setAction("com.train.test");
                        serviceIntent.putExtra("state", "alarm");
                        context.sendBroadcast(serviceIntent);
                    }

//                    MainActivity.detectService.startDetectService();

                    return true;
                }
            }

        } catch (Exception e) {

            Looper.prepare();
            Toast toast=Toast.makeText(context.getApplicationContext(), "network error", Toast.LENGTH_SHORT);
            toast.show();
            Looper.loop();
            e.printStackTrace();
        }

        return false;
    }

    public boolean isTrainFinished() {

        HttpRequestManager post = new HttpRequestManager();
        post.setCharset(HTTP.UTF_8).setConnectionTimeout(5000)
                .setSoTimeout(10000);
        final ContentType TEXT_PLAIN = ContentType.create("text/plain",
                Charset.forName(HTTP.UTF_8));


        post.setOnHttpRequestListener(new HttpRequestManager.OnHttpRequestListener() {

            @Override
            public void onRequest(HttpRequestManager request)
                    throws Exception {
                // 设置发送请求的header信息

                // 配置要POST的数据
                MultipartEntityBuilder builder = request
                        .getMultipartEntityBuilder();
                builder.addTextBody("imei", IMEI, TEXT_PLAIN);// 中文


                request.buildPostEntity();
            }

            @Override
            public String onSucceed(int statusCode,
                                    HttpRequestManager request) throws Exception {
                return request.getInputStream();
            }

            @Override
            public String onFailed(int statusCode,
                                   HttpRequestManager request) throws Exception {
                return request.getInputStream();
            }
        });

        try {
            String result = post.post("http://"+baseurl+"/ask_trained/");
//            Log.e("train","test");
            if(result.toString().indexOf("true") != -1){
                Log.e("train", "ok");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean getResult(String version) {

        final String Version = version;
        HttpRequestManager post = new HttpRequestManager();
        post.setCharset(HTTP.UTF_8).setConnectionTimeout(5000)
                .setSoTimeout(10000);
        final ContentType TEXT_PLAIN = ContentType.create("text/plain",
                Charset.forName(HTTP.UTF_8));


        post.setOnHttpRequestListener(new HttpRequestManager.OnHttpRequestListener() {

            @Override
            public void onRequest(HttpRequestManager request)
                    throws Exception {
                // 设置发送请求的header信息

                // 配置要POST的数据
                MultipartEntityBuilder builder = request
                        .getMultipartEntityBuilder();
                builder.addTextBody("imei", IMEI, TEXT_PLAIN);// 中文
                builder.addTextBody("version", Version, TEXT_PLAIN);


                request.buildPostEntity();
            }

            @Override
            public String onSucceed(int statusCode,
                                    HttpRequestManager request) throws Exception {
                return request.getInputStream();
            }

            @Override
            public String onFailed(int statusCode,
                                   HttpRequestManager request) throws Exception {
                return request.getInputStream();
            }
        });

        try {

            String result = post.post("http://"+baseurl+"/query/");
            Log.e("run",result);
            if(!result.equals("{}")){//不为空

                JSONObject obj = new JSONObject(result);
                Double d = obj.getDouble("result");
                Log.e("Result", String.valueOf(d));
                if(d > 0.2){
                    //self
                    return true;
                }
                //others
                return false;
            }


            //空值返回true
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("Fuwuqi","busy" );
        return true;
    }


    public boolean selfOrOthers(String version,String signal) {

        final String Version = version;
        final String Signal = signal;
        HttpRequestManager post = new HttpRequestManager();
        post.setCharset(HTTP.UTF_8).setConnectionTimeout(5000)
                .setSoTimeout(10000);
        final ContentType TEXT_PLAIN = ContentType.create("text/plain",
                Charset.forName(HTTP.UTF_8));


        post.setOnHttpRequestListener(new HttpRequestManager.OnHttpRequestListener() {

            @Override
            public void onRequest(HttpRequestManager request)
                    throws Exception {
                // 设置发送请求的header信息

                // 配置要POST的数据
                MultipartEntityBuilder builder = request
                        .getMultipartEntityBuilder();
                builder.addTextBody("imei", IMEI, TEXT_PLAIN);// 中文
                builder.addTextBody("version", Version, TEXT_PLAIN);
                builder.addTextBody("signal", Signal, TEXT_PLAIN);


                request.buildPostEntity();
            }

            @Override
            public String onSucceed(int statusCode,
                                    HttpRequestManager request) throws Exception {
                return request.getInputStream();
            }

            @Override
            public String onFailed(int statusCode,
                                   HttpRequestManager request) throws Exception {
                return request.getInputStream();
            }
        });

        try {

            String result = post.post("http://"+baseurl+"/manual_fix/");
            Log.e("feedback",result);


            JSONObject obj = new JSONObject(result);
            int i = obj.getInt("received_signal");

            if(i == 0 || i == 1){
                //成功修改为0或1
                return true;
            }
            return false;






        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    //记录
    public void recordFile(){
        try {
            configureObject.storeToXML(context.getApplicationContext().openFileOutput(configureFilePath, ContextThemeWrapper.MODE_PRIVATE), null);
        }catch(Exception e){
            Log.e("recordFile", e.getMessage());
        }
    }

}
