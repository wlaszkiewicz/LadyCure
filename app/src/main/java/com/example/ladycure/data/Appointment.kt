package com.example.ladycure.data

data class Appointment(
    val appointmentId: String,
    val doctorId: String,
    val patientId: String,
    val date: String,
    val time: String,
    val status: Status,
    val type: AppointmentType,
    val price: Double,
    val address: String = "",
    val doctorName: String = "",
    val patientName: String = "",
    val comments: String = "",
)
{
    companion object {
        const val STATUS_PENDING = "Pending"
        const val STATUS_CONFIRMED = "Confirmed"
        const val STATUS_CANCELLED = "Cancelled"
    }
}

enum class Status(val value: String) {
    PENDING(Appointment.STATUS_PENDING),
    CONFIRMED(Appointment.STATUS_CONFIRMED),
    CANCELLED(Appointment.STATUS_CANCELLED);

    companion object {
        fun fromValue(value: String): Status {
            return values().firstOrNull { it.value == value } ?: PENDING
        }
    }
}
enum class AppointmentType(
    val displayName: String,
    val speciality: String,
    val price: Double,
    val durationInMinutes: Int,
    val needsReferral: Boolean,
    val additionalInfo: String,
    val preparationInstructions: String
) {

    // Family Medicine
    CONSULTATION_FAMILY("Family Doctor Consultation", "Family Medicine", 100.0, 30, false, "General health checkup.", "Bring any previous medical records, medications, and a list of questions. For a child don't forget to bring their vaccination book."),
    PHYSICAL_EXAM("Physical Examination", "Family Medicine", 80.0, 15, false, "Routine physical checkup.", "Wear comfortable clothing and bring a list of medications."),
    BLOOD_PRESSURE_CHECK("Blood Pressure Check", "Family Medicine", 50.0, 15, false, "Monitoring blood pressure levels.", "No special preparation needed."),
    BLOOD_TEST_FAMILY("Blood Test", "Family Medicine", 70.0, 15, false, "Routine blood analysis.", "Fasting may be required; drink water before the test."),
    VACCINATION_FAMILY("Vaccination", "Family Medicine", 80.0, 15, false, "Immunization against diseases.", "Bring vaccination records and ensure the child is not ill."),
    HEALTH_SCREENING("Health Screening", "Family Medicine", 120.0, 30, false, "Comprehensive health assessment.", "Bring a list of medications and any concerns."),

    // Cardiology
    CONSULTATION_CARDIOLOGY("Cardiology Consultation", "Cardiology", 150.0, 30, true, "Heart health assessment.", "Bring previous test results and a list of medications."),
    HEART_CHECKUP("Heart Checkup", "Cardiology", 120.0, 30, true, "Initial assessment for heart health.", "Avoid caffeine and tobacco before the test."),
    ECG_TEST("ECG Test", "Cardiology", 60.0, 15, false, "Electrocardiogram to measure heart activity.", "Wear loose clothing; avoid lotions on chest."),
    STRESS_TEST("Cardiac Stress Test", "Cardiology", 180.0, 45, true, "Assesses heart under physical stress.", "Wear comfortable shoes and clothing; avoid eating 2 hours before."),
    ECHOCARDIOGRAM("Echocardiogram", "Cardiology", 200.0, 45, true, "Ultrasound of heart.", "No special prep needed."),

    // Dentistry
    DENTAL_CLEANING("Dental Cleaning", "Dentistry", 100.0, 30, false, "Routine teeth cleaning.", "Brush and floss before the appointment."),
    DENTAL_CHECKUP("Dental Checkup", "Dentistry", 80.0, 15, false, "Routine dental evaluation.", "Brush teeth before the visit."),
    TEETH_WHITENING("Teeth Whitening", "Dentistry", 250.0, 60, false, "Cosmetic whitening.", "Avoid staining foods 24 hours before."),
    ROOT_CANAL("Root Canal", "Dentistry", 500.0, 90, false, "Treatment for infected tooth pulp.", "Eat before the procedure; avoid alcohol."),
    ORTHODONTIC_CONSULTATION("Orthodontic Consultation", "Dentistry", 100.0, 30, false, "Evaluation for braces.", "Brush and floss before visit."),
    DENTAL_IMPLANT("Dental Implant", "Dentistry", 2000.0, 120, true, "Surgical implant placement.", "Fasting may be required; arrange transport after procedure."),

    // Dermatology
    DERMATOLOGY_CONSULTATION("Dermatology Consultation", "Dermatology", 120.0, 30, false, "Skin condition assessment.", "Bring a list of current medications and skin products."),
    SKIN_CHECK("Skin Examination", "Dermatology", 90.0, 30, false, "Full-body skin screening.", "Avoid makeup or lotions before the appointment."),
    ACNE_TREATMENT("Acne Treatment", "Dermatology", 100.0, 30, false, "Evaluation and treatment of acne.", "Come with clean, makeup-free skin."),
    MOLE_REMOVAL("Mole Removal", "Dermatology", 150.0, 30, false, "Surgical or laser mole removal.", "Avoid blood-thinning medications."),
    PSORIASIS_TREATMENT("Psoriasis Treatment", "Dermatology", 130.0, 30, false, "Management of chronic psoriasis.", "Document recent flare-ups and treatments."),
    COSMETIC_DERMATOLOGY("Cosmetic Consultation", "Dermatology", 180.0, 45, false, "Consultation for cosmetic treatments.", "List any current skincare products."),

    // Endocrinology
    ENDOCRINOLOGY_CONSULTATION("Endocrinology Consultation", "Endocrinology", 150.0, 30, true, "Hormonal health assessment.", "Bring previous lab results and medications."),
    DIABETES_MANAGEMENT("Diabetes Management", "Endocrinology", 110.0, 30, true, "Blood sugar and lifestyle management.", "Bring recent blood sugar logs."),
    THYROID_CHECK("Thyroid Function Test", "Endocrinology", 70.0, 15, false, "Blood test for thyroid hormones.", "No fasting required unless told otherwise."),
    HORMONE_THERAPY("Hormone Therapy", "Endocrinology", 140.0, 30, true, "Hormonal imbalance treatment.", "Bring previous hormone level test results."),
    PCOS_MANAGEMENT("PCOS Management", "Endocrinology", 130.0, 30, true, "Polycystic ovary syndrome treatment.", "Document symptoms and menstrual history."),
    OSTEOPOROSIS_SCREENING("Osteoporosis Screening", "Endocrinology", 160.0, 15, true, "Bone density check.", "Avoid calcium supplements 24 hours prior."),

    // Gastroenterology
    GASTRO_CONSULTATION("Gastroenterology Consultation", "Gastroenterology", 140.0, 30, true, "Digestive health assessment.", "Bring a list of medications and symptoms."),
    COLONOSCOPY("Colonoscopy", "Gastroenterology", 500.0, 90, true, "Colorectal cancer screening.", "Follow clear-liquid diet; bowel prep needed."),
    ENDOSCOPY("Endoscopy", "Gastroenterology", 450.0, 60, true, "Upper GI tract exam.", "No food 6–8 hours before test."),
    IBS_CONSULTATION("IBS Consultation", "Gastroenterology", 120.0, 30, false, "Assessment of IBS symptoms.", "Track symptoms and diet prior to appointment."),
    LIVER_FUNCTION_TEST("Liver Function Test", "Gastroenterology", 65.0, 15, false, "Blood test for liver enzymes.", "Fasting may be required."),
    H_PYLORI_TEST("H. Pylori Test", "Gastroenterology", 80.0, 15, false, "Detects H. pylori bacteria.", "Avoid antibiotics and antacids beforehand."),

    // Gynecology
    GYNECOLOGY_CONSULTATION("Gynecology Consultation", "Gynecology", 130.0, 30, false, "Routine gynecological checkup.", "Bring a list of medications and any concerns. Don't forget to bring your menstrual history."),
    TEEN_FIRST_VISIT("Teen First Visit", "Gynecology", 100.0, 30, false, "First gynecological visit for teens.", "Bringing a parent is optional. The doctor will ask for the date of the last period, cycle length, and any concerning symptoms. There may be a physical exam of the abdomen and breasts, and a pelvic exam may be performed if necessary. Don't worry, nothing will be done without your consent. If you're uncomfortable with any part of the exam, just let the doctor know."),
    PAP_SMEAR("Pap Smear", "Gynecology", 90.0, 15, false, "Cervical cancer screening.", "Avoid intercourse and vaginal products 48h before."),
    MAMMOGRAM("Mammogram", "Gynecology", 110.0, 30, false, "Breast cancer screening.", "Avoid deodorants or powders on exam day."),
    PRENATAL_CHECKUP("Prenatal Checkup", "Gynecology", 100.0, 30, false, "Routine pregnancy checkup.", "Bring list of medications and questions."),
    MENOPAUSE_CONSULTATION("Menopause Consultation", "Gynecology", 120.0, 30, false, "Support for menopausal symptoms.", "Track symptoms and menstrual history."),
    IUD_INSERTION("IUD Insertion/Removal", "Gynecology", 250.0, 45, false, "Contraceptive device management.", "Take pain reliever before; avoid intercourse before."),

    // Neurology
    NEUROLOGY_CONSULTATION("Neurology Consultation", "Neurology", 140.0, 30, true, "Brain and nervous system assessment.", "Bring previous imaging and test results."),
    MIGRAINE_CONSULTATION("Migraine Consultation", "Neurology", 130.0, 30, true, "Evaluation for chronic migraines.", "Keep headache diary and symptom log."),
    EEG_TEST("EEG Test", "Neurology", 160.0, 60, true, "Brainwave recording.", "Avoid caffeine; wash hair before test."),
    EMG_TEST("EMG and Nerve Test", "Neurology", 220.0, 60, true, "Nerve and muscle test.", "Avoid lotions; wear loose clothes."),
    STROKE_CONSULTATION("Stroke Consultation", "Neurology", 140.0, 45, true, "Post-stroke care and prevention.", "Bring previous imaging and test results."),
    EPILEPSY_MANAGEMENT("Epilepsy Management", "Neurology", 150.0, 30, true, "Treatment and monitoring of epilepsy.", "List seizure frequency and medications."),

    // Oncology
    ONCOLOGY_CONSULTATION("Oncology Consultation", "Oncology", 200.0, 30, true, "Cancer diagnosis and treatment planning.", "Bring previous imaging and test results."),
    CANCER_SCREENING("Cancer Screening", "Oncology", 300.0, 60, true, "Routine cancer tests.", "Follow specific prep instructions if provided."),
    CHEMOTHERAPY("Chemotherapy", "Oncology", 1000.0, 120, true, "Cancer drug treatment.", "Blood tests and hydration beforehand recommended."),
    RADIATION_CONSULTATION("Radiation Consultation", "Oncology", 200.0, 45, true, "Radiation therapy planning.", "Bring any prior imaging scans."),
    TUMOR_MARKER_TEST("Tumor Marker Test", "Oncology", 150.0, 15, true, "Blood test for cancer markers.", "No special prep needed unless advised."),
    HEMATOLOGY_CONSULTATION("Hematology Consultation", "Oncology", 160.0, 30, true, "Blood disorder evaluation.", "Bring complete blood count reports."),

    // Ophthalmology
    OPHTHALMOLOGY_CONSULTATION("Ophthalmology Consultation", "Ophthalmology", 130.0, 30, false, "Eye health assessment.", "Bring glasses or contacts."),
    EYE_TEST("Comprehensive Eye Exam", "Ophthalmology", 90.0, 30, false, "Vision and eye health check.", "Bring glasses or contacts."),
    CATARACT_CONSULTATION("Cataract Consultation", "Ophthalmology", 120.0, 30, false, "Assessment for cataracts.", "List vision issues and medical history."),
    GLAUCOMA_TEST("Glaucoma Test", "Ophthalmology", 100.0, 15, false, "Eye pressure check.", "Avoid caffeine before test."),
    LASIK_CONSULTATION("LASIK Consultation", "Ophthalmology", 200.0, 45, false, "Evaluation for laser eye surgery.", "Stop wearing contacts for a few days."),
    RETINAL_EXAM("Retinal Exam", "Ophthalmology", 130.0, 30, false, "Detailed view of retina.", "May cause temporary blurred vision."),

    // Orthopedics
    ORTHOPEDIC_CONSULTATION("Orthopedic Consultation", "Orthopedics", 150.0, 30, true, "Bone and joint health assessment.", "Bring previous imaging and test results."),
    JOINT_PAIN_CONSULTATION("Joint Pain Consultation", "Orthopedics", 110.0, 30, false, "Assessment of chronic joint pain.", "Wear loose-fitting clothing."),
    FRACTURE_CHECKUP("Fracture Follow-up", "Orthopedics", 100.0, 15, false, "Follow-up for healing fractures.", "Bring prior X-rays or cast details."),
    ARTHRITIS_MANAGEMENT("Arthritis Management", "Orthopedics", 120.0, 30, false, "Ongoing care for arthritis.", "Note pain frequency and severity."),
    SPINE_CONSULTATION("Spine Consultation", "Orthopedics", 130.0, 30, true, "Back and spinal issues consultation.", "Bring MRI or CT results if available."),
    SPORTS_INJURY("Sports Injury Evaluation", "Orthopedics", 140.0, 30, false, "Evaluation of sports-related injuries.", "Wear sportswear if physical exam needed."),

    // Pediatrics
    CHILD_CHECKUP("Child Wellness Checkup", "Pediatrics", 110.0, 30, false, "Routine health screening for children.", "Bring child's vaccination and health history records."),
    VACCINATION("Child Vaccination", "Pediatrics", 80.0, 15, false, "Scheduled immunization for children.", "Ensure the child is not ill and bring immunization records."),
    DEVELOPMENTAL_SCREENING("Developmental Screening", "Pediatrics", 130.0, 30, false, "Evaluation of child’s physical and mental development.", "Bring past developmental records or concerns."),
    BREASTFEEDING_CONSULTATION("Breastfeeding Consultation", "Pediatrics", 100.0, 30, false, "Support and advice for breastfeeding parents.", "Wear comfortable clothing for nursing and bring baby."),
    PEDIATRIC_URGENT_CARE("Pediatric Urgent Care", "Pediatrics", 150.0, 30, false, "Immediate care for non-life-threatening conditions.", "Bring current medications and health records."),

    // Physiotherapy
    INITIAL_ASSESSMENT("Initial Physiotherapy Assessment", "Physiotherapy", 120.0, 60, true, "Comprehensive evaluation of physical condition.", "Wear comfortable clothing and bring any previous assessments."),
    PHYSIOTHERAPY_SESSION("Physiotherapy Session", "Physiotherapy", 100.0, 45, false, "Treatment for injuries and chronic conditions.", "Wear loose clothing and bring prior assessments if available."),
    REHABILITATION("Rehabilitation Program", "Physiotherapy", 300.0, 90, true, "Post-injury or surgery recovery sessions.", "Bring referral and any relevant imaging results."),
    POST_SURGERY_THERAPY("Post-Surgery Therapy", "Physiotherapy", 200.0, 60, true, "Therapy to aid recovery after surgery.", "Bring post-operative notes and wear loose clothes."),
    SPORTS_REHAB("Sports Rehabilitation", "Physiotherapy", 220.0, 60, false, "Focused therapy for athletic injuries.", "Bring referral if available and sports history."),
    CHRONIC_PAIN_MANAGEMENT("Chronic Pain Management", "Physiotherapy", 180.0, 50, true, "Ongoing pain relief and treatment strategies.", "Prepare pain history and current treatments."),

    // Psychiatry
    PSYCHIATRY_CONSULTATION("Psychiatry Consultation", "Psychiatry", 150.0, 30, true, "Mental health assessment and treatment planning.", "Bring a list of medications and any previous mental health records."),
    ANXIETY_DEPRESSION_SCREENING("Anxiety and Depression Screening", "Psychiatry", 130.0, 40, false, "Mental health assessment.", "List current symptoms and mental health history."),
    COUNSELING_SESSION("Counseling Session", "Psychiatry", 150.0, 50, false, "Talk therapy session with a licensed counselor.", "Be ready to discuss personal and emotional issues."),
    ADHD_EVALUATION("ADHD Evaluation", "Psychiatry", 200.0, 60, true, "In-depth behavioral and cognitive assessment.", "Bring academic history and previous evaluations."),
    SLEEP_DISORDER_CONSULTATION("Sleep Disorder Consultation", "Psychiatry", 140.0, 45, false, "Evaluation of sleep-related problems.", "Log sleep patterns for the past week."),
    PSYCHIATRIC_EVALUATION("Psychiatric Evaluation", "Psychiatry", 180.0, 60, true, "Comprehensive psychiatric examination.", "Bring previous psychiatric and medical history."),

    // Radiology
    RADIOLOGY_CONSULTATION("Radiology Consultation", "Radiology", 150.0, 30, true, "Imaging and diagnostic assessment.", "Bring previous imaging results and a list of medications."),
    X_RAY("X-Ray", "Radiology", 100.0, 15, false, "Imaging for bones and organs.", "Remove metal objects and wear simple clothing."),
    ULTRASOUND("Ultrasound", "Radiology", 120.0, 30, false, "Sound-wave based internal imaging.", "Drink water beforehand if pelvic scan is required."),
    MRI_SCAN("MRI Scan", "Radiology", 600.0, 60, true, "Magnetic imaging of soft tissues.", "Avoid wearing metal and inform if claustrophobic."),
    CT_SCAN("CT Scan", "Radiology", 500.0, 45, true, "3D imaging using X-rays.", "Avoid eating if contrast dye is used."),
    MAMMOGRAPHY("Mammography", "Radiology", 250.0, 30, false, "Breast X-ray to screen for abnormalities.", "Do not wear deodorant or lotion under arms."),

    // Other
    TRAVEL_VACCINATION("Travel Vaccination", "Other", 90.0, 20, false, "Vaccines required for travel to specific regions.", "Bring travel itinerary and previous vaccine history."),
    NUTRITION_CONSULTATION("Nutrition Consultation", "Other", 120.0, 30, false, "Personalized diet planning and advice.", "Keep a food diary for the past 3 days."),
    SECOND_OPINION("Second Opinion Consultation", "Other", 150.0, 45, true, "Alternative perspective on diagnosis or treatment.", "Bring all relevant medical reports and imaging.");


    companion object {
        fun getTypesBySpecialization(specialization: String): List<AppointmentType> {
            return AppointmentType.entries.filter { it.speciality == specialization }
        }

        fun fromDisplayName(displayName: String): AppointmentType {
            return AppointmentType.entries.firstOrNull { it.displayName == displayName } ?: CONSULTATION_FAMILY
        }
    }

}
