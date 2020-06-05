package com.architectica.socialcomponents.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.Chat.ChatActivity;
import com.architectica.socialcomponents.main.postDetails.QuestionsActivity;
import com.architectica.socialcomponents.model.Profile;
import com.architectica.socialcomponents.views.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.QuestionsViewHolder> {

    private Context context;
    private List<String> questions;

    public QuestionsAdapter(Context context,List<String> questions){

        this.context = context;
        this.questions = questions;
    }

    @Override
    public QuestionsAdapter.QuestionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.question_card,parent, false);

        return new QuestionsAdapter.QuestionsViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull QuestionsAdapter.QuestionsViewHolder questionsViewHolder, int i) {

        questionsViewHolder.question.setText(questions.get(i));

    }

    @Override
    public int getItemCount() {

        return questions.size();

    }

    public class QuestionsViewHolder extends RecyclerView.ViewHolder {

        public TextView question;
        public EditText answer;

        public QuestionsViewHolder(View view) {
            super(view);

            question = view.findViewById(R.id.question);
            answer = view.findViewById(R.id.answer);

        }
    }
}

