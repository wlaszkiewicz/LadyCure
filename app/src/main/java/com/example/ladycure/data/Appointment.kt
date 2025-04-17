package com.example.ladycure.data

data class Appointment(
    val appointmentId: String,
    val doctorId: String,
    val patientId: String,
    val date: String,
    val time: String,
    val status: Status,
    val type: AppointmentType,
    val price: Double
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

enum class AppointmentType(val displayName: String, val specialization: String?) {
    // Cardiology
    HEART_CHECKUP("Heart Checkup", "Cardiology"),
    ECG_TEST("ECG Test", "Cardiology"),
    HOLTER_MONITORING("Holter Monitoring", "Cardiology"),
    STRESS_TEST("Cardiac Stress Test", "Cardiology"),
    ECHOCARDIOGRAM("Echocardiogram", "Cardiology"),

    // Dentistry
    DENTAL_CHECKUP("Dental Checkup", "Dentistry"),
    TEETH_WHITENING("Teeth Whitening", "Dentistry"),
    ROOT_CANAL("Root Canal", "Dentistry"),
    ORTHODONTIC_CONSULTATION("Orthodontic Consultation", "Dentistry"),
    DENTAL_IMPLANT("Dental Implant", "Dentistry"),

    // Dermatology
    SKIN_CHECK("Skin Examination", "Dermatology"),
    ACNE_TREATMENT("Acne Treatment", "Dermatology"),
    MOLE_REMOVAL("Mole Removal", "Dermatology"),
    PSORIASIS_TREATMENT("Psoriasis Treatment", "Dermatology"),
    COSMETIC_DERMATOLOGY("Cosmetic Consultation", "Dermatology"),

    // Endocrinology
    DIABETES_MANAGEMENT("Diabetes Management", "Endocrinology"),
    THYROID_CHECK("Thyroid Function Test", "Endocrinology"),
    HORMONE_THERAPY("Hormone Therapy", "Endocrinology"),
    PCOS_MANAGEMENT("PCOS Management", "Endocrinology"),
    OSTEOPOROSIS_SCREENING("Osteoporosis Screening", "Endocrinology"),

    // Gastroenterology
    COLONOSCOPY("Colonoscopy", "Gastroenterology"),
    ENDOSCOPY("Endoscopy", "Gastroenterology"),
    IBS_CONSULTATION("IBS Consultation", "Gastroenterology"),
    LIVER_FUNCTION_TEST("Liver Function Test", "Gastroenterology"),
    H_PYLORI_TEST("H. Pylori Test", "Gastroenterology"),

    // Gynecology
    PAP_SMEAR("Pap Smear", "Gynecology"),
    MAMMOGRAM("Mammogram", "Gynecology"),
    PRENATAL_CHECKUP("Prenatal Checkup", "Gynecology"),
    MENOPAUSE_CONSULTATION("Menopause Consultation", "Gynecology"),
    IUD_INSERTION("IUD Insertion/Removal", "Gynecology"),

    // Neurology
    MIGRAINE_CONSULTATION("Migraine Consultation", "Neurology"),
    EEG_TEST("EEG Test", "Neurology"),
    EMG_TEST("EMG/Nerve Test", "Neurology"),
    STROKE_CONSULTATION("Stroke Consultation", "Neurology"),
    EPILEPSY_MANAGEMENT("Epilepsy Management", "Neurology"),

    // Oncology
    CANCER_SCREENING("Cancer Screening", "Oncology"),
    CHEMOTHERAPY("Chemotherapy", "Oncology"),
    RADIATION_CONSULTATION("Radiation Consultation", "Oncology"),
    TUMOR_MARKER_TEST("Tumor Marker Test", "Oncology"),
    HEMATOLOGY_CONSULTATION("Hematology Consultation", "Oncology"),

    // Ophthalmology
    EYE_TEST("Comprehensive Eye Exam", "Ophthalmology"),
    CATARACT_CONSULTATION("Cataract Consultation", "Ophthalmology"),
    GLAUCOMA_TEST("Glaucoma Test", "Ophthalmology"),
    LASIK_CONSULTATION("LASIK Consultation", "Ophthalmology"),
    RETINAL_EXAM("Retinal Exam", "Ophthalmology"),

    // Orthopedics
    JOINT_PAIN_CONSULTATION("Joint Pain Consultation", "Orthopedics"),
    FRACTURE_CHECKUP("Fracture Follow-up", "Orthopedics"),
    ARTHRITIS_MANAGEMENT("Arthritis Management", "Orthopedics"),
    SPINE_CONSULTATION("Spine Consultation", "Orthopedics"),
    SPORTS_INJURY("Sports Injury Evaluation", "Orthopedics"),

    // Pediatrics
    CHILD_CHECKUP("Child Wellness Checkup", "Pediatrics"),
    VACCINATION("Child Vaccination", "Pediatrics"),
    DEVELOPMENTAL_SCREENING("Developmental Screening", "Pediatrics"),
    BREASTFEEDING_CONSULTATION("Breastfeeding Consultation", "Pediatrics"),
    PEDIATRIC_URGENT_CARE("Pediatric Urgent Care", "Pediatrics"),

    // Physiotherapy
    PHYSIOTHERAPY_SESSION("Physiotherapy Session", "Physiotherapy"),
    REHABILITATION("Rehabilitation Program", "Physiotherapy"),
    POST_SURGERY_THERAPY("Post-Surgery Therapy", "Physiotherapy"),
    SPORTS_REHAB("Sports Rehabilitation", "Physiotherapy"),
    CHRONIC_PAIN_MANAGEMENT("Chronic Pain Management", "Physiotherapy"),

    // Psychiatry
    ANXIETY_DEPRESSION_SCREENING("Anxiety/Depression Screening", "Psychiatry"),
    COUNSELING_SESSION("Counseling Session", "Psychiatry"),
    ADHD_EVALUATION("ADHD Evaluation", "Psychiatry"),
    SLEEP_DISORDER_CONSULTATION("Sleep Disorder Consultation", "Psychiatry"),
    PSYCHIATRIC_EVALUATION("Psychiatric Evaluation", "Psychiatry"),

    // Radiology
    X_RAY("X-Ray", "Radiology"),
    ULTRASOUND("Ultrasound", "Radiology"),
    MRI_SCAN("MRI Scan", "Radiology"),
    CT_SCAN("CT Scan", "Radiology"),
    MAMMOGRAPHY("Mammography", "Radiology"),


    // General/Other
    GENERAL_CHECKUP("General Health Checkup", null),
    BLOOD_TEST("Blood Test", null),
    TRAVEL_VACCINATION("Travel Vaccination", null),
    NUTRITION_CONSULTATION("Nutrition Consultation", null),
    SECOND_OPINION("Second Opinion Consultation", null);

    companion object {
        fun getTypesBySpecialization(specialization: String): List<AppointmentType> {
            return AppointmentType.entries.filter { it.specialization == specialization }
        }
        fun fromValue(value: String): AppointmentType {
            return AppointmentType.entries.firstOrNull { it.displayName == value } ?: GENERAL_CHECKUP
        }
    }

}
