package org.foxail.android.reader.client;

import org.foxail.android.reader.model.News;

import java.util.List;

public interface GetNewsList {

    void onSuccess(List<News> newsList);

    void onFailure(int errCode, Exception e);

}
