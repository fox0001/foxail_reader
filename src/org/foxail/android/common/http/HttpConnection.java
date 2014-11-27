package org.foxail.android.common.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieStore;
  
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;  
import org.apache.http.client.methods.HttpPost;  
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
  
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;  
import android.os.Message;  
import android.util.Config;
  
/** 
 * Asynchronous HTTP connections 
 *  
 * @author Greg Zavitz & Joseph Roth 
 */  
/**
 * Asynchronous HTTP connections
 * 
 * @author Greg Zavitz & Joseph Roth
 */
public class HttpConnection implements Runnable {
 
    public static final int DID_START = 0;
    public static final int DID_ERROR = 1;
    public static final int DID_SUCCEED = 2;
    public static final String DOWNLOAD_FILE_PATH = Environment.getExternalStorageDirectory().toString();
    public static final String DOWNLOAD_FILE_NAME = "foxailreader_update";
 
    private static final int GET = 0;
    private static final int POST = 1;
    private static final int PUT = 2;
    private static final int DELETE = 3;
    private static final int BITMAP = 4;
    private static final int FILE = 5;
    
    private static final int TIME_OUT = 10000;
 
    private String url;
    private int method;
    private Handler handler;
    private HttpEntity data;
 
    private HttpClient httpClient;
    private int timeOut;
    private CookieStore cookies;
 
    public HttpConnection() {
        this(new Handler());
    }
 
    public HttpConnection(Handler _handler) {
        this(_handler, TIME_OUT);
    }
 
    public HttpConnection(Handler _handler, int _timeOut) {
        handler = _handler;
        timeOut = _timeOut;
    }
 
    public void create(int method, String url, HttpEntity data) {
        this.method = method;
        this.url = url;
        this.data = data;
        ConnectionManager.getInstance().push(this);
    }
 
    public void get(String url) {
        create(GET, url, null);
    }
 
    public void post(String url, HttpEntity data) {
        create(POST, url, data);
    }
 
    public void put(String url, HttpEntity data) {
        create(PUT, url, data);
    }
 
    public void delete(String url) {
        create(DELETE, url, null);
    }
 
    public void bitmap(String url) {
        create(BITMAP, url, null);
    }
 
    public void file(String url) {
        create(FILE, url, null);
    }
 
    public void run() {
        handler.sendMessage(Message.obtain(handler, HttpConnection.DID_START, url));
        httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), timeOut);
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), timeOut);
        try {
            HttpResponse response = null;
            switch (method) {
            case GET:
                response = httpClient.execute(new HttpGet(url));
                break;
            case POST:
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(data);
                response = httpClient.execute(httpPost);
                break;
            case PUT:
                HttpPut httpPut = new HttpPut(url);
                httpPut.setEntity(data);
                response = httpClient.execute(httpPut);
                break;
            case DELETE:
                response = httpClient.execute(new HttpDelete(url));
                break;
            case BITMAP:
                response = httpClient.execute(new HttpGet(url));
                processBitmapEntity(response.getEntity());
                break;
            case FILE:
                response = httpClient.execute(new HttpGet(url));
                processFileEntity(response.getEntity());
                break;
            }
            if (method < BITMAP)
                processEntity(response.getEntity());
        } catch (Exception e) {
            handler.sendMessage(Message.obtain(handler, DID_ERROR, e));
        }
        ConnectionManager.getInstance().didComplete(this);
    }
 
    private void processEntity(HttpEntity entity) throws IllegalStateException,
            IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(entity
                .getContent()));
        String line, result = "";
        while ((line = br.readLine()) != null)
            result += line;
        handler.sendMessage(Message.obtain(handler, DID_SUCCEED, result));
    }
 
    private void processBitmapEntity(HttpEntity entity) throws IOException {
        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
        Bitmap bm = BitmapFactory.decodeStream(bufHttpEntity.getContent());
        handler.sendMessage(Message.obtain(handler, DID_SUCCEED, bm));
    }
 
    private void processFileEntity(HttpEntity entity) throws IOException {
        InputStream is = entity.getContent();
        FileOutputStream fos = null;
        File file = null;
        if (is != null) {
            file = new File(DOWNLOAD_FILE_PATH, DOWNLOAD_FILE_NAME);
            if(file.exists()) {
            	file.delete();
            }
            fos = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len = 0;
            while((len=(is.read(buf))) > 0){
            	fos.write(buf, 0, len);
            }
        }
        fos.flush();
        if (fos != null) {
        	fos.close();
        }
        handler.sendMessage(Message.obtain(handler, DID_SUCCEED, file));
    }
 
}