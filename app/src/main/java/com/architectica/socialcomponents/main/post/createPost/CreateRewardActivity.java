package com.architectica.socialcomponents.main.post.createPost;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.post.BaseCreateRewardActivity;

import androidx.annotation.NonNull;

public class CreateRewardActivity  extends BaseCreateRewardActivity<CreateRewardView, CreateRewardPresenter> implements CreateRewardView {
    public static final int CREATE_NEW_REWARD_REQUEST = 13;

    @NonNull
    @Override
    public CreateRewardPresenter createPresenter() {
        if (presenter == null) {
            return new CreateRewardPresenter(this);
        }
        return presenter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.post:
                presenter.doSaveReward(imageUri);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

