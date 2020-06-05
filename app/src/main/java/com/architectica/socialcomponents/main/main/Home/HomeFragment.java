package com.architectica.socialcomponents.main.main.Home;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.architectica.socialcomponents.main.main.Notifications.NotificationsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.PostsAdapter;
import com.architectica.socialcomponents.main.Chat.ChatActivity;
import com.architectica.socialcomponents.main.base.BaseFragment;
import com.architectica.socialcomponents.main.main.MainActivity;
import com.architectica.socialcomponents.main.post.createPost.CreatePostActivity;
import com.architectica.socialcomponents.main.postDetails.PostDetailsActivity;
import com.architectica.socialcomponents.main.profile.ProfileActivity;
import com.architectica.socialcomponents.main.search.SearchActivity;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.utils.AnimationUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.architectica.socialcomponents.main.login.LoginActivity;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends BaseFragment<HomeView, HomePresenter> implements HomeView {

    private PostsAdapter postsAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private TextView newPostsCounterTextView;
    private boolean counterAnimationInProgress = false;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;

    public HomeFragment() {

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.profile:
                presenter.onProfileClicked();
                return true;

            case R.id.notifications:
                presenter.onNotificationsClicked();
                return true;

            case R.id.search:
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
                //finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void openNotificationsActivity() {

        Intent intent = new Intent(getActivity(), NotificationsActivity.class);
        startActivity(intent);

    }

    @Override
    public void openUserProfileActivity() {

        Intent intent = new Intent(getActivity(), com.architectica.socialcomponents.main.main.Profile.ProfileActivity.class);
        startActivity(intent);

    }

    public static Fragment newInstance() {

        Fragment frag = new HomeFragment();

        Bundle args = new Bundle();

        frag.setArguments(args);

        return frag;

    }

    @Override
    public HomePresenter createPresenter() {

        if (presenter == null) {
            return new HomePresenter(getActivity());
        }
        return presenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        //POST_TYPE = "post";
        presenter.updateNewPostCounter();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //POST_TYPE = "post";

        initContentView(view);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST:
                    refreshPostList();
                    break;
                case CreatePostActivity.CREATE_NEW_POST_REQUEST:
                    presenter.onPostCreated();
                    break;

                case PostDetailsActivity.UPDATE_POST_REQUEST:
                    presenter.onPostUpdated(data);
                    break;
            }
        }
    }

    public void refreshPostList() {
        postsAdapter.loadFirstPage();
        if (postsAdapter.getItemCount() > 0) {
            recyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void removePost() {
        postsAdapter.removeSelectedPost();
    }

    @Override
    public void updatePost() {

        postsAdapter.updateSelectedPost();
    }

    @Override
    public void showCounterView(int count) {
        AnimationUtils.showViewByScaleAndVisibility(newPostsCounterTextView);
        String counterFormat = getResources().getQuantityString(R.plurals.new_posts_counter_format, count, count);
        newPostsCounterTextView.setText(String.format(counterFormat, count));
    }

    private void initContentView(View view) {
        if (recyclerView == null) {
            progressBar = view.findViewById(R.id.progressBar);
            swipeContainer = view.findViewById(R.id.swipeContainer);

            initFloatingActionButton(view);
            initPostListRecyclerView(view);
            initPostCounter(view);
        }
    }

    private void initFloatingActionButton(View view) {
        floatingActionButton = view.findViewById(R.id.addNewPostFab);
        if (floatingActionButton != null) {
            floatingActionButton.setOnClickListener(v -> presenter.onCreatePostClickAction(floatingActionButton));
        }
    }

    private void initPostListRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        postsAdapter = new PostsAdapter((MainActivity) getActivity(), swipeContainer);
        postsAdapter.setCallback(new PostsAdapter.Callback() {
            @Override
            public void onItemClick(final Post post, final View view) {
                presenter.onPostClicked(post, view);
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(postsAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        postsAdapter.loadFirstPage();
    }

    private void initPostCounter(View view) {
        newPostsCounterTextView = view.findViewById(R.id.newPostsCounterTextView);
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
        Intent intent = new Intent(getActivity(), PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post.getId());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);
            View authorImageView = v.findViewById(R.id.authorImageView);

            if (imageView.getVisibility() != View.GONE) {

                /*ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(getActivity(),
                                new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name)),
                                new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name))
                        );*/

               // startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());

                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);

            } else {

                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);

            }

        } else {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }
    }

    public void showFloatButtonRelatedSnackBar(int messageId) {
        showSnackBar(floatingActionButton, messageId);
    }

    @Override
    public void openCreatePostActivity() {
        Intent intent = new Intent(getActivity(), CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.CREATE_NEW_POST_REQUEST);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openProfileActivity(String userId, View view) {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST);
            //finish();
        }else {
            Intent intent=new Intent(getActivity(),LoginActivity.class);
            startActivity(intent);
        }

    }

}
