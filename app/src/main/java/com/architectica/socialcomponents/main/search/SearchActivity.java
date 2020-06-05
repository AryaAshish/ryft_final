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

package com.architectica.socialcomponents.main.search;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.viewPager.TabsPagerAdapter;
import com.architectica.socialcomponents.main.base.BaseActivity;
import com.architectica.socialcomponents.main.search.posts.SearchPostsFragment;
import com.architectica.socialcomponents.main.search.users.SearchUsersBySkillsFragment;
import com.architectica.socialcomponents.main.search.users.SearchUsersFragment;
import com.architectica.socialcomponents.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexey on 08.05.18.
 */

public class SearchActivity extends BaseActivity<SearchView, SearchPresenter> implements SearchView {
    private static final String TAG = SearchActivity.class.getSimpleName();
   // private TabsPagerAdapter tabsAdapter;
   ViewPagerAdapter av;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private androidx.appcompat.widget.SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initContentView();
    }

    @NonNull
    @Override
    public SearchPresenter createPresenter() {
        if (presenter == null) {
            return new SearchPresenter(this);
        }
        return presenter;
    }

    private void initContentView() {
        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabLayout);

        initTabs();
    }

    private void initTabs() {
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
      //  tabsAdapter = new TabsPagerAdapter(this, getSupportFragmentManager());

        /*Bundle argsPostsTab = new Bundle();
        tabsAdapter.addTab(SearchPostsFragment.class, argsPostsTab, getResources().getString(R.string.posts_tab_title));

        Bundle argsUsersTab = new Bundle();
        tabsAdapter.addTab(SearchUsersFragment.class, argsUsersTab, getResources().getString(R.string.users_tab_title));

        Bundle argsSkillsTab = new Bundle();
        tabsAdapter.addTab(SearchUsersBySkillsFragment.class, argsSkillsTab, getResources().getString(R.string.skills_tab_title));

        viewPager.setAdapter(tabsAdapter);
        tabLayout.setupWithViewPager(viewPager);*/

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                search(searchView.getQuery().toString());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {

         av=new ViewPagerAdapter(getSupportFragmentManager());
        av.addFragment(new SearchPostsFragment(),"POSTS");
        av.addFragment(new SearchUsersFragment(),"USERS");
        av.addFragment(new SearchUsersBySkillsFragment(),"SKILLS");
        viewPager.setAdapter(av);
    }

    private void search(String searchText) {
       //Fragment fragment = tabsAdapter.getItem(viewPager.getCurrentItem());
        Fragment fragment = av.getItem(viewPager.getCurrentItem());

        ((Searchable)fragment).search(searchText);
        LogUtil.logDebug(TAG, "search text: " + searchText);
    }

    private void initSearch(MenuItem searchMenuItem) {
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (androidx.appcompat.widget.SearchView) searchMenuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchMenuItem.expandActionView();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        initSearch(searchMenuItem);

        return true;
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> m=new ArrayList<>();
        private final List<String> n=new ArrayList<>();

        public ViewPagerAdapter(FragmentManager Manager)
        {
            super(Manager);
        }

        @Override
        public Fragment getItem(int position) {
            return m.get(position);
        }

        public void addFragment(Fragment fragment, String Title) {
            m.add(fragment);
            n.add(Title);
        }

        @Override
        public int getCount() {
            return m.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return n.get(position);
        }
    }
}
