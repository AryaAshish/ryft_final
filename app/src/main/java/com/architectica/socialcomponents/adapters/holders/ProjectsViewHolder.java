/*
 *  Copyright 2017 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.architectica.socialcomponents.adapters.holders;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import com.architectica.socialcomponents.main.Hashtags.HashtagPostsActivity;
import com.architectica.socialcomponents.main.Hashtags.HashtagProjectsActivity;
import com.architectica.socialcomponents.main.interactors.PostInteractor;
import com.architectica.socialcomponents.managers.DatabaseHelper;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.architectica.socialcomponents.Constants;
import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.controllers.LikeController;
import com.architectica.socialcomponents.main.base.BaseActivity;
import com.architectica.socialcomponents.managers.PostManager;
import com.architectica.socialcomponents.managers.ProfileManager;
import com.architectica.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.architectica.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.architectica.socialcomponents.managers.listeners.OnObjectExistListener;
import com.architectica.socialcomponents.model.Like;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.model.Profile;
import com.architectica.socialcomponents.utils.FormatterUtil;
import com.architectica.socialcomponents.utils.GlideApp;
import com.architectica.socialcomponents.utils.ImageUtil;
import com.google.firebase.storage.StorageReference;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.hendraanggrian.appcompat.widget.SocialView;

import java.io.IOException;

/**
 * Created by alexey on 27.12.16.
 */

