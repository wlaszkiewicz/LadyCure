import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

exports.sendAppointmentConfirmedNotification = functions
  .region("europe-west1") // eur3 = europe-west1
  .firestore
  .document("appointments/{appointmentId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();

    if (!before || !after) return null;

    if (before.status === "Confirmed" || after.status !== "Confirmed") {
      return null;
    }

    const patientId = after.patientId;
    const doctorName = after.doctorName || "";

    try {
      const userDoc = await admin.firestore().collection("users").doc(patientId).get();
      const fcmToken = userDoc.get("fcmToken");

      if (!fcmToken) {
        console.log("❌ No FCM token found");
        return null;
      }

      const message = {
        notification: {
          title: "Appointment Confirmed 💖",
          body: `Your appointment with Dr. ${doctorName} was confirmed by the doctor!`,
        },
        token: fcmToken,
      };

      await admin.messaging().send(message);
      console.log("✅ Notification sent");
    } catch (error) {
      console.error("🔥 Error sending notification:", error);
    }

    return null;
  });


exports.sendChatNotification = functions
  .region("europe-west1")
  .firestore
  .document("chats/{chatId}/messages/{messageId}")
  .onCreate(async (snapshot, context) => {
    const message = snapshot.data();
    if (!message) return null;

    const senderName = message.senderName || "";
    const receiverId = message.recipient;
    const text = message.text ?? "";
    const attachmentUrl = message.attachmentUrl;
    const attachmentFileName = message.attachmentFileName;
    const attachmentMimeType = message.attachmentMimeType;

    try {
      const userDoc = await admin.firestore().collection("users").doc(receiverId).get();
      const fcmToken = userDoc.get("fcmToken");
      if (!fcmToken) {
        console.log("❌ No FCM token found for receiver");
        return null;
      }

      let notificationBody = text;

      if (!text && attachmentMimeType?.startsWith("image/")) {
        notificationBody = "🖼️ Image attached";
      } else if (!text && attachmentFileName) {
        notificationBody = "📎 ${attachmentFileName}";
      } else if (text && attachmentMimeType?.startsWith("image/")) {
        notificationBody = "${text} (🖼️ Image attached)";
      } else if (text && attachmentFileName) {
        notificationBody = "${text} (📎 ${attachmentFileName})";
      } else {
        notificationBody = text ?? "New message";
      }


      const payload: admin.messaging.TokenMessage = {
        token: fcmToken,
        notification: {
          title: "💬 Message from ${senderName}",
          body: notificationBody.length > 50 ? notificationBody.substring(0, 50) + "..." : notificationBody,
          ...(attachmentUrl && attachmentMimeType?.startsWith("image/")
            ? { image: attachmentUrl }
            : {}),
        },
      };

      await admin.messaging().send(payload);
      console.log(`✅ Sent message notification to ${receiverId}`);
    } catch (error) {
      console.error("🔥 Error sending chat notification:", error);
    }

    return null;
  });

