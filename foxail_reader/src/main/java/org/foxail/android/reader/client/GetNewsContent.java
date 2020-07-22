package org.foxail.android.reader.client;

import org.foxail.android.reader.model.News;

import java.util.List;

public interface GetNewsContent {

    void onSuccess(String newsContent);

    void onFailure(int errCode, Exception e);

}
