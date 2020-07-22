package org.foxail.android.reader.client;

import java.util.List;

import org.foxail.android.reader.model.News;

public abstract class Client {
	
	abstract public String getListUrl(int pageNum);

	abstract public String getContentUrl(String id);

	abstract public String getShareUrl(String id);
	
	abstract public void getNewsList(int pageNum, GetNewsList getNewsList);
	
	abstract public void getNewsContent(String id, GetNewsContent getNewsContent);
}
