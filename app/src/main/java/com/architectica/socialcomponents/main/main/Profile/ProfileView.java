package com.architectica.socialcomponents.main.main.Profile;

import android.text.Spannable;
import android.view.View;

import com.architectica.socialcomponents.enums.FollowState;
import com.architectica.socialcomponents.main.base.BaseFragmentView;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.model.Profile;

public interface ProfileView extends BaseFragmentView {

    void showUnfollowConfirmation(Profile profile);

    void updateFollowButtonState(FollowState followState);

    void updateFollowersCount(int count);

    void updateFollowingsCount(int count);

    void setFollowStateChangeResultOk();

    void startEditProfileActivity();

    void openCreatePostActivity();

    void setProfileName(String username);

    void setBio(String bio);

    void setStatus(String status);

    void setSkill(String skill);

    void setCredits(long credits);

    void setProfilePhoto(String photoUrl);

    void setDefaultProfilePhoto();

    void updateLikesCounter(Spannable text);

    void onPostRemoved();

    void onPostUpdated();

    void showLikeCounter(boolean show);

    void showPostCounter(boolean show);

    void updatePostsCounter(Spannable text);

}
