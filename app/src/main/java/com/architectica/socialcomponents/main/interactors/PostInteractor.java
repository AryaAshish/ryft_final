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
import androidx.annotation.NonNull;
import android.util.Log;

import com.architectica.socialcomponents.model.Project;
import com.architectica.socialcomponents.utils.ValidationUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.architectica.socialcomponents.ApplicationHelper;
import com.architectica.socialcomponents.Constants;
import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.managers.DatabaseHelper;
import com.architectica.socialcomponents.managers.listeners.OnDataChangedListener;
import com.architectica.socialcomponents.managers.listeners.OnObjectExistListener;
import com.architectica.socialcomponents.managers.listeners.OnPostChangedListener;
import com.architectica.socialcomponents.managers.listeners.OnPostCreatedListener;
import com.architectica.socialcomponents.managers.listeners.OnRewardCreatedListener;
import com.architectica.socialcomponents.managers.listeners.OnProjectCreatedListener;
import com.architectica.socialcomponents.managers.listeners.OnPostListChangedListener;
import com.architectica.socialcomponents.managers.listeners.OnTaskCompleteListener;
import com.architectica.socialcomponents.model.Like;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.model.PostListResult;
import com.architectica.socialcomponents.utils.ImageUtil;
import com.architectica.socialcomponents.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alexey on 05.06.18.
 */

public class PostInteractor {

    private static final String TAG = PostInteractor.class.getSimpleName();
    private static PostInteractor instance;

    private ProfileInteractor profileInteractor;

    private DatabaseHelper databaseHelper;
    private Context context;

    String adminUid;

    public static PostInteractor getInstance(Context context) {
        if (instance == null) {
            instance = new PostInteractor(context);
        }

        return instance;
    }

    public PostInteractor(Context context) {
        this.context = context;
        databaseHelper = ApplicationHelper.getDatabaseHelper();

        profileInteractor = ProfileInteractor.getInstance(context);

    }

