package org.foxail.android.reader.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.foxail.android.common.CommonUtil;
import org.foxail.android.reader.R;
import org.foxail.android.reader.client.Client;
import org.foxail.android.reader.client.GetNewsList;
import org.foxail.android.reader.model.News;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    
    private final static String TAG = "MainActivity";
    protected final static int DIALOG_ALERT_EXIT = 1;
    
    private int curPage = 0;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView mainList;
    private MainListAdapter mainListAdapter;
    private long returnKeyPressed = 0;
    private Client client;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = clientFactory.getClient("cnbeta");
        
        //获取Toolbar
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        //Toolbar双击刷新
        toolbar.setOnClickListener(new View.OnClickListener() {
            private final long delayTime = 300;
            private long lastClickTime = 0;

            @Override
            public final void onClick(View v) {
                long nowClickTime = System.currentTimeMillis();
                if (nowClickTime - lastClickTime > delayTime) {
                    lastClickTime = nowClickTime;
                } else {
                    // 双击事件
                    doRefresh();
                }
            }
        });

        //菜单项单击事件
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
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
                        msg += "\n" + getString(R.string.app_name) + " v" + CommonUtil.getVerName(item.getActionView().getContext());

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
        });

        //下拉刷新
        swipeRefresh = findViewById(R.id.main_swiperefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        //新闻列表
        mainListAdapter = new MainListAdapter(this, new ArrayList<News>());
        mainList = findViewById(R.id.main_list);
        mainList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,
                false));
        mainList.setAdapter(mainListAdapter);

        //新闻列表滚动事件，到底时自动加载更多
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

        //默认加载第一页
        curPage = 1;
        doRefresh();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

        client.getNewsList(curPage, new GetNewsList() {
            @Override
            public void onSuccess(List<News> newsList) {
                //Log.d("TAG", response);
                //showToast("newsList: "+newsList.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainListAdapter.addItems(newsList);
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(int errCode, Exception e) {
                //Log.e("TAG", e.getMessage(), e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }
}