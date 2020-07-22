package org.foxail.android.common.http;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    private final static OkHttpClient client = new OkHttpClient();

    public static void get(String urlStr, Callback callback) {
        Request request = new Request.Builder()
                .get()
                .url(urlStr)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

}
