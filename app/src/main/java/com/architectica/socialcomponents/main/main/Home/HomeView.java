package com.architectica.socialcomponents.main.main.Home;

import android.view.View;

import com.architectica.socialcomponents.main.base.BaseFragmentView;
import com.architectica.socialcomponents.model.Post;

public interface HomeView extends BaseFragmentView {

    void openCreatePostActivity();
    void hideCounterView();
    void openPostDetailsActivity(Post post, View v);
    void showFloatButtonRelatedSnackBar(int messageId);
    void openProfileActivity(String userId, View view);
    void refreshPostList();
    void removePost();
    void updatePost();
    void showCounterView(int count);
    void openNotificationsActivity();
    void openUserProfileActivity();

}
