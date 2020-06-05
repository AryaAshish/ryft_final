package com.architectica.socialcomponents.main.postDetails;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.QuestionsAdapter;
import com.architectica.socialcomponents.main.interactors.ProfileInteractor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import static com.architectica.socialcomponents.main.postDetails.ProjectDetailsActivity.PROJECT_ID_EXTRA_KEY;

public class QuestionsActivity extends AppCompatActivity {

    //RecyclerView questionsView;

    //QuestionsAdapter questionsAdapter;

    private String postId;

    private List<String> questions = new ArrayList<>();

    private List<String> questids = new ArrayList<>();

    private Button submit;

    private LinearLayout questionsLayout;

    private ProfileInteractor profileInteractor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        postId = getIntent().getStringExtra(PROJECT_ID_EXTRA_KEY);

        //questionsView = findViewById(R.id.questions_view);

        profileInteractor = ProfileInteractor.getInstance(getApplicationContext());

        questionsLayout = findViewById(R.id.questionsLayout);

        submit = findViewById(R.id.submit);

        FirebaseDatabase.getInstance().getReference("projects/" + postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("questions")){

                    //questions are there..so questions

                    questions.clear();

                    DataSnapshot dataSnapshot1 = dataSnapshot.child("questions");

                    for(DataSnapshot snapshot : dataSnapshot1.getChildren()){

                        for (DataSnapshot snapshot1 : snapshot.getChildren()){

                            questions.add(snapshot1.getValue(String.class));

                            LinearLayout newView = (LinearLayout) getLayoutInflater().inflate(R.layout.question_card, null);

                            TextView ques = newView.findViewById(R.id.question);

                            ques.setText(snapshot1.getValue(String.class));

                            questionsLayout.addView(newView);

                        }

                    }

                }
                else{

                    //no questions..just show the submit button

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isResponseValid = true;

                ArrayList<String> ans = new ArrayList<String>();

                int size = questionsLayout.getChildCount();

                for(int i=0;i<size;i++){

                    View view = questionsLayout.getChildAt(i);
                    EditText text = view.findViewById(R.id.answer);

                    if(text.getText().toString() == ""){

                        //empty answer..invalid response

                        isResponseValid = false;
                        break;

                    }
                    else{

                        ans.add(text.getText().toString());

                    }

                }

                if(isResponseValid){

                    //response is valid..upload the response

                    FirebaseDatabase.getInstance().getReference("projects/" + postId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            if (dataSnapshot.hasChild("questions")){

                                //questions are there..so questions

                                questids.clear();

                                HashMap<String,Object> response = new HashMap<>();
                                response.put("userid",userid);

                                DataSnapshot dataSnapshot1 = dataSnapshot.child("questions");

                                int j = (int) dataSnapshot1.getChildrenCount();

                                for(DataSnapshot snapshot : dataSnapshot1.getChildren()){

                                    questids.add(snapshot.getKey());

                                }

                                HashMap<String,Object> ansmap = new HashMap<>();

                                for(int i=0;i<j;i++){

                                    HashMap<String,Object> answer = new HashMap<>();
                                    answer.put("questionid",questids.get(i));
                                    answer.put("answer",ans.get(i));

                                    String key = FirebaseDatabase.getInstance().getReference("projects").push().getKey();

                                    ansmap.put(key,answer);

                                }

                                response.put("answers",ansmap);

                                FirebaseDatabase.getInstance().getReference("projects/" + postId).child("responses").child(userid).setValue(response);

                            }
                            else{

                                //no questions..just show the submit button

                                HashMap<String,Object> response = new HashMap<>();

                                response.put("userid",userid);

                                FirebaseDatabase.getInstance().getReference("projects/" + postId).child("responses").child(userid).setValue(response);

                            }

                            profileInteractor.addCredits(5);

                           /* Intent intent = new Intent(getApplicationContext(),ProjectDetailsActivity.class);
                            intent.putExtra(ProjectDetailsActivity.POST_ID_EXTRA_KEY, postId);
                            startActivity(intent);*/
                            finish();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                else{

                    Toast.makeText(QuestionsActivity.this, "Please answer all the questions", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }
}
