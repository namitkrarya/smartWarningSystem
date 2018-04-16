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
          time: '10',
          status: 'Enabled',
          comment: 'Attentiveness Alert',
          state: `${state}`,
        };

      const alertPromise = admin.database().ref(`/Sessions/${session_name}/alerts/${Uid}/sentAlerts`).push().set(notification);
      let token;

      return Promise.all([getDeviceTokensPromise, alertPromise]).then(results => {
        token = results[0];

        console.log('FCM token is ', token.val());                

        // Notification details.
        const payload = {
          notification: {
            title: 'Attentiveness Alert',
            body: `Your state is ${state}`,
          },
          token: token.val()
        };

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
