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

public class ProjectDetailsActivity extends BaseActivity<ProjectDetailsView, ProjectDetailsPresenter> implements ProjectDetailsView,EditCommentDialog.CommentDialogCallback  {

    public static final String PROJECT_ID_EXTRA_KEY = "ProjectDetailsActivity.PROJECT_ID_EXTRA_KEY";
    public static final String AUTHOR_ANIMATION_NEEDED_EXTRA_KEY = "ProjectDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY";
    public static final int UPDATE_PROJECT_REQUEST = 2;
    public static final String PROJECT_STATUS_EXTRA_KEY = "ProjectDetailsActivity.PROJECT_STATUS_EXTRA_KEY";

    private EditText commentEditText;
    private EditText gitLink;
    @Nullable
    private ScrollView scrollView;
    private ViewGroup likesContainer;
    private ImageView likesImageView;
    private TextView commentsLabel;
    private TextView likeCounterTextView;
    private TextView commentsCountTextView;
    private TextView watcherCounterTextView;
    private TextView authorTextView;
    private TextView dateTextView,username,userskill;
    private ImageView authorImageView;
    private ImageView needMentorsIcon;
    private ProgressBar progressBar;
    private ImageView postImageView;
    private PlayerView postVideoView;
    private TextView titleTextView;
    private TextView descriptionEditText;
    private Button applyNow;
    private Button viewInGithub;
    private ProgressBar commentsProgressBar;
    private RecyclerView commentsRecyclerView;
    private TextView warningCommentsTextView;

    private SimpleExoPlayer exoPlayer;

    private MenuItem complainActionMenuItem;
    private MenuItem editActionMenuItem;
    private MenuItem deleteActionMenuItem;

    private String postId;

    private PostManager postManager;
    private LikeController likeController;
    private boolean authorAnimationInProgress = false;

    private boolean isAuthorAnimationRequired;
    private CommentsAdapter commentsAdapter;
    private ActionMode mActionMode;
    private boolean isEnterTransitionFinished = false;
    private Button sendButton;
    private RelativeLayout imageContainer;
    private LinearLayout counterLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        postManager = PostManager.getInstance(this);

        isAuthorAnimationRequired = getIntent().getBooleanExtra(AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, false);
        postId = getIntent().getStringExtra(PROJECT_ID_EXTRA_KEY);

        incrementWatchersCount();

        titleTextView = findViewById(R.id.titleTextView);
        username = findViewById(R.id.user);
        userskill = findViewById(R.id.userInfo);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        postImageView = findViewById(R.id.postImageView);
        postVideoView = findViewById(R.id.postVideoView);
        progressBar = findViewById(R.id.progressBar);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        scrollView = findViewById(R.id.scrollView);
        commentsLabel = findViewById(R.id.commentsLabel);
        commentEditText = findViewById(R.id.commentEditText);
        likesContainer = findViewById(R.id.likesContainer);
        likesImageView = findViewById(R.id.likesImageView);
        authorImageView = findViewById(R.id.authorImageView);
        authorTextView = findViewById(R.id.authorTextView);
        likeCounterTextView = findViewById(R.id.likeCounterTextView);
        commentsCountTextView = findViewById(R.id.commentsCountTextView);
        watcherCounterTextView = findViewById(R.id.watcherCounterTextView);
        dateTextView = findViewById(R.id.dateTextView);
        commentsProgressBar = findViewById(R.id.commentsProgressBar);
        warningCommentsTextView = findViewById(R.id.warningCommentsTextView);
        gitLink = findViewById(R.id.gitLink);

        needMentorsIcon = findViewById(R.id.needMentorsIcon);

        imageContainer = findViewById(R.id.imageContainer);

        sendButton = findViewById(R.id.sendButton);

        applyNow = findViewById(R.id.applyButton);
        viewInGithub = findViewById(R.id.githubrepo);

        counterLayout = findViewById(R.id.countersContainer);

