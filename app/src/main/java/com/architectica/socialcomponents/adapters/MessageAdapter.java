package com.architectica.socialcomponents.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.model.Messages;
import com.architectica.socialcomponents.views.CircularImageView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by AkshayeJH on 24/07/17.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private Activity activity;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    public MessageAdapter(Activity activity,List<Messages> mMessageList) {

        this.activity = activity;
        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

//        View v = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.message_left_layout, parent, false);
//        return new MessageViewHolder(v);

        if (viewType == MSG_TYPE_RIGHT) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_right_layout, parent, false);
            return new MessageViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_left_layout, parent, false);
            return new MessageViewHolder(v);
        }

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView downloadText;
        public CircularImageView profileImage;
        public TextView displayName;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            downloadText = (TextView) view.findViewById(R.id.message_download_layout);
            profileImage = (CircularImageView) view.findViewById(R.id.message_profile_layout);
            displayName = (TextView) view.findViewById(R.id.name_text_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();
        String message_type = c.getType();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("profiles").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("username").getValue().toString();
                String image = dataSnapshot.child("photoUrl").getValue().toString();

                viewHolder.displayName.setText(name);

                /*Picasso.with(viewHolder.profileImage.getContext()).load(image)
                        .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);*/

                Glide.with(activity).load(image).placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")) {

            viewHolder.downloadText.setVisibility(View.GONE);
            viewHolder.messageText.setVisibility(View.VISIBLE);
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageImage.setVisibility(View.INVISIBLE);


        } else {

            /*viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.default_avatar).into(viewHolder.messageImage);*/
            viewHolder.downloadText.setVisibility(View.VISIBLE);
            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.downloadText.setClickable(true);
            viewHolder.downloadText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String url = viewHolder.messageText.getText().toString();

                    Intent browserIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url));
                    activity.startActivity(browserIntent);

                }
            });
            viewHolder.messageImage.setVisibility(View.INVISIBLE);

        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mMessageList.get(position).getFrom().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;

        }
    }
}
