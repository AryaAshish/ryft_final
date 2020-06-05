package com.architectica.socialcomponents.main.main.Profile;

import android.app.Activity;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.enums.PostStatus;
import com.architectica.socialcomponents.main.base.BasePresenter;
import com.architectica.socialcomponents.main.postDetails.PostDetailsActivity;
import com.architectica.socialcomponents.managers.FollowManager;
import com.architectica.socialcomponents.managers.PostManager;
import com.architectica.socialcomponents.managers.ProfileManager;
import com.architectica.socialcomponents.model.Post;

public class PostsByUserPresenter extends BasePresenter<PostsByUserView> {

    private Activity activity;

    public PostsByUserPresenter(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    void onPostClick(Post post, View postItemView) {
        PostManager.getInstance(context).isPostExistSingleValue(post.getId(), exist -> {
            ifViewAttached(view -> {
                if (exist) {
                    view.openPostDetailsActivity(post, postItemView);
                } else {
                    view.showSnackBar(R.string.error_post_was_removed);
                }
            });
        });
    }

    public void onPostListChanged(int postsCount) {
        ifViewAttached(view -> {
            String postsLabel = context.getResources().getQuantityString(R.plurals.posts_counter_format, postsCount, postsCount);
           // view.updatePostsCounter(buildCounterSpannable(postsLabel, postsCount));
            //view.showLikeCounter(true);
            //view.showPostCounter(true);

            view.hideLoadingPostsProgress();

        });
    }

}
