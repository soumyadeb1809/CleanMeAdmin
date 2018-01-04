const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/full_dustbins/GVMC/{dustbin_id}')
    .onWrite(event => {

        const dustbin_id = event.params.dustbin_id;

        console.log('New notification to be sent for Dustbin ID: ', dustbin_id);

        const zoneUserQuery = admin.database().ref(`/dustbins/GVMC/${dustbin_id}/zone`).once('value');
        return zoneUserQuery.then(zoneUserResult =>{
        	const zoneUserId = zoneUserResult.val();
			console.log('Zone User ID: ', zoneUserId);

			const deviceToken = admin.database().ref(`/municipalities/GVMC/zones/${zoneUserId}/token`).once('value');
			return deviceToken.then(result =>{
                const token_id = result.val();
                console.log('Token ID: ', token_id);
                const payload = {
                    notification: {
                        title : "CLEANme Request",
                        body: `New cleaning request for dustbin ID: ${dustbin_id} in your zone.`,
                        icon: "default",
                        click_action: 'com.example.soumyadeb.cleanmeadmin_TARGET_NOTIFICATION'
                    }
                };

                return admin.messaging().sendToDevice(token_id, payload).then(response => {
                    console.log('Notification sent!', '!');

                });


              });


        });



    });
