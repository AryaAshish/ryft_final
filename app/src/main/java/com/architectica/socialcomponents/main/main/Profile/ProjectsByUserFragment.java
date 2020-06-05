package com.architectica.socialcomponents.main.main.Profile;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.PostsByUserAdapter;
import com.architectica.socialcomponents.adapters.ProjectsByUserAdapter;
import com.architectica.socialcomponents.main.base.BaseActivity;
import com.architectica.socialcomponents.main.base.BaseFragment;
import com.architectica.socialcomponents.main.postDetails.PostDetailsActivity;
import com.architectica.socialcomponents.main.postDetails.ProjectDetailsActivity;
import com.architectica.socialcomponents.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

public class ProjectsByUserFragment extends BaseFragment<ProjectsByUserView,ProjectsByUserPresenter> implements ProjectsByUserView{

    public static final String USER_ID_EXTRA_KEY = "ProfileActivity.USER_ID_EXTRA_KEY";

    private RecyclerView recyclerView;
    private ProjectsByUserAdapter postsAdapter;

    private ProgressBar postsProgressBar;

    private String userID;

    @Override
    public ProjectsByUserPresenter createPresenter() {
        if (presenter == null) {
            return new ProjectsByUserPresenter(getActivity());
        }
        return presenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userID = getArguments().getString("userid");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.posts_or_projects_by_user_layout, viewGroup, false);

        /*FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userID = firebaseUser.getUid();
        }*/

        postsProgressBar = view.findViewById(R.id.postsProgressBar);

        loadPostsList(view);
        getActivity().supportPostponeEnterTransition();

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        hideLoadingPostsProgress();

    }

    private void loadPostsList(View view) {
        if (recyclerView == null) {

            recyclerView = view.findViewById(R.id.recycler_view);
            postsAdapter = new ProjectsByUserAdapter((BaseActivity) getActivity(), userID);
            postsAdapter.setCallBack(new ProjectsByUserAdapter.CallBack() {
                @Override
                public void onItemClick(final Post post, final View view) {
                    presenter.onProjectClick(post, view);
                }

                @Override
                public void onPostsListChanged(int postsCount) {
                    //presenter.onPostListChanged(postsCount);
                }

                @Override
                public void onPostLoadingCanceled() {
                    hideLoadingPostsProgress();
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            recyclerView.setAdapter(postsAdapter);
            postsAdapter.loadProjects();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openPostDetailsActivity(Post post, View v) {
        Intent intent = new Intent(getActivity(), ProjectDetailsActivity.class);
        intent.putExtra(ProjectDetailsActivity.PROJECT_ID_EXTRA_KEY, post.getId());
        intent.putExtra(ProjectDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);

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
    public void hideLoadingPostsProgress() {

        if (postsProgressBar.getVisibility() != View.GONE) {
            postsProgressBar.setVisibility(View.GONE);
        }
    }

}
