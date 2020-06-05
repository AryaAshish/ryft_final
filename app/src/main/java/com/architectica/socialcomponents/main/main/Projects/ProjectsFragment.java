package com.architectica.socialcomponents.main.main.Projects;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import retrofit2.http.POST;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.NotificationsAdapter;
import com.architectica.socialcomponents.adapters.ProjectsAdapter;
import com.architectica.socialcomponents.main.base.BaseFragment;
import com.architectica.socialcomponents.main.login.LoginActivity;
import com.architectica.socialcomponents.main.main.MainActivity;
import com.architectica.socialcomponents.main.post.createPost.CreatePostActivity;
import com.architectica.socialcomponents.main.post.createPost.CreateProjectActivity;
import com.architectica.socialcomponents.main.postDetails.PostDetailsActivity;
import com.architectica.socialcomponents.main.postDetails.ProjectDetailsActivity;
import com.architectica.socialcomponents.main.profile.ProfileActivity;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.utils.AnimationUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import static android.app.Activity.RESULT_OK;

public class ProjectsFragment extends BaseFragment<ProjectsView,ProjectsPresenter> implements ProjectsView{

    private ProjectsAdapter postsAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private TextView newPostsCounterTextView;
    private boolean counterAnimationInProgress = false;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;

    public ProjectsFragment(){


    }

    public static Fragment newInstance() {

        Fragment frag = new ProjectsFragment();

        Bundle args = new Bundle();

        frag.setArguments(args);

        return frag;

    }

    @Override
    public void onResume() {
        super.onResume();
        //POST_TYPE = "project";
        presenter.updateNewPostCounter();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        //POST_TYPE = "project";

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
                case CreateProjectActivity.CREATE_NEW_PROJECT_REQUEST:
                    presenter.onPostCreated();
                    break;

                case ProjectDetailsActivity.UPDATE_PROJECT_REQUEST:
                    presenter.onPostUpdated(data);
                    break;
            }
        }
    }

    @Override
    public ProjectsPresenter createPresenter() {

        if (presenter == null) {
            return new ProjectsPresenter(getActivity());
        }
        return presenter;

    }

    public void refreshPostList() {
        postsAdapter.loadProjectsFirstPage();
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
        postsAdapter = new ProjectsAdapter((MainActivity) getActivity(), swipeContainer);
        postsAdapter.setCallback(new ProjectsAdapter.Callback() {
            @Override
            public void onItemClick(final Post post, final View view) {
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(postsAdapter);
        postsAdapter.loadProjectsFirstPage();
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
        Intent intent = new Intent(getActivity(), ProjectDetailsActivity.class);
        intent.putExtra(ProjectDetailsActivity.PROJECT_ID_EXTRA_KEY, post.getId());

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

                startActivityForResult(intent, ProjectDetailsActivity.UPDATE_PROJECT_REQUEST);

            } else {

                startActivityForResult(intent, ProjectDetailsActivity.UPDATE_PROJECT_REQUEST);

            }

        } else {
            startActivityForResult(intent, ProjectDetailsActivity.UPDATE_PROJECT_REQUEST);
        }
    }

    @Override
    public void showFloatButtonRelatedSnackBar(int messageId) {
        showSnackBar(floatingActionButton, messageId);
    }

    @Override
    public void openCreatePostActivity() {
        Intent intent = new Intent(getActivity(), CreateProjectActivity.class);
        startActivityForResult(intent, CreateProjectActivity.CREATE_NEW_PROJECT_REQUEST);
        //startActivity(intent);
        //finish();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openProfileActivity(String userId, View view) {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST);
        }else {
            Intent intent=new Intent(getActivity(),LoginActivity.class);
            startActivity(intent);
        }

    }

}
