/*
 * Copyright 2018 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.architectica.socialcomponents.main.editProfile;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.base.BaseView;
import com.architectica.socialcomponents.main.pickImageBase.PickImagePresenter;
import com.architectica.socialcomponents.managers.ProfileManager;
import com.architectica.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.architectica.socialcomponents.model.Profile;
import com.architectica.socialcomponents.utils.ValidationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Alexey on 03.05.18.
 */

public class EditProfilePresenter<V extends EditProfileView> extends PickImagePresenter<V> {

    protected Profile profile;
    protected ProfileManager profileManager;

    protected EditProfilePresenter(Context context) {
        super(context);
        profileManager = ProfileManager.getInstance(context.getApplicationContext());
    }

    public void loadProfile() {
        ifViewAttached(BaseView::showProgress);
        profileManager.getProfileSingleValue(getCurrentUserId(), new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                profile = obj;
                ifViewAttached(view -> {
                    if (profile != null) {
                        view.setName(profile.getUsername());
                        view.setUsertype(profile.getUsertype());
                        view.setBio(profile.getUserbio());
                        view.setUseruri(profile.getUseruri());
                        if (profile.getPhotoUrl() != null) {
                            view.setProfilePhoto(profile.getPhotoUrl());
                        }
                        view.setPhoneNumber(profile.getPhoneNumber());
                        view.setSkill(profile.getUsertype(),profile.getSkill());
                    }

                    view.hideProgress();
                    view.setNameError(null);
                });
            }
        });
    }

    public void attemptCreateProfile(Uri imageUri) {
        if (checkInternetConnection()) {
            ifViewAttached(view -> {
                view.setNameError(null);
                String usertype = view.getUsertypeText().trim();
                String name = view.getNameText().trim();
                String bio = view.getBioText().trim();
                String useruri = view.getUseruriText().trim();
                String number = view.getNumber().trim();
                String skill = view.getSkill();

                Log.i("uri","" + imageUri);

                boolean cancel = true;

                String[] skillTypes = {"Android Development(Java)","Android Development(Kotlin)","Web Development(MEAN Stack)","Web Development(MARN Stack)","Web Development","Business Development","Content Writing","Campus Ambassadors","IOS Development","Hybrid App Development"};

                /*if (TextUtils.isEmpty(name)) {
                    view.setNameError(context.getString(R.string.error_field_required));
                    cancel = false;
                } else if (!ValidationUtil.isNameValid(name)) {
                    view.setNameError(context.getString(R.string.error_profile_name_length));
                    cancel = false;
                } else if (!Arrays.asList(skillTypes).contains(skill)){
                    view.setSkillError(context.getString(R.string.error_skill));
                    cancel = false;
                }*/

                if (cancel) {
                    view.showProgress();
                    profile.setUsertype(usertype);
                    profile.setUsername(name);
                    profile.setUserbio(bio);
                    profile.setUseruri(useruri);
                    profile.setPhoneNumber(number);
                    profile.setStatus("Not Hired");
                    profile.setSkill(skill);
                    createOrUpdateProfile(imageUri);
                }
            });
        }
    }

    public void createOrUpdateProfile(Uri imageUri) {
        profileManager.createOrUpdateProfile(profile, imageUri, success -> {
            ifViewAttached(view -> {
                view.hideProgress();
                if (success) {
                    onProfileUpdatedSuccessfully();
                } else {
                    view.showSnackBar(R.string.error_fail_create_profile);
                }
            });
        });
    }

    protected void onProfileUpdatedSuccessfully() {

        ifViewAttached(

                BaseView::finish
        );

    }

}
