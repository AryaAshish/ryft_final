package com.architectica.socialcomponents.main.main.Profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.enums.FollowState;
import com.architectica.socialcomponents.enums.PostStatus;
import com.architectica.socialcomponents.main.base.BaseFragmentView;
import com.architectica.socialcomponents.main.base.BasePresenter;
import com.architectica.socialcomponents.main.base.BaseView;
import com.architectica.socialcomponents.main.postDetails.PostDetailsActivity;
import com.architectica.socialcomponents.managers.FollowManager;
import com.architectica.socialcomponents.managers.PostManager;
import com.architectica.socialcomponents.managers.ProfileManager;
import com.architectica.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.model.Profile;
import com.architectica.socialcomponents.utils.LogUtil;
import com.architectica.socialcomponents.views.FollowButton;

public class ProfilePresenter extends BasePresenter<ProfileView> {

    private final FollowManager followManager;
    private Activity activity;
    private ProfileManager profileManager;

    private Profile profile;

    public ProfilePresenter(Activity activity) {
        super(activity);
        this.activity = activity;
        profileManager = ProfileManager.getInstance(context);
        followManager = FollowManager.getInstance(context);
    }

    private void followUser(String targetUserId) {
        ifViewAttached(BaseView::showProgress);
        followManager.followUser(activity, getCurrentUserId(), targetUserId, success -> {
            ifViewAttached(view -> {
                if (success) {
                    view.setFollowStateChangeResultOk();
                    checkFollowState(targetUserId);
                } else {
                    LogUtil.logDebug(TAG, "followUser, success: " + false);
                }
            });
        });
    }

    public void unfollowUser(String targetUserId) {
        ifViewAttached(BaseView::showProgress);
        followManager.unfollowUser(activity, getCurrentUserId(), targetUserId, success ->
                ifViewAttached(view -> {
                    if (success) {
                        view.setFollowStateChangeResultOk();
                        checkFollowState(targetUserId);
                    } else {
                        LogUtil.logDebug(TAG, "unfollowUser, success: " + false);
                    }
                }));
    }

    public void onFollowButtonClick(int state, String targetUserId) {
        if (checkInternetConnection() && checkAuthorization()) {
            if (state == FollowButton.FOLLOW_STATE || state == FollowButton.FOLLOW_BACK_STATE) {
                followUser(targetUserId);
            } else if (state == FollowButton.FOLLOWING_STATE && profile != null) {
                ifViewAttached(view -> view.showUnfollowConfirmation(profile));
            }
        }
    }

    public void checkFollowState(String targetUserId) {
        String currentUserId = getCurrentUserId();

        if (currentUserId != null) {
            if (!targetUserId.equals(currentUserId)) {
                followManager.checkFollowState(currentUserId, targetUserId, followState -> {
                    ifViewAttached(view -> {
                        view.hideProgress();
                        view.updateFollowButtonState(followState);
                    });
                });
            } else {
                ifViewAttached(view -> view.updateFollowButtonState(FollowState.MY_PROFILE));
            }
        } else {
            ifViewAttached(view -> view.updateFollowButtonState(FollowState.NO_ONE_FOLLOW));
        }
    }

    public void getFollowersCount(String targetUserId) {
        followManager.getFollowersCount(context, targetUserId, count -> {
            ifViewAttached(view -> view.updateFollowersCount((int) count));
        });
    }

    public void getFollowingsCount(String targetUserId) {

        followManager.getFollowingsCount(context, targetUserId, count -> {
            ifViewAttached(view -> view.updateFollowingsCount((int) count));
        });
    }


    public Spannable buildCounterSpannable(String label, int value) {
        SpannableStringBuilder contentString = new SpannableStringBuilder();
        contentString.append(String.valueOf(value));
        contentString.append("\n");
        int start = contentString.length();
        contentString.append(label);
        contentString.setSpan(new TextAppearanceSpan(context, R.style.TextAppearance_Second_Light), start, contentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return contentString;
    }

    public void onEditProfileClick() {
        if (checkInternetConnection()) {
            ifViewAttached(com.architectica.socialcomponents.main.main.Profile.ProfileView::startEditProfileActivity);
        }
    }

    public void onCreatePostClick() {
        if (checkInternetConnection()) {
            ifViewAttached(com.architectica.socialcomponents.main.main.Profile.ProfileView::openCreatePostActivity);
        }
    }

    public void loadProfile(Context activityContext, String userID) {
        profileManager.getProfileValue(activityContext, userID, new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                profile = obj;
                ifViewAttached(view -> {
                    view.setProfileName(profile.getUsername());
                    view.setBio(profile.getUserbio());
                    view.setStatus(profile.getStatus());
                    view.setSkill(profile.getSkill());
                    view.setCredits(profile.getCredits());
                    if (profile.getPhotoUrl() != null) {
                        view.setProfilePhoto(profile.getPhotoUrl());
                    } else {
                        view.setDefaultProfilePhoto();
                    }
                    int likesCount = (int) profile.getLikesCount();
                    String likesLabel = context.getResources().getQuantityString(R.plurals.likes_counter_format, likesCount, likesCount);
                    view.updateLikesCounter(buildCounterSpannable(likesLabel, likesCount));
                });
            }
        });
    }

    public void checkPostChanges(Intent data) {
        ifViewAttached(view -> {
            if (data != null) {
                PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);

                if (postStatus.equals(PostStatus.REMOVED)) {
                    view.onPostRemoved();
                } else if (postStatus.equals(PostStatus.UPDATED)) {
                    view.onPostUpdated();
                }
            }
        });
    }

    public void onPostListChanged(int postsCount) {
        ifViewAttached(view -> {
            String postsLabel = context.getResources().getQuantityString(R.plurals.posts_counter_format, postsCount, postsCount);
            view.updatePostsCounter(buildCounterSpannable(postsLabel, postsCount));
            view.showLikeCounter(true);
            view.showPostCounter(true);
            //view.hideLoadingPostsProgress();

        });
    }

   /* public void showLikes(){

        ifViewAttached(view -> {

            view.showLikeCounter(true);

        });

    }

    public void showPosts(){

        ifViewAttached(view -> {

            view.showPostCounter(true);

        });

    }*/


}
