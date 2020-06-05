package com.architectica.socialcomponents.main.main.Profile;

import android.view.View;

import com.architectica.socialcomponents.main.base.BaseFragmentView;
import com.architectica.socialcomponents.model.Post;

public interface ProjectsByUserView extends BaseFragmentView {

    void openPostDetailsActivity(Post post, View postItemView);

    void hideLoadingPostsProgress();

}
