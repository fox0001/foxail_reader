package org.foxail.android.reader.activity;

import org.foxail.android.reader.R;
import org.foxail.android.reader.client.ClientFactory;
import org.foxail.android.reader.model.News;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

public class BaseActivity extends Activity {
	
	private final static String TAG = "org.foxail.android.reader.activity.BaseActivity";
	protected final static int DIALOG_ALERT_COMMON = 0;
	protected final static int DIALOG_PROGRESS_COMMON = 10;
	
	protected ProgressDialog pDialog;
	protected ClientFactory clientFactory = ClientFactory.getInstance();

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
    	String title = null;
    	String msg = null;
    	if (bundle != null) {
    		title = bundle.getString("title");
    		msg = bundle.getString("msg");
    	}
    	if (isEmpty(title)) title = getString(R.string.title_notice);
    	
    	switch (id) {
    	case DIALOG_ALERT_COMMON: 
    		return new AlertDialog.Builder(BaseActivity.this)
            //.setIconAttribute(android.R.attr.alertDialogIcon)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
					removeDialog(DIALOG_ALERT_COMMON);
                }
            })
            .create();
    		
    	case DIALOG_PROGRESS_COMMON:
            pDialog = new ProgressDialog(BaseActivity.this);
            //pDialog.setIconAttribute(android.R.attr.alertDialogIcon);
            pDialog.setTitle(title);
            pDialog.setMessage(msg);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            return pDialog;
            
    	default:
        	return null;
    	}
    }
    
    /**
     * 关闭进度框
     */
    protected void closePDialog() {
		if (pDialog.isShowing()) {
			pDialog.dismiss();
		}
    }
	
	/**
	 * 显示提示信息
	 * 
	 * @param msg
	 */
    protected void showToast(String msg){
		Toast toast = Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	/**
	 * 判断字符串是否为空
	 * 
	 * @param str
	 * @return boolean
	 */
    protected boolean isEmpty(String str) {
		return (str == null || str.length() <= 0); 
	}
    
    /**
     * 分享新闻
     * 
     * @param news
     */
    protected void share(News news) {
		//分享新闻
		Intent intent=new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, R.string.button_share);
		intent.putExtra(Intent.EXTRA_TEXT, news.getTitle() + " " + news.getShareUrl());
		startActivity(Intent.createChooser(intent, getTitle()));
    }

}
