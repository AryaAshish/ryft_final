package com.architectica.socialcomponents.main.postDetails;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.architectica.socialcomponents.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static com.architectica.socialcomponents.main.postDetails.RewardDetailsActivity.REWARD_ID_EXTRA_KEY;

public class RewardsResponse extends AppCompatActivity {

    private String postId;

    private String appliedUserId;

    private LinearLayout responseLayout;

    private Button select;

    private List<String> quesids = new ArrayList<>();

    private List<String> questions = new ArrayList<>();

    private List<String> answers = new ArrayList<>();

    private LinearLayout pointsLayout;

    private EditText pointsEditText;

    private String userPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        postId = getIntent().getStringExtra(REWARD_ID_EXTRA_KEY);

        //Log.i("postid",postId);

        appliedUserId = getIntent().getStringExtra("userid");

        responseLayout = findViewById(R.id.responseLayout);

        pointsLayout = findViewById(R.id.pointsLayout);

        pointsEditText = findViewById(R.id.pointsEditText);

        select = findViewById(R.id.select);

        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                pointsLayout.setVisibility(View.VISIBLE);

                DataSnapshot dataSnapshot1 = dataSnapshot.child("rewards/" + postId + "/requests/" + appliedUserId);

                LinearLayout newView = (LinearLayout) getLayoutInflater().inflate(R.layout.response_card, null);

                TextView ques = newView.findViewById(R.id.question);

                TextView ans = newView.findViewById(R.id.answer);

                ques.setText("Reward Points : ");

                DataSnapshot snapshot = dataSnapshot.child("profiles/" + appliedUserId);

                if (snapshot.hasChild("credits")){

                    long po = snapshot.child("credits").getValue(Long.class);

                    userPoints = Long.toString(po);

                }
                else {

                    userPoints = "0";

                }

                ans.setText(userPoints);

                responseLayout.addView(newView);

                String status = dataSnapshot1.child("status").getValue(String.class);

                if ("confirmed".equals(status)){

                    select.setText("CONFIRMED");
                    select.setEnabled(false);

                }
                else {

                    select.setText("CONFIRM ORDER");
                    select.setEnabled(true);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //decrease the respective credits for the user and send a notification and upadate the status of the order

                String text = pointsEditText.getText().toString();

                if (text != null && text != ""){

                    int pointsToDecrease = Integer.parseInt(text);

                    int points = Integer.parseInt(userPoints);

                    if (pointsToDecrease > points){

                        Toast.makeText(RewardsResponse.this, "User does not have the required number of reward points", Toast.LENGTH_SHORT).show();

                    }
                    else {

                        long newPoints = (long) points-pointsToDecrease;

                        FirebaseDatabase.getInstance().getReference("profiles/" + appliedUserId + "/credits").setValue(newPoints);

                        HashMap<String,Object> map = new HashMap<>();

                        map.put("userid",appliedUserId);
                        map.put("status","confirmed");

                        //Log.i("postid",postId);

                        FirebaseDatabase.getInstance().getReference("rewards/" + postId + "/requests/" + appliedUserId).setValue(map);

                    }

                    finish();

                }
                else {

                    Toast.makeText(RewardsResponse.this, "please enter the amount of reward points to be used", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }
}
