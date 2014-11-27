package org.foxail.android.reader.activity;

import org.foxail.android.common.volley.HtmlRequest;
import org.foxail.android.reader.BuildConfig;
import org.foxail.android.reader.R;
import org.foxail.android.reader.client.Client;
import org.foxail.android.reader.model.News;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

@SuppressLint("NewApi")
public class NewsActivity extends BaseActivity {
	
	private final static String TAG = "NewsActivity";
	
	private News news;
	private WebView newsWeb;
	private RequestQueue mQueue;
	private Client client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);
		
		mQueue = Volley.newRequestQueue(this);
		client = clientFactory.getClient("cnbeta");
		
		//设置ActionBar图标可以点击
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		newsWeb = (WebView) findViewById(R.id.news_web);
		//支持缩放功能
		newsWeb.getSettings().setSupportZoom(true);
		newsWeb.getSettings().setBuiltInZoomControls(true);
		newsWeb.getSettings().setDisplayZoomControls(false);
		
		Intent intent = getIntent();
		Bundle newsBundle = intent.getExtras();
		news = (News) newsBundle.get("news");
		
		setTitle(news.getTitle());
		showNewsContent(news);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.news, menu);
		return true;
	}
    
    //响应菜单项单击 
	@Override  
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case android.R.id.home: {
	    		finish();
    			break;
    		}
	    	case R.id.news_menu_share: {
	    		share(news);
	    		break;
	    	}
	    	case R.id.news_menu_refresh: {
	    		//刷新新闻内容
	    		showNewsContent(news);
	    		break;
	    	}
	    	default:
	    		break;
		}
	    return true;
    }
	
	private void showNewsContent(News news) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "get news content starting. news.id=" + news.getId());
		}
		
		Bundle bundle = new Bundle();
		bundle.putString("msg", getString(R.string.msg_loading));
		showDialog(DIALOG_PROGRESS_COMMON, bundle);
		
		try {
			//html = client.getNewsContent(news.getId());
			HtmlRequest request = new HtmlRequest(client.getContentUrl(news.getId()),  
	            new Response.Listener<String>() {  
	                @Override  
	                public void onResponse(String response) {  
	                    //Log.d("TAG", response);
	            		
	                	String html = client.getNewsContent(response);
	        			
	        			//显示处理后的新闻内容
	        			newsWeb.loadDataWithBaseURL("about:blank", 
	        					html, "text/html", "utf-8", null);
	        			closePDialog();
	                }
	            }, new Response.ErrorListener() {  
	                @Override  
	                public void onErrorResponse(VolleyError error) {  
	                    //Log.e("TAG", error.getMessage(), error);  
	                }
	            });
			mQueue.add(request);
		} catch(Exception e) {
			closePDialog();
			showToast(getString(R.string.msg_connectServerFailed));
			return;
		}
	}

}
