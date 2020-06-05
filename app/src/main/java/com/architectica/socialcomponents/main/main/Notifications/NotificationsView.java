package com.architectica.socialcomponents.main.main.Notifications;

import android.view.View;

import com.architectica.socialcomponents.main.base.BaseFragmentView;
import com.architectica.socialcomponents.model.Post;

public interface NotificationsView extends BaseFragmentView {

    void hideCounterView();
    void openPostDetailsActivity(Post post, View v);
    void showFloatButtonRelatedSnackBar(int messageId);
    void openProfileActivity(String userId, View view);
    void refreshPostList();
    void showCounterView(int count);


}
