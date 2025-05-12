# E-Clinic Mobile Application

The goal of this project is to develop a mobile application in Kotlin that simulates an e-clinic.  
The application should support multiple user roles, each with specific functionalities:

### User Roles:

- **Administrator**:  
  Responsible for managing the entire system, 	~~including adding and removing doctors, modifying doctor profiles,~~ **updating their availability schedules**, ~~and managing patient/user data.~~

- **Doctor**:  
 ~~A medical professional who can update their profile information, set their availability for appointments, view scheduled consultations,~~ and interact with patients via chat during e-consultations.

- **Patient/User**:  
  ~~A general user who can register for an account, browse available doctors, book appointments,~~ **attend e-consultations via chat**, ~~view past consultations in their medical history~~, and ~~upload~~/download medical documents such as prescriptions**.

---

### Key Features:

~~1. **Authentication & Authorization**:
    - Implement Firebase Authentication to manage user accounts and roles.~~

~~2. **Appointment Scheduling**:
    - Users should be able to view doctors' availability and book appointments.
    - Doctors should be able to modify their schedules.~~

3. **E-Consultation via Chat**:
    - Real-time chat functionality for online consultations.
    - Users should be able to send files (e.g., images, prescriptions) via chat.
    - *Support for QR-code generation for prescriptions (optional)*.

~~4. **Cloud-Based Storage**:
    - Utilize Firebase Storage to store user-uploaded files (e.g., prescriptions, medical reports, and images).~~

5. **Notifications**:
    - Implement Firebase Cloud Messaging (FCM) to send notifications regarding appointment reminders, updates, and important messages.

---

### Technologies to Use:

~~- **Kotlin** (for mobile app development)~~
~~- **Firebase Authentication** (for user authentication and role management)~~
~~- **Firebase Storage** (for storing medical reports, prescriptions, and images)~~
- **Firebase Cloud Messaging (FCM)** (for real-time notifications)
~~- **Firestore Database** or **Firebase Realtime Database** (for storing user information, schedules, and chat history)~~

---

### Project Requirements:

~~- The application must have a clean and user-friendly interface.~~
- Implement proper validation for user inputs!!!!!!!!!!
- Ensure data security and user authentication.
- Maintain proper documentation of the code and application functionality.
- The app should be functional and demonstrable at the end of the project.

---

### Submission Requirements:

- A GitHub repository with the complete project code.
- A short report explaining the architecture, features, and how to run the project.

