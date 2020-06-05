package com.architectica.socialcomponents.main.postDetails;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.VideoView;

import java.util.HashMap;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.CommentsAdapter;
import com.architectica.socialcomponents.controllers.LikeController;
import com.architectica.socialcomponents.dialogs.EditCommentDialog;
import com.architectica.socialcomponents.enums.PostStatus;
import com.architectica.socialcomponents.main.base.BaseActivity;
import com.architectica.socialcomponents.main.base.BaseView;
import com.architectica.socialcomponents.main.imageDetail.ImageDetailActivity;
import com.architectica.socialcomponents.main.login.LoginActivity;
import com.architectica.socialcomponents.main.post.editPost.EditPostActivity;
import com.architectica.socialcomponents.main.profile.ProfileActivity;
import com.architectica.socialcomponents.managers.DatabaseHelper;
import com.architectica.socialcomponents.managers.PostManager;
import com.architectica.socialcomponents.managers.ProfileManager;
import com.architectica.socialcomponents.model.Comment;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.utils.FormatterUtil;
import com.architectica.socialcomponents.utils.GlideApp;
import com.architectica.socialcomponents.utils.ImageUtil;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.internal.LockOnGetVariable;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RewardDetailsActivity extends BaseActivity<RewardDetailsView, RewardDetailsPresenter> implements RewardDetailsView{

    public static final String REWARD_ID_EXTRA_KEY = "RewardDetailsActivity.REWARD_ID_EXTRA_KEY";
    public static final String AUTHOR_ANIMATION_NEEDED_EXTRA_KEY = "RewardDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY";
    public static final int UPDATE_REWARD_REQUEST = 3;
    public static final String REWARD_STATUS_EXTRA_KEY = "RewardDetailsActivity.REWARD_STATUS_EXTRA_KEY";

    //private EditText commentEditText;
    @Nullable
    private ScrollView scrollView;
    //private ViewGroup likesContainer;
    //private ImageView likesImageView;
    //private TextView commentsLabel;
    //private TextView likeCounterTextView;
    //private TextView commentsCountTextView;
    //private TextView watcherCounterTextView;
    private TextView authorTextView;
    private TextView dateTextView,username,userskill;
    private ImageView authorImageView;
    //private ImageView needMentorsIcon;
    private ProgressBar progressBar;
    private ImageView postImageView;
    private PlayerView postVideoView;
    private TextView titleTextView;
    private TextView descriptionEditText;
    private Button applyNow;
    //private Button viewInGithub;
    //private ProgressBar commentsProgressBar;
    //private RecyclerView commentsRecyclerView;
    //private TextView warningCommentsTextView;

    private SimpleExoPlayer exoPlayer;

    private MenuItem complainActionMenuItem;
    private MenuItem editActionMenuItem;
    private MenuItem deleteActionMenuItem;

    private String postId;

    private PostManager postManager;
    //private LikeController likeController;
    private boolean authorAnimationInProgress = false;

    private boolean isAuthorAnimationRequired;
    //private CommentsAdapter commentsAdapter;
    private ActionMode mActionMode;
    private boolean isEnterTransitionFinished = false;
    private Button sendButton;
    private RelativeLayout imageContainer;
    //private LinearLayout counterLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_details);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        postManager = PostManager.getInstance(this);

        isAuthorAnimationRequired = getIntent().getBooleanExtra(AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, false);
        postId = getIntent().getStringExtra(REWARD_ID_EXTRA_KEY);

        titleTextView = findViewById(R.id.titleTextView);
        username = findViewById(R.id.user);
        userskill = findViewById(R.id.userInfo);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        postImageView = findViewById(R.id.postImageView);
        postVideoView = findViewById(R.id.postVideoView);
        progressBar = findViewById(R.id.progressBar);
        //commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        scrollView = findViewById(R.id.scrollView);
        //commentsLabel = findViewById(R.id.commentsLabel);
        //commentEditText = findViewById(R.id.commentEditText);
        //likesContainer = findViewById(R.id.likesContainer);
        //likesImageView = findViewById(R.id.likesImageView);
        authorImageView = findViewById(R.id.authorImageView);
        authorTextView = findViewById(R.id.authorTextView);
        //likeCounterTextView = findViewById(R.id.likeCounterTextView);
        //commentsCountTextView = findViewById(R.id.commentsCountTextView);
        //watcherCounterTextView = findViewById(R.id.watcherCounterTextView);
        dateTextView = findViewById(R.id.dateTextView);
        //commentsProgressBar = findViewById(R.id.commentsProgressBar);
        //warningCommentsTextView = findViewById(R.id.warningCommentsTextView);

        //needMentorsIcon = findViewById(R.id.needMentorsIcon);

        imageContainer = findViewById(R.id.imageContainer);

        sendButton = findViewById(R.id.sendButton);

        applyNow = findViewById(R.id.applyButton);
        //viewInGithub = findViewById(R.id.githubrepo);

        //counterLayout = findViewById(R.id.countersContainer);

        //counterLayout.setVisibility(View.GONE);

//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isAuthorAnimationRequired) {
//            authorImageView.setScaleX(0);
//            authorImageView.setScaleY(0);
//
//            // Add a listener to get noticed when the transition ends to animate the fab button
//            getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {
//                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//                @Override
//                public void onTransitionEnd(Transition transition) {
//                    super.onTransitionEnd(transition);
//                    //disable execution for exit transition
//                    if (!isEnterTransitionFinished) {
//                        isEnterTransitionFinished = true;
//                        com.architectica.socialcomponents.utils.AnimationUtils.showViewByScale(authorImageView)
//                                .setListener(authorAnimatorListener)
//                                .start();
//                    }
//                }
//            });
//        }

        initButtons();
        initRecyclerView();
        initListeners();

        presenter.loadProject(postId);
        supportPostponeEnterTransition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        postManager.closeListeners(this);
    }

    @NonNull
    @Override
    public RewardDetailsPresenter createPresenter() {
        if (presenter == null) {
            return new RewardDetailsPresenter(this);
        }
        return presenter;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyboard();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //POST_TYPE = "rewards";
        initButtons();
    }

    @Override
    public void onBackPressed() {
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
//                && isAuthorAnimationRequired
//                && !authorAnimationInProgress
//                && !AnimationUtils.isViewHiddenByScale(authorImageView)) {
//
//            ViewPropertyAnimator hideAuthorAnimator = com.architectica.socialcomponents.utils.AnimationUtils.hideViewByScale(authorImageView);
//            hideAuthorAnimator.setListener(authorAnimatorListener);
//            hideAuthorAnimator.withEndAction(PostDetailsActivity.this::onBackPressed);
//        } else {
        initButtons();
        super.onBackPressed();
//        }
    }

    private void initButtons(){

        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("rewards/" + postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot2) {

                //viewInGithub.setVisibility(View.GONE);
                //needMentorsIcon.setVisibility(View.GONE);

                String authorid = dataSnapshot2.child("authorId").getValue(String.class);

                if(userid.equals(authorid)){

                    //admin can't use the offer. admin is the author here

                    applyNow.setText("VIEW REQUESTS");

                }
                else{

                    //show the apply now button

                    DataSnapshot dataSnapshot1 = dataSnapshot2.child("requests");

                    if(dataSnapshot1.hasChild(userid)){

                        String status = dataSnapshot1.child(userid).child("status").getValue(String.class);

                        if ("confirmed".equals(status)){

                            applyNow.setText("ORDER CONFIRMED");

                        }
                        else{

                            applyNow.setText("AWAITING COMFIRMATION FOR YOUR ORDER");

                        }


                        applyNow.setEnabled(false);

                    }
                    else{

                        applyNow.setText("USE REWARD POINTS FOR THIS OFFER");
                        applyNow.setEnabled(true);

                    }

                }

                applyNow.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        applyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = applyNow.getText().toString();

                if ("VIEW REQUESTS".equals(text)){

                    Intent intent = new Intent(getApplicationContext(),RewardsAppliedPeople.class);
                    intent.putExtra(REWARD_ID_EXTRA_KEY,postId);
                    startActivity(intent);

                }
                else {

                    HashMap<String,Object> map = new HashMap();

                    map.put("userid",userid);
                    map.put("status","pending");

                    FirebaseDatabase.getInstance().getReference("rewards/" + postId + "/requests").child(userid).setValue(map);

                }

                /*FirebaseDatabase.getInstance().getReference("projects/" + postId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String authorid = dataSnapshot.child("authorid").getValue(String.class);

                        if(authorid.equals(userid)){

                            //go to people applied activity

                        }
                        else{

                            //check if user already applied for this project.if yes,disable the button..if not, show the apply now button

                            Intent intent = new Intent(getApplicationContext(),QuestionsActivity.class);
                            intent.putExtra(POST_ID_EXTRA_KEY,postId);
                            startActivity(intent);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });*/

            }
        });

    }

    private void initListeners() {
        postImageView.setOnClickListener(v -> presenter.onPostImageClick());

        /*commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sendButton.setEnabled(charSequence.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sendButton.setOnClickListener(v -> presenter.onSendButtonClick());

        commentsCountTextView.setOnClickListener(view -> scrollToFirstComment());*/

        authorImageView.setOnClickListener(v -> presenter.onAuthorClick(v));
        authorTextView.setOnClickListener(v -> presenter.onAuthorClick(v));

        /*likesContainer.setOnClickListener(v -> {
            if (likeController != null && presenter.isPostExist()) {
                likeController.handleLikeClickAction(this, presenter.getPost());
            }
        });

        //long click for changing animation
        likesContainer.setOnLongClickListener(v -> {
            if (likeController != null) {
                likeController.changeAnimationType();
                return true;
            }

            return false;
        });*/
    }

    private void initRecyclerView() {
        /*commentsAdapter = new CommentsAdapter();
        commentsAdapter.setCallback(new CommentsAdapter.Callback() {
            @Override
            public void onLongItemClick(View view, int position) {
                Comment selectedComment = commentsAdapter.getItemByPosition(position);
                startActionMode(selectedComment);
            }

            @Override
            public void onAuthorClick(String authorId, View view) {
                openProfileActivity(authorId, view);
            }
        });
        commentsRecyclerView.setAdapter(commentsAdapter);
        commentsRecyclerView.setNestedScrollingEnabled(false);
        commentsRecyclerView.addItemDecoration(new DividerItemDecoration(commentsRecyclerView.getContext(),
                ((LinearLayoutManager) commentsRecyclerView.getLayoutManager()).getOrientation()));

        presenter.getCommentsList(this, postId);*/
    }

    /*@Override
    public void scrollToFirstComment() {
        scrollView.smoothScrollTo(0, commentsLabel.getTop());
    }

    @Override
    public void clearCommentField() {
        commentEditText.setText(null);
        commentEditText.clearFocus();
        hideKeyboard();
    }*/

    private void scheduleStartPostponedTransition(final ImageView imageView) {
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    @Override
    public void openImageDetailScreen(String imageTitle) {

        if (imageTitle.equals("")){

            Toast.makeText(this, "No image for this post", Toast.LENGTH_SHORT).show();
            return;

        }

        Intent intent = new Intent(this, ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMAGE_TITLE_EXTRA_KEY, imageTitle);
        startActivity(intent);
    }

    @Override
    public void openProfileActivity(String userId, View view) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);
            startActivity(intent);
        }else {
            Intent intent=new Intent(this,LoginActivity.class);
            startActivity(intent);
        }

