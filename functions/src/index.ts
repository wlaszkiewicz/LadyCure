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
      console.error("❌ Error sending notification:", error);
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
         notificationBody = `🖼️ Image attached`;
       } else if (!text && attachmentFileName) {
         notificationBody = `📎 ${attachmentFileName}`;
       } else if (text && attachmentMimeType?.startsWith("image/")) {
         notificationBody = `${text} (🖼️ ${attachmentFileName})`;
       } else if (text && attachmentFileName) {
         notificationBody = `${text} (📎 ${attachmentFileName})`;
       } else {
         notificationBody = text ?? "New message";
       }


      const payload: admin.messaging.TokenMessage = {
        token: fcmToken,
        notification: {
          title: `💬 Message from ${senderName}`,
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


exports.sendUpcomingAppointmentReminders = functions.pubsub
  .schedule("every 5 minutes")
  .timeZone("Europe/Warsaw")
  .onRun(async (context) => {
    const now = admin.firestore.Timestamp.now();
    const nowMs = now.toMillis();

    const fiveMinFromNow = admin.firestore.Timestamp.fromMillis(nowMs + 5 * 60 * 1000);
    const tenMinFromNow = admin.firestore.Timestamp.fromMillis(nowMs + 10 * 60 * 1000);
    const fiftyFiveMinFromNow = admin.firestore.Timestamp.fromMillis(nowMs + 55 * 60 * 1000);
    const sixtyFiveMinFromNow = admin.firestore.Timestamp.fromMillis(nowMs + 65 * 60 * 1000);

    const snapshotOneHour = await admin.firestore()
      .collection("appointments")
      .where("status", "==", "Confirmed")
      .where("dateTime", ">=", fiftyFiveMinFromNow)
      .where("dateTime", "<=", sixtyFiveMinFromNow)
      .get();

    const snapshotFiveMin = await admin.firestore()
      .collection("appointments")
      .where("status", "==", "Confirmed")
      .where("dateTime", ">=", fiveMinFromNow)
      .where("dateTime", "<=", tenMinFromNow)
      .get();

    const sendReminders = async (
      docs: FirebaseFirestore.QueryDocumentSnapshot[],
      type: "hour" | "five"
    ) => {
      const promises = docs.map(async (doc) => {
        const data = doc.data();
        const patientId = data.patientId;
        const doctorId = data.doctorId;
        const doctorName = data.doctorName || "the doctor";
        const patientName = data.patientName || "the patient";

        const patientDoc = await admin.firestore().collection("users").doc(patientId).get();
        const doctorDoc = await admin.firestore().collection("users").doc(doctorId).get();

        const patientToken = patientDoc.get("fcmToken");
        const doctorToken = doctorDoc.get("fcmToken");

        const tasks = [];

        if (patientToken) {
          const patientPayload = {
            notification: {
              title: type === "hour" ? "⏳ Upcoming Appointment" : "⏰ Appointment Starting Soon",
              body:
                type === "hour"
                  ? `Reminder: Your appointment with Dr. ${doctorName} is in 1 hour.`
                  : `Your appointment with Dr. ${doctorName} is starting soon!`,
            },
            token: patientToken,
          };

          tasks.push(admin.messaging().send(patientPayload));
          console.log(`✅ ${type} reminder sent to patient ${patientId}`);
        }

        if (type === "five" && doctorToken) {
          const doctorPayload = {
            notification: {
              title: "👩‍⚕️ Appointment Starting Soon",
              body: `Upcoming appointment with ${patientName} is starting in a few minutes!`,
            },
            token: doctorToken,
          };

          tasks.push(admin.messaging().send(doctorPayload));
          console.log(`✅ ${type} reminder sent to doctor ${doctorId}`);
        }

        await Promise.all(tasks);
      });

      await Promise.all(promises);
    };

    if (!snapshotOneHour.empty) await sendReminders(snapshotOneHour.docs, "hour");
    if (!snapshotFiveMin.empty) await sendReminders(snapshotFiveMin.docs, "five");

    return null;
  });



exports.sendAppointmentCancelledNotification = functions
  .region("europe-west1")
  .firestore
  .document("appointments/{appointmentId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();

    if (!before || !after) return null;

    // Only send if it was Confirmed and now Cancelled
    if (before.status !== "Confirmed" || after.status !== "Cancelled") {
      return null;
    }

    const doctorId = after.doctorId;
    const patientName = after.patientName || "the patient";

    try {
      const userDoc = await admin.firestore().collection("users").doc(doctorId).get();
      const fcmToken = userDoc.get("fcmToken");

      if (!fcmToken) {
        console.log("❌ No FCM token found for doctor");
        return null;
      }

      const message = {
        notification: {
          title: "Appointment Cancelled ❌",
          body: `${patientName} just cancelled a confirmed appointment.`,
        },
        token: fcmToken,
      };

      await admin.messaging().send(message);
      console.log(`✅ Cancellation notification sent to Dr. ${doctorId}`);
    } catch (error) {
      console.error("❌ Error sending cancellation notification:", error);
    }

    return null;
  });


exports.cleanOldAvailabilities = functions
  .region("europe-west1")
  .pubsub.schedule("every day 00:00")
  .timeZone("Europe/Warsaw")
  .onRun(async (context) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0); // set to midnight
    const todayString = today.toISOString().split("T")[0]; // yyyy-MM-dd

    const usersSnapshot = await admin.firestore().collection("users").get();

    const batch = admin.firestore().batch();
    for (const userDoc of usersSnapshot.docs) {
      const availabilityRef = userDoc.ref.collection("availability");
      const pastDatesSnap = await availabilityRef.get();

      for (const doc of pastDatesSnap.docs) {
        if (doc.id < todayString) {
          batch.delete(doc.ref);
        }
      }
    }

    await batch.commit();
    console.log("🧹 Old availabilities cleaned");
    return null;
  });

