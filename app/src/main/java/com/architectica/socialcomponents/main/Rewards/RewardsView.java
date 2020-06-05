package com.architectica.socialcomponents.main.Rewards;

import android.view.View;

import com.architectica.socialcomponents.main.base.BaseView;
import com.architectica.socialcomponents.model.Post;

public interface RewardsView extends BaseView {

    void hideCounterView();
    void openPostDetailsActivity(Post post, View v);
    void showFloatButtonRelatedSnackBar(int messageId);
    void openProfileActivity(String userId, View view);
    void refreshPostList();
    void showCounterView(int count);
    void openCreatePostActivity();

}
