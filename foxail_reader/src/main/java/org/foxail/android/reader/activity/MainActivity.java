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
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

@SuppressLint("NewApi")
public class MainActivity extends BaseActivity {
    
    private final static String TAG = "MainActivity";
    protected final static int DIALOG_ALERT_EXIT = 1;
    
    private int curPage = 0;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView mainList;
    private MainListAdapter mainListAdapter;
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
        //getActionBar().setHomeButtonEnabled(true);
        //getActionBar().setHideOnContentScrollEnabled(true);

        swipeRefresh = findViewById(R.id.main_swiperefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        mainListAdapter = new MainListAdapter(this, new ArrayList<News>());
        mainList = findViewById(R.id.main_list);
        mainList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,
                false));
        mainList.setAdapter(mainListAdapter);

        mainList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE && isBottom()) {
                    loadMore();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        curPage = 1;
        doRefresh();
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
        case R.id.main_menu_home: {
            showTop();
            break;
        }
        case R.id.main_menu_refresh: {
            doRefresh();
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
    
    /**
     * 滚回顶端
     */
    private void showTop() {
        //showToast("showTop:"+mainList.getTop());
        //mainList.scrollTo(0, 0);
        mainList.scrollToPosition(0);
    }

    /**
     * 是否滚动到底部
     * @return
     */
    private boolean isBottom() {
        if (mainList == null) return false;
        if (mainList.computeVerticalScrollExtent() + mainList.computeVerticalScrollOffset()
                >= mainList.computeVerticalScrollRange())
            return true;
        return false;
    }

    /**
     * 刷新
     */
    private void doRefresh() {
        curPage = 1;
        showTop();
        loadNews();
    }

    /**
     * 加载下一页的新闻
     */
    private void loadMore() {
        if(curPage < 1) {
            curPage = 1;
        } else {
            curPage++;
        }
        loadNews();
    }
    
    /**
     * 获取并生成新闻列表
     */
    private void loadNews() {
        swipeRefresh.setRefreshing(true);

        if(curPage == 1) {
            //清除现有的新闻列表
            //mainList.removeAllViewsInLayout();
            mainListAdapter.clearItems();
        }
        
        try {
            //newsList = client.getNewsList(curPage);
            HtmlRequest htmlRequest = new HtmlRequest(client.getListUrl(curPage),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d("TAG", response);
                        
                        List<News> newsList = client.getNewsList(response);
                        //showToast("newsList: "+newsList.size());

                        mainListAdapter.addItems(newsList);

                        swipeRefresh.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError error) {  
                        //Log.e("TAG", error.getMessage(), error);

                        swipeRefresh.setRefreshing(false);
                    }
                });
            mQueue.add(htmlRequest);
        } catch(Exception e) {
            swipeRefresh.setRefreshing(false);
            showToast(getString(R.string.msg_connectServerFailed));
        }
    }
}