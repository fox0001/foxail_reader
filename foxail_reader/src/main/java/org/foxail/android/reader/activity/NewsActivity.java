package org.foxail.android.reader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import org.foxail.android.common.SettingsUtil;
import org.foxail.android.reader.BuildConfig;
import org.foxail.android.reader.R;
import org.foxail.android.reader.client.Client;
import org.foxail.android.reader.client.GetNewsContent;
import org.foxail.android.reader.model.News;

public class NewsActivity extends BaseActivity {
	
	private final static String TAG = "NewsActivity";

	private Toolbar toolbar;
	private News news;
	private WebView newsWeb;
	private Client client;
	
	private String defaultCss;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);

		client = clientFactory.getClient(SettingsUtil.getCurClient(getApplicationContext()));

		//获取Toolbar
		toolbar = findViewById(R.id.news_toolbar);
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

		//显示系统的返回按钮
		//ActionBar ab = getSupportActionBar();
		//ab.setDisplayHomeAsUpEnabled(true);

		//新闻内容WebView
		newsWeb = (WebView) findViewById(R.id.news_web);
		newsWeb.clearHistory();
		newsWeb.clearFormData();
		newsWeb.clearCache(true);
		newsWeb.setBackgroundColor(getColor(R.color.background));

		WebSettings wvSettings = newsWeb.getSettings();
		wvSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // no cache
		//wvSettings.setJavaScriptEnabled(true);
		wvSettings.setSupportZoom(true); //支持缩放功能
		wvSettings.setBuiltInZoomControls(true);
		wvSettings.setDisplayZoomControls(false);

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
		popupDialog(DIALOG_PROGRESS_COMMON, bundle);

		client.getNewsContent(news.getContentUrl(), new GetNewsContent() {
			@Override
			public void onSuccess(String newsContent) {
				//Log.d("TAG", newsContent);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						String html = getDefaultCss() + newsContent;

						//显示处理后的新闻内容
						newsWeb.loadDataWithBaseURL("about:blank",
								html, "text/html", "utf-8", null);
						closePDialog();
					}
				});
			}

			@Override
			public void onFailure(int errCode, Exception e) {
				//Log.e("TAG", e.getMessage(), e);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closePDialog();
						showToast(getString(R.string.msg_connectServerFailed));
					}
				});
			}
		});
	}

}
