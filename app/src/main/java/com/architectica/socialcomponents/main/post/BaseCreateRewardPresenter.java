package com.architectica.socialcomponents.main.post;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.pickImageBase.PickImagePresenter;
import com.architectica.socialcomponents.managers.PostManager;
import com.architectica.socialcomponents.managers.listeners.OnRewardCreatedListener;
import com.architectica.socialcomponents.utils.LogUtil;
import com.architectica.socialcomponents.utils.ValidationUtil;

import androidx.annotation.StringRes;

public abstract class BaseCreateRewardPresenter<V extends BaseCreateRewardView> extends PickImagePresenter<V> implements OnRewardCreatedListener {

    protected boolean creatingPost = false;
    protected PostManager postManager;

    public BaseCreateRewardPresenter(Context context) {
        super(context);
        postManager = PostManager.getInstance(context);
    }

    @StringRes
    protected abstract int getSaveFailMessage();

    protected abstract boolean isImageRequired();

    protected abstract void saveReward(final String title, final String description);

    protected void attemptCreateReward(Uri imageUri) {
        // Reset errors.
        ifViewAttached(view -> {
            view.setTitleError(null);
            view.setDescriptionError(null);

            String title = view.getTitleText().trim();
            String description = view.getDescriptionText().trim();

            boolean cancel = false;

            if (TextUtils.isEmpty(description)) {
                view.setDescriptionError(context.getString(R.string.warning_empty_description));
                cancel = true;
            }

            if (TextUtils.isEmpty(title)) {
                view.setTitleError(context.getString(R.string.warning_empty_title));
                cancel = true;
            } else if (!ValidationUtil.isPostTitleValid(title)) {
                view.setTitleError(context.getString(R.string.error_post_title_length));
                cancel = true;
            }

            if (isImageRequired() && view.getImageUri() == null) {
                view.showWarningDialog(R.string.warning_empty_image);
                view.requestImageViewFocus();
                cancel = true;
            }

            if (!cancel) {
                creatingPost = true;
                view.hideKeyboard();
                saveReward(title, description);
            }
        });
    }

    public void doSaveReward(Uri imageUri) {
        if (!creatingPost) {
            if (hasInternetConnection()) {
                attemptCreateReward(imageUri);
            } else {
                ifViewAttached(view -> view.showSnackBar(R.string.internet_connection_failed));
            }
        }
    }

    @Override
    public void onRewardSaved(boolean success) {
        creatingPost = false;

        ifViewAttached(view -> {
            view.hideProgress();
            if (success) {
                view.onPostSavedSuccess();
                LogUtil.logDebug(TAG, "Rewards post was saved");
            } else {
                view.showSnackBar(getSaveFailMessage());
                LogUtil.logDebug(TAG, "Failed to save a rewards post");
            }
        });
    }


}
