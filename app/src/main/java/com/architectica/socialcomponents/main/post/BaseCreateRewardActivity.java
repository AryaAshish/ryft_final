package com.architectica.socialcomponents.main.post;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.HashtagAdapter;
import com.architectica.socialcomponents.main.pickImageBase.PickImageActivity;
import com.architectica.socialcomponents.model.hashtag;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCreateRewardActivity<V extends BaseCreateRewardView, P extends BaseCreateRewardPresenter<V>>
        extends PickImageActivity<V, P> implements BaseCreateRewardView {

    protected ImageView imageView;
    protected ProgressBar progressBar;
    protected EditText titleEditText;
    protected SocialAutoCompleteTextView descriptionEditText;
    protected TextView uploadTxt;
    protected RelativeLayout imageContainer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_create_post_activity);

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black);
        }

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        descriptionEditText.setHashtagEnabled(true);
        descriptionEditText.setMentionEnabled(false);
        descriptionEditText.setHyperlinkEnabled(false);

        descriptionEditText.setHashtagColor(Color.BLUE);

        setHashtagAdapter();

        progressBar = findViewById(R.id.progressBar);

        imageView = findViewById(R.id.imageView);
        uploadTxt = findViewById(R.id.uploadtxt);
        imageContainer = findViewById(R.id.imageContainer);

        imageContainer.setOnClickListener(v -> onSelectImageClick(v));

        titleEditText.setOnTouchListener((v, event) -> {
            if (titleEditText.hasFocus() && titleEditText.getError() != null) {
                titleEditText.setError(null);
                return true;
            }

            return false;
        });
    }

    public void setHashtagAdapter(){

        FirebaseDatabase.getInstance().getReference("hashtags").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayAdapter<hashtag> hashtagAdapter = new HashtagAdapter(getApplicationContext());

                if (dataSnapshot.exists()){

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                        hashtagAdapter.add(new hashtag(snapshot.getKey()));

                    }

                }

                descriptionEditText.setHashtagAdapter(hashtagAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected ProgressBar getProgressView() {
        return progressBar;
    }

    @Override
    protected ImageView getImageView() {
        return imageView;
    }

    @Override
    protected void onImagePikedAction() {
        imageView.setAlpha((float) 1.0);
        uploadTxt.setVisibility(View.INVISIBLE);
        loadImageToImageView(imageUri);
    }

    @Override
    public void setDescriptionError(String error) {
        descriptionEditText.setError(error);
        descriptionEditText.requestFocus();
    }

    @Override
    public void setTitleError(String error) {
        titleEditText.setError(error);
        titleEditText.requestFocus();
    }

    @Override
    public String getTitleText() {
        return titleEditText.getText().toString();
    }

    @Override
    public String getDescriptionText() {
        return descriptionEditText.getText().toString();
    }

    @Override
    public void requestImageViewFocus() {
        imageView.requestFocus();
    }

    @Override
    public void onPostSavedSuccess() {
        setResult(RESULT_OK);
        this.finish();
    }

    @Override
    public Uri getImageUri() {
        return imageUri;
    }
}

