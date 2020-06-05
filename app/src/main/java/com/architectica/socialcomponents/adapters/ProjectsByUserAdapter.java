/*
 * Copyright 2017 Rozdoum
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

package com.architectica.socialcomponents.adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.base.BaseActivity;
import com.architectica.socialcomponents.adapters.holders.PostViewHolder;
import com.architectica.socialcomponents.controllers.LikeController;
import com.architectica.socialcomponents.managers.PostManager;
import com.architectica.socialcomponents.managers.listeners.OnDataChangedListener;
import com.architectica.socialcomponents.model.Post;

import java.util.List;


public class ProjectsByUserAdapter extends BaseProjectsAdapter {
    public static final String TAG = PostsByUserAdapter.class.getSimpleName();

    private String userId;
    private CallBack callBack;

    public ProjectsByUserAdapter(final BaseActivity activity, String userId) {
        super(activity);
        this.userId = userId;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.post_item_list_view, parent, false);

        return new PostViewHolder(view, createOnClickListener(), activity, false);
    }

    private PostViewHolder.OnClickListener createOnClickListener() {
        return new PostViewHolder.OnClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (callBack != null) {
                    selectedPostPosition = position;
                    callBack.onItemClick(getItemByPosition(position), view);
                }
            }

            @Override
            public void onLikeClick(LikeController likeController, int position) {
                Post post = getItemByPosition(position);
                likeController.handleProjectLikeClickAction(activity, post);
            }

            @Override
            public void onAuthorClick(int position, View view) {

            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((PostViewHolder) holder).bindData(postList.get(position));
    }

    private void setList(List<Post> list) {
        postList.clear();
        postList.addAll(list);
        notifyDataSetChanged();
    }

    public void loadProjects() {
        if (!activity.hasInternetConnection()) {
            activity.showSnackBar(R.string.internet_connection_failed);
            callBack.onPostLoadingCanceled();
            return;
        }

        OnDataChangedListener<Post> onPostsDataChangedListener = new OnDataChangedListener<Post>() {
            @Override
            public void onListChanged(List<Post> list) {

                Log.i("posts list",list.toString());

                setList(list);
                callBack.onPostsListChanged(list.size());
            }
        };

        PostManager.getInstance(activity).getProjectsListByUser(onPostsDataChangedListener, userId);
    }

    public void removeSelectedPost() {
        postList.remove(selectedPostPosition);
        callBack.onPostsListChanged(postList.size());
        notifyItemRemoved(selectedPostPosition);
    }

    public interface CallBack {
        void onItemClick(Post post, View view);
        void onPostsListChanged(int postsCount);
        void onPostLoadingCanceled();
    }
}
