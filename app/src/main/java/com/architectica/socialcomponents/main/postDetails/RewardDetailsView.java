package com.architectica.socialcomponents.main.postDetails;

import android.view.View;

import com.architectica.socialcomponents.main.base.BaseView;
import com.architectica.socialcomponents.model.Comment;
import com.architectica.socialcomponents.model.Post;

import java.util.List;

public interface RewardDetailsView extends BaseView {

    void onPostRemoved();

    void openImageDetailScreen(String imagePath);

    void openProfileActivity(String authorId, View authorView);

    void setTitle(String title);

    void setDescription(String description);

    void loadPostDetailImage(String imagePath, String contentType);

    void loadAuthorPhoto(String photoUrl);

    void setAuthorName(String username);

    void showComplainMenuAction(boolean show);

    void showEditMenuAction(boolean show);

    void showDeleteMenuAction(boolean show);

    void openEditPostActivity(Post post);

}
