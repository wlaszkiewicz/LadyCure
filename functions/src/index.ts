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

      await admin.firestore().collection("users").doc(patientId).collection("notifications").add({
        title: message.notification.title,
        body: message.notification.body,
        timestamp: admin.firestore.Timestamp.now(),
        isRead: false,
        type: "confirmation",
      });


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
    const fifteenMinAgo = admin.firestore.Timestamp.fromMillis(nowMs - 15 * 60 * 1000);
    const fiveMinAgo = admin.firestore.Timestamp.fromMillis(nowMs - 5 * 60 * 1000);

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
        type: string
    ) => {
      const promises = docs.map(async (doc) => {
        const data = doc.data();

        if ((type === "hour" && data.reminderSent_hour) || (type === "five" && data.reminderSent_five)) return;

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
              title: type === "hour" ? "Upcoming Appointment ⏳" : "Appointment Starting Soon! ⏰",
              body:
                type === "hour"
                  ? `Reminder: Your appointment with Dr. ${doctorName} is in 1 hour.`
                  : `Your appointment with Dr. ${doctorName} is starting soon!`,
            },
            token: patientToken,
          };

          tasks.push(admin.messaging().send(patientPayload));
          tasks.push(
            admin.firestore()
              .collection("users")
              .doc(patientId)
              .collection("notifications")
              .add({
                title: patientPayload.notification.title,
                body: patientPayload.notification.body,
                timestamp: admin.firestore.Timestamp.now(),
                isRead: false,
                type: "reminder"
              })
          );
        }

        if (type === "five" && doctorToken) {
          const doctorPayload = {
            notification: {
              title: "Appointment Starting Soon! ⏰",
              body: `Upcoming appointment with ${patientName} is starting in a few minutes!`,
            },
            token: doctorToken,
          };

          tasks.push(admin.messaging().send(doctorPayload));
          tasks.push(
            admin.firestore()
              .collection("users")
              .doc(doctorId)
              .collection("notifications")
              .add({
                title: doctorPayload.notification.title,
                body: doctorPayload.notification.body,
                timestamp: admin.firestore.Timestamp.now(),
                isRead: false,
                type: "reminder"
              })
          );
        }

        if (type === "hour") {
          tasks.push(doc.ref.update({ reminderSent_hour: true }));
        } else if (type === "five") {
          tasks.push(doc.ref.update({ reminderSent_five: true }));
        }

        await Promise.all(tasks);
      });

      await Promise.all(promises);
    };

    if (!snapshotOneHour.empty) await sendReminders(snapshotOneHour.docs, "hour");
    if (!snapshotFiveMin.empty) await sendReminders(snapshotFiveMin.docs, "five");

    const completedSnapshot = await admin.firestore()
      .collection("appointments")
      .where("status", "==", "Confirmed")
      .where("dateTime", ">=", fifteenMinAgo)
      .where("dateTime", "<=", fiveMinAgo)
      .get();

    const completeTasks = completedSnapshot.docs.map(async (doc) => {
      const data = doc.data();
      const patientId = data.patientId;
      const doctorName = data.doctorName || "your doctor";

      await doc.ref.update({ status: "Completed" });

      const patientDoc = await admin.firestore().collection("users").doc(patientId).get();
      const token = patientDoc.get("fcmToken");

      if (token) {
        const feedbackPayload = {
          notification: {
            title: "How was your appointment? 📝",
            body: `Let us know how your visit with Dr. ${doctorName} went!`,
          },
          token: token,
        };

        await admin.messaging().send(feedbackPayload);
        await admin.firestore()
          .collection("users")
          .doc(patientId)
          .collection("notifications")
          .add({
            title: feedbackPayload.notification.title,
            body: feedbackPayload.notification.body,
            timestamp: admin.firestore.Timestamp.now(),
            isRead: false,
            type: "feedback",
            relatedAppointmentId: doc.id,
          });
      }
    });
    await Promise.all(completeTasks);

    const pendingSnapshot = await admin.firestore()
      .collection("appointments")
      .where("status", "==", "Pending")
      .where("dateTime", ">=", fiftyFiveMinFromNow)
      .where("dateTime", "<=", sixtyFiveMinFromNow)
      .get();

    const autoCancelTasks = pendingSnapshot.docs.map(async (doc) => {
      const data = doc.data();
      const patientId = data.patientId;
      const doctorId = data.doctorId;
      const doctorName = data.doctorName || "your doctor";
      const patientName = data.patientName || "the patient";

      await doc.ref.update({
        status: "Cancelled",
        comments: "The appointment was automatically cancelled due to not being confirmed by the doctor within the allowed time. Please contact the doctor directly to reschedule.",
      });

      const patientDoc = await admin.firestore().collection("users").doc(patientId).get();
      const doctorDoc = await admin.firestore().collection("users").doc(doctorId).get();
      const patientToken = patientDoc.get("fcmToken");
      const doctorToken = doctorDoc.get("fcmToken");

      const tasks = [];

      if (patientToken) {
        const payload = {
          notification: {
            title: "Appointment Cancelled ❗",
            body: `Your appointment with Dr. ${doctorName} was cancelled due to not being confirmed by the doctor. We apologize for the inconvenience. You can contact the doctor directly to reschedule.`,
          },
          token: patientToken,
        };
        tasks.push(admin.messaging().send(payload));
        tasks.push(
          admin.firestore()
            .collection("users")
            .doc(patientId)
            .collection("notifications")
            .add({
              title: payload.notification.title,
              body: payload.notification.body,
              timestamp: admin.firestore.Timestamp.now(),
              isRead: false,
              type: "cancellation",
              relatedAppointmentId: doc.id,
            })
        );
      }

      if (doctorToken) {
        const payload = {
          notification: {
            title: "Auto-Cancelled Appointment ❌",
            body: `Your pending appointment with ${patientName} was cancelled automatically. Please try to confirm appointments in time. It helps us provide better service!`,
          },
          token: doctorToken,
        };
        tasks.push(admin.messaging().send(payload));
        tasks.push(
          admin.firestore()
            .collection("users")
            .doc(doctorId)
            .collection("notifications")
            .add({
              title: payload.notification.title,
              body: payload.notification.body,
              timestamp: admin.firestore.Timestamp.now(),
              isRead: false,
              type: "cancellation",
              relatedAppointmentId: doc.id,
            })
        );
      }

      await Promise.all(tasks);
    });

    await Promise.all(autoCancelTasks);
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
    if (before.status !== "Confirmed" || after.status !== "Cancelled") return null;

    const doctorId = after.doctorId;
    const patientId = after.patientId;
    const doctorName = after.doctorName || "the doctor";
    const patientName = after.patientName || "the patient";

    try {
      const doctorDoc = await admin.firestore().collection("users").doc(doctorId).get();
      const patientDoc = await admin.firestore().collection("users").doc(patientId).get();
      const doctorToken = doctorDoc.get("fcmToken");
      const patientToken = patientDoc.get("fcmToken");

      const timestamp = admin.firestore.Timestamp.now();

      const tasks = [];

      if (doctorToken) {
        const doctorMessage = {
          notification: {
            title: "Appointment Cancelled ❗",
            body: `${patientName} just cancelled a confirmed appointment.`,
          },
          token: doctorToken,
        };

        tasks.push(admin.messaging().send(doctorMessage));
        tasks.push(
          admin.firestore().collection("users").doc(doctorId).collection("notifications").add({
            title: doctorMessage.notification.title,
            body: doctorMessage.notification.body,
            timestamp,
            isRead: false,
            type: "cancellation",
            relatedAppointmentId: context.params.appointmentId,
          })
        );

        console.log(`✅ Notification sent to doctor ${doctorId}`);
      }

      if (patientToken) {
        const patientMessage = {
          notification: {
            title: "Appointment Cancelled ❌",
            body: `Your appointment with Dr. ${doctorName} has been cancelled.`,
          },
          token: patientToken,
        };

        tasks.push(admin.messaging().send(patientMessage));
        tasks.push(
          admin.firestore().collection("users").doc(patientId).collection("notifications").add({
            title: patientMessage.notification.title,
            body: patientMessage.notification.body,
            timestamp,
            isRead: false,
            type: "cancellation",
            relatedAppointmentId: context.params.appointmentId,
          })
        );

        console.log(`✅ Notification sent to patient ${patientId}`);
      }

      await Promise.all(tasks);
    } catch (error) {
      console.error("❌ Error sending cancellation notifications:", error);
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

