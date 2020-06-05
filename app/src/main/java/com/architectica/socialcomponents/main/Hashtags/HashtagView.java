package com.architectica.socialcomponents.main.Hashtags;

import android.view.View;

import com.architectica.socialcomponents.main.base.BaseView;
import com.architectica.socialcomponents.model.Post;

public interface HashtagView extends BaseView {

    void hideCounterView();
    void openPostDetailsActivity(Post post, View v);
    void showFloatButtonRelatedSnackBar(int messageId);
    void openProfileActivity(String userId, View view);
    void refreshPostList();
    void showCounterView(int count);

}
