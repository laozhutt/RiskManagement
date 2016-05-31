package com.sensordemo.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class HttpRequestManager {
    public final String HTTP_GET = "GET";

    public final String HTTP_POST = "POST";

    /**
     * ��ǰ����� URL
     */
    protected String url = "";

    /**
     * HTTP ���������
     */
    protected String requsetType = HTTP_GET;

    /**
     * ��������ĳ�ʱʱ��
     */
    protected int connectionTimeout = 5000;

    /**
     * ��ȡԶ�����ݵĳ�ʱʱ��
     */
    protected int soTimeout = 10000;

    /**
     * ����˷��ص�״̬��
     */
    protected int statusCode = -1;

    /**
     * ��ǰ���ӵ��ַ�����
     */
    protected String charset = HTTP.UTF_8;

    /**
     * HTTP GET ���������
     */
    protected HttpRequestBase httpRequest = null;

    /**
     * HTTP ��������ò���
     */
    protected HttpParams httpParameters = null;

    /**
     * HTTP ������Ӧ
     */
    protected HttpResponse httpResponse = null;

    /**
     * HTTP �ͻ������ӹ�����
     */
    protected HttpClient httpClient = null;

    /**
     * HTTP POST ��ʽ���Ͷ�����ݹ�����
     */
    protected MultipartEntityBuilder multipartEntityBuilder = null;

    /**
     * �� HTTP ������¼�������
     */
    protected OnHttpRequestListener onHttpRequestListener = null;

    public HttpRequestManager() {
    }

    public HttpRequestManager(OnHttpRequestListener listener) {
        this.setOnHttpRequestListener(listener);
    }

    /**
     * ���õ�ǰ���������
     *
     * @param url
     * @return
     */
    public HttpRequestManager setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * �������ӳ�ʱʱ��
     *
     * @param timeout ��λ�����룩��Ĭ�� 5000
     * @return
     */
    public HttpRequestManager setConnectionTimeout(int timeout) {
        this.connectionTimeout = timeout;
        return this;
    }

    /**
     * ���� socket ��ȡ��ʱʱ��
     *
     * @param timeout ��λ�����룩��Ĭ�� 10000
     * @return
     */
    public HttpRequestManager setSoTimeout(int timeout) {
        this.soTimeout = timeout;
        return this;
    }

    /**
     * ���û�ȡ���ݵı����ʽ
     *
     * @param charset Ĭ��Ϊ UTF-8
     * @return
     */
    public HttpRequestManager setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * ��ȡ��ǰ HTTP ���������
     *
     * @return
     */
    public String getRequestType() {
        return this.requsetType;
    }

    /**
     * �жϵ�ǰ�Ƿ� HTTP GET ����
     *
     * @return
     */
    public boolean isGet() {
        return this.requsetType == HTTP_GET;
    }

    /**
     * �жϵ�ǰ�Ƿ� HTTP POST ����
     *
     * @return
     */
    public boolean isPost() {
        return this.requsetType == HTTP_POST;
    }

    /**
     * ��ȡ HTTP ������Ӧ��Ϣ
     *
     * @return
     */
    public HttpResponse getHttpResponse() {
        return this.httpResponse;
    }

    /**
     * ��ȡ HTTP �ͻ������ӹ�����
     *
     * @return
     */
    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * ���һ�� HTTP ����� header ��Ϣ
     *
     * @param name
     * @param value
     * @return
     */
    public HttpRequestManager addHeader(String name, String value) {
        this.httpRequest.addHeader(name, value);
        return this;
    }

    /**
     * ��ȡ HTTP GET ������
     *
     * @return
     */
    public HttpGet getHttpGet() {
        return (HttpGet) this.httpRequest;
    }

    /**
     * ��ȡ HTTP POST ������
     *
     * @return
     */
    public HttpPost getHttpPost() {
        return (HttpPost) this.httpRequest;
    }

    /**
     * ��ȡ�����״̬��
     *
     * @return
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * ͨ�� GET ��ʽ��������
     *
     * @param url
     * @return
     * @throws IOException
     */
    public String get(String url) throws Exception {
        this.requsetType = HTTP_GET;
        // ���õ�ǰ���������
        this.setUrl(url);
        // �½� HTTP GET ����
        this.httpRequest = new HttpGet(this.url);
        // ִ�пͻ�������
        this.httpClientExecute();
        // �����������Ӧ�¼������ط��������
        return this.checkStatus();
    }

    /**
     * ��ȡ HTTP POST ��������ύ������
     *
     * @return
     */
    public MultipartEntityBuilder getMultipartEntityBuilder() {
        if (this.multipartEntityBuilder == null) {
            this.multipartEntityBuilder = MultipartEntityBuilder.create();
            // ����Ϊ���������ģʽ
            multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            // ��������ı����ʽ
            multipartEntityBuilder.setCharset(Charset.forName(this.charset));
        }
        return this.multipartEntityBuilder;
    }

    /**
     * ������Ҫ POST �ύ�����ݺ�, ִ�и÷�����������ʵ��ȴ�����
     */
    public void buildPostEntity() {
        // ���� HTTP POST ʵ��
        HttpEntity httpEntity = this.multipartEntityBuilder.build();
        this.getHttpPost().setEntity(httpEntity);
    }

    /**
     * ���� POST ����
     *
     * @param url
     * @return
     * @throws Exception
     */
    public String post(String url) throws Exception {
        this.requsetType = HTTP_POST;
        // ���õ�ǰ���������
        this.setUrl(url);
        // �½� HTTP POST ����
        this.httpRequest = new HttpPost(this.url);
        // ִ�пͻ�������
        this.httpClientExecute();
        // �����������Ӧ�¼������ط��������
        return this.checkStatus();
    }

    /**
     * ִ�� HTTP ����
     *
     * @throws Exception
     */
    protected void httpClientExecute() throws Exception {
        // ���� HTTP �������
        this.httpParameters = new BasicHttpParams();
        this.httpParameters.setParameter("charset", this.charset);
        // ���� ��������ʱʱ��
        HttpConnectionParams.setConnectionTimeout(this.httpParameters, this.connectionTimeout);
        // ���� socket ��ȡ��ʱʱ��
        HttpConnectionParams.setSoTimeout(this.httpParameters, this.soTimeout);
        // ����һ���ͻ��� HTTP ����
        this.httpClient = new DefaultHttpClient(this.httpParameters);
        // ���� HTTP POST ����ִ��ǰ���¼������ص�����(��: �Զ����ύ�������ֶλ��ϴ����ļ���)
        this.getOnHttpRequestListener().onRequest(this);
        // ���� HTTP ���󲢻�ȡ�������Ӧ״̬
        this.httpResponse = this.httpClient.execute(this.httpRequest);
        // ��ȡ���󷵻ص�״̬��
        this.statusCode = this.httpResponse.getStatusLine().getStatusCode();
    }

    /**
     * ��ȡ����˷��ص���������ת�����ַ�������
     *
     * @throws Exception
     */
    public String getInputStream() throws Exception {
        // ����Զ��������
        InputStream inStream = this.httpResponse.getEntity().getContent();
        // �ֶζ�ȡ����������
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        // ���ݽ�������˳�
        inStream.close();
        // ������ת��Ϊ�ַ�������
        return new String(baos.toByteArray(), this.charset);
    }

    /**
     * �ر����ӹ������ͷ���Դ
     */
    protected void shutdownHttpClient() {
        if (this.httpClient != null && this.httpClient.getConnectionManager() != null) {
            this.httpClient.getConnectionManager().shutdown();
        }
    }

    /**
     * �����������Ӧ�¼������ط��������
     *
     * @return
     * @throws Exception
     */
    protected String checkStatus() throws Exception {
        OnHttpRequestListener listener = this.getOnHttpRequestListener();
        String content;
        if (this.statusCode == HttpStatus.SC_OK) {
            // ����ɹ�, �ص������¼�
            content = listener.onSucceed(this.statusCode, this);
        } else {
            // ����ʧ�ܻ�����, �ص������¼�
            content = listener.onFailed(this.statusCode, this);
        }
        // �ر����ӹ������ͷ���Դ
        this.shutdownHttpClient();
        return content;
    }

    /**
     * HTTP �������ʱ���¼������ӿ�
     */
    public interface OnHttpRequestListener {
        /**
         * ��ʼ�� HTTP GET �� POST ����֮ǰ�� header ��Ϣ���� �� �����������õȲ���
         *
         * @param request
         * @throws Exception
         */
        public void onRequest(HttpRequestManager request) throws Exception;

        /**
         * �� HTTP ������Ӧ�ɹ�ʱ�Ļص�����
         *
         * @param statusCode ��ǰ״̬��
         * @param request
         * @return ���������õ��ַ�������
         * @throws Exception
         */
        public String onSucceed(int statusCode, HttpRequestManager request) throws Exception;

        /**
         * �� HTTP ������Ӧʧ��ʱ�Ļص�����
         *
         * @param statusCode ��ǰ״̬��
         * @param request
         * @return ��������ʧ�ܵ���ʾ����
         * @throws Exception
         */
        public String onFailed(int statusCode, HttpRequestManager request) throws Exception;
    }

    /**
     * �� HTTP ����ļ����¼�
     *
     * @param listener
     * @return
     */
    public HttpRequestManager setOnHttpRequestListener(OnHttpRequestListener listener) {
        this.onHttpRequestListener = listener;
        return this;
    }

    /**
     * ��ȡ�Ѱ󶨹��� HTTP ��������¼�
     *
     * @return
     */
    public OnHttpRequestListener getOnHttpRequestListener() {
        return this.onHttpRequestListener;
    }
}