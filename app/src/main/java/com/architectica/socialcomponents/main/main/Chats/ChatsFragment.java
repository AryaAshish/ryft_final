package com.architectica.socialcomponents.main.main.Chats;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.architectica.socialcomponents.AllUsersList.UsersActivity;
import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.ChatsListAdapter;
import com.architectica.socialcomponents.model.Profile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

public class ChatsFragment extends Fragment {

    private Toolbar mToolbar;

    private ChatsListAdapter adapter;
    private RecyclerView recyclerView;

    FloatingActionButton allUsers;
    //int i;

    List<Profile> profiles = new ArrayList<>();
    List<String> projectids = new ArrayList<>();
    List<String> projectNames = new ArrayList<>();
    List<String> timestamplist=new ArrayList<>();

    List<String> uids = new ArrayList<>();

    ProgressDialog pd;

    public static Fragment newInstance() {

        Fragment frag = new ChatsFragment();

        Bundle args = new Bundle();

        frag.setArguments(args);

        return frag;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_chats_list, container, false);

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DataSnapshot dataSnapshot1 = dataSnapshot.child("messages/" + FirebaseAuth.getInstance().getUid());

                if (dataSnapshot1.exists()){

                    uids.clear();

                    for (DataSnapshot snapshot : dataSnapshot1.getChildren()){

                        String id = snapshot.getKey();

                        uids.add(id);

                    }

                    DataSnapshot dataSnapshot2 = dataSnapshot.child("profiles");

                    profiles.clear();

                    for(int j=0;j<uids.size();j++){

                        DataSnapshot dataSnapshot3 = dataSnapshot2.child(uids.get(j));

                        Profile profile = dataSnapshot3.getValue(Profile.class);

                        //String message = dataSnapshot3.child("timestamp").getValue().toString();

                        profile.setId(dataSnapshot3.getKey());

                        //profile.setTimestamp(Long.valueOf(message));

                        profiles.add(profile);

                    }

                }

                DataSnapshot dataSnapshot4 = dataSnapshot.child("groupchats");

                projectids.clear();

                for (DataSnapshot dataSnapshot5 : dataSnapshot4.getChildren()){

                    DataSnapshot snapshot = dataSnapshot5.child("users");

                    for (DataSnapshot snapshot1 : snapshot.getChildren()){

                        String id = snapshot1.child("userid").getValue(String.class);

                        if(id.equals(currentUserId)){

                            String projid = dataSnapshot5.getKey();

                            projectids.add(projid);

                        }

                    }

                }

                DataSnapshot dataSnapshot6 = dataSnapshot.child("projects");

                for(int k=0;k<projectids.size();k++){

                    DataSnapshot dataSnapshot7 = dataSnapshot6.child(projectids.get(k));

                    projectNames.add(dataSnapshot7.child("title").getValue(String.class));

                }

                Log.i("projids",projectids.toString());
                Log.i("projnames",projectNames.toString());

                initChatsList(view,profiles,projectNames,projectids);

                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                pd.dismiss();
            }
        });

        allUsers = view.findViewById(R.id.viewAllUsers);

        allUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),UsersActivity.class);
                startActivity(intent);
            }
        });

        return view;

    }

    private void initChatsList(View view,List<Profile> profiles, List<String> names, List<String> ids){

        recyclerView = view.findViewById(R.id.conv_list);

        /*Collections.sort(profiles, new Comparator<Profile>() {
            @Override
            public int compare(Profile o1, Profile o2) {
                if (o1.getTimestamp() > o2.getTimestamp()){

                   return -1;
                }else if (o1.getTimestamp() < o2.getTimestamp()){

                    return 1;
                }else {
                    return 0;
                }

            }
        });*/

        adapter = new ChatsListAdapter(getActivity(),profiles,names,ids);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(adapter);

        pd.dismiss();

    }

}
