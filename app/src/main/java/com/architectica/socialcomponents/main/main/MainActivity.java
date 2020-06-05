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

package com.architectica.socialcomponents.main.main;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.architectica.socialcomponents.main.Rewards.RewardsActivity;
import com.architectica.socialcomponents.main.login.LoginActivity;
import com.architectica.socialcomponents.main.main.Chats.ChatsFragment;
import com.architectica.socialcomponents.main.main.GoogleNews.GoogleNewsFragment;
import com.architectica.socialcomponents.main.main.Profile.ProfileActivity;
import com.architectica.socialcomponents.main.main.Projects.ProjectsFragment;
import com.architectica.socialcomponents.utils.GoogleApiHelper;
import com.architectica.socialcomponents.utils.LogoutHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.base.BaseActivity;
import com.architectica.socialcomponents.main.main.Home.HomeFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MainActivity extends BaseActivity<MainView, MainPresenter> implements MainView,GoogleApiClient.OnConnectionFailedListener,NavigationView.OnNavigationItemSelectedListener {

    private static final String SELECTED_ITEM = "arg_selected_item";

    //public static String POST_TYPE = "post";

    private BottomNavigationView mBottomNav;

    private int mSelectedItem;
    DrawerLayout drawer;

    String currentVersion, latestVersion;
    Dialog dialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Rift");
        FirebaseApp.initializeApp(this);
        mBottomNav = (BottomNavigationView) findViewById(R.id.navigation);


        drawer = findViewById(R.id.activity_main);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override

            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                selectFragment(item);

                return true;

            }

        });

        /*int backstack = getSupportFragmentManager().getBackStackEntryCount();

        if (backstack > 0) {
            getSupportFragmentManager().popBackStack();
        } else {



        }*/

        MenuItem selectedItem;

        if (savedInstanceState != null) {

            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);

            selectedItem = mBottomNav.getMenu().findItem(mSelectedItem);

        } else {

            selectedItem = mBottomNav.getMenu().getItem(0);

        }

        selectFragment(selectedItem);

    }

    private void getCurrentVersion(){
        PackageManager pm = this.getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo =  pm.getPackageInfo(this.getPackageName(),0);

        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        currentVersion = pInfo.versionName;

        new GetLatestVersion().execute();

    }

    private class GetLatestVersion extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                //It retrieves the latest version by scraping the content of current version from play store at runtime
                Document doc = Jsoup.connect("appstoreurl").get();
                Log.i("doc",doc.toString());
                latestVersion = doc.getElementsByClass("htlgb").get(6).text();

            }catch (Exception e){
                e.printStackTrace();

            }

            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if(latestVersion!=null) {
                if (!currentVersion.equalsIgnoreCase(latestVersion)){
                    if(!isFinishing()){ //This would help to prevent Error : BinderProxy@45d459c0 is not valid; is your activity running? error
                        showUpdateDialog();
                    }
                }
            }
            /*else{

                //startApp();

            }*/
            super.onPostExecute(jsonObject);
        }
    }

    private void showUpdateDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("A New Update is Available");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                        ("market://details?id=com.architectica.socialcomponents")));
                dialog.dismiss();
            }
        });

        /*builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                background.start();
            }
        });*/

        builder.setCancelable(false);
        dialog = builder.show();
    }

    /*private void startApp(){

        MenuItem selectedItem = mBottomNav.getMenu().getItem(0);

        selectFragment(selectedItem);

    }*/

    private void selectFragment(MenuItem item) {


        Fragment frag=null ;

        switch (item.getItemId()) {

            case R.id.menu_home:

                frag = HomeFragment.newInstance();

                break;

            case R.id.menu_projects:

                frag = ProjectsFragment.newInstance();

                break;

            case R.id.menu_chats:
                if (checkAuthorization()) {

                    frag = ChatsFragment.newInstance();

                }
                break;

            case R.id.menu_google_news:
                frag = GoogleNewsFragment.newInstance();
                break;

        }

        // update selected item

        mSelectedItem = item.getItemId();

        mBottomNav.getMenu().findItem(mSelectedItem).setChecked(true);

        if (frag != null) {

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            ft.replace(R.id.container, frag, frag.getTag()).addToBackStack(null);

            ft.commit();

        }

    }

    public void showFloatButtonRelatedSnackBar(int messageId) {
        //showSnackBar(floatingActionButton, messageId);
        Toast.makeText(this, "press back button again to exit", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        drawer.closeDrawer(GravityCompat.START);
        MenuItem homeItem = mBottomNav.getMenu().getItem(0);

        if (mSelectedItem != homeItem.getItemId()) {

            // select home item

            selectFragment(homeItem);

        } else {

            finish();
            //super.onBackPressed();

        }

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        MenuItem homeItem = mBottomNav.getMenu().getItem(0);

        selectFragment(homeItem);

    }

    @NonNull
    @Override
    public MainPresenter createPresenter() {
        if (presenter == null) {
            return new MainPresenter(this);
        }
        return presenter;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //LogUtil.logDebug(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        MenuItem navselectedItem;
        switch (id){
            case R.id.nav_home :
                navselectedItem = mBottomNav.getMenu().getItem(0);
                selectFragment(navselectedItem);
                break;

            case R.id.nav_projects :
                navselectedItem = mBottomNav.getMenu().getItem(1);
                selectFragment(navselectedItem);
                break;

            case R.id.nav_profile:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent chatIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(chatIntent);
                }else {
                    Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                }
                break;

            case R.id.nav_chat :
                navselectedItem = mBottomNav.getMenu().getItem(2);
                selectFragment(navselectedItem);
                break;

            case R.id.nav_rewards:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent intent = new Intent(getApplicationContext(), RewardsActivity.class);
                    startActivity(intent);
                    //finish();
                }else {
                    Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                }
                break;

            case R.id.nav_google_news :
                navselectedItem = mBottomNav.getMenu().getItem(3);
                selectFragment(navselectedItem);
                break;

            case R.id.nav_logout :

                if (FirebaseAuth.getInstance().getCurrentUser() != null){
                    LogoutHelper.logoutFirebase(MainActivity.this);
                    LogoutHelper.logoutGoogle(GoogleApiHelper.createGoogleApiClient(MainActivity.this),MainActivity.this);
                }

                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
