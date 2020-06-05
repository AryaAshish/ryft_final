package com.architectica.socialcomponents.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.Chat.ChatActivity;
import com.architectica.socialcomponents.main.postDetails.ResponseActivity;
import com.architectica.socialcomponents.main.postDetails.RewardsResponse;
import com.architectica.socialcomponents.model.Profile;
import com.architectica.socialcomponents.views.CircularImageView;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.architectica.socialcomponents.main.postDetails.ProjectDetailsActivity.PROJECT_ID_EXTRA_KEY;
import static com.architectica.socialcomponents.main.postDetails.RewardDetailsActivity.REWARD_ID_EXTRA_KEY;

public class RewardsAppliedAdapter extends RecyclerView.Adapter<RewardsAppliedAdapter.PeopleAppliedViewHolder> {

    private Context context;
    private List<Profile> profiles;
    private String projectid;

    public RewardsAppliedAdapter(Context context,List<Profile> profiles,String projectid){

        this.context = context;
        this.profiles = profiles;
        this.projectid = projectid;
    }

    @Override
    public PeopleAppliedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_single_layout,parent, false);

        return new PeopleAppliedViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull PeopleAppliedViewHolder chatsListViewHolder, int i) {

        chatsListViewHolder.displayName.setText(profiles.get(i).getUsername());
        /*Picasso.with(chatsListViewHolder.profileImage.getContext()).load(profiles.get(i).getPhotoUrl())
                .placeholder(R.drawable.default_avatar).into(chatsListViewHolder.profileImage);*/

        Glide.with(context).load(profiles.get(i).getPhotoUrl()).placeholder(R.drawable.default_avatar).into(chatsListViewHolder.profileImage);


        chatsListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, RewardsResponse.class);
                intent.putExtra("userid",profiles.get(i).getId());
                intent.putExtra(REWARD_ID_EXTRA_KEY,projectid);
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {

        return profiles.size();

    }

    public class PeopleAppliedViewHolder extends RecyclerView.ViewHolder {

        public CircularImageView profileImage;
        public TextView displayName;

        public PeopleAppliedViewHolder(View view) {
            super(view);

            profileImage = (CircularImageView) view.findViewById(R.id.user_single_image);
            displayName = (TextView) view.findViewById(R.id.user_single_name);

        }
    }
}
