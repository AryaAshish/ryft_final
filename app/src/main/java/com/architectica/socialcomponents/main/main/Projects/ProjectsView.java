package com.architectica.socialcomponents.main.main.Projects;

import android.view.View;

import com.architectica.socialcomponents.main.base.BaseFragmentView;
import com.architectica.socialcomponents.model.Post;

public interface ProjectsView extends BaseFragmentView {

    void openCreatePostActivity();
    void hideCounterView();
    void openPostDetailsActivity(Post post, View v);
    void showFloatButtonRelatedSnackBar(int messageId);
    void openProfileActivity(String userId, View view);
    void refreshPostList();
    void showCounterView(int count);
    void removePost();
    void updatePost();

}
