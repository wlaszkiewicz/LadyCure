package com.example.ladycure.presentation.home.components

import DefaultPrimary
import DefaultOnPrimary
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.data.doctor.Specialization


@Composable
fun BookAppointmentSection(
    navController: NavHostController,
    specializations: List<Specialization>,
    onSpecializationSelected: (Specialization) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Book Appointment",
            style = MaterialTheme.typography.titleLarge,
            color = DefaultPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Horizontal scrollable list of specializations
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            specializations.forEach { specialization ->
                SpecializationCard(specialization, onSpecializationSelected)
            }
        }
    }
}



//val specializationColors = listOf(
//    Color(0xFFFFF0F5), // Lavender Blush (very light pink)
//    Color(0xFFF0F8FF),
//    Color.White
//)



@Composable
fun SpecializationCard(
    specialization: Specialization,
    onSpecializationSelected: (Specialization) -> Unit
) {

    val specializationColors = listOf(Color(0xFFFFF0F5),
        Color(0xFFF0F8FF),
        Color(0xFFFAFAD2),
        Color(0xFFF5FFFA),
        Color(0xFFFFFACD),
        Color(0xFFEAFFEA),
    )

    val cardColor = specializationColors[specialization.ordinal % specializationColors.size]

    Card(
        modifier = Modifier
            .width(150.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor.copy(alpha = 0.9f) // Slightly transparent
        ),
        onClick = { onSpecializationSelected(specialization) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // You would replace this with actual icons for each specialization
            Icon(
                painter = when (specialization.displayName) {
                    "Cardiology" -> painterResource(id = com.example.ladycure.R.drawable.ic_cardiology)
                    "Dentistry" -> painterResource(id = com.example.ladycure.R.drawable.ic_dentistry)
                    "Dermatology" -> painterResource(id = com.example.ladycure.R.drawable.ic_dermatology)
                    "Gynecology" -> painterResource(id = com.example.ladycure.R.drawable.ic_gynecology)
                    "Endocrinology" -> painterResource(id = com.example.ladycure.R.drawable.ic_endocrinology)
                    "Gastroenterology" -> painterResource(id = com.example.ladycure.R.drawable.ic_gastroenterology)
                    "Neurology" -> painterResource(id = com.example.ladycure.R.drawable.ic_neurology)
                    "Oncology" -> painterResource(id = com.example.ladycure.R.drawable.ic_oncology)
                    "Ophthalmology" -> painterResource(id = com.example.ladycure.R.drawable.ic_ophthalmology)
                    "Orthopedics" -> painterResource(id = com.example.ladycure.R.drawable.ic_orthopedics)
                    "Pediatrics" -> painterResource(id = com.example.ladycure.R.drawable.ic_pediatrics)
                    "Physiotherapy" -> painterResource(id = com.example.ladycure.R.drawable.ic_physiotherapy)
                    "Psychiatry" -> painterResource(id = com.example.ladycure.R.drawable.ic_psychology)
                    "Radiology" -> painterResource(id = com.example.ladycure.R.drawable.ic_radiology)
                    else -> painterResource(id = com.example.ladycure.R.drawable.ic_medical_services)
                },
                contentDescription = specialization.displayName,
                modifier = Modifier.size(32.dp),
                tint = DefaultPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = specialization.displayName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                color = DefaultOnPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun PreviewBookAppointmentSection() {
    BookAppointmentSection(
        navController = rememberNavController(),
        specializations = listOf(
            Specialization.CARDIOLOGY,
            Specialization.DERMATOLOGY,
            Specialization.GYNECOLOGY,
            Specialization.PEDIATRICS,
            Specialization.PSYCHIATRY,
        ),
        onSpecializationSelected = {}
    )
}