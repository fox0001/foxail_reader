package org.foxail.android.reader.client;

import org.foxail.android.common.http.HttpUtil;
import org.foxail.android.reader.model.News;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OsChinaClient extends Client {

	@Override
	public String getListUrl(int pageNum) {
		return String.format("https://www.oschina.net/news/widgets/_news_index_all_list?p=%1$s&type=ajax", pageNum);
	}

	@Override
	public void getNewsList(int pageNum, GetNewsList getNewsList) {
		String urlStr = getListUrl(pageNum);
		HttpUtil.get(urlStr, new Callback() {
			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				getNewsList.onFailure(0, e);
			}

			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
				int responeCode = response.code();
				if(responeCode != HttpURLConnection.HTTP_OK) {
					getNewsList.onFailure(responeCode, null);
				} else {
					String responseStr = response.body().string();
					List<News> newsList = getNewsList(responseStr);
					getNewsList.onSuccess(newsList);
				}
			}
		});
	}

	@Override
	public void getNewsContent(String contentUrl, GetNewsContent getNewsContent) {
		HttpUtil.get(contentUrl, new Callback() {
			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				getNewsContent.onFailure(0, e);
			}

			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
				int responeCode = response.code();
				if(responeCode != HttpURLConnection.HTTP_OK) {
					getNewsContent.onFailure(responeCode, null);
				} else {
					String responseStr = response.body().string();
					String newsContent = getNewsContent(responseStr);
					getNewsContent.onSuccess(newsContent);
				}
			}
		});
	}

	private List<News> getNewsList(String responseStr) {
		List<News> newsList = new ArrayList<News>();
		if(responseStr == null || responseStr.isEmpty()) {
			return newsList;
		}

		Pattern titlePattern = Pattern.compile(
				"<h3 class=\"header\"><a href=\"(https://.+?)\" [^>]*>(.+?)</a></h3>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher matcher = titlePattern.matcher(responseStr);
		while (matcher.find()) {
			String url = matcher.group(1);
			String title = matcher.group(2);

			News news = new News();
			news.setTitle(title);
			news.setContentUrl(url);
			news.setShareUrl(url);
			newsList.add(news);
		}

		return newsList;
	}

	private String getNewsContent(String responseStr) {
		StringBuilder sb = new StringBuilder();

		//标题
		Pattern pattern = Pattern.compile(
				"<h1 class=\"article\\-box__title\"><a [^>]*>.+?</a></h1>",
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(responseStr);
		if (matcher.find()) {
			sb.append(matcher.group(0));
		}

		//时间
		pattern = Pattern.compile(
				"<div class=\"article\\-box__meta\">.+?</div>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(responseStr);
		if (matcher.find()) {
			sb.append(matcher.group(0));
		}

		//内容
		pattern = Pattern.compile("<div [^>]*class=\"article-detail\"[^>]*>",
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

	private String getHtmlByTag(String str, String tag, int beginIndex) {
		int endIndex = getEndIndexOfTag(str, tag, beginIndex);
		return str.substring(beginIndex, endIndex);
	}

	private int getEndIndexOfTag(String str, String tag, int beginIndex) {
		String beginTag = "<" + tag;
		String endTag = "</" + tag + ">";

		int endIndex = beginIndex;
		int bIndex = beginIndex;
		int eIndex = bIndex;
		int tagCount = 1;

		while(tagCount > 0) {
			eIndex = str.indexOf(endTag, endIndex);
			if(eIndex < 0) {
				break;
			}
			tagCount--;
			endIndex = eIndex + endTag.length();

			while(true) {
				bIndex = str.indexOf(">", bIndex) + 1;
				bIndex = str.indexOf(beginTag, bIndex);
				if(bIndex < 0 || bIndex > endIndex) {
					break;
				}
				tagCount++;
			}
			bIndex = endIndex;
		}
		return endIndex;
	}
}
