package org.foxail.android.reader.activity;

import org.foxail.android.common.volley.HtmlRequest;
import org.foxail.android.reader.BuildConfig;
import org.foxail.android.reader.R;
import org.foxail.android.reader.client.Client;
import org.foxail.android.reader.model.News;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import android.content.res.*;

@SuppressLint("NewApi")
public class NewsActivity extends BaseActivity {
	
	private final static String TAG = "NewsActivity";

	private Toolbar toolbar;
	private News news;
	private WebView newsWeb;
	private RequestQueue mQueue;
	private Client client;
	
	private String defaultCss;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);

		mQueue = Volley.newRequestQueue(this);
		client = clientFactory.getClient("cnbeta");

		//获取Toolbar
		toolbar = findViewById(R.id.main_toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public final void onClick(View v) {
				finish();
			}
		});

		//Toolbar点击刷新
		toolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public final void onClick(View v) {
				//单击复制标题和URL
				setClipboard(news.getTitle() + " " + news.getShareUrl());
			}
		});

		//菜单项单击事件
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
		    @Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.news_menu_share: {
						share(news);
						break;
					}
					case R.id.news_menu_refresh: {
						//刷新新闻内容
						showNewsContent(news);
						break;
					}
					case R.id.news_menu_tobrowser: {
						//用浏览器打开
						browser(news.getShareUrl());
						break;
					}
					default:
						break;
				}
				return true;
			}
	    });

		//新闻内容WebView
		newsWeb = (WebView) findViewById(R.id.news_web);
		newsWeb.getSettings().setSupportZoom(true); //支持缩放功能
		newsWeb.getSettings().setBuiltInZoomControls(true);
		newsWeb.getSettings().setDisplayZoomControls(false);
		newsWeb.setBackgroundColor(getColor(R.color.background));
		
		Intent intent = getIntent();
		Bundle newsBundle = intent.getExtras();
		news = (News) newsBundle.get("news");

		toolbar.setTitle(news.getTitle());
		showNewsContent(news);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.news, menu);
		return true;
	}
	
	// Get default CSS
	private String getDefaultCss(){
		if(defaultCss == null){
			String bgColor = String.format("#%06X", 0xFFFFFF & getColor(R.color.background));
			String fontColor = String.format("#%06X", 0xFFFFFF & getColor(R.color.accent));
			defaultCss = String.format("<style>body{background-color:%s}\n* {color:%s;}</style>", bgColor, fontColor);
		}
		return defaultCss;
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
	            		
						String html = getDefaultCss() + client.getNewsContent(response);
						
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