    public String generatePostId() {

        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POSTS_DB_KEY)
                .push()
                .getKey();

    }

    public String generateProjectId() {

        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.PROJECTS_DB_KEY)
                .push()
                .getKey();

    }

    public String generateRewardId() {

        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.REWARDS_DB_KEY)
                .push()
                .getKey();

    }


    public void createOrUpdatePost(Post post,List<String> hashtags) {
        try {

            uploadHashtagMap(post.getId(),hashtags);

            profileInteractor.addCredits(5);

            Map<String, Object> postValues = post.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/" + DatabaseHelper.POSTS_DB_KEY + "/" + post.getId(), postValues);

            databaseHelper.getDatabaseReference().updateChildren(childUpdates);


        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void uploadHashtagMap(String postid,List<String> hashtags){

        for (int i=0;i<hashtags.size();i++){

            Map<String,Object> item = new HashMap<>();

            item.put("postid",postid);

            databaseHelper.getDatabaseReference().child(DatabaseHelper.HASHTAGS_DB_KEY + "/" + hashtags.get(i)).push().setValue(item);

        }

    }

    public void createOrUpdateProject(Project post, List<String> hashtags) {
        try {

            uploadHashtagMap(post.getId(),hashtags);

            profileInteractor.addCredits(5);

            Map<String, Object> postValues = post.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/" + DatabaseHelper.PROJECTS_DB_KEY + "/" + post.getId(), postValues);

            databaseHelper.getDatabaseReference().updateChildren(childUpdates);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void createOrUpdateReward(Post post) {
        try {
            Map<String, Object> postValues = post.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/" + DatabaseHelper.REWARDS_DB_KEY + "/" + post.getId(), postValues);

            profileInteractor.addCredits(5);

            databaseHelper.getDatabaseReference().updateChildren(childUpdates);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public Task<Void> removePost(Post post) {

        DatabaseReference postRef;

        postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(post.getId());

        return postRef.removeValue();
    }

    public Task<Void> removeProject(Post post) {

        DatabaseReference postRef;

        postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROJECTS_DB_KEY).child(post.getId());

        return postRef.removeValue();
    }

    public Task<Void> removeReward(Post post) {

        DatabaseReference postRef;

        postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.REWARDS_DB_KEY).child(post.getId());

        return postRef.removeValue();
    }


    public void incrementWatchersCount(String postId) {
        DatabaseReference postRef;

        postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY + "/" + postId + "/watchersCount");

        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                LogUtil.logInfo(TAG, "Updating Watchers count transaction is completed.");
            }
        });
    }

    public void incrementProjectWatchersCount(String postId) {
        DatabaseReference postRef;

        postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROJECTS_DB_KEY + "/" + postId + "/watchersCount");

        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                LogUtil.logInfo(TAG, "Updating Watchers count transaction is completed.");
            }
        });
    }

    public void incrementRewardsWatchersCount(String postId) {
        DatabaseReference postRef;

        postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.REWARDS_DB_KEY + "/" + postId + "/watchersCount");

        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                LogUtil.logInfo(TAG, "Updating Watchers count transaction is completed.");
            }
        });
    }

    public void getPostList(final OnPostListChangedListener<Post> onDataChangedListener, long date) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
        Query postsQuery;
        if (date == 0) {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).orderByChild("createdDate");
        } else {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).endAt(date).orderByChild("createdDate");
        }

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> objectMap = (Map<String, Object>) dataSnapshot.getValue();
                PostListResult result = parsePostList(objectMap);

                if (result.getPosts().isEmpty() && result.isMoreDataAvailable()) {
                    getPostList(onDataChangedListener, result.getLastItemCreatedDate() - 1);
                } else {
                    onDataChangedListener.onListChanged(parsePostList(objectMap));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPostList(), onCancelled", new Exception(databaseError.getMessage()));
                onDataChangedListener.onCanceled(context.getString(R.string.permission_denied_error));
            }
        });
    }

    public void getAdminPostList(final OnPostListChangedListener<Post> onDataChangedListener, long date) {

        FirebaseDatabase.getInstance().getReference("profiles").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                    if ("RiftAdmin".equals(dataSnapshot1.child("username").getValue(String.class))){

                        adminUid = dataSnapshot1.getKey();

                    }

                }

                DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
                Query postsQuery;
                if (date == 0) {
                    postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).orderByChild("createdDate");
                } else {
                    postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).endAt(date).orderByChild("createdDate");
                }

                postsQuery.keepSynced(true);
                postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        List<Post> postList = new ArrayList<>();

                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){

                            if (adminUid.equals(snapshot.child("authorId").getValue(String.class))){

                                Post post = snapshot.getValue(Post.class);
                                post.setId(snapshot.getKey());

                                postList.add(post);

                            }

                        }

                        PostListResult result = new PostListResult();

                        result.setPosts(postList);

                        if (result.getPosts().isEmpty() && result.isMoreDataAvailable()) {
                            getAdminPostList(onDataChangedListener, result.getLastItemCreatedDate() - 1);
                        } else {
                            onDataChangedListener.onListChanged(result);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        LogUtil.logError(TAG, "getPostList(), onCancelled", new Exception(databaseError.getMessage()));
                        onDataChangedListener.onCanceled(context.getString(R.string.permission_denied_error));
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getProjectsList(final OnPostListChangedListener<Post> onDataChangedListener, long date) {

        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROJECTS_DB_KEY);
        Query postsQuery;
        if (date == 0) {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).orderByChild("createdDate");
        } else {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).endAt(date).orderByChild("createdDate");
        }

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Object> objectMap = (Map<String, Object>) dataSnapshot.getValue();
                PostListResult result = parseProjectList(objectMap);
                //PostListResult result = parsePostList(objectMap);

                if (result.getPosts().isEmpty() && result.isMoreDataAvailable()) {
                    getProjectsList(onDataChangedListener, result.getLastItemCreatedDate() - 1);
                } else {
                    onDataChangedListener.onListChanged(parseProjectList(objectMap));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPostList(), onCancelled", new Exception(databaseError.getMessage()));
                onDataChangedListener.onCanceled(context.getString(R.string.permission_denied_error));
            }
        });

    }

    public void getAllProjectsList(final OnPostListChangedListener<Post> onDataChangedListener, long date) {

        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROJECTS_DB_KEY);
        Query postsQuery;
        if (date == 0) {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).orderByChild("createdDate");
        } else {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).endAt(date).orderByChild("createdDate");
        }

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Post> postList = new ArrayList<>();

                for (DataSnapshot snapshot:dataSnapshot.getChildren()){

                    Post post = snapshot.getValue(Post.class);
                    post.setId(snapshot.getKey());

                    postList.add(post);

                }

                PostListResult result = new PostListResult();

                result.setPosts(postList);

                if (result.getPosts().isEmpty() && result.isMoreDataAvailable()) {
                    getAllProjectsList(onDataChangedListener, result.getLastItemCreatedDate() - 1);
                } else {
                    onDataChangedListener.onListChanged(result);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPostList(), onCancelled", new Exception(databaseError.getMessage()));
                onDataChangedListener.onCanceled(context.getString(R.string.permission_denied_error));
            }
        });

    }

    public void getPostListByUser(final OnDataChangedListener<Post> onDataChangedListener, String userId) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
        Query postsQuery;
        postsQuery = databaseReference.orderByChild("authorId").equalTo(userId);

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostListResult result = parsePostList((Map<String, Object>) dataSnapshot.getValue());
                onDataChangedListener.onListChanged(result.getPosts());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPostListByUser(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void getProjectsListByUser(final OnDataChangedListener<Post> onDataChangedListener, String userId) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROJECTS_DB_KEY);
        Query postsQuery;
        postsQuery = databaseReference.orderByChild("authorId").equalTo(userId);

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostListResult result = parseProjectList((Map<String, Object>) dataSnapshot.getValue());
                onDataChangedListener.onListChanged(result.getPosts());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPostListByUser(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public ValueEventListener getPost(final String id, final OnPostChangedListener listener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                        Post post = dataSnapshot.getValue(Post.class);
                        if (post != null) {
                            post.setId(id);
                        }
                        listener.onObjectChanged(post);
                    } else {
                        listener.onError(String.format(context.getString(R.string.error_general_post), id));
                    }
                } else {
                    listener.onObjectChanged(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public ValueEventListener getReward(final String id, final OnPostChangedListener listener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.REWARDS_DB_KEY).child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                        Post post = dataSnapshot.getValue(Post.class);
                        if (post != null) {
                            post.setId(id);
                        }
                        listener.onObjectChanged(post);
                    } else {
                        listener.onError(String.format(context.getString(R.string.error_general_post), id));
                    }
                } else {
                    listener.onObjectChanged(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }


    public ValueEventListener getProject(final String id, final OnPostChangedListener listener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROJECTS_DB_KEY).child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                        Post post = dataSnapshot.getValue(Post.class);
                        if (post != null) {
                            post.setId(id);
                        }
                        listener.onObjectChanged(post);
                    } else {
                        listener.onError(String.format(context.getString(R.string.error_general_post), id));
                    }
                } else {
                    listener.onObjectChanged(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public void getSinglePost(final String id, final OnPostChangedListener listener) {

        DatabaseReference databaseReference;

        databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.exists()) {
                    if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                        Post post = dataSnapshot.getValue(Post.class);
                        post.setId(id);
                        listener.onObjectChanged(post);
                    } else {
                        listener.onError(String.format(context.getString(R.string.error_general_post), id));
                    }
                } else {
                    listener.onError(context.getString(R.string.message_post_was_removed));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getSinglePost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void getSingleProject(final String id, final OnPostChangedListener listener) {

        DatabaseReference databaseReference;

        databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROJECTS_DB_KEY).child(id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.exists()) {
                    if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                        Post post = dataSnapshot.getValue(Post.class);
                        post.setId(id);
                        listener.onObjectChanged(post);
                    } else {
                        listener.onError(String.format(context.getString(R.string.error_general_post), id));
                    }
                } else {
                    listener.onError(context.getString(R.string.message_post_was_removed));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getSinglePost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void getSingleReward(final String id, final OnPostChangedListener listener) {

        DatabaseReference databaseReference;

        databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.REWARDS_DB_KEY).child(id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.exists()) {
                    if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                        Post post = dataSnapshot.getValue(Post.class);
                        post.setId(id);
                        listener.onObjectChanged(post);
                    } else {
                        listener.onError(String.format(context.getString(R.string.error_general_post), id));
                    }
                } else {
                    listener.onError(context.getString(R.string.message_post_was_removed));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getSinglePost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void getHashtagPostList(String hashtag,final OnPostListChangedListener<Post> onDataChangedListener, long date) {

        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                PostListResult result = new PostListResult();
                List<Post> list = new ArrayList<Post>();
                boolean isMoreDataAvailable = true;
                long lastItemCreatedDate = 0;

                List<String> hashtagpostids = new ArrayList<>();

                DataSnapshot dataSnapshot1 = dataSnapshot.child(DatabaseHelper.HASHTAGS_DB_KEY + "/" + hashtag);

                for (DataSnapshot snapshot : dataSnapshot1.getChildren()){

                    String postid = snapshot.child("postid").getValue(String.class);

                    hashtagpostids.add(postid);

                    DataSnapshot dataSnapshot2;

                    dataSnapshot2 = dataSnapshot.child(DatabaseHelper.POSTS_DB_KEY + "/" + postid);

                    if (dataSnapshot2.exists()){

                        Map<String,Object> mapObj = (Map<String, Object>) dataSnapshot2.getValue();

                        if (!isPostValid(mapObj)) {
                            LogUtil.logDebug(TAG, "Invalid post, id: " + postid);
                            continue;
                        }

                        boolean hasComplain = mapObj.containsKey("hasComplain") && (boolean) mapObj.get("hasComplain");
                        long createdDate = (long) mapObj.get("createdDate");

                        if (lastItemCreatedDate == 0 || lastItemCreatedDate > createdDate) {
                            lastItemCreatedDate = createdDate;
                        }

                        if (!hasComplain) {
                            Post post = new Post();
                            post.setId(postid);
                            post.setTitle((String) mapObj.get("title"));
                            post.setDescription((String) mapObj.get("description"));
                            post.setImageTitle((String) mapObj.get("imageTitle"));
                            post.setAuthorId((String) mapObj.get("authorId"));

                            if(mapObj.containsKey("contentType")){

                                post.setContentType((String) mapObj.get("contentType"));

                            }

                            post.setCreatedDate(createdDate);
                            if (mapObj.containsKey("commentsCount")) {
                                post.setCommentsCount((long) mapObj.get("commentsCount"));
                            }
                            if (mapObj.containsKey("likesCount")) {
                                post.setLikesCount((long) mapObj.get("likesCount"));
                            }
                            if (mapObj.containsKey("watchersCount")) {
                                post.setWatchersCount((long) mapObj.get("watchersCount"));
                            }

                            Log.i("hashtagposts",post.toString());

                            list.add(post);
                        }


                    }

                }

                isMoreDataAvailable = Constants.Post.POST_AMOUNT_ON_PAGE == hashtagpostids.size();

                Collections.sort(list, (lhs, rhs) -> ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate()));

                result.setPosts(list);
                result.setLastItemCreatedDate(lastItemCreatedDate);
                result.setMoreDataAvailable(isMoreDataAvailable);

                if (result.getPosts().isEmpty() && result.isMoreDataAvailable()) {
                    getHashtagPostList(hashtag,onDataChangedListener, result.getLastItemCreatedDate() - 1);
                } else {
                    onDataChangedListener.onListChanged(result);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getHashtagProjectList(String hashtag,final OnPostListChangedListener<Post> onDataChangedListener, long date) {

        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                PostListResult result = new PostListResult();
                List<Post> list = new ArrayList<Post>();
                boolean isMoreDataAvailable = true;
                long lastItemCreatedDate = 0;

                List<String> hashtagpostids = new ArrayList<>();

                DataSnapshot dataSnapshot1 = dataSnapshot.child(DatabaseHelper.HASHTAGS_DB_KEY + "/" + hashtag);

                for (DataSnapshot snapshot : dataSnapshot1.getChildren()){

                    String postid = snapshot.child("postid").getValue(String.class);

                    hashtagpostids.add(postid);

                    DataSnapshot dataSnapshot2;

                    dataSnapshot2 = dataSnapshot.child(DatabaseHelper.PROJECTS_DB_KEY + "/" + postid);

                    if (dataSnapshot2.exists()){

                        Map<String,Object> mapObj = (Map<String, Object>) dataSnapshot2.getValue();

                        if (!isPostValid(mapObj)) {
                            LogUtil.logDebug(TAG, "Invalid post, id: " + postid);
                            continue;
                        }

                        boolean hasComplain = mapObj.containsKey("hasComplain") && (boolean) mapObj.get("hasComplain");
                        long createdDate = (long) mapObj.get("createdDate");

                        if (lastItemCreatedDate == 0 || lastItemCreatedDate > createdDate) {
                            lastItemCreatedDate = createdDate;
                        }

                        if (!hasComplain) {
                            Post post = new Post();
                            post.setId(postid);
                            post.setTitle((String) mapObj.get("title"));
                            post.setDescription((String) mapObj.get("description"));
                            post.setImageTitle((String) mapObj.get("imageTitle"));
                            post.setAuthorId((String) mapObj.get("authorId"));

                            if(mapObj.containsKey("contentType")){

                                post.setContentType((String) mapObj.get("contentType"));

                            }

                            post.setCreatedDate(createdDate);
                            if (mapObj.containsKey("commentsCount")) {
                                post.setCommentsCount((long) mapObj.get("commentsCount"));
                            }
                            if (mapObj.containsKey("likesCount")) {
                                post.setLikesCount((long) mapObj.get("likesCount"));
                            }
                            if (mapObj.containsKey("watchersCount")) {
                                post.setWatchersCount((long) mapObj.get("watchersCount"));
                            }

                            Log.i("hashtagposts",post.toString());

                            list.add(post);
                        }

                    }

                }

                isMoreDataAvailable = Constants.Post.POST_AMOUNT_ON_PAGE == hashtagpostids.size();

                Collections.sort(list, (lhs, rhs) -> ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate()));

                result.setPosts(list);
                result.setLastItemCreatedDate(lastItemCreatedDate);
                result.setMoreDataAvailable(isMoreDataAvailable);

                if (result.getPosts().isEmpty() && result.isMoreDataAvailable()) {
                    getHashtagPostList(hashtag,onDataChangedListener, result.getLastItemCreatedDate() - 1);
                } else {
                    onDataChangedListener.onListChanged(result);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getRewardsPostList(final OnPostListChangedListener<Post> onDataChangedListener, long date) {

        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.REWARDS_DB_KEY);
        Query postsQuery;
        if (date == 0) {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).orderByChild("createdDate");
        } else {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).endAt(date).orderByChild("createdDate");
        }

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> objectMap = (Map<String, Object>) dataSnapshot.getValue();
                PostListResult result = parsePostList(objectMap);

                if (result.getPosts().isEmpty() && result.isMoreDataAvailable()) {
                    getPostList(onDataChangedListener, result.getLastItemCreatedDate() - 1);
                } else {
                    onDataChangedListener.onListChanged(parsePostList(objectMap));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPostList(), onCancelled", new Exception(databaseError.getMessage()));
                onDataChangedListener.onCanceled(context.getString(R.string.permission_denied_error));
            }
        });

    }

    private PostListResult parseProjectList(Map<String, Object> objectMap) {
        PostListResult result = new PostListResult();
        List<Post> list = new ArrayList<Post>();
        boolean isMoreDataAvailable = true;
        long lastItemCreatedDate = 0;

        if (objectMap != null) {
            isMoreDataAvailable = Constants.Post.POST_AMOUNT_ON_PAGE == objectMap.size();

            for (String key : objectMap.keySet()) {
                Object obj = objectMap.get(key);
                if (obj instanceof Map) {
                    Map<String, Object> mapObj = (Map<String, Object>) obj;

                    if (!isPostValid(mapObj)) {
                        LogUtil.logDebug(TAG, "Invalid post, id: " + key);
                        continue;
                    }

                    boolean hasComplain = mapObj.containsKey("hasComplain") && (boolean) mapObj.get("hasComplain");
                    long createdDate = (long) mapObj.get("createdDate");

                    if (lastItemCreatedDate == 0 || lastItemCreatedDate > createdDate) {
                        lastItemCreatedDate = createdDate;
                    }

                    String isVerified = "";

                    if (mapObj.containsKey("status")){

                        isVerified = mapObj.get("status").toString();

                    }

                    if (!hasComplain) {

                        if (isVerified.equals("verified")){

                            Post post = new Post();
                            post.setId(key);
                            post.setTitle((String) mapObj.get("title"));
                            post.setDescription((String) mapObj.get("description"));
                            post.setImageTitle((String) mapObj.get("imageTitle"));
                            post.setAuthorId((String) mapObj.get("authorId"));

                            if(mapObj.containsKey("contentType")){

                                post.setContentType((String) mapObj.get("contentType"));

                            }

                            post.setCreatedDate(createdDate);
                            if (mapObj.containsKey("commentsCount")) {
                                post.setCommentsCount((long) mapObj.get("commentsCount"));
                            }
                            if (mapObj.containsKey("likesCount")) {
                                post.setLikesCount((long) mapObj.get("likesCount"));
                            }
                            if (mapObj.containsKey("watchersCount")) {
                                post.setWatchersCount((long) mapObj.get("watchersCount"));
                            }
                            list.add(post);

                        }
                    }
                }
            }

            Collections.sort(list, (lhs, rhs) -> ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate()));

            result.setPosts(list);
            result.setLastItemCreatedDate(lastItemCreatedDate);
            result.setMoreDataAvailable(isMoreDataAvailable);
        }

        return result;
    }

    private PostListResult parsePostList(Map<String, Object> objectMap) {
        PostListResult result = new PostListResult();
        List<Post> list = new ArrayList<Post>();
        boolean isMoreDataAvailable = true;
        long lastItemCreatedDate = 0;

        if (objectMap != null) {
            isMoreDataAvailable = Constants.Post.POST_AMOUNT_ON_PAGE == objectMap.size();

            for (String key : objectMap.keySet()) {
                Object obj = objectMap.get(key);
                if (obj instanceof Map) {
                    Map<String, Object> mapObj = (Map<String, Object>) obj;

                    if (!isPostValid(mapObj)) {
                        LogUtil.logDebug(TAG, "Invalid post, id: " + key);
                        continue;
                    }

                    boolean hasComplain = mapObj.containsKey("hasComplain") && (boolean) mapObj.get("hasComplain");
                    long createdDate = (long) mapObj.get("createdDate");

                    if (lastItemCreatedDate == 0 || lastItemCreatedDate > createdDate) {
                        lastItemCreatedDate = createdDate;
                    }

                    if (!hasComplain) {
                        Post post = new Post();
                        post.setId(key);
                        post.setTitle((String) mapObj.get("title"));
                        post.setDescription((String) mapObj.get("description"));
                        post.setImageTitle((String) mapObj.get("imageTitle"));
                        post.setAuthorId((String) mapObj.get("authorId"));

                        if(mapObj.containsKey("contentType")){

                            post.setContentType((String) mapObj.get("contentType"));

                        }

                        post.setCreatedDate(createdDate);
                        if (mapObj.containsKey("commentsCount")) {
                            post.setCommentsCount((long) mapObj.get("commentsCount"));
                        }
                        if (mapObj.containsKey("likesCount")) {
                            post.setLikesCount((long) mapObj.get("likesCount"));
                        }
                        if (mapObj.containsKey("watchersCount")) {
                            post.setWatchersCount((long) mapObj.get("watchersCount"));
                        }
                        list.add(post);
                    }
                }
            }

            Collections.sort(list, (lhs, rhs) -> ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate()));

            result.setPosts(list);
            result.setLastItemCreatedDate(lastItemCreatedDate);
            result.setMoreDataAvailable(isMoreDataAvailable);
        }

        return result;
    }

    private boolean isPostValid(Map<String, Object> post) {
        return post.containsKey("title")
                && post.containsKey("authorId")
                && post.containsKey("description");
    }

    public void addComplainToPost(Post post) {

        databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(post.getId()).child("hasComplain").setValue(true);

    }

    public void addComplainToProject(Post post) {

        databaseHelper.getDatabaseReference().child(DatabaseHelper.PROJECTS_DB_KEY).child(post.getId()).child("hasComplain").setValue(true);

    }

    public void addComplainToReward(Post post) {

        databaseHelper.getDatabaseReference().child(DatabaseHelper.REWARDS_DB_KEY).child(post.getId()).child("hasComplain").setValue(true);

    }


    public void isPostExistSingleValue(String postId, final OnObjectExistListener<Post> onObjectExistListener) {

        DatabaseReference databaseReference;

        databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(postId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "isPostExistSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void isProjectExistSingleValue(String postId, final OnObjectExistListener<Post> onObjectExistListener) {

        DatabaseReference databaseReference;

        databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROJECTS_DB_KEY).child(postId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "isPostExistSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void isRewardExistSingleValue(String postId, final OnObjectExistListener<Post> onObjectExistListener) {

        DatabaseReference databaseReference;

        databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.REWARDS_DB_KEY).child(postId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "isPostExistSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void subscribeToNewPosts() {
        FirebaseMessaging.getInstance().subscribeToTopic("postsTopic");
    }

    public void removePost(final Post post, final OnTaskCompleteListener onTaskCompleteListener) {

        final DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        Task<Void> removeImageTask = databaseHelper.removeImage(post.getImageTitle());


        removeImageTask.addOnSuccessListener(aVoid -> {
            removePost(post).addOnCompleteListener(task -> {
                onTaskCompleteListener.onTaskComplete(task.isSuccessful());
                ProfileInteractor.getInstance(context).updateProfileLikeCountAfterRemovingPost(post);
                removeObjectsRelatedToPost(post.getId());
                LogUtil.logDebug(TAG, "removePost(), is success: " + task.isSuccessful());
            });
            LogUtil.logDebug(TAG, "removeImage(): success");
        }).addOnFailureListener(exception -> {
            LogUtil.logError(TAG, "removeImage()", exception);
            onTaskCompleteListener.onTaskComplete(false);
        });
    }

    public void removeProject(final Post post, final OnTaskCompleteListener onTaskCompleteListener) {

        final DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        Task<Void> removeImageTask = databaseHelper.removeImage(post.getImageTitle());


        removeImageTask.addOnSuccessListener(aVoid -> {
            removeProject(post).addOnCompleteListener(task -> {
                onTaskCompleteListener.onTaskComplete(task.isSuccessful());
                ProfileInteractor.getInstance(context).updateProfileLikeCountAfterRemovingPost(post);
                removeObjectsRelatedToPost(post.getId());
                LogUtil.logDebug(TAG, "removePost(), is success: " + task.isSuccessful());
            });
            LogUtil.logDebug(TAG, "removeImage(): success");
        }).addOnFailureListener(exception -> {
            LogUtil.logError(TAG, "removeImage()", exception);
            onTaskCompleteListener.onTaskComplete(false);
        });
    }

    private void removeObjectsRelatedToPost(final String postId) {
        CommentInteractor.getInstance(context).removeCommentsByPost(postId).addOnSuccessListener(aVoid -> LogUtil.logDebug(TAG, "Comments related to post with id: " + postId + " was removed")).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LogUtil.logError(TAG, "Failed to remove comments related to post with id: " + postId, e);
            }
        });

        removeLikesByPost(postId).addOnSuccessListener(aVoid -> LogUtil.logDebug(TAG, "Likes related to post with id: " + postId + " was removed")).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LogUtil.logError(TAG, "Failed to remove likes related to post with id: " + postId, e);
            }
        });
    }

    public void createOrUpdatePostWithImage(Uri imageUri, final OnPostCreatedListener onPostCreatedListener, final Post post,List<String> hashtags) {
        // Register observers to listen for when the download is done or if it fails

        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        if (post.getId() == null) {
            post.setId(generatePostId());
        }

        String contentType = ValidationUtil.getMimeType(imageUri,context);

        final String imageTitle = ImageUtil.generatePostImageTitle(post.getId());
        UploadTask uploadTask = databaseHelper.uploadImage(imageUri, imageTitle, contentType);

        if (uploadTask != null) {
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
                onPostCreatedListener.onPostSaved(false);

            }).addOnSuccessListener(taskSnapshot -> {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                post.setImageTitle(imageTitle);
                post.setContentType(contentType);
                createOrUpdatePost(post,hashtags);

                onPostCreatedListener.onPostSaved(true);
            });
        }

    }

    public void createPost(final OnPostCreatedListener onPostCreatedListener, final Post post,List<String> hashtags){

        if (post.getId() == null) {
            post.setId(generatePostId());
        }

        post.setImageTitle("");
        post.setContentType("text");
        createOrUpdatePost(post,hashtags);

        onPostCreatedListener.onPostSaved(true);

    }

    public void createOrUpdateRewardWithImage(Uri imageUri, final OnRewardCreatedListener onPostCreatedListener, final Post post) {
        // Register observers to listen for when the download is done or if it fails

        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        if (post.getId() == null) {
            post.setId(generateRewardId());
        }

        String contentType = ValidationUtil.getMimeType(imageUri,context);

        final String imageTitle = ImageUtil.generatePostImageTitle(post.getId());
        UploadTask uploadTask = databaseHelper.uploadImage(imageUri, imageTitle, contentType);

        if (uploadTask != null) {
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
                onPostCreatedListener.onRewardSaved(false);

            }).addOnSuccessListener(taskSnapshot -> {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                post.setImageTitle(imageTitle);
                post.setContentType(contentType);
                createOrUpdateReward(post);

                onPostCreatedListener.onRewardSaved(true);
            });
        }

    }

    public void createReward(final OnRewardCreatedListener onPostCreatedListener, final Post post){

        if (post.getId() == null) {
            post.setId(generateRewardId());
        }

        post.setImageTitle("");
        post.setContentType("text");
        createOrUpdateReward(post);

        onPostCreatedListener.onRewardSaved(true);

    }


    public void createOrUpdateProjectWithImage(Uri imageUri, final OnProjectCreatedListener onPostCreatedListener, final Project post,List<String> hashtags) {
        // Register observers to listen for when the download is done or if it fails

        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        if (post.getId() == null) {
            post.setId(generatePostId());
        }

        String contentType = ValidationUtil.getMimeType(imageUri,context);

        final String imageTitle = ImageUtil.generatePostImageTitle(post.getId());
        UploadTask uploadTask = databaseHelper.uploadImage(imageUri, imageTitle, contentType);

        if (uploadTask != null) {
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
                onPostCreatedListener.onProjectSaved(false);

            }).addOnSuccessListener(taskSnapshot -> {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                post.setImageTitle(imageTitle);
                post.setContentType(contentType);
                createOrUpdateProject(post,hashtags);

                onPostCreatedListener.onProjectSaved(true);
            });
        }

    }

    public void createProject(final OnProjectCreatedListener onPostCreatedListener, final Project post,List<String> hashtags){

        if (post.getId() == null) {
            post.setId(generateRewardId());
        }

        post.setImageTitle("");
        post.setContentType("text");
        createOrUpdateProject(post,hashtags);

        onPostCreatedListener.onProjectSaved(true);

    }

    public void createOrUpdateLike(final String postId, final String postAuthorId) {
        try {
            String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference mLikesReference;

            mLikesReference = databaseHelper
                    .getDatabaseReference()
                    .child(DatabaseHelper.POST_LIKES_DB_KEY)
                    .child(postId)
                    .child(authorId);


            mLikesReference.push();
            String id = mLikesReference.push().getKey();
            Like like = new Like(authorId);
            like.setId(id);

            mLikesReference.child(id).setValue(like, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        DatabaseReference postRef;

                        postRef = databaseHelper
                                .getDatabaseReference()
                                .child(DatabaseHelper.POSTS_DB_KEY + "/" + postId + "/likesCount");

                        incrementLikesCount(postRef);
                        DatabaseReference profileRef = databaseHelper
                                .getDatabaseReference()
                                .child(DatabaseHelper.PROFILES_DB_KEY + "/" + postAuthorId + "/likesCount");

                        incrementLikesCount(profileRef);

                        profileInteractor.addCredits(1);

                    } else {
                        LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                    }
                }

                private void incrementLikesCount(DatabaseReference postRef) {
                    postRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Integer currentValue = mutableData.getValue(Integer.class);
                            if (currentValue == null) {
                                mutableData.setValue(1);
                            } else {
                                mutableData.setValue(currentValue + 1);
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                        }
                    });
                }

            });
        } catch (Exception e) {
            LogUtil.logError(TAG, "createOrUpdateLike()", e);
        }

    }

    public void createOrUpdateProjectLike(final String postId, final String postAuthorId) {
        try {
            String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference mLikesReference;

            mLikesReference = databaseHelper
                    .getDatabaseReference()
                    .child(DatabaseHelper.PROJECT_LIKES_DB_KEY)
                    .child(postId)
                    .child(authorId);

            mLikesReference.push();
            String id = mLikesReference.push().getKey();
            Like like = new Like(authorId);
            like.setId(id);

            mLikesReference.child(id).setValue(like, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        DatabaseReference postRef;

                        postRef = databaseHelper
                                .getDatabaseReference()
                                .child(DatabaseHelper.PROJECTS_DB_KEY + "/" + postId + "/likesCount");

                        incrementLikesCount(postRef);
                        DatabaseReference profileRef = databaseHelper
                                .getDatabaseReference()
                                .child(DatabaseHelper.PROFILES_DB_KEY + "/" + postAuthorId + "/likesCount");

                        incrementLikesCount(profileRef);

                        profileInteractor.addCredits(1);

                    } else {
                        LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                    }
                }

                private void incrementLikesCount(DatabaseReference postRef) {
                    postRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Integer currentValue = mutableData.getValue(Integer.class);
                            if (currentValue == null) {
                                mutableData.setValue(1);
                            } else {
                                mutableData.setValue(currentValue + 1);
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                        }
                    });
                }

            });
        } catch (Exception e) {
            LogUtil.logError(TAG, "createOrUpdateLike()", e);
        }

    }

    public void removeLike(final String postId, final String postAuthorId) {
        String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference mLikesReference;

        mLikesReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .child(authorId);

        mLikesReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    DatabaseReference postRef;

                    postRef = databaseHelper
                            .getDatabaseReference()
                            .child(DatabaseHelper.POSTS_DB_KEY + "/" + postId + "/likesCount");
                    decrementLikesCount(postRef);

                    DatabaseReference profileRef = databaseHelper
                            .getDatabaseReference()
                            .child(DatabaseHelper.PROFILES_DB_KEY + "/" + postAuthorId + "/likesCount");
                    decrementLikesCount(profileRef);
                } else {
                    LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                }
            }

            private void decrementLikesCount(DatabaseReference postRef) {
                postRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Long currentValue = mutableData.getValue(Long.class);
                        if (currentValue == null) {
                            mutableData.setValue(0);
                        } else {
                            mutableData.setValue(currentValue - 1);
                        }

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                    }
                });
            }
        });
    }

    public void removeProjectLike(final String postId, final String postAuthorId) {
        String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference mLikesReference;

        mLikesReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.PROJECT_LIKES_DB_KEY)
                .child(postId)
                .child(authorId);

        mLikesReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    DatabaseReference postRef;

                    postRef = databaseHelper
                            .getDatabaseReference()
                            .child(DatabaseHelper.PROJECTS_DB_KEY + "/" + postId + "/likesCount");
                    decrementLikesCount(postRef);

                    DatabaseReference profileRef = databaseHelper
                            .getDatabaseReference()
                            .child(DatabaseHelper.PROFILES_DB_KEY + "/" + postAuthorId + "/likesCount");
                    decrementLikesCount(profileRef);
                } else {
                    LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                }
            }

            private void decrementLikesCount(DatabaseReference postRef) {
                postRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Long currentValue = mutableData.getValue(Long.class);
                        if (currentValue == null) {
                            mutableData.setValue(0);
                        } else {
                            mutableData.setValue(currentValue - 1);
                        }

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                    }
                });
            }
        });
    }

    public ValueEventListener hasCurrentUserLike(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference;

        databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .child(userId);

        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLike(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public ValueEventListener hasCurrentUserProjectLike(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference;

        databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.PROJECT_LIKES_DB_KEY)
                .child(postId)
                .child(userId);


        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLike(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }


    public void hasCurrentUserLikeSingleValue(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference;

        databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLikeSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void hasCurrentUserProjectLikeSingleValue(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference;

        databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.PROJECT_LIKES_DB_KEY)
                .child(postId)
                .child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLikeSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }


    public Task<Void> removeLikesByPost(String postId) {

        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .removeValue();

    }

    public Task<Void> removeLikesByProject(String postId) {

        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.PROJECT_LIKES_DB_KEY)
                .child(postId)
                .removeValue();


    }


    public ValueEventListener searchPostsByTitle(String searchText, OnDataChangedListener<Post> onDataChangedListener) {
        DatabaseReference reference;

        reference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);

        ValueEventListener valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                PostListResult result = new PostListResult();
                List<Post> list = new ArrayList<Post>();
                boolean isMoreDataAvailable = true;
                long lastItemCreatedDate = 0;

                isMoreDataAvailable = Constants.Post.POST_AMOUNT_ON_PAGE == dataSnapshot.getChildrenCount();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                    if (dataSnapshot1.hasChild("title")){

                        String title = dataSnapshot1.child("title").getValue(String.class).toLowerCase();

                        String searchsmall = searchText.toLowerCase();

                        if (title.contains(searchsmall)){

                            Map<String,Object> mapObj = (Map<String, Object>) dataSnapshot1.getValue();

                            if (!isPostValid(mapObj)) {
                                LogUtil.logDebug(TAG, "Invalid post, id: " + mapObj);
                                continue;
                            }

                            boolean hasComplain = mapObj.containsKey("hasComplain") && (boolean) mapObj.get("hasComplain");
                            long createdDate = (long) mapObj.get("createdDate");

                            if (lastItemCreatedDate == 0 || lastItemCreatedDate > createdDate) {
                                lastItemCreatedDate = createdDate;
                            }

                            if (!hasComplain) {
                                Post post = new Post();
                                post.setId((String) dataSnapshot1.getKey());
                                post.setTitle((String) mapObj.get("title"));
                                post.setDescription((String) mapObj.get("description"));
                                post.setImageTitle((String) mapObj.get("imageTitle"));
                                post.setAuthorId((String) mapObj.get("authorId"));

                                if(mapObj.containsKey("contentType")){

                                    post.setContentType((String) mapObj.get("contentType"));

                                }

                                post.setCreatedDate(createdDate);
                                if (mapObj.containsKey("commentsCount")) {
                                    post.setCommentsCount((long) mapObj.get("commentsCount"));
                                }
                                if (mapObj.containsKey("likesCount")) {
                                    post.setLikesCount((long) mapObj.get("likesCount"));
                                }
                                if (mapObj.containsKey("watchersCount")) {
                                    post.setWatchersCount((long) mapObj.get("watchersCount"));
                                }

                                list.add(post);
                            }

                        }

                    }

                }

                Collections.sort(list, (lhs, rhs) -> ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate()));

                result.setPosts(list);
                result.setLastItemCreatedDate(lastItemCreatedDate);
                result.setMoreDataAvailable(isMoreDataAvailable);

                onDataChangedListener.onListChanged(result.getPosts());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "searchPostsByTitle(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, reference);

        return valueEventListener;
    }

    public ValueEventListener filterPostsByLikes(int  limit, OnDataChangedListener<Post> onDataChangedListener) {
        DatabaseReference reference;

        reference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);

        ValueEventListener valueEventListener = getFilteredQuery(reference,"likesCount", limit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostListResult result = parsePostList((Map<String, Object>) dataSnapshot.getValue());
                onDataChangedListener.onListChanged(result.getPosts());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "filterPostsByLikes(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, reference);

        return valueEventListener;
    }

    public ValueEventListener filterProjectsByLikes(int  limit, OnDataChangedListener<Post> onDataChangedListener) {
        DatabaseReference reference;

        reference = databaseHelper.getDatabaseReference().child(DatabaseHelper.PROJECTS_DB_KEY);

        ValueEventListener valueEventListener = getFilteredQuery(reference,"likesCount", limit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostListResult result = parsePostList((Map<String, Object>) dataSnapshot.getValue());
                onDataChangedListener.onListChanged(result.getPosts());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "filterPostsByLikes(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, reference);

        return valueEventListener;
    }

    public ValueEventListener filterRewardsByLikes(int  limit, OnDataChangedListener<Post> onDataChangedListener) {
        DatabaseReference reference;

        reference = databaseHelper.getDatabaseReference().child(DatabaseHelper.REWARDS_DB_KEY);

        ValueEventListener valueEventListener = getFilteredQuery(reference,"likesCount", limit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostListResult result = parsePostList((Map<String, Object>) dataSnapshot.getValue());
                onDataChangedListener.onListChanged(result.getPosts());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "filterPostsByLikes(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, reference);

        return valueEventListener;
    }



    private Query getSearchQuery(DatabaseReference databaseReference, String childOrderBy, String searchText) {
        return databaseReference
                .orderByChild(childOrderBy)
                .startAt(searchText)
                .endAt(searchText + "\uf8ff");
    }

    private Query getFilteredQuery(DatabaseReference databaseReference, String childOrderBy, int limit) {
        return databaseReference
                .orderByChild(childOrderBy)
                .limitToLast(limit);
    }

    public StorageReference getMediumImageStorageRef(String imageTitle) {
        return databaseHelper.getMediumImageStorageRef(imageTitle);
    }

    public StorageReference getOriginImageStorageRef(String imageTitle) {
        return databaseHelper.getOriginImageStorageRef(imageTitle);
    }

    public StorageReference getSmallImageStorageRef(String imageTitle) {
        return databaseHelper.getSmallImageStorageRef(imageTitle);
    }
}
