package com.architectica.socialcomponents.main.Rewards;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.HashtagPostsAdapter;
import com.architectica.socialcomponents.adapters.RewardsPostsAdapter;
import com.architectica.socialcomponents.main.base.BaseActivity;
import com.architectica.socialcomponents.main.login.LoginActivity;
import com.architectica.socialcomponents.main.post.createPost.CreateProjectActivity;
import com.architectica.socialcomponents.main.post.createPost.CreateRewardActivity;
import com.architectica.socialcomponents.main.postDetails.ProjectDetailsActivity;
import com.architectica.socialcomponents.main.postDetails.RewardDetailsActivity;
import com.architectica.socialcomponents.main.profile.ProfileActivity;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.utils.AnimationUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;

public class RewardsActivity extends BaseActivity<RewardsView,RewardsPresenter> implements RewardsView {

    private RewardsPostsAdapter postsAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private TextView newPostsCounterTextView;
    private boolean counterAnimationInProgress = false;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public void onResume() {
        super.onResume();
        //POST_TYPE = "rewards";
        presenter.updateNewPostCounter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        //POST_TYPE = "rewards";

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        actionBar.setTitle("Redeem Reward Points");

        initContentView();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST:
                    refreshPostList();
                    break;
                case CreateRewardActivity.CREATE_NEW_REWARD_REQUEST:
                    presenter.onRewardCreated();
                    break;

                case RewardDetailsActivity.UPDATE_REWARD_REQUEST:
                    presenter.onRewardUpdated(data);
                    break;
            }
        }
    }

    @Override
    public RewardsPresenter createPresenter() {

        if (presenter == null) {
            return new RewardsPresenter(getApplicationContext());
        }
        return presenter;

    }

    public void refreshPostList() {
        postsAdapter.loadRewardsFirstPage();
        if (postsAdapter.getItemCount() > 0) {
            recyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void showCounterView(int count) {
        AnimationUtils.showViewByScaleAndVisibility(newPostsCounterTextView);
        String counterFormat = getResources().getQuantityString(R.plurals.new_posts_counter_format, count, count);
        newPostsCounterTextView.setText(String.format(counterFormat, count));
    }

    private void initContentView() {
        if (recyclerView == null) {
            progressBar = findViewById(R.id.progressBar);
            swipeContainer = findViewById(R.id.swipeContainer);

            initFloatingActionButton();
            initPostListRecyclerView();
            initPostCounter();
        }
    }

    private void initFloatingActionButton() {
        floatingActionButton = findViewById(R.id.addNewPostFab);

        /*FirebaseDatabase.getInstance().getReference("profiles").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String adminUid = "";

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                    if ("RiftAdmin".equals(dataSnapshot1.child("username").getValue(String.class))){

                        adminUid = dataSnapshot1.getKey();

                    }

                }

                String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if (userid.equals(adminUid)){

                    floatingActionButton.setVisibility(View.VISIBLE);

                }
                else {

                    floatingActionButton.setVisibility(View.GONE);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        if (floatingActionButton != null) {
            floatingActionButton.setOnClickListener(v -> presenter.onCreatePostClickAction(floatingActionButton));
        }
    }

    private void initPostListRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        postsAdapter = new RewardsPostsAdapter(this,swipeContainer);
        postsAdapter.setCallback(new HashtagPostsAdapter.Callback() {
            @Override
            public void onItemClick(Post post, View view) {
                //openPostDetailsActivity(post, view);
                presenter.onRewardClicked(post, view);
            }

            @Override
            public void onListLoadingFinished() {
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onAuthorClick(String authorId, View view) {
                openProfileActivity(authorId, view);
            }

            @Override
            public void onCanceled(String message) {
                progressBar.setVisibility(View.GONE);
                showToast(message);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(postsAdapter);

        postsAdapter.loadRewardsFirstPage();
    }

    private void initPostCounter() {
        newPostsCounterTextView = findViewById(R.id.newPostsCounterTextView);
        newPostsCounterTextView.setOnClickListener(v -> refreshPostList());

        presenter.initPostCounter();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                hideCounterView();
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @Override
    public void hideCounterView() {
        if (!counterAnimationInProgress && newPostsCounterTextView.getVisibility() == View.VISIBLE) {
            counterAnimationInProgress = true;
            AlphaAnimation alphaAnimation = AnimationUtils.hideViewByAlpha(newPostsCounterTextView);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    counterAnimationInProgress = false;
                    newPostsCounterTextView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            alphaAnimation.start();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openPostDetailsActivity(Post post, View v) {
        Intent intent = new Intent(this, RewardDetailsActivity.class);
        intent.putExtra(RewardDetailsActivity.REWARD_ID_EXTRA_KEY, post.getId());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);
            View authorImageView = v.findViewById(R.id.authorImageView);

            if (imageView.getVisibility() != View.GONE){

                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(this,
                                new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name)),
                                new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name))
                        );

                startActivityForResult(intent, RewardDetailsActivity.UPDATE_REWARD_REQUEST, options.toBundle());

            }
            else {

                startActivityForResult(intent, RewardDetailsActivity.UPDATE_REWARD_REQUEST);

            }

        } else {
            startActivityForResult(intent, RewardDetailsActivity.UPDATE_REWARD_REQUEST);
        }
    }

    @Override
    public void showFloatButtonRelatedSnackBar(int messageId) {
        showSnackBar(floatingActionButton, messageId);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void openProfileActivity(String userId, View view) {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST);
        }else {
            Intent intent=new Intent(this,LoginActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void openCreatePostActivity() {
        Intent intent = new Intent(this, CreateRewardActivity.class);
        startActivityForResult(intent, CreateRewardActivity.CREATE_NEW_REWARD_REQUEST);
        //startActivity(intent);
        //finish();
    }

}

