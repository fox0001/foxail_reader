package org.foxail.android.reader.model;

import java.io.Serializable;
import java.sql.Date;

public class News implements Serializable {
	
	/*id*/
	String id;
	
	/*标题*/
	String title;
	
	/*发布日期*/
	String publicDate;
	
	/*内容*/
	String content;
	
	/*获取新闻时间*/
	Date receiveDate;
	
	/*获取内容的链接*/
	String contentUrl;
	
	/*分享链接*/
	String shareUrl;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPublicDate() {
		return publicDate;
	}

	public void setPublicDate(String publicDate) {
		this.publicDate = publicDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getReceiveDate() {
		return receiveDate;
	}

	public void setReceiveDate(Date receiveDate) {
		this.receiveDate = receiveDate;
	}

	public String getContentUrl() {
		return contentUrl;
	}

	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}

	public String getShareUrl() {
		return shareUrl;
	}

	public void setShareUrl(String shareUrl) {
		this.shareUrl = shareUrl;
	}
	
}