public class ProjectsViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = PostViewHolder.class.getSimpleName();

    private LinearLayout countersLayout;
    protected Context context;
    private ImageView postImageView;
    private SimpleExoPlayer exoPlayer;
    private PlayerView postVideoView;
    private TextView titleTextView;
    private SocialTextView detailsTextView;
    private TextView likeCounterTextView,userName,userSkill;
    private ImageView likesImageView;
    private TextView commentsCountTextView;
    private TextView watcherCounterTextView;
    private TextView dateTextView;
    private ImageView authorImageView;
    private ViewGroup likeViewGroup;

    private ProfileManager profileManager;
    protected PostManager postManager;
    protected PostInteractor postInteractor;

    private LikeController likeController;
    private BaseActivity baseActivity;

    public ProjectsViewHolder(View view, final OnClickListener onClickListener, BaseActivity activity) {
        this(view, onClickListener, activity, true);
    }

    public ProjectsViewHolder(View view, final OnClickListener onClickListener, BaseActivity activity, boolean isAuthorNeeded) {
        super(view);
        this.context = view.getContext();
        this.baseActivity = activity;

        postImageView = view.findViewById(R.id.postImageView);
        postVideoView = view.findViewById(R.id.postVideoView);
        userName=view.findViewById(R.id.user);
        userSkill=view.findViewById(R.id.userInfo);
        likeCounterTextView = view.findViewById(R.id.likeCounterTextView);
        likesImageView = view.findViewById(R.id.likesImageView);
        commentsCountTextView = view.findViewById(R.id.commentsCountTextView);
        watcherCounterTextView = view.findViewById(R.id.watcherCounterTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        titleTextView = view.findViewById(R.id.titleTextView);
        detailsTextView = view.findViewById(R.id.detailsTextView);
        countersLayout = view.findViewById(R.id.countersContainer);

        countersLayout.setVisibility(View.VISIBLE);

        detailsTextView.setHashtagColor(Color.RED);
        detailsTextView.setOnHashtagClickListener(new SocialView.OnClickListener() {
            @Override
            public void onClick(SocialView view, CharSequence text) {

                //Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

                Log.i("text","" + text);

                Intent intent = new Intent(context, HashtagProjectsActivity.class);
                intent.putExtra("hashtag","" + text);
                context.startActivity(intent);

            }
        });

        authorImageView = view.findViewById(R.id.authorImageView);
        likeViewGroup = view.findViewById(R.id.likesContainer);

        authorImageView.setVisibility(isAuthorNeeded ? View.VISIBLE : View.GONE);

        profileManager = ProfileManager.getInstance(context.getApplicationContext());
        postManager = PostManager.getInstance(context.getApplicationContext());
        postInteractor = PostInteractor.getInstance(context.getApplicationContext());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        view.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onItemClick(getAdapterPosition(), v);
            }
        });

        likeViewGroup.setOnClickListener(view1 -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onLikeClick(likeController, position);
            }
        });

        authorImageView.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onAuthorClick(getAdapterPosition(), v);
            }
        });

    }

    public void bindData(Post post) {

        if (post.getImageTitle().equals("")){
            postImageView.setVisibility(View.GONE);
            postVideoView.setVisibility(View.GONE);
        }
        else {

            if(post.getContentType() != null){

                if (post.getContentType().contains("video")){

                    postImageView.setVisibility(View.GONE);

                    postVideoView.setVisibility(View.VISIBLE);

                    loadVideo(post.getImageTitle());

                }
                else{

                    postVideoView.setVisibility(View.GONE);

                    postImageView.setVisibility(View.VISIBLE);
                    postManager.loadImageMediumSize(GlideApp.with(baseActivity), post.getImageTitle(), postImageView);

                }

            }
            else {

                postVideoView.setVisibility(View.GONE);

                postImageView.setVisibility(View.VISIBLE);
                postManager.loadImageMediumSize(GlideApp.with(baseActivity), post.getImageTitle(), postImageView);

            }

        }

        likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);

        String title = removeNewLinesDividers(post.getTitle());
        titleTextView.setText(title);
        String description = removeNewLinesDividers(post.getDescription());
        detailsTextView.setText(description);
        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
        dateTextView.setText(date);

        if (post.getAuthorId() != null) {
            ProfileManager profileManager=new ProfileManager(context);
            profileManager.getProfileDetail(post.getAuthorId(),userName,userSkill);
            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postInteractor.hasCurrentUserProjectLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    private void loadVideo(String imageTitle){

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);

        StorageReference reference = databaseHelper.getOriginImageStorageRef(imageTitle);

        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                //setThumbnail(postImageView,uri.toString());

                //postVideoView.setVideoURI(uri);

                try{

                    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();
                    TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                    exoPlayer = ExoPlayerFactory.newSimpleInstance(context);
                    DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("posts");
                    ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

                    MediaSource mediaSource = new ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null);

                    postVideoView.setPlayer(exoPlayer);
                    exoPlayer.prepare(mediaSource);
                    exoPlayer.setPlayWhenReady(false);

                }
                catch (Exception e){

                    Toast.makeText(context, "exo player exception", Toast.LENGTH_SHORT).show();

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "Video loading failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setThumbnail(final ImageView imageView, String thumbnailUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(thumbnailUrl).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d(TAG, "Thumbnail: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                try {
                    imageView.setImageBitmap(BitmapFactory.decodeStream(response.body().byteStream()));
                } catch (Exception e) {
                    Log.d(TAG, "Thumbnail onResponse: " + e.getMessage());
                    // pass
                }
            }
        });
    }

    private String removeNewLinesDividers(String text) {
        int decoratedTextLength = text.length() < Constants.Post.MAX_TEXT_LENGTH_IN_LIST ?
                text.length() : Constants.Post.MAX_TEXT_LENGTH_IN_LIST;
        return text.substring(0, decoratedTextLength).replaceAll("\n", " ").trim();
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener(final ImageView authorImageView) {
        return new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                if (obj != null && obj.getPhotoUrl() != null) {
                    if (!baseActivity.isFinishing() && !baseActivity.isDestroyed()) {
                        ImageUtil.loadImage(GlideApp.with(baseActivity), obj.getPhotoUrl(), authorImageView);
                    }
                }
            }
        };
    }

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return exist -> likeController.initLike(exist);
    }

    public interface OnClickListener {
        void onItemClick(int position, View view);

        void onLikeClick(LikeController likeController, int position);

        void onAuthorClick(int position, View view);
    }
}