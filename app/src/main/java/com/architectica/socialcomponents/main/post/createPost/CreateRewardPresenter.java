package com.architectica.socialcomponents.main.post.createPost;

import android.content.Context;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.post.BaseCreateRewardPresenter;
import com.architectica.socialcomponents.model.Post;
import com.google.firebase.auth.FirebaseAuth;

public class CreateRewardPresenter extends BaseCreateRewardPresenter<CreateRewardView> {

    public CreateRewardPresenter(Context context) {
        super(context);
    }

    @Override
    protected int getSaveFailMessage() {
        return R.string.error_fail_create_reward;
    }

    @Override
    protected void saveReward(String title, String description) {
        ifViewAttached(view -> {
            view.showProgress(R.string.message_creating_offer);
            Post post = new Post();
            post.setTitle(title);
            post.setDescription(description);
            post.setAuthorId(FirebaseAuth.getInstance().getCurrentUser().getUid());

            if (view.getImageUri() != null){

                //postManager.createOrUpdatePostWithImage(view.getImageUri(), this, post, hashtags);
                postManager.createOrUpdateRewardWithImage(view.getImageUri(),this, post);


            }
            else {

                //postManager.createPost(this,post, hashtags);
                postManager.createReward(this,post);

            }

        });
    }

    @Override
    protected boolean isImageRequired() {
        return false;
    }
}
