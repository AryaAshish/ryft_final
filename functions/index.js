'use strict';

const functions = require('firebase-functions');

const admin = require('firebase-admin');

admin.initializeApp();

const actionTypeNewPostLike = "new_post_like";
const actionTypeNewProjectLike = "new_project_like";
const actionTypeNewPostComment = "new_post_comment";
const actionTypeNewProjectComment = "new_project_comment";
const actionTypeNewMessage = "new_message";
const actionTypeNewGroupMessage = "new_group_message";
const actionTypeNewPost = "new_post";
const actionTypeNewResponse = "new_response";
const actionTypeNewSelection = "new_selection";
const notificationTitle = "Rift";

const path = require('path');
const sharp = require('sharp');
const os = require('os');
const fs = require('fs');

exports.pushNotificationPostLikes = functions.database.ref('/post-likes/{postId}/{authorId}/{likeId}').onCreate((snap, context)  => {

    console.log('New post like was added');

    const likeAuthorId = context.params.authorId;
    const postId = context.params.postId;
    const likeId = context.params.likeId;

    return sendPostLikeNotification(postId,likeAuthorId,likeId);

});

exports.pushNotificationProjectLikes = functions.database.ref('/project-likes/{postId}/{authorId}/{likeId}').onCreate((snap, context)  => {

    console.log('New project like was added');

    const likeAuthorId = context.params.authorId;
    const postId = context.params.postId;
    const likeId = context.params.likeId;

    return sendProjectLikeNotification(postId,likeAuthorId,likeId);

});

exports.pushNotificationPostComments = functions.database.ref('/post-comments/{postId}/{commentId}').onCreate((snap, context) => {

    console.log('New post comment was added');

    const commentAuthorId = snap.val().authorId;
    const postId = context.params.postId;
    const commentId = context.params.commentId;

    return sendPostCommentNotification(postId,commentAuthorId,commentId);

});

exports.pushNotificationProjectComments = functions.database.ref('/project-comments/{postId}/{commentId}').onCreate((snap, context) => {

    console.log('New project comment was added');

    const commentAuthorId = snap.val().authorId;
    const postId = context.params.postId;
    const commentId = context.params.commentId;

    return sendProjectCommentNotification(postId,commentAuthorId,commentId);

});

exports.pushNotificationMessages = functions.database.ref('/messages/{toId}/{fromId}/{messageId}').onCreate((snap, context) => {

    const fromId = context.params.fromId;
    const toId = context.params.toId;
    const messageId = context.params.messageId;
    const from = snap.val().from;

    if(fromId === from){

        console.log("send a message notification");

        return sendMessageNotification(toId,fromId,messageId);

    }

    return null;

});

exports.pushNotificationGroupMessages = functions.database.ref('/groupchats/{projectId}/messages/{messageId}').onCreate((snap, context) => {

    const projectId = context.params.projectId;
    const messageId = context.params.messageId;
    const fromId = snap.val().from;

    return sendGroupMessageNotification(projectId,fromId,messageId);

});

exports.pushNotificationNewProjectApplication = functions.database.ref('/projects/{projectId}/responses/{responseId}').onCreate((snap, context) => {

    const projectId = context.params.projectId;
    const responseId = context.params.responseId;

    return sendNewProjectApplicationNotification(projectId,responseId);

});

exports.pushNotificationCandidateSelection = functions.database.ref('/projects/{projectId}/selectedCandidates/{candidateId}').onCreate((snap, context) => {

    const projectId = context.params.projectId;
    const candidateId = context.params.candidateId;

    return sendCandidateSelectionNotification(projectId,candidateId);

});

async function sendCandidateSelectionNotification(projectId,candidateId){
    
    const tokens = await getDeviceTokens(candidateId);

    if (tokens.length > 0) {

        // Notification details.

        // Create a notification
        let payload = {
            data: {
                actionType: actionTypeNewSelection,
                title: notificationTitle,
                body: `You are selected for a project`,
                postId: projectId
            },
        };

        console.log("send notification to : ",tokens[0]);
    
        // Send notifications to all tokens.
    
        return admin.messaging().sendToDevice(tokens, payload);
    
    }

    return null;
    
}

