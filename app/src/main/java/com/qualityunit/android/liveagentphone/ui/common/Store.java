package com.qualityunit.android.liveagentphone.ui.common;

import android.app.Activity;

import com.qualityunit.android.liveagentphone.net.PaginationList;

public interface Store<T> {
    void init(Activity activity, int initFlag);
    void search(Activity activity, String searchTerm);
    void setListener(PaginationList.CallbackListener<T> callbackListener);
    void reload(Activity activity);
    void refresh(Activity activity);
    void nextPage(Activity activity);
    void clear();
}
