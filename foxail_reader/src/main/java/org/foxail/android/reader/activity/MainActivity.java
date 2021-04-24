package org.foxail.android.reader.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.foxail.android.common.CommonUtil;
import org.foxail.android.common.SettingsUtil;
import org.foxail.android.reader.R;
import org.foxail.android.reader.client.Client;
import org.foxail.android.reader.client.ClientSource;
import org.foxail.android.reader.client.GetNewsList;
import org.foxail.android.reader.model.ClientItem;
import org.foxail.android.reader.model.News;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity {
    
    private final static String TAG = "MainActivity";
    protected final static int DIALOG_ALERT_EXIT = 1;
    
    private int curPage = 0;
    private DrawerLayout mainDrawer;
    private ListView leftitemList;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView mainList;
    private MainListAdapter mainListAdapter;
    private long returnKeyPressed = 0;
    private Client client;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = clientFactory.getClient(SettingsUtil.getCurClient(getApplicationContext()));
        
        // Toolbar
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainDrawer = findViewById(R.id.main_drawer);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mainDrawer,
                R.string.app_name, R.string.app_name);
        mainDrawer.addDrawerListener(mDrawerToggle);
        //mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();

        // Left menu. Show client list
        leftitemList = findViewById(R.id.leftitem_list);
        List<ClientItem> clientItems = new ArrayList<>();
        for(ClientSource clientSource : ClientSource.values()) {
            String name = null;
            switch(clientSource) {
                case cnbeta:
                    name = getString(R.string.client_cnbeta);
                    break;
                case oschina:
                    name = getString(R.string.client_oschina);
                    break;
            }
            ClientItem clientItem = new ClientItem(clientSource, name);
            clientItems.add(clientItem);
        }
        BaseAdapter itemListAdapter = new BaseAdapter(){
            @Override
            public int getCount() {
                return clientItems.size();
            }

            @Override
            public ClientItem getItem(int position) {
                return clientItems.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ClientItem clientItem = getItem(position);
                if(convertView == null) {
                    convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.clientlist_item, null, false);
                }
                TextView textView = convertView.findViewById(R.id.client_name);
                textView.setText(clientItem.getName());
                textView.setTag(clientItem.getClientSource());
                return convertView;
            }
        };
        leftitemList.setAdapter(itemListAdapter);

        leftitemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.client_name);
                ClientSource clientSource = (ClientSource) textView.getTag();
                SettingsUtil.setCurClient(getApplicationContext(), clientSource);
                client = clientFactory.getClient(clientSource);
                mainDrawer.close();
                doRefresh();
            }
        });

        // Toolbar. Double click event: refresh
        toolbar.setOnClickListener(new View.OnClickListener() {
            private final long delayTime = 300;
            private long lastClickTime = 0;
            private long nowClickTime = 0;

            @Override
            public final void onClick(View v) {
                nowClickTime = System.currentTimeMillis();
                if (nowClickTime - lastClickTime > delayTime) {
                    lastClickTime = nowClickTime;
                } else {
                    // 双击事件
                    doRefresh();
                }
            }
        });

        // Menu items. Click event
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
                        String msg = getString(R.string.app_name) + " v" + CommonUtil.getVerName(item.getActionView().getContext());

                        Bundle bundle = new Bundle();
                        bundle.putString("title", getString(R.string.button_about));
                        bundle.putString("msg", msg);
                        popupDialog(DIALOG_ALERT_COMMON, bundle);
                        break;
                    }
                    default:
                        // do nothing
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(mainDrawer.isOpen()) {
            // close drawer
            mainDrawer.close();
        } else {
            // open drawer
            mainDrawer.open();
        }
        return super.onOptionsItemSelected(item);
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