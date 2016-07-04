package com.sensordemo.utils;

/**
 * Created by herbertxu on 5/29/16.
 */

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import com.sensordemo.R;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

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
            String result  = post.post("http://"+baseurl+"/"+method+"/");
            if(result.toString().equals(context.getString(R.string.server_response_ok))){
                Log.e("upload","ok");
                return true;
            }
        } catch (Exception e) {

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

    public boolean getResult() {

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
            String result = post.post("http://"+baseurl+"/query/");
            JSONObject obj = new JSONObject(result);
            Double d = obj.getDouble("result");
            Log.e("Result", String.valueOf(d));
            if(d > 0.3){
                //self
                return true;
            }
            //others
            return false;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
