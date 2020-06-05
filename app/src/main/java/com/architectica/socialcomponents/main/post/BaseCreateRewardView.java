package com.architectica.socialcomponents.main.post;

import android.net.Uri;

import com.architectica.socialcomponents.main.pickImageBase.PickImageView;

public interface BaseCreateRewardView extends PickImageView {

    void setDescriptionError(String error);

    void setTitleError(String error);

    String getTitleText();

    String getDescriptionText();

    void requestImageViewFocus();

    void onPostSavedSuccess();

    Uri getImageUri();
}
