package com.architectica.socialcomponents.main.main.Profile;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.architectica.socialcomponents.ApplicationHelper;
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
import com.architectica.socialcomponents.managers.DatabaseHelper;
import com.architectica.socialcomponents.managers.FollowManager;
import com.architectica.socialcomponents.managers.ProfileManager;
import com.architectica.socialcomponents.model.Profile;
import com.architectica.socialcomponents.utils.GlideApp;
import com.architectica.socialcomponents.utils.ImageUtil;
import com.architectica.socialcomponents.utils.LogoutHelper;
import com.architectica.socialcomponents.views.FollowButton;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class ProfileActivity extends BaseActivity<ProfileView,ProfilePresenter> implements ProfileView, UnfollowConfirmationDialog.Callback{

    //private static final String TAG = ProfileActivity.class.getSimpleName();
    public static final int CREATE_POST_FROM_PROFILE_REQUEST = 22;
    public static final String USER_ID_EXTRA_KEY = "ProfileActivity.USER_ID_EXTRA_KEY";

    // UI references.
    private TextView nameEditText;
    private TextView bioTextView;
    private TextView statusTextView;
    private TextView skillTextView;
    private ImageView imageView;
    //private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView postsCounterTextView;
    private TextView creditsTextView;

    private TabLayout tabs;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private String currentUserId;
    private String userID;

    private PostsByUserAdapter postsAdapter;
    private TextView likesCountersTextView;
    private TextView followersCounterTextView;
    private TextView followingsCounterTextView,selfEdit;
    private FollowButton followButton;

    LinearLayout linearLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.editProfile:
                presenter.onEditProfileClick();
                return true;
            case R.id.signOut:
                if (FirebaseAuth.getInstance().getCurrentUser() != null){
                    FirebaseAuth.getInstance().signOut();
                    LogoutHelper.signOut(mGoogleApiClient, this);
                    startMainActivity();
                }

                return true;
            case R.id.createPost:
                presenter.onCreatePostClick();
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public ProfilePresenter createPresenter() {
        if (presenter == null) {
            return new ProfilePresenter(this);
        }
        return presenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        //POST_TYPE = "post";

        //userID = getIntent().getStringExtra(USER_ID_EXTRA_KEY);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userID = firebaseUser.getUid();
        }

        Log.i("userid",userID);

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
        bioTextView = findViewById(R.id.bio);
        statusTextView = findViewById(R.id.status);
        skillTextView = findViewById(R.id.skill);
        postsCounterTextView = findViewById(R.id.postsCounterTextView);
        likesCountersTextView = findViewById(R.id.likesCountersTextView);
        followersCounterTextView = findViewById(R.id.followersCounterTextView);
        followingsCounterTextView = findViewById(R.id.followingsCounterTextView);

        creditsTextView = findViewById(R.id.credits);

        followButton = findViewById(R.id.followButton);

        linearLayout=findViewById(R.id.msglay);
        selfEdit=findViewById(R.id.selfedit);

        linearLayout.setVisibility(View.GONE);
        selfEdit.setVisibility(View.VISIBLE);

        selfEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onEditProfileClick();
            }
        });

        setPostCounter();

        initListeners();

        presenter.checkFollowState(userID);

        //loadPostsList(view);
        supportPostponeEnterTransition();


    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.loadProfile(this, userID);
        presenter.getFollowersCount(userID);
        presenter.getFollowingsCount(userID);
        // presenter.showLikes();
        // presenter.showPosts();

        //int postsCount = 0;

        //presenter.onPostListChanged(postsCount);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        contentString.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Second_Light), start, contentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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


    }

    private void onRefreshAction() {
        postsAdapter.loadPosts();
    }

    private void startUsersListActivity(int usersListType) {
        Intent intent = new Intent(this, UsersListActivity.class);
        intent.putExtra(UsersListActivity.USER_ID_EXTRA_KEY, userID);
        intent.putExtra(UsersListActivity.USER_LIST_TYPE, usersListType);
        startActivity(intent);
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void startEditProfileActivity() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
        //finish();
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
        creditsTextView.setText("Reward Points : " + credits);
    }

    @Override
    public void setStatus(String status) {
        /*if ("Not Hired".equals(status)){

            statusTextView.setText(status);
            statusTextView.setTextColor(getResources().getColor(R.color.red));

        }
        else if ("Hired".equals(status)){

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
    public void showUnfollowConfirmation(@NonNull Profile profile) {
        UnfollowConfirmationDialog unfollowConfirmationDialog = new UnfollowConfirmationDialog();
        Bundle args = new Bundle();
        args.putSerializable(UnfollowConfirmationDialog.PROFILE, profile);
        unfollowConfirmationDialog.setArguments(args);
        unfollowConfirmationDialog.show(getFragmentManager(),UnfollowConfirmationDialog.TAG);
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
    public void onPostRemoved() {
        postsAdapter.removeSelectedPost();
    }

    @Override
    public void onPostUpdated() {
        postsAdapter.updateSelectedPost();
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


}
