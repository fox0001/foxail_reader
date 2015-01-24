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

public class CnBetaClient extends Client {

	@Override
	public String getListUrl(int pageNum) {
		String url = "http://m.cnbeta.com/wap/index.htm?page=" + pageNum;
		return url;
	}

	@Override
	public String getContentUrl(String id) {
		String url = "http://m.cnbeta.com/wap/view_" + id + ".htm";
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
        
        Pattern pattern = Pattern.compile(
				"<div class=\"list\"><a href=\"/wap/view_(\\d+).htm\">([^<]+)</a></div>", 
				Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(responseStr);
        while(matcher.find()) {
    		News news = new News();
    		news.setId(matcher.group(1));
    		news.setTitle(matcher.group(2));
    		news.setContentUrl(this.getContentUrl(news.getId()));
    		news.setShareUrl(this.getShareUrl(news.getId()));
    		news.setReceiveDate(new Date(System.currentTimeMillis()));
    		newsList.add(news);
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
				"(<div class=\"content\"><p.+?>.+?</div>)\\s+<a", 
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
