package com.architectica.socialcomponents.main.main.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.view.View;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.enums.PostStatus;
import com.architectica.socialcomponents.main.base.BasePresenter;
import com.architectica.socialcomponents.main.interactors.PostInteractor;
import com.architectica.socialcomponents.main.postDetails.PostDetailsActivity;
import com.architectica.socialcomponents.managers.PostManager;
import com.architectica.socialcomponents.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePresenter extends BasePresenter<HomeView> {

    private PostManager postManager;
    private PostInteractor postInteractor;

    public HomePresenter(Context context) {
        super(context);
        postManager = PostManager.getInstance(context);
        postInteractor = PostInteractor.getInstance(context);
    }

    void onNotificationsClicked(){

        if (checkAuthorization()){

            ifViewAttached(view -> {

                view.openNotificationsActivity();

            });

        }

    }

    void onProfileClicked(){

        if (checkAuthorization()){

            ifViewAttached(view -> {

                view.openUserProfileActivity();

            });

        }

    }


    void onCreatePostClickAction(View anchorView) {
        if (checkInternetConnection(anchorView)) {
            if (checkAuthorization()) {
                ifViewAttached(HomeView::openCreatePostActivity);
            }
        }
    }

    void onPostClicked(final Post post, final View postView) {
        postInteractor.isPostExistSingleValue(post.getId(), exist -> ifViewAttached(view -> {
            if (exist) {
                view.openPostDetailsActivity(post, postView);
            } else {
                view.showFloatButtonRelatedSnackBar(R.string.error_post_was_removed);
            }
        }));
    }

    void onPostCreated() {
        ifViewAttached(view -> {
            view.refreshPostList();
            view.showFloatButtonRelatedSnackBar(R.string.message_post_was_created);
        });
    }

    void onPostUpdated(Intent data) {
        if (data != null) {
            ifViewAttached(view -> {
                PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);
                if (postStatus.equals(PostStatus.REMOVED)) {
                    view.removePost();
                    view.showFloatButtonRelatedSnackBar(R.string.message_post_was_removed);
                } else if (postStatus.equals(PostStatus.UPDATED)) {
                    view.updatePost();
                }
            });
        }
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
