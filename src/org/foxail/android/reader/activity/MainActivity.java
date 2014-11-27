package org.foxail.android.reader.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.foxail.android.common.CommonUtil;
import org.foxail.android.common.volley.HtmlRequest;
import org.foxail.android.reader.R;
import org.foxail.android.reader.client.Client;
import org.foxail.android.reader.model.News;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

@SuppressLint("NewApi")
public class MainActivity extends BaseActivity {
	
	private final static String TAG = "MainActivity";
	protected final static int DIALOG_ALERT_EXIT = 1;
	
	private int curPage = 0;
	private ListView mainList;
	private long returnKeyPressed = 0;
	private RequestQueue mQueue;
	private Client client;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mQueue = Volley.newRequestQueue(this);
		client = clientFactory.getClient("cnbeta");
		
		//设置ActionBar的图标可点击
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mainList = (ListView) findViewById(R.id.main_list);
		mainList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView newsId =(TextView) view.findViewById(R.id.main_list_id);
				TextView newsTitle =(TextView) view.findViewById(R.id.main_list_title);
				News news = new News();
				news.setId((String) newsId.getText());
				news.setTitle((String) newsTitle.getText());
				
				Intent intent = new Intent(view.getContext(), NewsActivity.class);
				intent.putExtra("news", news);
				startActivity(intent);
			}
        });
		
		showHome();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
    
    //响应菜单项单击  
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case android.R.id.home: {
    		if(curPage == 1) {
    			//首页，则退出
    			confirmExit();
    		} else {
    			//不是首页，则返回首页
        		showHome();
    		}
    		break;
		}
    	case R.id.main_menu_home: {
    		showHome();
    		break;
    	}
    	case R.id.main_menu_next: {
    		showNext();
    		break;
    	}
    	case R.id.main_menu_previous: {
    		showPrevious();
    		break;
    	}
    	case R.id.main_menu_refresh: {
    		getMobileNews();
    		break;
    	}
    	case R.id.main_menu_settings: {
    		
    		break;
    	}
    	case R.id.main_menu_about: {
    		String msg = getString(R.string.button_about);
    		msg += "\n" + getString(R.string.app_name) + " v" + CommonUtil.getVerName(this);
    		
    		Bundle bundle = new Bundle();
    		bundle.putString("title", getString(R.string.button_about));
    		bundle.putString("msg", msg);
			showDialog(DIALOG_ALERT_COMMON, bundle);
    		break;
    	}
    	default:
    		break;
    	}
        return true;
    }
    
    /**
     * 捕获“返回键”事件
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 如果是返回键，提示再按返回则退出
		if(keyCode == KeyEvent.KEYCODE_BACK){
			//退出
			if(returnKeyPressed <= 0 || 
					event.getEventTime() - returnKeyPressed > 3000) {
				showToast(getString(R.string.msg_againExit));
				returnKeyPressed = event.getEventTime();
				return true;
			} else {
				finish();
				System.exit(0);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
    	if(id == DIALOG_ALERT_EXIT){
    		return new AlertDialog.Builder(MainActivity.this)
            //.setIconAttribute(android.R.attr.alertDialogIcon)
            .setTitle(getString(R.string.title_exit))
            .setMessage(getString(R.string.msg_confirmExit))
            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
					finish();
					System.exit(0);
                }
            })
            .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
                }
            })
            .create();
    	} else {
    		return super.onCreateDialog(id, bundle);
    	}
    }
    
    /**
     * 显示首页
     */
    private void showHome() {
		if(curPage != 1) {
			curPage = 1;
			getMobileNews();
		}
    }
    
    /**
     * 显示上一页
     */
    private void showPrevious() {
    	if(curPage == 1) {
    		return;
    	} else if(curPage > 1) {
    		--curPage;
    	} else {
    		curPage = 1;
    	}
    	getMobileNews();
    }
    
    /**
     * 显示下一页
     */
    private void showNext() {
		++curPage;
		getMobileNews();
    }
    
    /**
     * 退出
     */
    protected void confirmExit() {
    	showDialog(DIALOG_ALERT_EXIT, null);
    }
	
	/**
	 * 获取并生成新闻显示(移动版)
	 */
	private void getMobileNews() {
		Bundle bundle = new Bundle();
		bundle.putString("msg", getString(R.string.msg_loading));
		showDialog(DIALOG_PROGRESS_COMMON, bundle);
		
		//设置标题显示当前页码
		setTitle(getString(R.string.app_name) + " #" + curPage);
		//清除现有的新闻列表
		mainList.removeAllViewsInLayout();
		//滚动到顶端
		mainList.scrollTo(0, 0);
		
		try {
			//newsList = client.getNewsList(curPage);
			HtmlRequest htmlRequest = new HtmlRequest(client.getListUrl(curPage),
	            new Response.Listener<String>() {
	                @Override
	                public void onResponse(String response) {
	                    //Log.d("TAG", response);
	            		
	            		List<News> newsList = client.getNewsList(response);

	            		List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
	            		for (News news : newsList) {
	            			Map<String,String> map = new HashMap<String,String>();
	            			map.put("id", news.getId());
	            			map.put("title", news.getTitle());
	            			resultList.add(map);
	            		}
	            		
	            		SimpleAdapter mainListAdapter = new SimpleAdapter(
	            				MainActivity.this, 
	            				resultList, 
	            				R.layout.layout_main_list, 
	            				new String[]{"id", "title"}, 
	            				new int[]{R.id.main_list_id, R.id.main_list_title}
	                    );
	            		mainList.setAdapter(mainListAdapter);
	            		
	            		closePDialog();
	                }
	            },
	            new Response.ErrorListener() {  
	                @Override  
	                public void onErrorResponse(VolleyError error) {  
	                    //Log.e("TAG", error.getMessage(), error);  
	                }
	            });
			mQueue.add(htmlRequest);
		} catch(Exception e) {
			closePDialog();
			showToast(getString(R.string.msg_connectServerFailed));
			return;
		}
	}
}