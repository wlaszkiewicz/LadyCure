import * as admin from "firebase-admin";

export async function sendNotificationWithFallback({
  userId,
  token,
  title,
  body,
  type,
  relatedAppointmentId
}: {
  userId: string;
  token?: string;
  title: string;
  body: string;
  type: string;
  relatedAppointmentId?: string;
}) {
  if (token) {
    try {
      await admin.messaging().send({
        token,
        notification: { title, body },
      });
      console.log(`✅ Notification sent to ${userId}`);
    } catch (e) {
      console.log(`⚠️ Failed to send FCM to ${userId}: ${e}`);
    }
  } else {
    console.log(`ℹ️ No FCM token for ${userId}, saving notification to Firestore only.`);
  }

  await admin.firestore().collection("users").doc(userId).collection("notifications").add({
    title,
    body,
    timestamp: admin.firestore.Timestamp.now(),
    isRead: false,
    type,
    ...(relatedAppointmentId ? { relatedAppointmentId } : {})
  });
}

exports.sendNotificationWithFallback = sendNotificationWithFallback;