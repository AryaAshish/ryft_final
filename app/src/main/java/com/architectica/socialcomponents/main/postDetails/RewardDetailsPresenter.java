package com.architectica.socialcomponents.main.postDetails;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.base.BasePresenter;
import com.architectica.socialcomponents.main.base.BaseView;
import com.architectica.socialcomponents.managers.CommentManager;
import com.architectica.socialcomponents.managers.PostManager;
import com.architectica.socialcomponents.managers.ProfileManager;
import com.architectica.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.architectica.socialcomponents.managers.listeners.OnPostChangedListener;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.model.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class RewardDetailsPresenter extends BasePresenter<RewardDetailsView> {

    private static final int TIME_OUT_LOADING_COMMENTS = 30000;

    private PostManager postManager;
    private ProfileManager profileManager;
    private CommentManager commentManager;
    private Post post;
    private boolean isPostExist;
    private boolean postRemovingProcess = false;

    private boolean attemptToLoadComments = false;

    RewardDetailsPresenter(Activity activity) {
        super(activity);

        postManager = PostManager.getInstance(context.getApplicationContext());
        profileManager = ProfileManager.getInstance(context.getApplicationContext());
        commentManager = CommentManager.getInstance(context.getApplicationContext());
    }

    public void loadProject(String postId) {

        postManager.getReward(context, postId, new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                ifViewAttached(view -> {
                    if (obj != null) {
                        post = obj;
                        isPostExist = true;
                        //view.initLikeController(post);
                        fillInUI(post);
                        //view.updateCounters(post);
                        //initLikeButtonState();
                        updateOptionMenuVisibility();
                    } else if (!postRemovingProcess) {
                        isPostExist = false;
                        view.onPostRemoved();
                        view.showNotCancelableWarningDialog(context.getString(R.string.error_post_was_removed));
                    }
                });
            }

            @Override
            public void onError(String errorText) {
                ifViewAttached(view -> {
                    view.showNotCancelableWarningDialog(errorText);
                });
            }
        });

    }

    private void fillInUI(@NonNull Post post) {
        ifViewAttached(view -> {
            view.setTitle(post.getTitle());
            view.setDescription(post.getDescription());
            view.loadPostDetailImage(post.getImageTitle(),post.getContentType());

            loadAuthorProfile();
        });
    }

    private void loadAuthorProfile() {
        if (post != null && post.getAuthorId() != null) {
            profileManager.getProfileSingleValue(post.getAuthorId(), new OnObjectChangedListenerSimple<Profile>() {
                @Override
                public void onObjectChanged(Profile profile) {
                    ifViewAttached(view -> {
                        if (profile.getPhotoUrl() != null) {
                            view.loadAuthorPhoto(profile.getPhotoUrl());
                        }

                        view.setAuthorName(profile.getUsername());
                    });
                }
            });
        }
    }

    public void onAuthorClick(View authorView) {
        if (post != null) {
            ifViewAttached(view -> view.openProfileActivity(post.getAuthorId(), authorView));
        }
    }

    public boolean hasAccessToModifyPost() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && post != null && post.getAuthorId().equals(currentUser.getUid());
    }

    private void openComplainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.add_complain)
                .setMessage(R.string.complain_text)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.add_complain, (dialogInterface, i) -> addComplain());

        builder.create().show();
    }

    private void addComplain() {
        postManager.addComplain(post);
        ifViewAttached(view -> {
            view.showComplainMenuAction(false);
            view.showSnackBar(R.string.complain_sent);
        });
    }

    public void doComplainAction() {
        if (checkAuthorization()) {
            openComplainDialog();
        }
    }

    public void attemptToRemovePost() {
        if (hasAccessToModifyPost() && checkInternetConnection()) {
            if (!postRemovingProcess) {
                openConfirmDeletingDialog();
            }
        }
    }

    private void openConfirmDeletingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.confirm_deletion_post)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.button_ok, (dialogInterface, i) -> removePost());

        builder.create().show();
    }

    private void removePost() {



        postRemovingProcess = true;
        ifViewAttached(view -> view.showProgress(R.string.removing));
        postManager.removePost(post, success -> ifViewAttached(view -> {
            FirebaseDatabase.getInstance().getReference().child("posts").child(post.getId()).removeValue();
            view.onPostRemoved();
            view.finish();

            /*if (success) {
                view.onPostRemoved();
                view.finish();
            } else {
                postRemovingProcess = false;
                view.showSnackBar(R.string.error_fail_remove_post);
            }*/

            view.hideProgress();
        }));


    }

    public void editPostAction() {
        if (hasAccessToModifyPost() && checkInternetConnection()) {
            ifViewAttached(view -> view.openEditPostActivity(post));
        }
    }

    public void updateOptionMenuVisibility() {
        ifViewAttached(view -> {
            if (post != null) {
                view.showEditMenuAction(false);
                view.showDeleteMenuAction(false);
                view.showComplainMenuAction(!post.isHasComplain());
            }
        });
    }

    public boolean isPostExist() {
        return isPostExist;
    }

    public Post getPost() {
        return post;
    }

    public void onPostImageClick() {
        ifViewAttached(view -> {
            if (post != null && post.getTitle() != null) {
                view.openImageDetailScreen(post.getImageTitle());
            }
        });
    }
}
