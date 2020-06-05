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

package com.architectica.socialcomponents.model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.architectica.socialcomponents.enums.ItemType;

import java.io.Serializable;
import java.util.List;

@IgnoreExtraProperties
public class Profile implements Serializable, LazyLoading,Comparable<Profile> {

    private String id;
    private String username;
    private String email;
    private String photoUrl;
    private long likesCount;
    private String registrationToken;
    private ItemType itemType;
    private String usertype;
    private String userbio;
    private String useruri;
    private String phoneNumber;
    private String status;
    private long credits = 0;
    long timestamp;
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    private String skill;

    public Profile() {
        // Default constructor required for calls to DataSnapshot.getValue(Profile.class)
    }
    public String getUserbio(){return userbio;}
    public String getUseruri(){return useruri;}

    public void setUseruri(String useruri){this.useruri=useruri;}
    public void setUserbio(String userbio){this.userbio=userbio;}


    public String getUsertype(){return usertype;}
    public void setUsertype(String usertype1){this.usertype=usertype1;}

    public Profile(String id) {
        this.id = id;
    }

    public Profile(ItemType load) {
        itemType = load;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    @Override
    public ItemType getItemType() {
        return itemType;
    }

    @Override
    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public int compareTo(Profile o) {
        return this.timestamp < o.timestamp ? -1 : this.timestamp > o.timestamp ? 1 :0;
    }

    public long getCredits() {
        return credits;
    }

    public void setCredits(long credits) {
        this.credits = credits;
    }
}
