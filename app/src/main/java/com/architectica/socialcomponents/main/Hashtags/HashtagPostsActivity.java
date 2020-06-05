package com.architectica.socialcomponents.main.Hashtags;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.HashtagPostsAdapter;
import com.architectica.socialcomponents.adapters.PostsAdapter;
import com.architectica.socialcomponents.adapters.ProjectsAdapter;
import com.architectica.socialcomponents.main.base.BaseActivity;
import com.architectica.socialcomponents.main.main.MainActivity;
import com.architectica.socialcomponents.main.main.Projects.ProjectsFragment;
import com.architectica.socialcomponents.main.main.Projects.ProjectsPresenter;
import com.architectica.socialcomponents.main.post.createPost.CreateProjectActivity;
import com.architectica.socialcomponents.main.postDetails.PostDetailsActivity;
import com.architectica.socialcomponents.main.postDetails.ProjectDetailsActivity;
import com.architectica.socialcomponents.main.profile.ProfileActivity;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.utils.AnimationUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HashtagPostsActivity extends BaseActivity<HashtagView,HashtagPresenter> implements HashtagView {

    private HashtagPostsAdapter postsAdapter;
    private RecyclerView recyclerView;

    private TextView newPostsCounterTextView;
    private boolean counterAnimationInProgress = false;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;
    private String hashtag;

    @Override
    public void onResume() {
        super.onResume();
        presenter.updateNewPostCounter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashtag);

        //POST_TYPE = "Hashtag";

        hashtag = getIntent().getStringExtra("hashtag");

        setTitle(hashtag);

        initContentView();

    }

    @Override
    public HashtagPresenter createPresenter() {

        if (presenter == null) {
            return new HashtagPresenter(getApplicationContext());
        }
        return presenter;

    }

    public void refreshPostList() {
        postsAdapter.loadHashtagFirstPage();
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

            //initFloatingActionButton(view);
            initPostListRecyclerView();
            initPostCounter();
        }
    }

    /*private void initFloatingActionButton(View view) {
        floatingActionButton = view.findViewById(R.id.addNewPostFab);
        if (floatingActionButton != null) {
            floatingActionButton.setOnClickListener(v -> presenter.onCreatePostClickAction(floatingActionButton));
        }
    }*/

    private void initPostListRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        postsAdapter = new HashtagPostsAdapter(hashtag,this,swipeContainer);
        postsAdapter.setCallback(new HashtagPostsAdapter.Callback() {
            @Override
            public void onItemClick(Post post, View view) {
                //openPostDetailsActivity(post, view);
                presenter.onProjectClicked(post, view);
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

        postsAdapter.loadHashtagFirstPage();
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
        Intent intent = new Intent(this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post.getId());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);
            View authorImageView = v.findViewById(R.id.authorImageView);

            if (imageView.getVisibility() != View.GONE){

                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(this,
                                new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name)),
                                new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name))
                        );

                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST , options.toBundle());

            }
            else {

                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);

            }

        } else {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }
    }

    @Override
    public void showFloatButtonRelatedSnackBar(int messageId) {
        Toast.makeText(this, "press back button again to exit", Toast.LENGTH_SHORT).show();
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            View authorImageView = view.findViewById(R.id.authorImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(this,
                            new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name)));
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST, options.toBundle());
        } else {
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST);
        }
    }

}