async function sendNewProjectApplicationNotification(projectId,responseId){

    const projectAuthor = await getProjectAuthorId(projectId);

    const appliedUserName = await getUsername(responseId);

    const tokens = await getDeviceTokens(projectAuthor);

    if (tokens.length > 0) {

        // Notification details.

        // Create a notification
        let payload = {
            data: {
                actionType: actionTypeNewResponse,
                title: notificationTitle,
                body: `${appliedUserName} applied to your project`,
                postId: projectId,
                responseId: responseId
            },
        };

        console.log("send notification to : ",tokens[0]);
    
        // Send notifications to all tokens.
    
        return admin.messaging().sendToDevice(tokens, payload);
    
    }

    return null;

}

async function getDeviceTokensForGroup(projectId){

    const snap = await admin.database().ref(`/groupchats/${projectId}/users`).once('value');

    if (snap.exists()) {

        const snap1 = await admin.database().ref(`/profiles`).once('value');

        let tokens = [];

        let users = snap.val();

        for(var i=0;i<users.length;i++){

            let user = users[i];

            let uid = user.userid;

            console.log(uid);

            if(snap1.exists()){

                let usertokens = Object.keys(snap1.val().uid.notificationTokens);

                for(var j=0;j<usertokens.length;j++){

                    tokens.push(usertokens[j]);

                }

            }

        }

        /*users.forEach(user => {

            let uid = user.userid;

            console.log(uid);

            if(snap1.exists()){

                Object.keys(snap1.val().uid.notificationTokens).forEach(token => {

                    tokens.push(token);
    
                });

            }

        });*/

        return tokens;
    
    }
    
    return [];

}

async function sendGroupMessageNotification(projectId,fromId,messageId){

    const fromUsername = await getUsername(fromId);

    const tokens = await getDeviceTokensForGroup(projectId);

    if (tokens.length > 0) {

        // Notification details.

        // Create a notification
        let payload = {
            data: {
                actionType: actionTypeNewGroupMessage,
                title: notificationTitle,
                body: `${fromUsername} messaged in your project group`,
                //icon: postImagePath,
                fromId: fromId
            },
        };

        console.log("send notification to : ",tokens[0]);
    
        // Send notifications to all tokens.
    
        return admin.messaging().sendToDevice(tokens, payload);
    
    }

    return null;


}

async function sendMessageNotification(toId,fromId,messageId){

    const fromUsername = await getUsername(fromId);

    const tokens = await getDeviceTokens(toId);

    if (tokens.length > 0) {

        // Notification details.

        // Create a notification
        let payload = {
            data: {
                actionType: actionTypeNewMessage,
                title: notificationTitle,
                body: `${fromUsername} messaged you`,
                fromId: fromId,
                toId: toId
            },
        };

        console.log("send notification to : ",tokens[0]);
    
        // Send notifications to all tokens.
    
        return admin.messaging().sendToDevice(tokens, payload);
    
    }

    return null;

}

async function getPostAuthorId(postId){

    const snap = await admin.database().ref(`/posts/${postId}`).once('value');

    if (snap.exists()) {

        return snap.val().authorId;
    
    }
    
    return null;

}

async function getUsername(uid){

    const snap = await admin.database().ref(`/profiles/${uid}`).once('value');

    if (snap.exists()) {

        return snap.val().username;
    
    }
    
    return null;

}

async function getDeviceTokens(uid){

    const snap = await admin.database().ref(`/profiles/${uid}/notificationTokens`).once('value');

    if (snap.exists()) {

        return Object.keys(snap.val());
    
    }
    
    return [];

}

async function getProjectImagePath(postId){

    const snap = await admin.database().ref(`/projects/${postId}`).once('value');

    if (snap.exists()) {

        return snap.val().imageTitle;
    
    }
    
    return null;

}

