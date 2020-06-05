package com.architectica.socialcomponents.main.post.createPost;

import android.content.Context;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.main.post.BaseCreateProjectPresenter;
import com.architectica.socialcomponents.model.Project;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class CreateProjectPresenter extends BaseCreateProjectPresenter<CreateProjectView> {

        public CreateProjectPresenter(Context context) {
        super(context);
        }

       @Override
        protected int getSaveFailMessage() {
        return R.string.error_fail_create_project;
        }

        @Override
        protected void saveProject(String title, String description, ArrayList<String> questions, Boolean needMentors, List<String> hashtags) {
        ifViewAttached(view -> {
        view.showProgress(R.string.message_creating_project);
        Project post = new Project();
        post.setTitle(title);
        post.setDescription(description);
        post.setAuthorId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        post.setStatus("not-verified");
        post.setQuestions(questions);
        post.setNeedMentors(needMentors);

        if (view.getImageUri() != null){

        postManager.createOrUpdateProjectWithImage(view.getImageUri(),this, post,hashtags);

        }
        else {

        postManager.createProject(this,post,hashtags);

        }

        });
        }

        @Override
        protected boolean isImageRequired() {
        return false;
        }
        }
