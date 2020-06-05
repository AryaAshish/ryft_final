package com.architectica.socialcomponents.main.post;

import android.net.Uri;
import android.view.View;

import com.architectica.socialcomponents.main.pickImageBase.PickImageView;

import java.util.ArrayList;
import java.util.List;

public interface BaseCreateProjectView extends PickImageView {

    void setDescriptionError(String error);

    void setTitleError(String error);

    Boolean getNeedMentorsBoolean();

    String getTitleText();

    String getDescriptionText();

    List<String> getHashtagsList();

    ArrayList<String> getQuestionsList();

    void requestImageViewFocus();

    void onPostSavedSuccess();

    Uri getImageUri();
}