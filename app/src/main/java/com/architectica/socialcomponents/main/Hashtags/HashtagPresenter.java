package com.architectica.socialcomponents.main.Hashtags;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.enums.PostStatus;
import com.architectica.socialcomponents.main.base.BasePresenter;
import com.architectica.socialcomponents.main.main.Projects.ProjectsView;
import com.architectica.socialcomponents.main.postDetails.PostDetailsActivity;
import com.architectica.socialcomponents.managers.PostManager;
import com.architectica.socialcomponents.model.Post;

public class HashtagPresenter extends BasePresenter<HashtagView> {

    private PostManager postManager;

    public HashtagPresenter(Context context) {

        super(context);
        postManager = PostManager.getInstance(context);

    }

    void onProjectClicked(final Post post, final View postView) {

        postManager.isPostExistSingleValue(post.getId(), exist -> ifViewAttached(view -> {
            if (exist) {
                view.openPostDetailsActivity(post, postView);
            } else {
                view.showFloatButtonRelatedSnackBar(R.string.error_post_was_removed);
            }
        }));
    }

    void updateNewPostCounter() {
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(() -> ifViewAttached(view -> {
            int newPostsQuantity = postManager.getNewPostsCounter();
            if (newPostsQuantity > 0) {
                view.showCounterView(newPostsQuantity);
            } else {
                view.hideCounterView();
            }
        }));
    }

    public void initPostCounter() {
        postManager.setPostCounterWatcher(newValue -> updateNewPostCounter());
    }


}