        counterLayout.setVisibility(View.VISIBLE);

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
    public ProjectDetailsPresenter createPresenter() {
        if (presenter == null) {
            return new ProjectDetailsPresenter(this);
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
        //POST_TYPE = "project";
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

        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DataSnapshot dataSnapshot2 = dataSnapshot.child("projects/" + postId);

                String authorid = dataSnapshot2.child("authorId").getValue(String.class);

                //Log.i("uid",userid);

                //Log.i("aid",authorid);

                String adminUid = "";

                DataSnapshot dataSnapshot3 = dataSnapshot.child("profiles");

                for (DataSnapshot dataSnapshot4 : dataSnapshot3.getChildren()){

                    if ("RiftAdmin".equals(dataSnapshot4.child("username").getValue(String.class))){

                        adminUid = dataSnapshot4.getKey();

                    }

                }

                if(userid.equals(authorid) || userid.equals(adminUid)){

                    if(userid.equals(adminUid)){

                        String verifyStatus = dataSnapshot2.child("status").getValue(String.class);

                        if("verified".equals(verifyStatus)){

                            //show the people applied button
                            gitLink.setVisibility(View.GONE);
                            applyNow.setText("PEOPLE APPLIED");

                        }
                        else{

                            //show the verify button
                            gitLink.setVisibility(View.VISIBLE);
                            applyNow.setText("VERIFY");

                        }

                    }
                    else {
                        gitLink.setVisibility(View.GONE);
                        applyNow.setText("PEOPLE APPLIED");
                    }

                }
                else{

                    //show the apply now button

                    DataSnapshot dataSnapshot1 = dataSnapshot2.child("responses");

                    if(dataSnapshot1.hasChild(userid)){
                        gitLink.setVisibility(View.GONE);
                        applyNow.setText("APPLIED");
                        applyNow.setEnabled(false);

                    }
                    else{
                        gitLink.setVisibility(View.GONE);
                        applyNow.setText("APPLY NOW");
                        applyNow.setEnabled(true);
                    }

                }

                applyNow.setVisibility(View.VISIBLE);
                viewInGithub.setVisibility(View.VISIBLE);

                Boolean needMentors = dataSnapshot2.child("needMentors").getValue(Boolean.class);

                if(needMentors){

                    needMentorsIcon.setVisibility(View.VISIBLE);

                }
                else {

                    needMentorsIcon.setVisibility(View.GONE);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        applyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = applyNow.getText().toString();

                if("PEOPLE APPLIED".equals(text)){

                    //go to people applied activity

                    Intent intent = new Intent(getApplicationContext(),PeopleAppliedActivity.class);
                    intent.putExtra(PROJECT_ID_EXTRA_KEY,postId);
                    startActivity(intent);

                }
                else if ("VERIFY".equals(text)){

                    String link = gitLink.getText().toString();

                    if (link.length() != 0){

                        FirebaseDatabase.getInstance().getReference("projects/" + postId).child("githublink").setValue(link);

                        FirebaseDatabase.getInstance().getReference("projects/" + postId).child("status").setValue("verified");

                        Toast.makeText(ProjectDetailsActivity.this, "project status changed to verified", Toast.LENGTH_SHORT).show();

                        applyNow.setText("PEOPLE APPLIED");

                    }
                    else {

                        Toast.makeText(ProjectDetailsActivity.this, "Please provide the github link", Toast.LENGTH_SHORT).show();

                    }

                }
                else{

                    //check if user already applied for this project.if yes,disable the button..if not, show the apply now button

                    Intent intent = new Intent(getApplicationContext(),QuestionsActivity.class);
                    intent.putExtra(PROJECT_ID_EXTRA_KEY,postId);
                    startActivity(intent);

                }

                finish();

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

        viewInGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("projects/" + postId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("githublink")){

                            String url = dataSnapshot.child("githublink").getValue(String.class);

                            if (!url.startsWith("http://") && !url.startsWith("https://"))
                                url = "http://" + url;

                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(browserIntent);

                        }
                        else {

                            Toast.makeText(ProjectDetailsActivity.this, "github link not provided for this project.", Toast.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    private void initListeners() {
        postImageView.setOnClickListener(v -> presenter.onPostImageClick());

        commentEditText.addTextChangedListener(new TextWatcher() {
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

        commentsCountTextView.setOnClickListener(view -> scrollToFirstComment());

        authorImageView.setOnClickListener(v -> presenter.onAuthorClick(v));
        authorTextView.setOnClickListener(v -> presenter.onAuthorClick(v));

        likesContainer.setOnClickListener(v -> {
            if (likeController != null && presenter.isPostExist()) {
                likeController.handleProjectLikeClickAction(this, presenter.getPost());
            }
        });

        //long click for changing animation
        likesContainer.setOnLongClickListener(v -> {
            if (likeController != null) {
                likeController.changeAnimationType();
                return true;
            }

            return false;
        });
    }

    private void initRecyclerView() {
        commentsAdapter = new CommentsAdapter();
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

        presenter.getCommentsList(this, postId);
    }

    private void startActionMode(Comment selectedComment) {
        if (mActionMode != null) {
            return;
        }

        //check access to modify or remove post
        if (presenter.hasAccessToEditComment(selectedComment.getAuthorId()) || presenter.hasAccessToModifyPost()) {
            mActionMode = startSupportActionMode(new ProjectDetailsActivity.ActionModeCallback(selectedComment));
        }
    }

    private void incrementWatchersCount() {
        postManager.incrementProjectWatchersCount(postId);
        Intent intent = getIntent();
        setResult(RESULT_OK, intent.putExtra(PROJECT_STATUS_EXTRA_KEY, PostStatus.UPDATED));
    }

    @Override
    public void scrollToFirstComment() {
        scrollView.smoothScrollTo(0, commentsLabel.getTop());
    }

    @Override
    public void clearCommentField() {
        commentEditText.setText(null);
        commentEditText.clearFocus();
        hideKeyboard();
    }

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
                    DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("projects");
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
        ImageUtil.loadImage(GlideApp.with(ProjectDetailsActivity.this), photoUrl, authorImageView, DiskCacheStrategy.DATA);
    }

    @Override
    public void setAuthorName(String username) {
        authorTextView.setText(username);
    }

    @Override
    public void initLikeController(@NonNull Post post) {
        likeController = new LikeController(this, post, likeCounterTextView, likesImageView, false);
    }

    @Override
    public void updateCounters(@NonNull Post post) {
        ProfileManager profileManager=new ProfileManager(this);
        profileManager.getProfileDetail(post.getAuthorId(),username,userskill);


        long commentsCount = post.getCommentsCount();
        commentsCountTextView.setText(String.valueOf(commentsCount));
        commentsLabel.setText(String.format(getString(R.string.label_comments), commentsCount));
        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        likeController.setUpdatingLikeCounter(false);

        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(this, post.getCreatedDate());
        dateTextView.setText(date);

        presenter.updateCommentsVisibility(commentsCount);
    }

    @Override
    public void initLikeButtonState(boolean exist) {
        if (likeController != null) {
            likeController.initLike(exist);
        }
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
    public String getCommentText() {
        return commentEditText.getText().toString();
    }

    @Override
    public void openEditPostActivity(Post post) {
        /*Intent intent = new Intent(ProjectDetailsActivity.this, EditPostActivity.class);
        intent.putExtra(EditPostActivity.POST_EXTRA_KEY, post);
        startActivityForResult(intent, EditPostActivity.EDIT_POST_REQUEST);*/
    }

    @Override
    public void showCommentProgress(boolean show) {
        commentsProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }



    @Override
    public void showCommentsWarning(boolean show) {
        warningCommentsTextView.setVisibility(show ? View.VISIBLE : View.GONE);
    }



    @Override
    public void showCommentsRecyclerView(boolean show) {
        commentsRecyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCommentsListChanged(List<Comment> list) {
        commentsAdapter.setList(list);
    }

    @Override
    public void showCommentsLabel(boolean show) {
        commentsLabel.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void openEditCommentDialog(Comment comment) {
        EditCommentDialog editCommentDialog = new EditCommentDialog();
        Bundle args = new Bundle();
        args.putString(EditCommentDialog.COMMENT_TEXT_KEY, comment.getText());
        args.putString(EditCommentDialog.COMMENT_ID_KEY, comment.getId());
        editCommentDialog.setArguments(args);
        editCommentDialog.show(getFragmentManager(), EditCommentDialog.TAG);
    }

    @Override
    public void onCommentChanged(String newText, String commentId) {
        presenter.updateComment(newText, commentId);
    }

    @Override
    public void onPostRemoved() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent.putExtra(PROJECT_STATUS_EXTRA_KEY, PostStatus.REMOVED));
    }

    private class ActionModeCallback implements ActionMode.Callback {

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
    }

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
