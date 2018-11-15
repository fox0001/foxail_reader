package org.foxail.android.reader.client;

import org.foxail.android.reader.model.News;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CnBetaClient extends Client {

	@Override
	public String getListUrl(int pageNum) {
		return String.format("https://m.cnbeta.com/touch/default/timeline.json?page=%1$s", pageNum);
	}

	@Override
	public String getContentUrl(String id) {
		return String.format("https://m.cnbeta.com/wap/view/%1$s.htm", id);
	}

	@Override
	public String getShareUrl(String id) {
		return String.format("https://m.cnbeta.com/view/%1$s.htm", id);
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
				"<div class=\"time\">.+?</div>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(responseStr);
		if (matcher.find()) {
			sb.append(matcher.group(0));
		}
		
		//内容
		pattern = Pattern.compile("<div [^>]*class=\"content\"[^>]*>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(responseStr);
		int startIndex = 0;
		if(matcher.find()) {
			startIndex = matcher.start();
		}
        sb.append(getHtmlByTag(responseStr, "div", startIndex));
		
		String html = sb.toString();
		//去掉所有class属性
		//html = html.replaceAll("(?s)class=[\"'].+?[\"']", "");
		//图片设置自动宽度
		html = html.replaceAll("(?s)(?u)<img.+?src=[\"'](.+?)[\"'].+?>", 
				"<img width='100%' src='$1'>");
		
		return html;
	}
    
    private String getHtmlByTag(String str, String tag, int startIndex) {
        String startTag = "<" + tag;
        String endTag = "</" + tag + ">";
        
        int fromIndex = str.indexOf(startTag, startIndex);
        if(fromIndex < 0) {
            return "";
        }
        int endIndex = str.indexOf(">", fromIndex) + 1;
        int mIndex = endIndex;
        
        while(true) {
            endIndex = str.indexOf(endTag, endIndex);
            if(endIndex < 0) {
                return str.substring(fromIndex);
            }
            endIndex += endTag.length();
        
            mIndex = str.indexOf(startTag, mIndex);
            if(mIndex < 0 || mIndex > endIndex) {
                return str.substring(fromIndex, endIndex);
            } else {
                mIndex = endIndex;
            }
        }
    }
}
