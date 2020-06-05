package com.architectica.socialcomponents.model;

import android.util.Log;

import com.architectica.socialcomponents.enums.ItemType;
import com.architectica.socialcomponents.utils.FormatterUtil;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Project implements Serializable, LazyLoading {

    private String id;
    private String title;
    private String description;
    private String status;

    public ArrayList<String> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<String> questions) {
        this.questions = questions;
    }

    private ArrayList<String> questions;
    private long createdDate;
    private String imagePath;
    private String imageTitle;
    private String authorId;
    private long commentsCount;
    private long likesCount;
    private long watchersCount;
    private boolean hasComplain;
    private boolean needMentors;
    private ItemType itemType;

    private String contentType;

    public Project() {
        this.createdDate = new Date().getTime();
        itemType = ItemType.ITEM;
    }

    public Project(ItemType itemType) {
        this.itemType = itemType;
        setId(itemType.toString());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public long getWatchersCount() {
        return watchersCount;
    }

    public void setWatchersCount(long watchersCount) {
        this.watchersCount = watchersCount;
    }

    public boolean isHasComplain() {
        return hasComplain;
    }

    public void setHasComplain(boolean hasComplain) {
        this.hasComplain = hasComplain;
    }

    public void setNeedMentors(boolean needMentors) {
        this.needMentors = needMentors;
    }

    public boolean getNeedMentors(){return needMentors;}

    public Map<String, Object> toMap() {

        if (imageTitle == null){
            imageTitle = "";
        }

        HashMap<String, Object> result = new HashMap<>();

        result.put("title", title);
        result.put("description", description);
        result.put("createdDate", createdDate);
        result.put("imagePath", imagePath);
        result.put("imageTitle", imageTitle);
        result.put("authorId", authorId);
        result.put("commentsCount", commentsCount);
        result.put("likesCount", likesCount);
        result.put("watchersCount", watchersCount);
        result.put("hasComplain", hasComplain);
        result.put("createdDateText", FormatterUtil.getFirebaseDateFormat().format(new Date(createdDate)));
        result.put("status","not-verified");
        result.put("needMentors",needMentors);
        result.put("contentType",contentType);

        HashMap<String,Object> quesmap = new HashMap<>();

        //Log.i("1st",questions.get(0));
        //Log.i("2nd",questions.get(1));

        for(int i=0;i<questions.size();i++){

            HashMap<String,Object> ques = new HashMap<>();

            ques.put("question",questions.get(i));

            String key = FirebaseDatabase.getInstance().getReference("projects").push().getKey();

            quesmap.put(key,ques);

        }

        result.put("questions",quesmap);

        return result;
    }

    @Override
    public ItemType getItemType() {
        return itemType;
    }

    @Override
    public void setItemType(ItemType itemType) {

    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
