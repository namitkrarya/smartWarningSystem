'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
/**
 * Triggers when a user gets a new follower and sends a notification.
 *
 * Followers add a flag to `/followers/{followedUid}/{followerUid}`.
 * Users save their device notification tokens to `/users/{followedUid}/notificationTokens/{notificationToken}`.
 */
exports.sendNotification = functions.database.ref('/Sessions/{session_name}/joinedUsers/{Uid}/state')
    .onWrite((change, context) => {
      const Uid = context.params.Uid;
      const session_name = context.params.session_name;
      // If un-follow we exit the function.
      if (!change.after.val()) {
        return console.log('User with ', Uid, 'state save corrupted');
      }
      console.log('New state of user with UID : ', Uid, 'is',  change.after.val());
      const state = change.after.val();
      if(state > 9){
        return console.log('State is good!');
      }
      // Get the list of device notification tokens.
      const getDeviceTokensPromise = admin.database()
          .ref(`Students/${Uid}/token`).once('value');

      const notification = {
          title: 'Attentiveness Alert',
          body: `Your state is ${state}`,
          // icon: follower.photoURL
        };

      const alertPromise = admin.database().ref(`/Sessions/${session_name}/alerts/${Uid}/sentAlerts`).push().set(notification);

      
      // Get the follower profile.
      // const getFollowerProfilePromise = admin.auth().getUser(Uid);

      // The snapshot to the user's tokens.
      // let tokensSnapshot;

      // The array containing all the user's tokens.
      let token;

      return Promise.all([getDeviceTokensPromise, alertPromise]).then(results => {
        token = results[0];
        // const follower = results[1];

        // Check if there are any device tokens.
        // if (!tokensSnapshot.hasChildren()) {
        //   return console.log('There are no notification tokens to send to.');
        // }
        // console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
        console.log('FCM token is ', token.val());                
        // console.log('Fetched follower profile', follower);

        // Notification details.
        const payload = {
          notification: {
            title: 'Attentiveness Alert',
            body: `Your state is ${state}`,
            // icon: follower.photoURL
          },
          token: token.val()
        };

        // Listing all tokens as an array.
        // tokens = Object.keys(tokensSnapshot.val());
        // Send notifications to all tokens.
        return admin.messaging().send(payload);
      }).then((response) => {
        const arr = [];
        // Response is a message ID string.
        console.log('Successfully sent message:', response);
        return Promise.all(arr);
      })
      .catch((error) => {
        console.log('Error sending message:', error);
      });
    });

    

    // .then((response) => {
    //   // For each message check if there was an error.
    //   const tokensToRemove = [];
    //   response.results.forEach((result, index) => {
    //     const error = result.error;
    //     if (error) {
    //       console.error('Failure sending notification to', tokens[index], error);
    //       // Cleanup the tokens who are not registered anymore.
    //       if (error.code === 'messaging/invalid-registration-token' ||
    //           error.code === 'messaging/registration-token-not-registered') {
    //         tokensToRemove.push(token.ref.remove());
    //       }
    //     }
    //   });
    //   return Promise.all(tokensToRemove);
    // });