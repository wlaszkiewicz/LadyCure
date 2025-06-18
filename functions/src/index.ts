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
