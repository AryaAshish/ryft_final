/*
 *
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
 *
 */

package com.architectica.socialcomponents.main.interactors;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.architectica.socialcomponents.model.Project;
import com.architectica.socialcomponents.utils.ValidationUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.UploadTask;
import com.architectica.socialcomponents.ApplicationHelper;
import com.architectica.socialcomponents.enums.UploadImagePrefix;
import com.architectica.socialcomponents.managers.DatabaseHelper;
import com.architectica.socialcomponents.managers.listeners.OnDataChangedListener;
import com.architectica.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.architectica.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.architectica.socialcomponents.managers.listeners.OnObjectExistListener;
import com.architectica.socialcomponents.managers.listeners.OnProfileCreatedListener;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.model.Profile;
import com.architectica.socialcomponents.utils.ImageUtil;
import com.architectica.socialcomponents.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexey on 05.06.18.
 */

public class ProfileInteractor {

    private static final String TAG = ProfileInteractor.class.getSimpleName();
    private static ProfileInteractor instance;

    private DatabaseHelper databaseHelper;
    private Context context;

    public static ProfileInteractor getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileInteractor(context);
        }

        return instance;
    }

    private ProfileInteractor(Context context) {
        this.context = context;
        databaseHelper = ApplicationHelper.getDatabaseHelper();
    }

    public void createOrUpdateProfile(final Profile profile, final OnProfileCreatedListener onProfileCreatedListener) {

        //Log.i("id","" + profile.getId());

        Task<Void> task = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.PROFILES_DB_KEY)
                .child(profile.getId())
                .setValue(profile);
        task.addOnCompleteListener(task1 -> {
            onProfileCreatedListener.onProfileCreated(task1.isSuccessful());
            //Log.i("task","" + task1.isSuccessful());
            //Log.i("token","" + FirebaseInstanceId.getInstance().getToken());
            addRegistrationToken(FirebaseInstanceId.getInstance().getToken(), profile.getId());
            LogUtil.logDebug(TAG, "createOrUpdateProfile, success: " + task1.isSuccessful());
        });
    }

    public void createOrUpdateProfileWithImage(final Profile profile, Uri imageUri, final OnProfileCreatedListener onProfileCreatedListener) {
        String imageTitle = ImageUtil.generateImageTitle(UploadImagePrefix.PROFILE, profile.getId());

        String contentType = ValidationUtil.getMimeType(imageUri,context);
        //String contentType = "image";

        UploadTask uploadTask = databaseHelper.uploadImage(imageUri, imageTitle, contentType);

        if (uploadTask != null) {
            uploadTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Uri downloadUrl = task1.getResult();
                            LogUtil.logDebug(TAG, "successful upload image, image url: " + String.valueOf(downloadUrl));

                            profile.setPhotoUrl(downloadUrl.toString());
                            createOrUpdateProfile(profile, onProfileCreatedListener);
                        } else {
                            onProfileCreatedListener.onProfileCreated(false);
                            LogUtil.logDebug(TAG, "createOrUpdateProfileWithImage, fail to getDownloadUrl");
                        }
                    });
                } else {
                    onProfileCreatedListener.onProfileCreated(false);
                    LogUtil.logDebug(TAG, "createOrUpdateProfileWithImage, fail to upload image");
                }

            });
        } else {
            onProfileCreatedListener.onProfileCreated(false);
            LogUtil.logDebug(TAG, "fail to upload image");
        }
    }

    public void isProfileExist(String id, final OnObjectExistListener<Profile> onObjectExistListener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child("profiles").child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public ValueEventListener getProfile(String id, final OnObjectChangedListener<Profile> listener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROFILES_DB_KEY).child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild("credits")){

                    dataSnapshot.getRef().child("credits").setValue(0);

                }

                Profile profile = dataSnapshot.getValue(Profile.class);
                listener.onObjectChanged(profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onError(databaseError.getMessage());
                LogUtil.logError(TAG, "getProfile(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public void getProfileSingleValue(String id, final OnObjectChangedListener<Profile> listener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROFILES_DB_KEY).child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild("credits")){

                    dataSnapshot.getRef().child("credits").setValue(0);

                }

                Profile profile = dataSnapshot.getValue(Profile.class);
                listener.onObjectChanged(profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onError(databaseError.getMessage());
                LogUtil.logError(TAG, "getProfileSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void updateProfileLikeCountAfterRemovingPost(Post post) {
        DatabaseReference profileRef = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.PROFILES_DB_KEY + "/" + post.getAuthorId() + "/likesCount");
        final long likesByPostCount = post.getLikesCount();

        profileRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue != null && currentValue >= likesByPostCount) {
                    mutableData.setValue(currentValue - likesByPostCount);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
            }
        });

    }

    public void addCredits(long credits){

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        DatabaseReference profileRef = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.PROFILES_DB_KEY + "/" + uid + "/credits");

        profileRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(credits);
                } else {
                    mutableData.setValue(currentValue + credits);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                LogUtil.logInfo(TAG, "Updating credits count transaction is completed.");
            }
        });

    }

    public void updateProfileLikeCountAfterRemovingProject(Project post) {
        DatabaseReference profileRef = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.PROFILES_DB_KEY + "/" + post.getAuthorId() + "/likesCount");
        final long likesByPostCount = post.getLikesCount();

        profileRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue != null && currentValue >= likesByPostCount) {
                    mutableData.setValue(currentValue - likesByPostCount);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
            }
        });

    }

    public void addRegistrationToken(String token, String userId) {
        Task<Void> task = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.PROFILES_DB_KEY)
                .child(userId).child("notificationTokens")
                .child(token).setValue(true);
        task.addOnCompleteListener(task1 -> LogUtil.logDebug(TAG, "addRegistrationToken, success: " + task1.isSuccessful()));
    }

    public void updateRegistrationToken(final String token) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            final String currentUserId = firebaseUser.getUid();

            getProfileSingleValue(currentUserId, new OnObjectChangedListenerSimple<Profile>() {
                @Override
                public void onObjectChanged(Profile obj) {
                    if(obj != null) {
                        addRegistrationToken(token, currentUserId);
                    } else {
                        LogUtil.logError(TAG, "updateRegistrationToken",
                                new RuntimeException("Profile is not found"));
                    }
                }
            });
        }
    }

    public void removeRegistrationToken(String token, String userId) {
        DatabaseReference databaseReference = ApplicationHelper.getDatabaseHelper().getDatabaseReference();
        DatabaseReference tokenRef = databaseReference.child(DatabaseHelper.PROFILES_DB_KEY).child(userId).child("notificationTokens").child(token);
        Task<Void> task = tokenRef.removeValue();
        task.addOnCompleteListener(task1 -> LogUtil.logDebug(TAG, "removeRegistrationToken, success: " + task1.isSuccessful()));
    }

    public ValueEventListener searchProfiles(String searchText, OnDataChangedListener<Profile> onDataChangedListener) {
        DatabaseReference reference = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROFILES_DB_KEY);

        ValueEventListener valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Profile> list = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    if (snapshot.hasChild("username")){

                        String usernamesmall = snapshot.child("username").getValue(String.class).toLowerCase();

                        String searchsmall = searchText.toLowerCase();

                        if (usernamesmall.contains(searchsmall)){

                            Profile profile = snapshot.getValue(Profile.class);
                            list.add(profile);

                        }

                    }
                }

                onDataChangedListener.onListChanged(list);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*ValueEventListener valueEventListener = getSearchQuery(reference, "username", searchText).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Profile> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Profile profile = snapshot.getValue(Profile.class);
                    list.add(profile);
                }
                onDataChangedListener.onListChanged(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "searchProfiles(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });*/

        databaseHelper.addActiveListener(valueEventListener, reference);
        return valueEventListener;
    }

    public ValueEventListener searchProfilesBySkill(String searchText, OnDataChangedListener<Profile> onDataChangedListener) {
        DatabaseReference reference = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROFILES_DB_KEY);

        ValueEventListener valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Profile> list = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    if (snapshot.hasChild("skill")){

                        String skillsmall = snapshot.child("skill").getValue(String.class).toLowerCase();

                        String searchsmall = searchText.toLowerCase();

                        if (skillsmall.contains(searchsmall)){

                            Profile profile = snapshot.getValue(Profile.class);
                            list.add(profile);

                        }

                    }

                }

                onDataChangedListener.onListChanged(list);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*ValueEventListener valueEventListener = getSearchQuery(reference, "skill", searchText).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Profile> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Profile profile = snapshot.getValue(Profile.class);
                    list.add(profile);
                }
                onDataChangedListener.onListChanged(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "searchProfiles(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });*/

        databaseHelper.addActiveListener(valueEventListener, reference);
        return valueEventListener;
    }


    private Query getSearchQuery(DatabaseReference databaseReference, String childOrderBy, String searchText) {
        return databaseReference
                .orderByChild(childOrderBy)
                .startAt(searchText)
                .endAt(searchText + "\uf8ff");
    }
}
