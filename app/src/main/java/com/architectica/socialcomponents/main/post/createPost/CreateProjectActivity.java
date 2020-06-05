package com.architectica.socialcomponents.main.post.createPost;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.post.BaseCreateProjectActivity;

import androidx.annotation.NonNull;

public class CreateProjectActivity extends BaseCreateProjectActivity<CreateProjectView, CreateProjectPresenter> implements CreateProjectView {

    public static final int CREATE_NEW_PROJECT_REQUEST = 12;

    @NonNull
    @Override
    public CreateProjectPresenter createPresenter() {
        if (presenter == null) {
            return new CreateProjectPresenter(this);
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
                presenter.doSaveProject(imageUri);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