async function getPostImagePath(postId){

    const snap = await admin.database().ref(`/posts/${postId}`).once('value');

    if (snap.exists()) {

        return snap.val().imageTitle;
    
    }
    
    return null;

}

async function sendPostCommentNotification(postId,likeAuthorId,likeId){

    const postAuthorId = await getPostAuthorId(postId);

    if (likeAuthorId === postAuthorId){
        console.log('User commented own post');
        return 'User commented own post';
    }

    const postImagePath = await getPostImagePath(postId);

    const likeAuthorUsername = await getUsername(likeAuthorId);

    const tokens = await getDeviceTokens(postAuthorId);

    if (tokens.length > 0) {

        // Notification details.

        // Create a notification
        let payload = {
            data: {
                actionType: actionTypeNewPostComment,
                title: notificationTitle,
                body: `${likeAuthorUsername} commented on your post`,
                icon: postImagePath,
                postId: postId
            },
        };
    
        // Send notifications to all tokens.
    
        return admin.messaging().sendToDevice(tokens, payload);
    
    }

    return null;

}

async function sendPostLikeNotification(postId,likeAuthorId,likeId){

    const postAuthorId = await getPostAuthorId(postId);

    if (likeAuthorId === postAuthorId){
        console.log('User liked own post');
        return 'User liked own post';
    }

    const postImagePath = await getPostImagePath(postId);

    const likeAuthorUsername = await getUsername(likeAuthorId);

    const tokens = await getDeviceTokens(postAuthorId);

    if (tokens.length > 0) {

        // Notification details.

        // Create a notification
        let payload = {
            data: {
                actionType: actionTypeNewPostLike,
                title: notificationTitle,
                body: `${likeAuthorUsername} liked your post`,
                icon: postImagePath,
                postId: postId
            },
        };
    
        // Send notifications to all tokens.
    
        return admin.messaging().sendToDevice(tokens, payload);
    
    }

    return null;

}

async function getProjectAuthorId(postId){

    const snap = await admin.database().ref(`/projects/${postId}`).once('value');

    if (snap.exists()) {

        return snap.val().authorId;
    
    }
    
    return null;

}

async function sendProjectCommentNotification(postId,likeAuthorId,likeId){

    const postAuthorId = await getProjectAuthorId(postId);

    if (likeAuthorId === postAuthorId){
        console.log('User commented own project');
        return 'User commented own project';
    }

    const postImagePath = await getProjectImagePath(postId);

    const likeAuthorUsername = await getUsername(likeAuthorId);

    const tokens = await getDeviceTokens(postAuthorId);

    if (tokens.length > 0) {

        // Notification details.

        // Create a notification
        let payload = {
            data: {
                actionType: actionTypeNewProjectComment,
                title: notificationTitle,
                body: `${likeAuthorUsername} commented on your project`,
                icon: postImagePath,
                postId: postId
            },
        };
    
        // Send notifications to all tokens.
    
        return admin.messaging().sendToDevice(tokens, payload);
    
    }

    return null;

}

async function sendProjectLikeNotification(postId,likeAuthorId,likeId){

    const postAuthorId = await getProjectAuthorId(postId);

    if (likeAuthorId === postAuthorId){
        console.log('User liked own post');
        return 'User liked own post';
    }

    const postImagePath = await getProjectImagePath(postId);

    const likeAuthorUsername = await getUsername(likeAuthorId);

    const tokens = await getDeviceTokens(postAuthorId);

    if (tokens.length > 0) {

        // Notification details.

        // Create a notification
        let payload = {
            data: {
                actionType: actionTypeNewProjectLike,
                title: notificationTitle,
                body: `${likeAuthorUsername} liked your project`,
                icon: postImagePath,
                postId: postId
            },
        };
    
        // Send notifications to all tokens.
    
        return admin.messaging().sendToDevice(tokens, payload);
    
    }

    return null;

}