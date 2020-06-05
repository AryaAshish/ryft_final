package com.architectica.socialcomponents.main.main.Projects;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.enums.PostStatus;
import com.architectica.socialcomponents.main.base.BasePresenter;
import com.architectica.socialcomponents.main.interactors.PostInteractor;
import com.architectica.socialcomponents.main.postDetails.ProjectDetailsActivity;
import com.architectica.socialcomponents.managers.PostManager;
import com.architectica.socialcomponents.model.Post;

public class ProjectsPresenter extends BasePresenter<ProjectsView> {

    private PostManager postManager;
    private PostInteractor postInteractor;

    public ProjectsPresenter(Context context) {

        super(context);
        postManager = PostManager.getInstance(context);
        postInteractor = PostInteractor.getInstance(context);

    }

    void onCreatePostClickAction(View anchorView) {
        if (checkInternetConnection(anchorView)) {
            if (checkAuthorization()) {
                ifViewAttached(ProjectsView::openCreatePostActivity);
            }
        }
    }

    void onProjectClicked(final Post post, final View postView) {

        if (checkAuthorization()){

            postInteractor.isProjectExistSingleValue(post.getId(), exist -> ifViewAttached(view -> {
                if (exist) {
                    view.openPostDetailsActivity(post, postView);
                } else {
                    view.showFloatButtonRelatedSnackBar(R.string.error_post_was_removed);
                }
            }));

        }

    }

    void onPostCreated() {
        ifViewAttached(view -> {
            view.refreshPostList();
            view.showFloatButtonRelatedSnackBar(R.string.message_new_project_was_created);
        });
    }

    void onPostUpdated(Intent data) {
        if (data != null) {
            ifViewAttached(view -> {
                PostStatus postStatus = (PostStatus) data.getSerializableExtra(ProjectDetailsActivity.PROJECT_STATUS_EXTRA_KEY);
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
