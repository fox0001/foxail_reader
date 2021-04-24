package org.foxail.android.reader.activity;

import org.foxail.android.reader.R;
import org.foxail.android.reader.client.ClientFactory;
import org.foxail.android.reader.model.News;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BaseActivity extends AppCompatActivity {

	private final static String TAG = "org.foxail.android.reader.activity.BaseActivity";
	protected final static int DIALOG_ALERT_COMMON = 0;
	protected final static int DIALOG_PROGRESS_COMMON = 10;

	protected AlertDialog alertDialog;
	protected AlertDialog progressDialog;
	protected ClientFactory clientFactory = ClientFactory.getInstance();
	protected Toolbar toolbar;

	/**
	 * 弹出对话框
	 *
	 * @param id
	 * @param bundle
	 */
	protected void popupDialog(int id, Bundle bundle) {
		String title = null;
		String msg = null;
		if (bundle != null) {
			title = bundle.getString("title");
			msg = bundle.getString("msg");
		}
		if (isEmpty(title)) title = getString(R.string.title_notice);

		switch (id) {
			case DIALOG_ALERT_COMMON:
				if(alertDialog == null) {
					alertDialog = new AlertDialog.Builder(BaseActivity.this)
							.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// do nothing
								}
							})
							.create();
				}
				alertDialog.setTitle(title);
				alertDialog.setMessage(msg);
				alertDialog.show();
				break;
			case DIALOG_PROGRESS_COMMON:
				if(progressDialog == null) {
					progressDialog = new AlertDialog.Builder(BaseActivity.this)
							.create();
				}
				progressDialog.setTitle(title);
				progressDialog.setMessage(msg);
				progressDialog.show();
				break;
			default:
				// do nothing
				break;
		}
	}

	/**
	 * 关闭进度对话框
	 */
	protected void closePDialog() {
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
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
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, R.string.button_share);
		intent.putExtra(Intent.EXTRA_TEXT, news.getTitle() + " " + news.getShareUrl());
		startActivity(Intent.createChooser(intent, getTitle()));
    }

	/**
	 * 用浏览器打开URL
	 *
	 * @param url
	 */
	protected void browser(String url) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}

	/**
	 * 复制文本到剪切板
	 *
	 * @param text
	 */
	protected void setClipboard(String text) {
		//获取剪贴板管理器：
		ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		// 创建普通字符型ClipData
		ClipData mClipData = ClipData.newPlainText("Label", text);
		// 将ClipData内容放到系统剪贴板里。
		cm.setPrimaryClip(mClipData);

		showToast(getString(R.string.msg_copyText));
	}

}
