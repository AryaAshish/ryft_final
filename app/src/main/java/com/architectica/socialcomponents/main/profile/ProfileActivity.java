/*
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
 */

package com.architectica.socialcomponents.main.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.architectica.socialcomponents.ApplicationHelper;
import com.architectica.socialcomponents.main.Chat.ChatActivity;
import com.architectica.socialcomponents.main.main.Profile.PostsByUserFragment;
import com.architectica.socialcomponents.main.main.Profile.ProjectsByUserFragment;
import com.architectica.socialcomponents.managers.DatabaseHelper;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.PostsByUserAdapter;
import com.architectica.socialcomponents.dialogs.UnfollowConfirmationDialog;
import com.architectica.socialcomponents.enums.FollowState;
import com.architectica.socialcomponents.main.base.BaseActivity;
import com.architectica.socialcomponents.main.editProfile.EditProfileActivity;
import com.architectica.socialcomponents.main.login.LoginActivity;
import com.architectica.socialcomponents.main.main.MainActivity;
import com.architectica.socialcomponents.main.post.createPost.CreatePostActivity;
import com.architectica.socialcomponents.main.postDetails.PostDetailsActivity;
import com.architectica.socialcomponents.main.usersList.UsersListActivity;
import com.architectica.socialcomponents.main.usersList.UsersListType;
import com.architectica.socialcomponents.managers.FollowManager;
import com.architectica.socialcomponents.managers.ProfileManager;
import com.architectica.socialcomponents.model.Post;
import com.architectica.socialcomponents.model.Profile;
import com.architectica.socialcomponents.utils.GlideApp;
import com.architectica.socialcomponents.utils.ImageUtil;
import com.architectica.socialcomponents.utils.LogUtil;
import com.architectica.socialcomponents.utils.LogoutHelper;
import com.architectica.socialcomponents.views.FollowButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity<ProfileView, ProfilePresenter> implements ProfileView, GoogleApiClient.OnConnectionFailedListener, UnfollowConfirmationDialog.Callback {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    public static final int CREATE_POST_FROM_PROFILE_REQUEST = 22;
    public static final String USER_ID_EXTRA_KEY = "ProfileActivity.USER_ID_EXTRA_KEY";

    // UI references.
    private TextView nameEditText;
    private TextView creditsText;
    private TextView bioTextView;
    private TextView statusTextView;
    private TextView skillTextView;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView postsCounterTextView;
    //private ProgressBar postsProgressBar;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private String currentUserId;
    private String userID;

    private PostsByUserAdapter postsAdapter;
  //  private SwipeRefreshLayout swipeContainer;
    private TextView likesCountersTextView;
    private TextView followersCounterTextView,selfrdit;
    LinearLayout msglayout;
    private TextView followingsCounterTextView,meesagePro;
    private FollowButton followButton;
    private Button editProfile;

    private TabLayout tabs;

    private TextView creditsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        userID = getIntent().getStringExtra(USER_ID_EXTRA_KEY);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId =String.valueOf(firebaseUser.getUid()) ;
        }

        /*if (currentUserId=="null" || currentUserId ==null){
            Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            finish();
        }
        //Toast.makeText(getApplicationContext(),String.valueOf(currentUserId)+" "+String.valueOf(userID),Toast.LENGTH_SHORT).show();

        Log.i("userid",currentUserId);*/

        tabs = (TabLayout) findViewById(R.id.result_tabs);
        // Setting ViewPager for each Tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        tabs.setupWithViewPager(viewPager);

        // Set up the login form.
        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageView);
        nameEditText = findViewById(R.id.nameEditText);
        creditsText = findViewById(R.id.credits);
        bioTextView = findViewById(R.id.bio);
        statusTextView = findViewById(R.id.status);
        skillTextView = findViewById(R.id.skill);
        postsCounterTextView = findViewById(R.id.postsCounterTextView);
        likesCountersTextView = findViewById(R.id.likesCountersTextView);
        followersCounterTextView = findViewById(R.id.followersCounterTextView);
        followingsCounterTextView = findViewById(R.id.followingsCounterTextView);
        //postsProgressBar = findViewById(R.id.postsProgressBar);
        followButton = findViewById(R.id.followButton);
       // swipeContainer = findViewById(R.id.swipeContainer);
        editProfile = findViewById(R.id.editProfile);
        meesagePro=findViewById(R.id.messagepro);
        msglayout=findViewById(R.id.msglay);
        selfrdit=findViewById(R.id.selfedit);

        creditsTextView = findViewById(R.id.credits);

        meesagePro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                chatIntent.putExtra("ChatType","NormalChat");
                chatIntent.putExtra("ChatUser",userID);
                chatIntent.putExtra("UserName",nameEditText.getText().toString());
                startActivity(chatIntent);
            }
        });

        setPostCounter();

       // Toast.makeText(getApplicationContext(),String.valueOf(currentUserId)+" "+String.valueOf(userID),Toast.LENGTH_SHORT).show();

        if (!String.valueOf(currentUserId).equals("null") || String.valueOf(currentUserId) != null) {
            if (String.valueOf(currentUserId).equals(userID)) {
                msglayout.setVisibility(View.GONE);
                selfrdit.setVisibility(View.VISIBLE);

            } else {
                msglayout.setVisibility(View.VISIBLE);
                selfrdit.setVisibility(View.GONE);
            }
        }

        selfrdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onEditProfileClick();
            }
        });

        initListeners();

        presenter.checkFollowState(userID);

        //loadPostsList();
        supportPostponeEnterTransition();


    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.loadProfile(this, userID);
        presenter.getFollowersCount(userID);
        presenter.getFollowingsCount(userID);

        showPostCounter(true);
        showLikeCounter(true);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        FollowManager.getInstance(this).closeListeners(this);
        ProfileManager.getInstance(this).closeListeners(this);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
    }

    @NonNull
    @Override
    public ProfilePresenter createPresenter() {
        if (presenter == null) {
            return new ProfilePresenter(this);
        }
        return presenter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CreatePostActivity.CREATE_NEW_POST_REQUEST:
                    postsAdapter.loadPosts();
                    showSnackBar(R.string.message_post_was_created);
                    setResult(RESULT_OK);
                    break;

                case PostDetailsActivity.UPDATE_POST_REQUEST:
                    presenter.checkPostChanges(data);
                    break;

                case LoginActivity.LOGIN_REQUEST_CODE:
                    presenter.checkFollowState(userID);
                    break;
            }
        }
    }

    private void setPostCounter(){

        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();

        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
        Query postsQuery;
        postsQuery = databaseReference.orderByChild("authorId").equalTo(userID);

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int postsCount = (int) dataSnapshot.getChildrenCount();

                String postsLabel = "posts";

                postsCounterTextView.setText(buildCounterSpannable(postsLabel, postsCount));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public Spannable buildCounterSpannable(String label, int value) {
        SpannableStringBuilder contentString = new SpannableStringBuilder();
        contentString.append(String.valueOf(value));
        contentString.append("\n");
        int start = contentString.length();
        contentString.append(label);
        contentString.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.TextAppearance_Second_Light), start, contentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return contentString;
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {

        PostsByUserFragment postsFragment = new PostsByUserFragment();

        ProjectsByUserFragment projectsFragment = new ProjectsByUserFragment();

        Bundle bundle = new Bundle();
        bundle.putString("userid", userID);

        postsFragment.setArguments(bundle);
        projectsFragment.setArguments(bundle);

        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(postsFragment, "Posts");
        adapter.addFragment(projectsFragment, "Projects");
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                //Log.i("pos","" + tab.getPosition());

                /*if (tab.getPosition() == 0){

                    POST_TYPE = "post";

                }
                else if (tab.getPosition() == 1){

                    POST_TYPE = "project";

                }*/

                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    private void initListeners() {

        followButton.setOnClickListener(v -> {
            presenter.onFollowButtonClick(followButton.getState(), userID);
        });


        followingsCounterTextView.setOnClickListener(v -> {
            startUsersListActivity(UsersListType.FOLLOWINGS);
        });

        followersCounterTextView.setOnClickListener(v -> {
            startUsersListActivity(UsersListType.FOLLOWERS);
        });

      //  swipeContainer.setOnRefreshListener(this::onRefreshAction);
    }

    private void onRefreshAction() {
        postsAdapter.loadPosts();
    }

    private void startUsersListActivity(int usersListType) {
        Intent intent = new Intent(ProfileActivity.this, UsersListActivity.class);
        intent.putExtra(UsersListActivity.USER_ID_EXTRA_KEY, userID);
        intent.putExtra(UsersListActivity.USER_LIST_TYPE, usersListType);
        startActivity(intent);
    }

    private void loadPostsList() {
        if (recyclerView == null) {

            recyclerView = findViewById(R.id.recycler_view);
            postsAdapter = new PostsByUserAdapter(this, userID);
            postsAdapter.setCallBack(new PostsByUserAdapter.CallBack() {
                @Override
                public void onItemClick(final Post post, final View view) {
                    presenter.onPostClick(post, view);
                }

                @Override
                public void onPostsListChanged(int postsCount) {
                    presenter.onPostListChanged(postsCount);
                }

                @Override
                public void onPostLoadingCanceled() {
                    hideLoadingPostsProgress();
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            recyclerView.setAdapter(postsAdapter);
            postsAdapter.loadPosts();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openPostDetailsActivity(Post post, View v) {
        Intent intent = new Intent(ProfileActivity.this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post.getId());
        intent.putExtra(PostDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);

            if (imageView.getVisibility() != View.GONE) {

                /*ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(ProfileActivity.this,
                                new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name))
                        );
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());*/

                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);

            } else {

                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);

            }

        } else {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }

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

    private void startMainActivity() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void startEditProfileActivity() {
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtil.logDebug(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void openCreatePostActivity() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.CREATE_NEW_POST_REQUEST);
    }

    @Override
    public void setProfileName(String username) {
        nameEditText.setText(username);
    }

    @Override
    public void setBio(String bio) {
        bioTextView.setText(bio);
    }

    @Override
    public void setCredits(long credits) {
        bioTextView.setText("Reward Points : " + credits);
    }

    @Override
    public void setStatus(String status) {
        /*if ("Not Hired".equals(status)) {

            statusTextView.setText(status);
            statusTextView.setTextColor(getResources().getColor(R.color.red));

        } else if ("Hired".equals(status)) {

            statusTextView.setText(status);
            statusTextView.setTextColor(getResources().getColor(R.color.green));

        }*/
        statusTextView.setVisibility(View.GONE);
    }

    @Override
    public void setSkill(String skill) {

        skillTextView.setText(skill);

    }

    @Override
    public void setProfilePhoto(String photoUrl) {
        ImageUtil.loadImage(GlideApp.with(this), photoUrl, imageView, new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                scheduleStartPostponedTransition(imageView);
                progressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                scheduleStartPostponedTransition(imageView);
                progressBar.setVisibility(View.GONE);
                return false;
            }
        });
    }

    @Override
    public void setDefaultProfilePhoto() {
        progressBar.setVisibility(View.GONE);
        imageView.setImageResource(R.drawable.ic_stub);
    }

    @Override
    public void updateLikesCounter(Spannable text) {
        likesCountersTextView.setText(text);
    }

    @Override
    public void hideLoadingPostsProgress() {
        //swipeContainer.setRefreshing(false);
        /*if (postsProgressBar.getVisibility() != View.GONE) {
            postsProgressBar.setVisibility(View.GONE);
        }*/
    }

    @Override
    public void showLikeCounter(boolean show) {
        likesCountersTextView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updatePostsCounter(Spannable text) {
        postsCounterTextView.setText(text);
    }

    @Override
    public void showPostCounter(boolean show) {
        postsCounterTextView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPostRemoved() {
        postsAdapter.removeSelectedPost();
    }

    @Override
    public void onPostUpdated() {
        postsAdapter.updateSelectedPost();
    }

    @Override
    public void showUnfollowConfirmation(@NonNull Profile profile) {
        UnfollowConfirmationDialog unfollowConfirmationDialog = new UnfollowConfirmationDialog();
        Bundle args = new Bundle();
        args.putSerializable(UnfollowConfirmationDialog.PROFILE, profile);
        unfollowConfirmationDialog.setArguments(args);
        unfollowConfirmationDialog.show(getFragmentManager(), UnfollowConfirmationDialog.TAG);
    }

    @Override
    public void updateFollowButtonState(FollowState followState) {
        followButton.setState(followState);
    }

    @Override
    public void updateFollowersCount(int count) {
        followersCounterTextView.setVisibility(View.VISIBLE);
        String followersLabel = getResources().getQuantityString(R.plurals.followers_counter_format, count, count);
        followersCounterTextView.setText(presenter.buildCounterSpannable(followersLabel, count));
    }

    @Override
    public void updateFollowingsCount(int count) {
        followingsCounterTextView.setVisibility(View.VISIBLE);
        String followingsLabel = getResources().getQuantityString(R.plurals.followings_counter_format, count, count);
        followingsCounterTextView.setText(presenter.buildCounterSpannable(followingsLabel, count));
    }

    @Override
    public void setFollowStateChangeResultOk() {
        setResult(UsersListActivity.UPDATE_FOLLOWING_STATE_RESULT_OK);
    }

    @Override
    public void onUnfollowButtonClicked() {
        presenter.unfollowUser(userID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (userID.equals(currentUserId)) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.profile_menu, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.editProfile:
                presenter.onEditProfileClick();
                return true;
            case R.id.signOut:
                LogoutHelper.signOut(mGoogleApiClient, this);
                startMainActivity();
                return true;
            case R.id.createPost:
                presenter.onCreatePostClick();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
