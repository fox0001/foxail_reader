package org.foxail.android.reader.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.foxail.android.reader.BuildConfig;
import org.foxail.android.reader.model.News;

import android.util.Log;
import com.android.volley.toolbox.*;
import org.json.*;

public class CnBetaClient extends Client {

	@Override
	public String getListUrl(int pageNum) {
		String url = "http://m.cnbeta.com/touch/default/timeline.json?page=" + pageNum;
		return url;
	}

	@Override
	public String getContentUrl(String id) {
		String url = "http://m.cnbeta.com/wap/view/" + id + ".htm";
		return url;
	}

	@Override
	public String getShareUrl(String id) {
		String url = "http://www.cnbeta.com/articles/" + id + ".htm";
		return url;
	}

	@Override
	public List<News> getNewsList(String responseStr) {
		List<News> newsList = new ArrayList<News>();
		if(responseStr == null || responseStr.isEmpty()) {
			return newsList;
		}
		
		try{
			JSONObject json = new JSONObject(responseStr);
			JSONArray list = json.getJSONObject("result").getJSONArray("list");
			JSONObject item = null;
			News news = null;
			for(int i = 0; i < list.length(); i++){
				item = list.getJSONObject(i);
				news = new News();
				news.setId(item.getString("sid"));
				news.setTitle(item.getString("title"));
				news.setContentUrl(this.getContentUrl(news.getId()));
				news.setShareUrl(this.getShareUrl(news.getId()));
				news.setReceiveDate(new Date(System.currentTimeMillis()));
				newsList.add(news);
			}
		} catch(Exception e){
			// do nothiong
		}
		
		return newsList;
	}

	@Override
	public String getNewsContent(String responseStr) {
		StringBuilder sb = new StringBuilder();
		
		//标题
		Pattern pattern = Pattern.compile(
				"<div class=\"title\"><b>.+?</b></div>", 
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(responseStr);
		if (matcher.find()) {
			sb.append(matcher.group(0));
		}
		
		//时间
		pattern = Pattern.compile(
				"(<div class=\"time\">.+?</div>)", 
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(responseStr);
		if (matcher.find()) {
			sb.append(matcher.group(0));
		}
		
		//内容
		pattern = Pattern.compile(
			"(<div class=\"content\">.+?</div>)\\s+<div style=\"text-align:center;margin:0 0 15px 0\">", 
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(responseStr);
		if (matcher.find()) {
			sb.append(matcher.group(1));
		}
		
		String html = sb.toString();
		//去掉所有class属性
		//html = html.replaceAll("(?s)class=[\"'].+?[\"']", "");
		//图片设置自动宽度
		html = html.replaceAll("(?s)(?u)<img.+?src=[\"'](.+?)[\"'].+?>", 
				"<img width='100%' src='$1'>");
		
		return html;
	}
}
