package com.architectica.socialcomponents.main.main.Profile;

import android.text.Spannable;
import android.view.View;

import com.architectica.socialcomponents.main.base.BaseFragmentView;
import com.architectica.socialcomponents.model.Post;

public interface PostsByUserView extends BaseFragmentView {

    void openPostDetailsActivity(Post post, View postItemView);

    void hideLoadingPostsProgress();

}