//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {
//
//            ActivityOptions options = ActivityOptions.
//                    makeSceneTransitionAnimation(PostDetailsActivity.this,
//                            new android.util.Pair<>(view, getString(R.string.post_author_image_transition_name)));
//            startActivity(intent, options.toBundle());
//        } else {
//            startActivity(intent);
//        }
    }

    @Override
    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    @Override
    public void setDescription(String description) {
        descriptionEditText.setText(description);
    }

    @Override
    public void loadPostDetailImage(String imageTitle, String contentType) {

        if (imageTitle.equals("")){
            imageContainer.setVisibility(View.GONE);
            postVideoView.setVisibility(View.GONE);
            //postImageView.setImageResource(R.drawable.noimage);

        }
        else {

            if(contentType != null){

                if (contentType.contains("video")){

                    postImageView.setVisibility(View.GONE);

                    postVideoView.setVisibility(View.VISIBLE);

                    loadVideo(imageTitle);

                }
                else{

                    postVideoView.setVisibility(View.GONE);

                    postImageView.setVisibility(View.VISIBLE);

                    postManager.loadImageMediumSize(GlideApp.with(this), imageTitle, postImageView, () -> {
                        scheduleStartPostponedTransition(postImageView);
                        progressBar.setVisibility(View.GONE);
                    });

                }

            }
            else {

                postVideoView.setVisibility(View.GONE);

                postImageView.setVisibility(View.VISIBLE);

                postManager.loadImageMediumSize(GlideApp.with(this), imageTitle, postImageView, () -> {
                    scheduleStartPostponedTransition(postImageView);
                    progressBar.setVisibility(View.GONE);
                });


            }

        }

    }

    /*private void loadVideo(String imageTitle){

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());

        StorageReference reference = databaseHelper.getOriginImageStorageRef(imageTitle);

        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                //setThumbnail(postImageView,uri.toString());

                postVideoView.setVideoURI(uri);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "Video loading failed", Toast.LENGTH_SHORT).show();
            }
        });

    }*/

    private void loadVideo(String imageTitle){

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());

        StorageReference reference = databaseHelper.getOriginImageStorageRef(imageTitle);

        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                //setThumbnail(postImageView,uri.toString());

                //postVideoView.setVideoURI(uri);

                try{

                    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(getApplicationContext()).build();
                    TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                    exoPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext());
                    DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("rewards");
                    ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

                    MediaSource mediaSource = new ExtractorMediaSource(uri,dataSourceFactory,extractorsFactory,null,null);

                    postVideoView.setPlayer(exoPlayer);
                    exoPlayer.prepare(mediaSource);
                    exoPlayer.setPlayWhenReady(false);

                }
                catch (Exception e){

                    Toast.makeText(getApplicationContext(), "exo player exception", Toast.LENGTH_SHORT).show();

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "Video loading failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void loadAuthorPhoto(String photoUrl) {
        ImageUtil.loadImage(GlideApp.with(RewardDetailsActivity.this), photoUrl, authorImageView, DiskCacheStrategy.DATA);
    }

    @Override
    public void setAuthorName(String username) {
        authorTextView.setText(username);
    }


    @Override
    public void showComplainMenuAction(boolean show) {
        if (complainActionMenuItem != null) {
            complainActionMenuItem.setVisible(show);
        }
    }



    @Override
    public void showEditMenuAction(boolean show) {
        if (editActionMenuItem != null) {
            editActionMenuItem.setVisible(show);
        }
    }



    @Override
    public void showDeleteMenuAction(boolean show) {
        if (deleteActionMenuItem != null) {
            deleteActionMenuItem.setVisible(show);
        }
    }


    @Override
    public void openEditPostActivity(Post post) {
        Intent intent = new Intent(RewardDetailsActivity.this, EditPostActivity.class);
        intent.putExtra(EditPostActivity.POST_EXTRA_KEY, post);
        startActivityForResult(intent, EditPostActivity.EDIT_POST_REQUEST);
    }

    @Override
    public void onPostRemoved() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent.putExtra(REWARD_STATUS_EXTRA_KEY, PostStatus.REMOVED));
    }

    /*private class ActionModeCallback implements ActionMode.Callback {

        Comment selectedComment;

        ActionModeCallback(Comment selectedComment) {
            this.selectedComment = selectedComment;
        }

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.comment_context_menu, menu);

            menu.findItem(R.id.editMenuItem).setVisible(presenter.hasAccessToEditComment(selectedComment.getAuthorId()));

            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }
        // Called when the user selects a contextual menu item

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.editMenuItem:
                    openEditCommentDialog(selectedComment);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.deleteMenuItem:
                    presenter.removeComment(selectedComment.getId());
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }
        // Called when the user exits the action mode

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    }*/

    Animator.AnimatorListener authorAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            authorAnimationInProgress = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            authorAnimationInProgress = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            authorAnimationInProgress = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_details_menu, menu);
        complainActionMenuItem = menu.findItem(R.id.complain_action);
        editActionMenuItem = menu.findItem(R.id.edit_post_action);
        deleteActionMenuItem = menu.findItem(R.id.delete_post_action);
        presenter.updateOptionMenuVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!presenter.isPostExist()) {
            return super.onOptionsItemSelected(item);
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.complain_action:
                presenter.doComplainAction();
                return true;

            case R.id.edit_post_action:
                presenter.editPostAction();
                return true;

            case R.id.delete_post_action:
                presenter.attemptToRemovePost();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
